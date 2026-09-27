package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RideStatus
import com.example.ui.components.InAppChatDialog
import com.example.ui.components.TripCompletedDialog
import com.example.ui.screens.DriverRideScreen
import com.example.ui.screens.RideHistoryScreen
import com.example.ui.screens.UserRideScreen
import com.example.ui.screens.ChowkWalletScreen
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TealAccent
import com.example.ui.viewmodel.ChowkRiderViewModel

enum class MainTab {
    RIDE,
    HISTORY,
    WALLET
}

class MainActivity : ComponentActivity() {

    private val viewModel: ChowkRiderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                ChowkRiderApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ChowkRiderApp(viewModel: ChowkRiderViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(MainTab.RIDE) }
    var showChatDialog by remember { mutableStateOf(false) }

    val isDriverMode by viewModel.isDriverMode.collectAsStateWithLifecycle()
    val activeRide by viewModel.activeRide.collectAsStateWithLifecycle()
    val driverInfo by viewModel.driverInfo.collectAsStateWithLifecycle()
    val selectedPickup by viewModel.selectedPickup.collectAsStateWithLifecycle()
    val selectedDrop by viewModel.selectedDrop.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
    val promoDiscountPercent by viewModel.promoDiscountPercent.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val history by viewModel.rideHistory.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // App Header with Dual-Mode Passenger / Driver Switcher
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 1200.dp)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    // Logo & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDriverMode) Icons.Default.LocalTaxi else Icons.Default.ElectricRickshaw,
                                contentDescription = "Logo",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CHOWK",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "RIDER",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = AmberDark,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = if (isDriverMode) "DAHARKI • RIDER SIDE" else "DAHARKI • USER SIDE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDriverMode) EmeraldSuccess else TealAccent,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Role Switcher Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDriverMode) EmeraldSuccess else AmberPrimary
                        ),
                        modifier = Modifier
                            .clickable {
                                viewModel.toggleDriverMode()
                                val newRole = if (!isDriverMode) "Rider (Captain)" else "User (Passenger)"
                                Toast.makeText(context, "Switched to $newRole Side", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("role_switcher_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SyncAlt,
                                contentDescription = "Switch Role",
                                tint = if (isDriverMode) EmeraldSuccess else AmberDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isDriverMode) "Switch to User" else "Switch to Rider",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == MainTab.RIDE,
                    onClick = { currentTab = MainTab.RIDE },
                    icon = {
                        Icon(
                            imageVector = if (isDriverMode) Icons.Default.LocalTaxi else Icons.Default.DirectionsCar,
                            contentDescription = "Ride"
                        )
                    },
                    label = { Text(if (isDriverMode) "Captain" else "Ride") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = AmberPrimary
                    ),
                    modifier = Modifier.testTag("tab_ride")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.HISTORY,
                    onClick = { currentTab = MainTab.HISTORY },
                    icon = {
                        BadgedBox(badge = {
                            if (history.isNotEmpty()) {
                                Badge(containerColor = AmberDark) {
                                    Text("${history.size}")
                                }
                            }
                        }) {
                            Icon(imageVector = Icons.Default.History, contentDescription = "History")
                        }
                    },
                    label = { Text("Activity") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = AmberPrimary
                    ),
                    modifier = Modifier.testTag("tab_history")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.WALLET,
                    onClick = { currentTab = MainTab.WALLET },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet"
                        )
                    },
                    label = { Text("Wallet") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = AmberPrimary
                    ),
                    modifier = Modifier.testTag("tab_wallet")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    MainTab.RIDE -> {
                        if (isDriverMode) {
                            DriverRideScreen(
                                driverInfo = driverInfo,
                                chowks = viewModel.chowks,
                                activeRide = activeRide,
                                onToggleOnline = { viewModel.setDriverOnline(it) },
                                onAcceptRide = { viewModel.driverAcceptRide() },
                                onDeclineRide = { viewModel.driverDeclineRide() },
                                onDriverArrived = { viewModel.driverMarkArrived() },
                                onStartTrip = { viewModel.startTrip(it) },
                                onCompleteTrip = { viewModel.completeTrip() },
                                onOpenChat = { showChatDialog = true },
                                onCallPassenger = {
                                    Toast.makeText(context, "Calling Passenger Ayesha K. (+92 321 445-6677)...", Toast.LENGTH_SHORT).show()
                                },
                                onSimulateIncomingRequest = {
                                    viewModel.requestRide()
                                }
                            )
                        } else {
                            UserRideScreen(
                                chowks = viewModel.chowks,
                                selectedPickup = selectedPickup,
                                selectedDrop = selectedDrop,
                                selectedCategory = selectedCategory,
                                paymentMethod = paymentMethod,
                                walletBalance = walletBalance,
                                promoDiscountPercent = promoDiscountPercent,
                                activeRide = activeRide,
                                onPickupSelected = { viewModel.selectPickup(it) },
                                onDropSelected = { viewModel.selectDrop(it) },
                                onCategorySelected = { viewModel.selectCategory(it) },
                                onPaymentMethodSelected = { viewModel.setPaymentMethod(it) },
                                onApplyPromo = { viewModel.applyPromoCode(it) },
                                onRequestRide = { viewModel.requestRide() },
                                onCallDriver = {
                                    Toast.makeText(context, "Calling Captain Tariq (${activeRide?.driver?.phone})...", Toast.LENGTH_SHORT).show()
                                },
                                onOpenChat = { showChatDialog = true },
                                onCancelRide = { viewModel.cancelRide() },
                                onOpenWallet = { currentTab = MainTab.WALLET }
                            )
                        }
                    }

                    MainTab.HISTORY -> {
                        RideHistoryScreen(
                            history = history,
                            onClearHistory = {
                                viewModel.clearRideHistory()
                            }
                        )
                    }

                    MainTab.WALLET -> {
                        ChowkWalletScreen(
                            balance = walletBalance,
                            onTopUp = { viewModel.topUpWallet(it) }
                        )
                    }
                }
            }

            // Real-time In-App Chat Modal
            if (showChatDialog && activeRide != null) {
                InAppChatDialog(
                    messages = chatMessages,
                    currentRole = if (isDriverMode) "DRIVER" else "USER",
                    otherPartyName = if (isDriverMode) "Passenger Ayesha" else activeRide!!.driver.name,
                    onSendMessage = { text ->
                        viewModel.sendChatMessage(text, if (isDriverMode) "DRIVER" else "USER")
                    },
                    onDismiss = { showChatDialog = false }
                )
            }

            // Trip Completed Modal (Ratings, Tipping, Itemized Receipt)
            if (activeRide != null && activeRide!!.status == RideStatus.COMPLETED) {
                TripCompletedDialog(
                    ride = activeRide!!,
                    onDone = { rating, tip ->
                        viewModel.finishCompletedRideDialog()
                        Toast.makeText(context, "Thank you! Receipt saved in Activity tab.", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}
