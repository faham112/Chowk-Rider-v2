package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveRide
import com.example.data.model.ChowkLocation
import com.example.data.model.RideCategory
import com.example.data.model.RideStatus
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CoralError
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealLight
import kotlin.math.atan2

@Composable
fun ChowkMapCanvas(
    modifier: Modifier = Modifier,
    chowks: List<ChowkLocation>,
    selectedPickup: ChowkLocation?,
    selectedDrop: ChowkLocation?,
    activeRide: ActiveRide?,
    driverMode: Boolean = false,
    onChowkSelected: (ChowkLocation) -> Unit = {}
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var showTraffic by remember { mutableStateOf(true) }

    // Pulsing animations for active ride markers and radar
    val infiniteTransition = rememberInfiniteTransition(label = "map_radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 48f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Animated dashed line phase for active route
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dashPhase"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .pointerInput(chowks, zoomScale) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height
                        // Find closest chowk to tap
                        var closest: ChowkLocation? = null
                        var minDistance = Float.MAX_VALUE
                        for (c in chowks) {
                            val px = (c.x / 1000f) * w
                            val py = (c.y / 1000f) * h
                            val dist = kotlin.math.hypot(px - tapOffset.x, py - tapOffset.y)
                            if (dist < 80f && dist < minDistance) {
                                minDistance = dist
                                closest = c
                            }
                        }
                        closest?.let { onChowkSelected(it) }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw city blocks & terrain
            drawCityBackground(width, height)

            // 2. Draw streets & traffic arteries
            drawCityRoads(width, height, showTraffic)

            // 3. Draw chowk roundabouts
            drawChowkIntersections(chowks, width, height, textMeasurer)

            // 4. Draw Route Polyline if pickup and drop or active trip
            if (activeRide != null) {
                drawActiveRideRoute(activeRide, width, height, dashPhase)
            } else if (selectedPickup != null && selectedDrop != null) {
                drawProposedRoute(selectedPickup, selectedDrop, width, height, dashPhase)
            }

            // 5. Draw Pickup Marker
            selectedPickup?.let { p ->
                val px = (p.x / 1000f) * width
                val py = (p.y / 1000f) * height
                drawPickupMarker(px, py, pulseRadius, pulseAlpha)
            }

            // 6. Draw Drop Marker
            selectedDrop?.let { d ->
                val dx = (d.x / 1000f) * width
                val dy = (d.y / 1000f) * height
                drawDestinationMarker(dx, dy)
            }

            // 7. Draw Driver Vehicle
            if (activeRide != null && (activeRide.status == RideStatus.ACCEPTED ||
                        activeRide.status == RideStatus.ARRIVED ||
                        activeRide.status == RideStatus.IN_TRIP)) {
                val dvX = (activeRide.currentDriverX / 1000f) * width
                val dvY = (activeRide.currentDriverY / 1000f) * height

                // Compute heading angle
                val targetX = if (activeRide.status == RideStatus.IN_TRIP) {
                    (activeRide.destination.x / 1000f) * width
                } else {
                    (activeRide.pickup.x / 1000f) * width
                }
                val targetY = if (activeRide.status == RideStatus.IN_TRIP) {
                    (activeRide.destination.y / 1000f) * height
                } else {
                    (activeRide.pickup.y / 1000f) * height
                }
                val angle = Math.toDegrees(atan2((targetY - dvY).toDouble(), (targetX - dvX).toDouble())).toFloat()

                drawDriverVehicle(dvX, dvY, angle, activeRide.rideType.category)
            } else {
                // Draw nearby idle vehicles to give realistic ride-hailing vibe
                drawNearbyIdleVehicles(width, height, activeRide?.status == RideStatus.SEARCHING, pulseRadius, pulseAlpha)
            }
        }

        // Floating Map Controls (Zoom, Recenter, Traffic toggle)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF1E293B).copy(alpha = 0.92f),
                shadowElevation = 6.dp,
                modifier = Modifier.border(1.dp, Color(0xFF334155), CircleShape)
            ) {
                IconButton(
                    onClick = { showTraffic = !showTraffic },
                    modifier = Modifier.size(44.dp).testTag("toggle_traffic_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Traffic,
                        contentDescription = "Toggle Traffic",
                        tint = if (showTraffic) EmeraldSuccess else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.92f),
                shadowElevation = 6.dp,
                modifier = Modifier.border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            ) {
                Column {
                    IconButton(
                        onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(2.0f) },
                        modifier = Modifier.size(44.dp).testTag("zoom_in_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(modifier = Modifier.size(width = 32.dp, height = 1.dp).background(Color(0xFF334155)).align(Alignment.CenterHorizontally))
                    IconButton(
                        onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.8f) },
                        modifier = Modifier.size(44.dp).testTag("zoom_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = AmberPrimary,
                shadowElevation = 6.dp
            ) {
                IconButton(
                    onClick = {
                        zoomScale = 1.0f
                        selectedPickup?.let { onChowkSelected(it) }
                    },
                    modifier = Modifier.size(44.dp).testTag("recenter_map_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Mode and Live Status Overlay badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.88f),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (driverMode) AmberPrimary else EmeraldSuccess)
                )
                Text(
                    text = if (driverMode) "DAHARKI RIDER GPS" else "DAHARKI CHOWK RADAR",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            }
        }
    }
}

private fun DrawScope.drawCityBackground(w: Float, h: Float) {
    // Urban grid blocks
    val blockColor = Color(0xFF131D33)
    val blockBorder = Color(0xFF1E293B)

    val stepX = w / 6f
    val stepY = h / 7f

    for (i in 0 until 6) {
        for (j in 0 until 7) {
            val left = i * stepX + 6f
            val top = j * stepY + 6f
            val right = (i + 1) * stepX - 6f
            val bottom = (j + 1) * stepY - 6f

            drawRoundRect(
                color = blockColor,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = blockBorder,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

private fun DrawScope.drawCityRoads(w: Float, h: Float, showTraffic: Boolean) {
    val mainRoadColor = Color(0xFF1E293B)
    val roadWidth = 26f

    // Major horizontal and vertical avenues
    val hLines = listOf(0.18f, 0.35f, 0.52f, 0.70f, 0.88f)
    val vLines = listOf(0.15f, 0.32f, 0.50f, 0.70f, 0.86f)

    for (yFrac in hLines) {
        val y = yFrac * h
        drawLine(
            color = mainRoadColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = roadWidth,
            cap = StrokeCap.Round
        )
        // Center divider line
        drawLine(
            color = Color(0xFF334155),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
        )
        if (showTraffic) {
            val trafficColor = if (yFrac > 0.5f) EmeraldSuccess.copy(alpha = 0.6f) else AmberPrimary.copy(alpha = 0.5f)
            drawLine(
                color = trafficColor,
                start = Offset(0f, y - 6f),
                end = Offset(w, y - 6f),
                strokeWidth = 3f
            )
        }
    }

    for (xFrac in vLines) {
        val x = xFrac * w
        drawLine(
            color = mainRoadColor,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = roadWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF334155),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
        )
        if (showTraffic) {
            val trafficColor = if (xFrac > 0.4f) EmeraldSuccess.copy(alpha = 0.6f) else CoralError.copy(alpha = 0.45f)
            drawLine(
                color = trafficColor,
                start = Offset(x + 6f, 0f),
                end = Offset(x + 6f, h),
                strokeWidth = 3f
            )
        }
    }

    // Diagonal arterial connector
    drawLine(
        color = mainRoadColor,
        start = Offset(0.18f * w, 0.78f * h),
        end = Offset(0.79f * w, 0.22f * h),
        strokeWidth = 24f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawChowkIntersections(
    chowks: List<ChowkLocation>,
    w: Float,
    h: Float,
    textMeasurer: TextMeasurer
) {
    for (chowk in chowks) {
        val cx = (chowk.x / 1000f) * w
        val cy = (chowk.y / 1000f) * h

        // Roundabout circle
        drawCircle(
            color = Color(0xFF0F172A),
            radius = 24f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = AmberDark.copy(alpha = 0.4f),
            radius = 24f,
            center = Offset(cx, cy),
            style = Stroke(width = 3.5f)
        )
        // Center monument / fountain plaza dot
        drawCircle(
            color = AmberPrimary,
            radius = 6.5f,
            center = Offset(cx, cy)
        )

        // Label pill
        val label = chowk.name.replace(" Chowk", "")
        val measured = textMeasurer.measure(
            text = label,
            style = TextStyle(
                color = Color(0xFFF1F5F9),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        )

        val pillW = measured.size.width + 16f
        val pillH = 18f
        val pillLeft = cx - pillW / 2f
        val pillTop = cy + 26f

        drawRoundRect(
            color = Color(0xFF1E293B).copy(alpha = 0.95f),
            topLeft = Offset(pillLeft, pillTop),
            size = androidx.compose.ui.geometry.Size(pillW, pillH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(9f, 9f)
        )
        drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(pillLeft, pillTop),
            size = androidx.compose.ui.geometry.Size(pillW, pillH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(9f, 9f),
            style = Stroke(width = 1f)
        )

        drawText(
            textMeasurer = textMeasurer,
            text = label,
            topLeft = Offset(pillLeft + 8f, pillTop + 2f),
            style = TextStyle(
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private fun DrawScope.drawProposedRoute(
    p: ChowkLocation,
    d: ChowkLocation,
    w: Float,
    h: Float,
    dashPhase: Float
) {
    val px = (p.x / 1000f) * w
    val py = (p.y / 1000f) * h
    val dx = (d.x / 1000f) * w
    val dy = (d.y / 1000f) * h

    val path = Path().apply {
        moveTo(px, py)
        // Waypoint corner via grid intersection
        val midX = dx
        val midY = py
        cubicTo(
            midX, midY,
            midX, midY,
            dx, dy
        )
    }

    // Outer glow
    drawPath(
        path = path,
        color = AmberPrimary.copy(alpha = 0.25f),
        style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Animated dashed line
    drawPath(
        path = path,
        color = AmberPrimary,
        style = Stroke(
            width = 6f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), dashPhase)
        )
    )
}

private fun DrawScope.drawActiveRideRoute(
    ride: ActiveRide,
    w: Float,
    h: Float,
    dashPhase: Float
) {
    val px = (ride.pickup.x / 1000f) * w
    val py = (ride.pickup.y / 1000f) * h
    val dx = (ride.destination.x / 1000f) * w
    val dy = (ride.destination.y / 1000f) * h
    val dvX = (ride.currentDriverX / 1000f) * w
    val dvY = (ride.currentDriverY / 1000f) * h

    val path = Path()
    if (ride.status == RideStatus.ACCEPTED || ride.status == RideStatus.ARRIVED) {
        // Driver approaching pickup
        path.moveTo(dvX, dvY)
        path.lineTo(px, py)

        drawPath(
            path = path,
            color = SkyBlue.copy(alpha = 0.3f),
            style = Stroke(width = 12f, cap = StrokeCap.Round)
        )
        drawPath(
            path = path,
            color = SkyBlue,
            style = Stroke(
                width = 5f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), dashPhase)
            )
        )
    } else if (ride.status == RideStatus.IN_TRIP) {
        // Driver moving towards destination
        path.moveTo(dvX, dvY)
        val midX = dx
        val midY = dvY
        path.cubicTo(midX, midY, midX, midY, dx, dy)

        drawPath(
            path = path,
            color = EmeraldSuccess.copy(alpha = 0.35f),
            style = Stroke(width = 14f, cap = StrokeCap.Round)
        )
        drawPath(
            path = path,
            color = EmeraldSuccess,
            style = Stroke(
                width = 6f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 14f), dashPhase)
            )
        )
    }
}

private fun DrawScope.drawPickupMarker(x: Float, y: Float, pulseRadius: Float, pulseAlpha: Float) {
    // Pulse ring
    drawCircle(
        color = EmeraldSuccess.copy(alpha = pulseAlpha),
        radius = pulseRadius + 14f,
        center = Offset(x, y)
    )
    // Pin base
    drawCircle(
        color = Color(0xFF0F172A),
        radius = 16f,
        center = Offset(x, y)
    )
    drawCircle(
        color = EmeraldSuccess,
        radius = 13f,
        center = Offset(x, y)
    )
    drawCircle(
        color = Color.White,
        radius = 5.5f,
        center = Offset(x, y)
    )
}

private fun DrawScope.drawDestinationMarker(x: Float, y: Float) {
    drawCircle(
        color = Color(0xFF0F172A),
        radius = 16f,
        center = Offset(x, y)
    )
    drawCircle(
        color = CoralError,
        radius = 13f,
        center = Offset(x, y)
    )
    drawCircle(
        color = Color.White,
        radius = 5.5f,
        center = Offset(x, y)
    )
}

private fun DrawScope.drawDriverVehicle(x: Float, y: Float, angleDeg: Float, category: RideCategory) {
    rotate(degrees = angleDeg, pivot = Offset(x, y)) {
        // Vehicle shadow
        drawCircle(
            color = Color.Black.copy(alpha = 0.5f),
            radius = 20f,
            center = Offset(x + 2f, y + 4f)
        )

        val vehicleColor = when (category) {
            RideCategory.CHINGCHI -> AmberPrimary // Iconic yellow/green Qingqi
            RideCategory.MOTO -> TealLight
            RideCategory.AUTO -> Color(0xFFF59E0B)
            RideCategory.CAR -> SkyBlue
        }

        // Vehicle body capsule
        drawRoundRect(
            color = vehicleColor,
            topLeft = Offset(x - 12f, y - 20f),
            size = androidx.compose.ui.geometry.Size(24f, 40f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
        // Windshield
        drawRoundRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(x - 9f, y - 10f),
            size = androidx.compose.ui.geometry.Size(18f, 14f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Headlights
        drawCircle(color = Color(0xFFFEF08A), radius = 2.5f, center = Offset(x - 7f, y - 19f))
        drawCircle(color = Color(0xFFFEF08A), radius = 2.5f, center = Offset(x + 7f, y - 19f))
    }
}

private fun DrawScope.drawNearbyIdleVehicles(
    w: Float,
    h: Float,
    isSearching: Boolean,
    pulseRadius: Float,
    pulseAlpha: Float
) {
    // Pre-calculated idle positions around chowks
    val idlePoints = listOf(
        Pair(0.35f, 0.31f),
        Pair(0.48f, 0.42f),
        Pair(0.65f, 0.26f),
        Pair(0.72f, 0.61f),
        Pair(0.22f, 0.74f),
        Pair(0.46f, 0.78f)
    )

    for ((xf, yf) in idlePoints) {
        val vx = xf * w
        val vy = yf * h

        if (isSearching) {
            drawCircle(
                color = AmberPrimary.copy(alpha = pulseAlpha * 0.5f),
                radius = pulseRadius,
                center = Offset(vx, vy)
            )
        }

        // Draw small auto rickshaw / cab icon
        drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = 10f, center = Offset(vx + 1f, vy + 2f))
        drawCircle(color = AmberPrimary, radius = 9f, center = Offset(vx, vy))
        drawCircle(color = Color(0xFF0F172A), radius = 4f, center = Offset(vx, vy))
    }
}
