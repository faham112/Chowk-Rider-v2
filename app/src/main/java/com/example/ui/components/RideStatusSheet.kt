package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.RideStatus
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldStar
import com.example.ui.theme.TealAccent

@Composable
fun RideStatusSheet(
    activeRide: ActiveRide,
    onCallDriver: () -> Unit,
    onOpenChat: () -> Unit,
    onCancelRide: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Status header & ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val statusTitle = when (activeRide.status) {
                        RideStatus.SEARCHING -> "Finding Nearest Chowk Captain..."
                        RideStatus.ACCEPTED -> "Captain is Heading to Pickup"
                        RideStatus.ARRIVED -> "Captain Has Arrived at Chowk!"
                        RideStatus.IN_TRIP -> "Trip in Progress"
                        RideStatus.COMPLETED -> "Arrived at Destination"
                        RideStatus.CANCELLED -> "Ride Cancelled"
                        RideStatus.IDLE -> ""
                    }

                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val statusSubtitle = when (activeRide.status) {
                        RideStatus.SEARCHING -> "Broadcasting request to nearby drivers"
                        RideStatus.ACCEPTED -> "${activeRide.etaSecondsRemaining / 60 + 1} min away • ${activeRide.pickup.name}"
                        RideStatus.ARRIVED -> "Meet at ${activeRide.pickup.landmark}"
                        RideStatus.IN_TRIP -> "Destination: ${activeRide.destination.name}"
                        else -> ""
                    }
                    Text(
                        text = statusSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status pill
                Box(
                    modifier = Modifier
                        .background(
                            when (activeRide.status) {
                                RideStatus.ARRIVED -> EmeraldSuccess.copy(alpha = 0.15f)
                                RideStatus.IN_TRIP -> AmberPrimary.copy(alpha = 0.15f)
                                else -> TealAccent.copy(alpha = 0.15f)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = activeRide.status.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (activeRide.status) {
                            RideStatus.ARRIVED -> EmeraldSuccess
                            RideStatus.IN_TRIP -> AmberDark
                            else -> TealAccent
                        }
                    )
                }
            }

            // Progress bar
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { activeRide.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AmberPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // OTP Box (Crucial Chowk rider safety verification)
            if (activeRide.status == RideStatus.ACCEPTED || activeRide.status == RideStatus.ARRIVED) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = AmberContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "START RIDE PIN / OTP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Share with Captain to start trip",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            color = Color.Black,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = activeRide.otp,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberPrimary,
                                letterSpacing = 4.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Driver profile card
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Driver Avatar
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(AmberPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getVehicleIcon(activeRide.driver.vehicleType),
                            contentDescription = "Driver",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeRide.driver.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = GoldStar,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${activeRide.driver.rating}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = activeRide.driver.vehicleModel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // License plate badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = activeRide.driver.vehiclePlate,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Quick call & chat buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            modifier = Modifier.clickable(onClick = onCallDriver)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Driver",
                                tint = EmeraldSuccess,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(20.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = TealAccent.copy(alpha = 0.15f),
                            modifier = Modifier
                                .clickable(onClick = onOpenChat)
                                .testTag("open_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat with Driver",
                                tint = TealAccent,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                }
            }

            // Fare and actions row
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FARE • ${activeRide.paymentMethod}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Rs. ${activeRide.fare.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberDark
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // SOS Emergency button
                    OutlinedButton(
                        onClick = { showSosDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CoralError
                        ),
                        modifier = Modifier.testTag("emergency_sos_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "SOS",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "SOS", fontWeight = FontWeight.Bold)
                    }

                    // Cancel button
                    if (activeRide.status != RideStatus.IN_TRIP && activeRide.status != RideStatus.COMPLETED) {
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("cancel_ride_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Cancel")
                        }
                    }
                }
            }
        }
    }

    // Cancel confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Chowk Ride?") },
            text = { Text("Are you sure you want to cancel this ride? The driver is already allocated.") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancelRide()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralError)
                ) {
                    Text("Yes, Cancel Ride")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Ride")
                }
            }
        )
    }

    // SOS Emergency Dialog
    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alert",
                    tint = CoralError,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Chowk Emergency Assistance") },
            text = {
                Column {
                    Text("Do you want to share live location with Emergency Helpline (112 / Police) and trusted contacts?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Trip ID: ${activeRide.id}\nVehicle: ${activeRide.driver.vehiclePlate}\nCaptain: ${activeRide.driver.name}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSosDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralError)
                ) {
                    Text("Trigger Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
