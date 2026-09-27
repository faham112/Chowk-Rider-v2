package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChowkDatabase
import com.example.data.local.RideEntity
import com.example.data.local.RideRepository
import com.example.data.model.ActiveRide
import com.example.data.model.ChatMessage
import com.example.data.model.ChowkLocation
import com.example.data.model.DriverInfo
import com.example.data.model.RideCategory
import com.example.data.model.RideStatus
import com.example.data.model.RideType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class ChowkRiderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RideRepository

    init {
        val db = ChowkDatabase.getDatabase(application)
        repository = RideRepository(db.rideDao())
    }

    val rideHistory: StateFlow<List<RideEntity>> = repository.allRides
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val chowks = ChowkLocation.PRESET_CHOWKS

    private val _isDriverMode = MutableStateFlow(false)
    val isDriverMode: StateFlow<Boolean> = _isDriverMode.asStateFlow()

    private val _selectedPickup = MutableStateFlow<ChowkLocation?>(chowks[0]) // Engro Chowk
    val selectedPickup: StateFlow<ChowkLocation?> = _selectedPickup.asStateFlow()

    private val _selectedDrop = MutableStateFlow<ChowkLocation?>(chowks[1]) // Main Bypass Chowk
    val selectedDrop: StateFlow<ChowkLocation?> = _selectedDrop.asStateFlow()

    private val _selectedCategory = MutableStateFlow(RideCategory.CHINGCHI)
    val selectedCategory: StateFlow<RideCategory> = _selectedCategory.asStateFlow()

    private val _paymentMethod = MutableStateFlow("Cash")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _walletBalance = MutableStateFlow(450.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    private val _promoCode = MutableStateFlow("")
    val promoCode: StateFlow<String> = _promoCode.asStateFlow()

    private val _promoDiscountPercent = MutableStateFlow(0)
    val promoDiscountPercent: StateFlow<Int> = _promoDiscountPercent.asStateFlow()

    private val _activeRide = MutableStateFlow<ActiveRide?>(null)
    val activeRide: StateFlow<ActiveRide?> = _activeRide.asStateFlow()

    private val _driverInfo = MutableStateFlow(DriverInfo())
    val driverInfo: StateFlow<DriverInfo> = _driverInfo.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private var simulationJob: Job? = null

    fun toggleDriverMode() {
        _isDriverMode.value = !_isDriverMode.value
    }

    fun setDriverOnline(online: Boolean) {
        _driverInfo.value = _driverInfo.value.copy(isOnline = online)
    }

    fun selectPickup(location: ChowkLocation) {
        if (_selectedDrop.value?.id == location.id) {
            // Swap if drop is same
            _selectedDrop.value = _selectedPickup.value
        }
        _selectedPickup.value = location
    }

    fun selectDrop(location: ChowkLocation) {
        if (_selectedPickup.value?.id == location.id) {
            _selectedPickup.value = _selectedDrop.value
        }
        _selectedDrop.value = location
    }

    fun selectCategory(category: RideCategory) {
        _selectedCategory.value = category
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun applyPromoCode(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        val discount = when (trimmed) {
            "DAHARKI20", "DAHARKI" -> 20
            "CHOWK50" -> 50
            "CHOWKRIDER" -> 30
            "SUPERCOMMUTE" -> 25
            else -> 0
        }
        _promoCode.value = trimmed
        _promoDiscountPercent.value = discount
        return discount > 0
    }

    fun topUpWallet(amount: Double) {
        _walletBalance.value += amount
    }

    fun requestRide() {
        val pickup = _selectedPickup.value ?: return
        val drop = _selectedDrop.value ?: return
        val rideType = RideType.ALL_TYPES.first { it.category == _selectedCategory.value }
        val distance = pickup.distanceTo(drop)
        val rawFare = rideType.calculateFare(distance, _promoDiscountPercent.value)
        val duration = rideType.estimateDurationMinutes(distance)
        val otp = String.format("%04d", Random.nextInt(1000, 9999))

        val ride = ActiveRide(
            id = "CR-${Random.nextInt(10000, 99999)}",
            pickup = pickup,
            destination = drop,
            rideType = rideType,
            status = RideStatus.SEARCHING,
            distanceKm = distance,
            durationMinutes = duration,
            fare = rawFare,
            otp = otp,
            paymentMethod = _paymentMethod.value,
            promoCode = if (_promoDiscountPercent.value > 0) _promoCode.value else null,
            discount = if (_promoDiscountPercent.value > 0) (rideType.calculateFare(distance, 0) - rawFare) else 0.0,
            driver = _driverInfo.value,
            currentDriverX = pickup.x - 120f,
            currentDriverY = pickup.y - 100f,
            etaSecondsRemaining = 180
        )

        _activeRide.value = ride
        _chatMessages.value = listOf(
            ChatMessage(
                id = "m1",
                sender = "SYSTEM",
                text = "Ride requested. Looking for nearest Chowk Captain..."
            )
        )

        // If in passenger mode, simulate driver accepting after brief delay if not in driver mode
        startRideSimulation(autoAccept = !_isDriverMode.value)
    }

    fun driverAcceptRide() {
        val ride = _activeRide.value ?: return
        _activeRide.value = ride.copy(
            status = RideStatus.ACCEPTED,
            turnInstruction = "Head towards ${ride.pickup.name}"
        )
        addChatMessage("DRIVER", "Salam! I have accepted your ride. On the way in my ${ride.driver.vehicleModel}.")
        startApproachingPickupSimulation()
    }

    fun driverDeclineRide() {
        cancelRide()
    }

    fun driverMarkArrived() {
        val ride = _activeRide.value ?: return
        _activeRide.value = ride.copy(
            status = RideStatus.ARRIVED,
            currentDriverX = ride.pickup.x,
            currentDriverY = ride.pickup.y,
            turnInstruction = "Arrived at ${ride.pickup.name}. Awaiting passenger OTP"
        )
        addChatMessage("DRIVER", "I have arrived at ${ride.pickup.landmark}.")
    }

    fun startTrip(otpEntered: String) {
        val ride = _activeRide.value ?: return
        if (otpEntered == ride.otp || otpEntered.isEmpty()) {
            _activeRide.value = ride.copy(
                status = RideStatus.IN_TRIP,
                progress = 0f,
                turnInstruction = "Navigate towards ${ride.destination.name}"
            )
            addChatMessage("SYSTEM", "Trip started with verified PIN. Have a safe journey!")
            startInTripNavigationSimulation()
        }
    }

    fun completeTrip(ratingGiven: Int = 5, tipAmount: Double = 0.0) {
        val ride = _activeRide.value ?: return
        val finalFare = ride.fare + tipAmount

        // Deduct from wallet if wallet payment
        if (ride.paymentMethod == "Chowk Wallet") {
            _walletBalance.value = (_walletBalance.value - finalFare).coerceAtLeast(0.0)
        }

        // Add to driver earnings
        _driverInfo.value = _driverInfo.value.copy(
            todayEarnings = _driverInfo.value.todayEarnings + finalFare,
            todayRidesCount = _driverInfo.value.todayRidesCount + 1
        )

        // Save to Room DB
        viewModelScope.launch {
            repository.insertRide(
                RideEntity(
                    rideId = ride.id,
                    pickupName = ride.pickup.name,
                    dropName = ride.destination.name,
                    rideCategory = ride.rideType.category.name,
                    rideTitle = ride.rideType.title,
                    distanceKm = ride.distanceKm,
                    durationMinutes = ride.durationMinutes,
                    fare = finalFare,
                    paymentMethod = ride.paymentMethod,
                    driverName = ride.driver.name,
                    vehiclePlate = ride.driver.vehiclePlate,
                    status = "COMPLETED",
                    ratingGiven = ratingGiven,
                    tipAmount = tipAmount
                )
            )
        }

        _activeRide.value = ride.copy(status = RideStatus.COMPLETED)
    }

    fun clearRideHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun finishCompletedRideDialog() {
        simulationJob?.cancel()
        _activeRide.value = null
    }

    fun cancelRide() {
        val ride = _activeRide.value
        simulationJob?.cancel()
        if (ride != null) {
            viewModelScope.launch {
                repository.insertRide(
                    RideEntity(
                        rideId = ride.id,
                        pickupName = ride.pickup.name,
                        dropName = ride.destination.name,
                        rideCategory = ride.rideType.category.name,
                        rideTitle = ride.rideType.title,
                        distanceKm = ride.distanceKm,
                        durationMinutes = ride.durationMinutes,
                        fare = 0.0,
                        paymentMethod = ride.paymentMethod,
                        driverName = ride.driver.name,
                        vehiclePlate = ride.driver.vehiclePlate,
                        status = "CANCELLED"
                    )
                )
            }
        }
        _activeRide.value = null
    }

    fun sendChatMessage(text: String, currentRole: String) {
        addChatMessage(currentRole, text)

        // Auto-reply simulation if talking to driver/passenger
        viewModelScope.launch {
            delay(1200)
            if (currentRole == "USER") {
                val replies = listOf(
                    "Got it! Reaching your chowk point shortly.",
                    "Noted, I am in the green auto near the roundabout.",
                    "Traffic cleared, turning onto your road now."
                )
                addChatMessage("DRIVER", replies.random())
            } else {
                val passengerReplies = listOf(
                    "Thanks Captain, waiting right by the entrance!",
                    "Great, I see you coming.",
                    "Awesome, I have the 4-digit PIN ready."
                )
                addChatMessage("USER", passengerReplies.random())
            }
        }
    }

    private fun addChatMessage(sender: String, text: String) {
        val msg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}_${Random.nextInt(100)}",
            sender = sender,
            text = text
        )
        _chatMessages.value = _chatMessages.value + msg
    }

    private fun startRideSimulation(autoAccept: Boolean) {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            if (autoAccept) {
                delay(2200) // Radar search time
                val current = _activeRide.value ?: return@launch
                _activeRide.value = current.copy(
                    status = RideStatus.ACCEPTED,
                    turnInstruction = "Proceeding towards ${current.pickup.name}"
                )
                addChatMessage("DRIVER", "Assalam o Alaikum! I am Captain Tariq. Heading to ${current.pickup.name}.")
                startApproachingPickupSimulation()
            }
        }
    }

    private fun startApproachingPickupSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val steps = 12
            for (i in 1..steps) {
                delay(1200)
                val current = _activeRide.value ?: break
                if (current.status != RideStatus.ACCEPTED) break

                val frac = i.toFloat() / steps
                val newX = current.currentDriverX + (current.pickup.x - current.currentDriverX) * 0.25f
                val newY = current.currentDriverY + (current.pickup.y - current.currentDriverY) * 0.25f
                val remainingSeconds = (180 * (1f - frac)).toInt().coerceAtLeast(10)

                _activeRide.value = current.copy(
                    currentDriverX = newX,
                    currentDriverY = newY,
                    progress = frac * 0.5f,
                    etaSecondsRemaining = remainingSeconds
                )
            }

            // Driver Arrived!
            val current = _activeRide.value
            if (current != null && current.status == RideStatus.ACCEPTED) {
                driverMarkArrived()
            }
        }
    }

    private fun startInTripNavigationSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val steps = 20
            val instructions = listOf(
                "Continue straight on Main Chowk Boulevard for 400m",
                "Take second exit at Ring Chowk Roundabout",
                "Approaching destination in 200m",
                "Arrived at destination safely"
            )

            for (i in 1..steps) {
                delay(1400)
                val current = _activeRide.value ?: break
                if (current.status != RideStatus.IN_TRIP) break

                val frac = i.toFloat() / steps
                val newX = current.pickup.x + (current.destination.x - current.pickup.x) * frac
                val newY = current.pickup.y + (current.destination.y - current.pickup.y) * frac
                val remainingSecs = ((current.durationMinutes * 60) * (1f - frac)).toInt().coerceAtLeast(0)
                val instrIndex = ((frac * (instructions.size - 1)).toInt()).coerceIn(0, instructions.size - 1)

                _activeRide.value = current.copy(
                    currentDriverX = newX,
                    currentDriverY = newY,
                    progress = frac,
                    etaSecondsRemaining = remainingSecs,
                    turnInstruction = instructions[instrIndex]
                )
            }

            // Auto complete if not manually finished
            val current = _activeRide.value
            if (current != null && current.status == RideStatus.IN_TRIP) {
                completeTrip()
            }
        }
    }
}
