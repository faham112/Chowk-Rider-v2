package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveRide
import com.example.data.model.ChowkLocation
import com.example.data.model.RideCategory
import com.example.data.model.RideStatus
import com.example.data.model.RideType
import com.example.ui.components.ChowkMapCanvas
import com.example.ui.components.RideStatusSheet
import com.example.ui.components.VehicleSelector
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldSuccess

@Composable
fun UserRideScreen(
    chowks: List<ChowkLocation>,
    selectedPickup: ChowkLocation?,
    selectedDrop: ChowkLocation?,
    selectedCategory: RideCategory,
    paymentMethod: String,
    walletBalance: Double,
    promoDiscountPercent: Int,
    activeRide: ActiveRide?,
    onPickupSelected: (ChowkLocation) -> Unit,
    onDropSelected: (ChowkLocation) -> Unit,
    onCategorySelected: (RideCategory) -> Unit,
    onPaymentMethodSelected: (String) -> Unit,
    onApplyPromo: (String) -> Boolean,
    onRequestRide: () -> Unit,
    onCallDriver: () -> Unit,
    onOpenChat: () -> Unit,
    onCancelRide: () -> Unit,
    onOpenWallet: () -> Unit
) {
    val distanceKm = if (selectedPickup != null && selectedDrop != null) {
        selectedPickup.distanceTo(selectedDrop)
    } else {
        2.5
    }

    var showPickupDropdown by remember { mutableStateOf(false) }
    var showDropDropdown by remember { mutableStateOf(false) }
    var showPromoDialog by remember { mutableStateOf(false) }
    var promoInput by remember { mutableStateOf("") }
    var promoMsg by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp
        val isShortScreen = maxHeight < 660.dp
        val isVerySmallScreen = maxWidth < 360.dp

        if (isWideScreen) {
            // Adaptive 2-Pane Layout for Tablets, Foldables, Landscape
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Pane: Map Canvas
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                ) {
                    ChowkMapCanvas(
                        modifier = Modifier.fillMaxSize(),
                        chowks = chowks,
                        selectedPickup = selectedPickup,
                        selectedDrop = selectedDrop,
                        activeRide = activeRide,
                        driverMode = false,
                        onChowkSelected = { chowk ->
                            if (selectedPickup == null) {
                                onPickupSelected(chowk)
                            } else {
                                onDropSelected(chowk)
                            }
                        }
                    )
                }

                // Right Pane: Floating / Docked Side Panel
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
                        // Daharki locality badge
                        DaharkiBadge()

                        Spacer(modifier = Modifier.height(14.dp))

                        if (activeRide != null && activeRide.status != RideStatus.IDLE) {
                            // Active Ride Status in Side Panel
                            RideStatusSheet(
                                activeRide = activeRide,
                                onCallDriver = onCallDriver,
                                onOpenChat = onOpenChat,
                                onCancelRide = onCancelRide,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Pickup & Drop Selector
                            ChowkPickupDropCard(
                                chowks = chowks,
                                selectedPickup = selectedPickup,
                                selectedDrop = selectedDrop,
                                showPickupDropdown = showPickupDropdown,
                                onTogglePickupDropdown = { showPickupDropdown = it },
                                showDropDropdown = showDropDropdown,
                                onToggleDropDropdown = { showDropDropdown = it },
                                onPickupSelected = onPickupSelected,
                                onDropSelected = onDropSelected
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Vehicle Selector
                            VehicleSelector(
                                selectedCategory = selectedCategory,
                                distanceKm = distanceKm,
                                promoDiscountPercent = promoDiscountPercent,
                                onCategorySelected = onCategorySelected
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Payment & Promo chips
                            PaymentPromoRow(
                                paymentMethod = paymentMethod,
                                walletBalance = walletBalance,
                                promoDiscountPercent = promoDiscountPercent,
                                onPaymentMethodSelected = onPaymentMethodSelected,
                                onOpenPromoDialog = { showPromoDialog = true }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Book CTA
                            val selectedRideType = RideType.ALL_TYPES.first { it.category == selectedCategory }
                            val currentFare = selectedRideType.calculateFare(distanceKm, promoDiscountPercent)

                            Button(
                                onClick = onRequestRide,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("book_chowk_ride_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberPrimary,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(
                                    text = "BOOK ${selectedRideType.title.uppercase()} • Rs. ${currentFare.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Standard / Mobile Compact Layout
            Box(modifier = Modifier.fillMaxSize()) {
                // Map as background
                ChowkMapCanvas(
                    modifier = Modifier.fillMaxSize(),
                    chowks = chowks,
                    selectedPickup = selectedPickup,
                    selectedDrop = selectedDrop,
                    activeRide = activeRide,
                    driverMode = false,
                    onChowkSelected = { chowk ->
                        if (selectedPickup == null) {
                            onPickupSelected(chowk)
                        } else {
                            onDropSelected(chowk)
                        }
                    }
                )

                // Top Chowk Hub Picker Card (when idle)
                if (activeRide == null || activeRide.status == RideStatus.IDLE) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .widthIn(max = 480.dp)
                            .padding(
                                top = if (isShortScreen) 6.dp else 10.dp,
                                start = if (isVerySmallScreen) 6.dp else 12.dp,
                                end = if (isVerySmallScreen) 6.dp else 12.dp
                            )
                    ) {
                        DaharkiBadge(modifier = Modifier.align(Alignment.CenterHorizontally))

                        Spacer(modifier = Modifier.height(6.dp))

                        ChowkPickupDropCard(
                            chowks = chowks,
                            selectedPickup = selectedPickup,
                            selectedDrop = selectedDrop,
                            showPickupDropdown = showPickupDropdown,
                            onTogglePickupDropdown = { showPickupDropdown = it },
                            showDropDropdown = showDropDropdown,
                            onToggleDropDropdown = { showDropDropdown = it },
                            onPickupSelected = onPickupSelected,
                            onDropSelected = onDropSelected,
                            compactMode = isShortScreen
                        )
                    }
                }

                // Bottom Booking Tray (when idle)
                if (activeRide == null || activeRide.status == RideStatus.IDLE) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .widthIn(max = 480.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isShortScreen) Modifier.verticalScroll(rememberScrollState())
                                    else Modifier
                                )
                                .padding(
                                    horizontal = if (isVerySmallScreen) 10.dp else 16.dp,
                                    vertical = if (isShortScreen) 10.dp else 14.dp
                                )
                        ) {
                            VehicleSelector(
                                selectedCategory = selectedCategory,
                                distanceKm = distanceKm,
                                promoDiscountPercent = promoDiscountPercent,
                                onCategorySelected = onCategorySelected
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            PaymentPromoRow(
                                paymentMethod = paymentMethod,
                                walletBalance = walletBalance,
                                promoDiscountPercent = promoDiscountPercent,
                                onPaymentMethodSelected = onPaymentMethodSelected,
                                onOpenPromoDialog = { showPromoDialog = true }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val selectedRideType = RideType.ALL_TYPES.first { it.category == selectedCategory }
                            val currentFare = selectedRideType.calculateFare(distanceKm, promoDiscountPercent)

                            Button(
                                onClick = onRequestRide,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isShortScreen) 48.dp else 52.dp)
                                    .testTag("book_chowk_ride_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberPrimary,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(
                                    text = "BOOK ${selectedRideType.title.uppercase()} • Rs. ${currentFare.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = if (isVerySmallScreen) 13.sp else 15.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // Active Ride Status Sheet (when ride is ongoing)
                if (activeRide != null && activeRide.status != RideStatus.IDLE) {
                    RideStatusSheet(
                        activeRide = activeRide,
                        onCallDriver = onCallDriver,
                        onOpenChat = onOpenChat,
                        onCancelRide = onCancelRide,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .widthIn(max = 480.dp)
                    )
                }
            }
        }

        // Promo Dialog (Responsive)
        if (showPromoDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showPromoDialog = false },
                modifier = Modifier.widthIn(max = 440.dp),
                title = { Text("Daharki Promo Code", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Enter promo code for discount on your Daharki Chowk trip (e.g. DAHARKI20 or CHOWK50):",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = promoInput,
                            onValueChange = { promoInput = it },
                            placeholder = { Text("DAHARKI20") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (promoMsg != null) {
                            Text(
                                text = promoMsg ?: "",
                                color = if (promoDiscountPercent > 0) EmeraldSuccess else CoralError,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val success = onApplyPromo(promoInput)
                            if (success) {
                                promoMsg = "Promo applied successfully!"
                                showPromoDialog = false
                            } else {
                                promoMsg = "Invalid promo code. Try DAHARKI20 or CHOWK50"
                            }
                        }
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPromoDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun DaharkiBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.94f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(EmeraldSuccess)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Daharki, Sindh • Chowk Transit Network",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )
        }
    }
}

@Composable
private fun ChowkPickupDropCard(
    chowks: List<ChowkLocation>,
    selectedPickup: ChowkLocation?,
    selectedDrop: ChowkLocation?,
    showPickupDropdown: Boolean,
    onTogglePickupDropdown: (Boolean) -> Unit,
    showDropDropdown: Boolean,
    onToggleDropDropdown: (Boolean) -> Unit,
    onPickupSelected: (ChowkLocation) -> Unit,
    onDropSelected: (ChowkLocation) -> Unit,
    compactMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(if (compactMode) 10.dp else 14.dp)) {
            // Pickup row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTogglePickupDropdown(true) }
                    .padding(vertical = 4.dp)
                    .testTag("select_pickup_row")
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(EmeraldSuccess.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Pickup",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PICKUP CHOWK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                    Text(
                        text = selectedPickup?.name ?: "Select Pickup Chowk",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")

                DropdownMenu(
                    expanded = showPickupDropdown,
                    onDismissRequest = { onTogglePickupDropdown(false) }
                ) {
                    chowks.forEach { chowk ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(chowk.name, fontWeight = FontWeight.Bold)
                                    Text(chowk.landmark, fontSize = 11.sp, color = Color.Gray)
                                }
                            },
                            onClick = {
                                onPickupSelected(chowk)
                                onTogglePickupDropdown(false)
                            }
                        )
                    }
                }
            }

            // Divider with Swap button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                IconButton(
                    onClick = {
                        val p = selectedPickup
                        val d = selectedDrop
                        if (p != null && d != null) {
                            onPickupSelected(d)
                            onDropSelected(p)
                        }
                    },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("swap_locations_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap",
                        tint = AmberDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }

            // Dropoff row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleDropDropdown(true) }
                    .padding(vertical = 4.dp)
                    .testTag("select_drop_row")
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(CoralError.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Drop",
                        tint = CoralError,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DESTINATION CHOWK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                    Text(
                        text = selectedDrop?.name ?: "Select Destination Chowk",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")

                DropdownMenu(
                    expanded = showDropDropdown,
                    onDismissRequest = { onToggleDropDropdown(false) }
                ) {
                    chowks.forEach { chowk ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(chowk.name, fontWeight = FontWeight.Bold)
                                    Text(chowk.landmark, fontSize = 11.sp, color = Color.Gray)
                                }
                            },
                            onClick = {
                                onDropSelected(chowk)
                                onToggleDropDropdown(false)
                            }
                        )
                    }
                }
            }

            // Quick chips for Daharki hubs
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(chowks.take(6)) { chowk ->
                    val isSelected = selectedDrop?.id == chowk.id || selectedPickup?.id == chowk.id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) AmberPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AmberPrimary else Color.Transparent
                        ),
                        modifier = Modifier.clickable {
                            if (selectedPickup != null && selectedDrop == null) {
                                onDropSelected(chowk)
                            } else {
                                onDropSelected(chowk)
                            }
                        }
                    ) {
                        Text(
                            text = chowk.name.replace(" Chowk", ""),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (isSelected) AmberDark else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentPromoRow(
    paymentMethod: String,
    walletBalance: Double,
    promoDiscountPercent: Int,
    onPaymentMethodSelected: (String) -> Unit,
    onOpenPromoDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Payment selector chip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .clickable {
                    val next = when (paymentMethod) {
                        "Cash" -> "Chowk Wallet"
                        "Chowk Wallet" -> "JazzCash / EasyPaisa"
                        else -> "Cash"
                    }
                    onPaymentMethodSelected(next)
                }
                .testTag("payment_method_chip")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (paymentMethod == "Chowk Wallet") Icons.Default.AccountBalanceWallet else Icons.Default.Payment,
                    contentDescription = "Payment",
                    tint = AmberDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (paymentMethod == "Chowk Wallet") "Wallet: Rs. ${walletBalance.toInt()}" else paymentMethod,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Promo discount chip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (promoDiscountPercent > 0) EmeraldSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .clickable(onClick = onOpenPromoDialog)
                .testTag("promo_code_chip")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Discount,
                    contentDescription = "Promo",
                    tint = if (promoDiscountPercent > 0) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (promoDiscountPercent > 0) "$promoDiscountPercent% OFF" else "Promo (DAHARKI)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (promoDiscountPercent > 0) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
