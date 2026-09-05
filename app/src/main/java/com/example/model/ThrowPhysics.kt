package com.example.model

import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

enum class ThrowStyle(val displayName: String, val description: String) {
    SMOOTH_CURVEBALL("Master Curveball (J-Arc)", "Rotational angular velocity for 1.7x catch bonus"),
    STRAIGHT_SNIPER("Straight Sniper (Center)", "Laser straight vertical flick into the center ring"),
    L_SHAPED_CORNER("L-Throw (Edge Sweep)", "Side-wall flick preferred by competitive fast-catchers")
}

object ThrowPhysics {

    /**
     * Generates a realistic high-velocity touch trajectory calibrated for
     * Pokémon GO's Unity physics engine.
     *
     * @param curveDirection Direction of spin (Clockwise, Counter-Clockwise, or Straight)
     * @param powerBoost Velocity scaling factor (0.8f to 1.5f)
     * @param targetCenter Normalized center of the Pokémon hitbox (default 0.50f, 0.38f)
     * @param style Selected throwing technique
     */
    fun calculateCalibratedTrajectory(
        curveDirection: CurveDirection = CurveDirection.COUNTER_CLOCKWISE,
        powerBoost: Float = 1.0f,
        targetCenter: Offset = Offset(0.50f, 0.38f),
        style: ThrowStyle = ThrowStyle.SMOOTH_CURVEBALL
    ): List<Offset> {
        val points = mutableListOf<Offset>()

        // Resting Pokéball position in Pokémon GO catch interface
        val startX = 0.50f
        val startY = 0.79f

        // Adjusted target Y: powerBoost > 1.0 flicks higher on the screen for far Pokémon
        val targetY = (targetCenter.y - ((powerBoost - 1.0f) * 0.16f)).coerceIn(0.20f, 0.48f)
        val targetX = targetCenter.x

        when {
            curveDirection == CurveDirection.STRAIGHT || style == ThrowStyle.STRAIGHT_SNIPER -> {
                // Direct straight upward flick: 7 evenly spaced points over 210ms
                val steps = 7
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    val y = startY + t * (targetY - startY)
                    points.add(Offset(startX, y))
                }
            }

            style == ThrowStyle.L_SHAPED_CORNER -> {
                // L-Throw: Drag to corner, flick up side edge, release inward
                val isLeft = curveDirection == CurveDirection.COUNTER_CLOCKWISE
                val cornerX = if (isLeft) 0.14f else 0.86f
                val cornerY = 0.82f
                val wallApexY = targetY + 0.08f

                // 1. Drag ball to edge corner
                points.add(Offset(startX, startY))
                points.add(Offset(if (isLeft) 0.30f else 0.70f, 0.81f))
                points.add(Offset(cornerX, cornerY))

                // 2. Flick up the wall
                points.add(Offset(cornerX, 0.65f))
                points.add(Offset(cornerX, wallApexY))

                // 3. Release curve into center ring
                points.add(Offset(if (isLeft) cornerX + 0.15f else cornerX - 0.15f, targetY + 0.04f))
                points.add(Offset(targetX, targetY))
            }

            else -> {
                // Master J-Curve Arc:
                // Sweeps down and sideways to generate Angular Momentum (triggering Curveball bonus),
                // then sharply accelerates along a smooth Bezier arc directly into the Pokémon's hitbox.
                val isLeft = curveDirection == CurveDirection.COUNTER_CLOCKWISE

                // Control points for smooth parabolic release arc
                val p0 = Offset(startX, startY)
                val p1 = if (isLeft) {
                    Offset(0.28f, startY + 0.02f) // pocket spin-start
                } else {
                    Offset(0.72f, startY + 0.02f)
                }
                val p2 = if (isLeft) {
                    Offset(0.20f, 0.60f) // upward acceleration flank
                } else {
                    Offset(0.80f, 0.60f)
                }
                val p3 = Offset(targetX, targetY) // release into target center

                val steps = 9
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    val invT = 1.0f - t
                    val x = invT * invT * invT * p0.x +
                            3f * invT * invT * t * p1.x +
                            3f * invT * t * t * p2.x +
                            t * t * t * p3.x
                    val y = invT * invT * invT * p0.y +
                            3f * invT * invT * t * p1.y +
                            3f * invT * t * t * p2.y +
                            t * t * t * p3.y
                    points.add(Offset(x, y))
                }
            }
        }

        return points
    }

    /**
     * Backward compatibility helper
     */
    fun calculateCurveballTrajectory(
        curveDirection: CurveDirection,
        powerBoost: Float = 1.0f,
        targetCenter: Offset = Offset(0.50f, 0.38f)
    ): List<Offset> = calculateCalibratedTrajectory(curveDirection, powerBoost, targetCenter, ThrowStyle.SMOOTH_CURVEBALL)

    /**
     * Converts normalized coordinates into a physical Android Path with sub-pixel precision.
     */
    fun buildPixelPath(
        normalizedPoints: List<Offset>,
        screenWidth: Float,
        screenHeight: Float,
        variancePx: Float = 0f
    ): Path {
        val path = Path()
        if (normalizedPoints.isEmpty()) return path

        val first = normalizedPoints.first()
        val jitterX = if (variancePx > 0) (Math.random().toFloat() - 0.5f) * variancePx else 0f
        val jitterY = if (variancePx > 0) (Math.random().toFloat() - 0.5f) * variancePx else 0f

        path.moveTo(first.x * screenWidth + jitterX, first.y * screenHeight + jitterY)

        for (i in 1 until normalizedPoints.size) {
            val pt = normalizedPoints[i]
            val subJitterX = if (variancePx > 0 && i == normalizedPoints.lastIndex) {
                (Math.random().toFloat() - 0.5f) * (variancePx * 0.5f)
            } else 0f
            val subJitterY = if (variancePx > 0 && i == normalizedPoints.lastIndex) {
                (Math.random().toFloat() - 0.5f) * (variancePx * 0.5f)
            } else 0f

            path.lineTo(pt.x * screenWidth + subJitterX, pt.y * screenHeight + subJitterY)
        }

        return path
    }

    /**
     * Fast horizontal swipe for spinning PokéStop photo discs.
     * A clean left-to-right or right-to-left swipe across the photo disc.
     */
    fun buildPokestopSpinPath(
        centerX: Float,
        centerY: Float,
        radiusPx: Float = 140f
    ): Path {
        val path = Path()
        val startX = centerX - radiusPx * 0.9f
        val endX = centerX + radiusPx * 0.9f
        path.moveTo(startX, centerY)
        path.quadTo(centerX, centerY - 25f, endX, centerY)
        return path
    }
}

