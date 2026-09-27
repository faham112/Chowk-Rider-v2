package com.example.data.model

enum class RideStatus {
    IDLE,
    SEARCHING,
    ACCEPTED,
    ARRIVED,
    IN_TRIP,
    COMPLETED,
    CANCELLED
}

data class ActiveRide(
    val id: String,
    val pickup: ChowkLocation,
    val destination: ChowkLocation,
    val rideType: RideType,
    val status: RideStatus,
    val distanceKm: Double,
    val durationMinutes: Int,
    val fare: Double,
    val otp: String,
    val paymentMethod: String = "Cash",
    val promoCode: String? = null,
    val discount: Double = 0.0,
    val driver: DriverInfo,
    val progress: Float = 0f, // 0.0 to 1.0 along the route
    val currentDriverX: Float = 0f,
    val currentDriverY: Float = 0f,
    val turnInstruction: String = "Proceed towards pickup location",
    val etaSecondsRemaining: Int = 180,
    val createdAt: Long = System.currentTimeMillis()
)

data class DriverInfo(
    val id: String = "drv_daharki_1",
    val name: String = "Captain Tariq Chachar",
    val rating: Float = 4.93f,
    val totalRides: Int = 1240,
    val phone: String = "+92 301 7891234",
    val vehicleModel: String = "Super Star 6-Seater Qingqi",
    val vehiclePlate: String = "SINDH-GHK-4821",
    val vehicleType: RideCategory = RideCategory.CHINGCHI,
    val isOnline: Boolean = true,
    val acceptanceRate: Int = 98,
    val todayEarnings: Double = 1450.0,
    val todayRidesCount: Int = 8
)

data class ChatMessage(
    val id: String,
    val sender: String, // "USER" or "DRIVER" or "SYSTEM"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
