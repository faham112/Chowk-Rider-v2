package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveRide
import com.example.data.model.ChowkLocation
import com.example.data.model.DriverInfo
import com.example.data.model.RideStatus
import com.example.ui.components.ChowkMapCanvas
import com.example.ui.components.DriverActiveTripView
import com.example.ui.components.DriverIncomingSheet
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldStar
import com.example.ui.theme.TealAccent

@Composable
fun DriverRideScreen(
    driverInfo: DriverInfo,
    chowks: List<ChowkLocation>,
    activeRide: ActiveRide?,
    onToggleOnline: (Boolean) -> Unit,
    onAcceptRide: () -> Unit,
    onDeclineRide: () -> Unit,
    onDriverArrived: () -> Unit,
    onStartTrip: (String) -> Unit,
    onCompleteTrip: () -> Unit,
    onOpenChat: () -> Unit,
    onCallPassenger: () -> Unit,
    onSimulateIncomingRequest: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp
        val isVerySmallScreen = maxWidth < 360.dp

        if (isWideScreen) {
            // Adaptive 2-Pane Layout for Tablets & Large Screens
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Pane: Driver Map
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                ) {
                    ChowkMapCanvas(
                        modifier = Modifier.fillMaxSize(),
                        chowks = chowks,
                        selectedPickup = activeRide?.pickup,
                        selectedDrop = activeRide?.destination,
                        activeRide = activeRide,
                        driverMode = true
                    )
                }

                // Right Pane: Driver Console Dashboard
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        DriverHeaderCard(
                            driverInfo = driverInfo,
                            onToggleOnline = onToggleOnline,
                            isVerySmallScreen = false
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (activeRide != null) {
                            when (activeRide.status) {
                                RideStatus.SEARCHING -> {
                                    DriverIncomingSheet(
                                        ride = activeRide,
                                        onAccept = onAcceptRide,
                                        onDecline = onDeclineRide,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                RideStatus.ACCEPTED, RideStatus.ARRIVED, RideStatus.IN_TRIP -> {
                                    DriverActiveTripView(
                                        ride = activeRide,
                                        onDriverArrived = onDriverArrived,
                                        onStartTrip = onStartTrip,
                                        onCompleteTrip = onCompleteTrip,
                                        onOpenChat = onOpenChat,
                                        onCallPassenger = onCallPassenger,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                else -> {}
                            }
                        } else if (driverInfo.isOnline) {
                            Button(
                                onClick = onSimulateIncomingRequest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("simulate_incoming_ride_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberDark,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Alert")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("TEST INCOMING RIDE REQUEST", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Standard / Mobile Compact Layout
            Box(modifier = Modifier.fillMaxSize()) {
                // Map with driver's vehicle perspective
                ChowkMapCanvas(
                    modifier = Modifier.fillMaxSize(),
                    chowks = chowks,
                    selectedPickup = activeRide?.pickup,
                    selectedDrop = activeRide?.destination,
                    activeRide = activeRide,
                    driverMode = true
                )

                // Top Driver Stats Dashboard
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                        .padding(top = 10.dp, start = 12.dp, end = 12.dp)
                ) {
                    DriverHeaderCard(
                        driverInfo = driverInfo,
                        onToggleOnline = onToggleOnline,
                        isVerySmallScreen = isVerySmallScreen
                    )

                    // Test dispatch button if online and idle
                    if (driverInfo.isOnline && (activeRide == null || activeRide.status == RideStatus.IDLE)) {
                        Button(
                            onClick = onSimulateIncomingRequest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(46.dp)
                                .testTag("simulate_incoming_ride_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberDark,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Alert")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TEST INCOMING RIDE REQUEST", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Bottom State Sheets (Responsive width constraint)
                if (activeRide != null) {
                    when (activeRide.status) {
                        RideStatus.SEARCHING -> {
                            DriverIncomingSheet(
                                ride = activeRide,
                                onAccept = onAcceptRide,
                                onDecline = onDeclineRide,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .widthIn(max = 500.dp)
                            )
                        }

                        RideStatus.ACCEPTED, RideStatus.ARRIVED, RideStatus.IN_TRIP -> {
                            DriverActiveTripView(
                                ride = activeRide,
                                onDriverArrived = onDriverArrived,
                                onStartTrip = onStartTrip,
                                onCompleteTrip = onCompleteTrip,
                                onOpenChat = onOpenChat,
                                onCallPassenger = onCallPassenger,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .widthIn(max = 500.dp)
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverHeaderCard(
    driverInfo: DriverInfo,
    onToggleOnline: (Boolean) -> Unit,
    isVerySmallScreen: Boolean
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Online/Offline switch row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (driverInfo.isOnline) EmeraldSuccess else CoralError)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (driverInfo.isOnline) "ONLINE • DAHARKI" else "OFFLINE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (driverInfo.isOnline) EmeraldSuccess else CoralError
                    )
                }

                Switch(
                    checked = driverInfo.isOnline,
                    onCheckedChange = onToggleOnline,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = EmeraldSuccess
                    ),
                    modifier = Modifier.testTag("driver_online_switch")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metric Chips (Adaptive layout: 2x2 on tiny screens, 4-row on normal/large screens)
            if (isVerySmallScreen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricCard(
                        label = "Today Earned",
                        value = "Rs. ${driverInfo.todayEarnings.toInt()}",
                        accent = AmberDark,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Rides",
                        value = "${driverInfo.todayRidesCount}",
                        accent = TealAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricCard(
                        label = "Rating",
                        value = "${driverInfo.rating}★",
                        accent = GoldStar,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Acceptance",
                        value = "${driverInfo.acceptanceRate}%",
                        accent = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        label = "Today Earned",
                        value = "Rs. ${driverInfo.todayEarnings.toInt()}",
                        accent = AmberDark,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Rides",
                        value = "${driverInfo.todayRidesCount}",
                        accent = TealAccent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Rating",
                        value = "${driverInfo.rating}★",
                        accent = GoldStar,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Acceptance",
                        value = "${driverInfo.acceptanceRate}%",
                        accent = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Vehicle plate sub-banner
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = driverInfo.vehicleModel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF0F172A)
                ) {
                    Text(
                        text = driverInfo.vehiclePlate,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AmberPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = accent,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
