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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurveDirection
import com.example.model.ThrowGrade
import com.example.model.ThrowPhysics
import com.example.ui.theme.AmberEnergy
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep

@Composable
fun CurveballCanvas(
    powerBoost: Float,
    curveDirection: CurveDirection,
    targetGrade: ThrowGrade,
    onTestGesture: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userThrowResult by remember { mutableStateOf("EXCELLENT HIT (+1.95x XP)") }
    var interactiveOffset by remember { mutableStateOf<Offset?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "ring_transition")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = targetGrade.targetRadiusFactor,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "target_ring"
    )
    val ballFlightProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flight_anim"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ObsidianCard)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TRAJECTORY PHYSICS BENCH",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Simulated Encounter & Curve Arc",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldSuccess.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = targetGrade.label.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSuccess
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Physics Canvas Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianDeep)
                .border(1.dp, Color(0xFF1B2836), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            interactiveOffset = change.position
                        },
                        onDragEnd = {
                            userThrowResult = "SIMULATED HIT! Grade: ${targetGrade.label}"
                            interactiveOffset = null
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Grid lines background
                val gridStep = 40f
                for (x in 0..(w / gridStep).toInt()) {
                    drawLine(
                        color = Color(0xFF131D28),
                        start = Offset(x * gridStep, 0f),
                        end = Offset(x * gridStep, h),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(h / gridStep).toInt()) {
                    drawLine(
                        color = Color(0xFF131D28),
                        start = Offset(0f, y * gridStep),
                        end = Offset(w, y * gridStep),
                        strokeWidth = 1f
                    )
                }

                // Target Monster Center (y ~ 0.35h)
                val targetCenter = Offset(w * 0.5f, h * 0.32f)
                val targetMaxRadius = w * 0.22f

                // Outer fixed ring
                drawCircle(
                    color = Color(0xFF334B63),
                    radius = targetMaxRadius,
                    center = targetCenter,
                    style = Stroke(width = 2f)
                )

                // Shrinking target ring (Nice / Great / Excellent zone)
                val activeRingRadius = targetMaxRadius * ringScale
                val ringColor = when {
                    ringScale <= 0.35f -> EmeraldSuccess
                    ringScale <= 0.65f -> AmberEnergy
                    else -> CyanNeon
                }
                drawCircle(
                    color = ringColor,
                    radius = activeRingRadius,
                    center = targetCenter,
                    style = Stroke(width = 2.5f)
                )

                // Center target bullseye
                drawCircle(
                    color = ringColor.copy(alpha = 0.25f),
                    radius = activeRingRadius * 0.4f,
                    center = targetCenter
                )

                // Calculate trajectory points
                val normPoints = ThrowPhysics.calculateCurveballTrajectory(
                    curveDirection = curveDirection,
                    powerBoost = powerBoost,
                    targetCenter = Offset(0.5f, 0.32f)
                )

                // Draw calculated curveball trajectory path
                val trajectoryPath = Path()
                normPoints.forEachIndexed { i, pt ->
                    val screenX = pt.x * w
                    val screenY = pt.y * h
                    if (i == 0) {
                        trajectoryPath.moveTo(screenX, screenY)
                    } else {
                        trajectoryPath.lineTo(screenX, screenY)
                    }
                }

                // Trajectory glow line
                drawPath(
                    path = trajectoryPath,
                    brush = Brush.linearGradient(
                        colors = listOf(CyanNeon.copy(alpha = 0.2f), CyanGlow, EmeraldSuccess),
                        start = Offset(w * 0.5f, h * 0.85f),
                        end = targetCenter
                    ),
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )

                // Draw moving ball along the trajectory
                if (normPoints.isNotEmpty()) {
                    val pointIndex = ((normPoints.size - 1) * ballFlightProgress).toInt().coerceIn(0, normPoints.size - 1)
                    val ballPos = Offset(normPoints[pointIndex].x * w, normPoints[pointIndex].y * h)

                    // Trailing glow
                    drawCircle(
                        color = CyanNeon.copy(alpha = 0.4f),
                        radius = 16f,
                        center = ballPos
                    )
                    // Poké Ball outer
                    drawCircle(
                        color = Color.White,
                        radius = 11f,
                        center = ballPos
                    )
                    // Top half red
                    drawArc(
                        color = Color(0xFFEE1515),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(ballPos.x - 11f, ballPos.y - 11f),
                        size = androidx.compose.ui.geometry.Size(22f, 22f)
                    )
                    // Center button
                    drawCircle(
                        color = Color(0xFF1E293B),
                        radius = 4f,
                        center = ballPos
                    )
                }

                // Interactive touch point if user is dragging
                interactiveOffset?.let { pos ->
                    drawCircle(
                        color = AmberEnergy,
                        radius = 20f,
                        center = pos,
                        style = Stroke(width = 2f)
                    )
                }
            }

            // Interactive instruction banner at bottom of canvas
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianCard.copy(alpha = 0.85f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SWIPE ON SCREEN TO TEST DRAG OR CLICK RUN GESTURE BELOW",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status & Trigger Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TARGET RESULT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userThrowResult,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSuccess
                )
            }

            Button(
                onClick = onTestGesture,
                modifier = Modifier
                    .testTag("test_throw_button")
                    .height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanNeon,
                    contentColor = Color(0xFF090D12)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TEST GESTURE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}
