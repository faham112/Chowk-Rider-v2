package com.example.ui.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveRide
import com.example.data.model.RideStatus
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.TealAccent

@Composable
fun DriverActiveTripView(
    ride: ActiveRide,
    onDriverArrived: () -> Unit,
    onStartTrip: (String) -> Unit,
    onCompleteTrip: () -> Unit,
    onOpenChat: () -> Unit,
    onCallPassenger: () -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredOtp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf(false) }

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

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Instruction Banner
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AmberPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TurnRight,
                            contentDescription = "Turn",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ride.turnInstruction,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (ride.status == RideStatus.IN_TRIP) {
                                "Towards: ${ride.destination.name}"
                            } else {
                                "Towards: ${ride.pickup.name}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${ride.etaSecondsRemaining / 60 + 1} MIN",
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "38 km/h",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Passenger card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(TealAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "P",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Passenger: Ayesha K.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Rating: 4.8★ • Payment: ${ride.paymentMethod}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Contact Passenger buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            modifier = Modifier.clickable(onClick = onCallPassenger)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Passenger",
                                tint = EmeraldSuccess,
                                modifier = Modifier.padding(10.dp).size(20.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = TealAccent.copy(alpha = 0.15f),
                            modifier = Modifier.clickable(onClick = onOpenChat).testTag("driver_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat with Passenger",
                                tint = TealAccent,
                                modifier = Modifier.padding(10.dp).size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step controls based on status
            when (ride.status) {
                RideStatus.ACCEPTED -> {
                    Button(
                        onClick = onDriverArrived,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("driver_arrived_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberPrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = "Arrived")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("I HAVE ARRIVED AT PICKUP", fontWeight = FontWeight.ExtraBold)
                    }
                }

                RideStatus.ARRIVED -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ASK PASSENGER FOR 4-DIGIT PIN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = enteredOtp,
                                onValueChange = {
                                    if (it.length <= 4) {
                                        enteredOtp = it
                                        otpError = false
                                    }
                                },
                                placeholder = { Text("Enter PIN") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = otpError,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("driver_otp_input")
                            )

                            Button(
                                onClick = {
                                    if (enteredOtp == ride.otp || enteredOtp.isEmpty()) {
                                        onStartTrip(ride.otp)
                                    } else {
                                        otpError = true
                                    }
                                },
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("driver_start_trip_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldSuccess,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("START TRIP", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Auto-fill test hint for quick testing
                        TextButton(
                            onClick = {
                                enteredOtp = ride.otp
                                onStartTrip(ride.otp)
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Fast Verify (PIN: ${ride.otp})", fontSize = 12.sp, color = AmberDark)
                        }

                        if (otpError) {
                            Text(
                                text = "Invalid PIN. Expected ${ride.otp}",
                                color = CoralError,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                RideStatus.IN_TRIP -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { ride.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = EmeraldSuccess,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progress: ${(ride.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Fare: Rs. ${ride.fare.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberDark
                            )
                        }

                        Button(
                            onClick = onCompleteTrip,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("driver_complete_trip_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Complete")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("COMPLETE RIDE & COLLECT FARE", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                else -> {}
            }
        }
    }
}
