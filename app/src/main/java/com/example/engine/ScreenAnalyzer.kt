package com.example.engine

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

enum class DetectedScreen(val label: String) {
    POKEMON_GO_MAP("Overworld Map"),
    ENCOUNTER_CATCH("Encounter / Catch Screen"),
    POKESTOP_DISC("PokéStop Photo Disc"),
    CATCH_SUCCESS_DIALOG("Catch Summary / Checkmark"),
    UNKNOWN_OR_BACKGROUND("Waiting for Screen Frame")
}

data class VisionAnalysisResult(
    val screenType: DetectedScreen,
    val confidence: Float,
    val targetCoordinates: Offset? = null,
    val fleeButtonCoordinates: Offset? = null,
    val ballPositionCoordinates: Offset? = null,
    val exitButtonCoordinates: Offset? = null,
    val statusMessage: String = ""
)

object ScreenAnalyzer {

    fun analyzeScreen(bitmap: Bitmap?, isPokemonGoForeground: Boolean): VisionAnalysisResult {
        if (!isPokemonGoForeground) {
            return VisionAnalysisResult(
                screenType = DetectedScreen.UNKNOWN_OR_BACKGROUND,
                confidence = 0.0f,
                statusMessage = "Pokémon GO is not active in foreground"
            )
        }

        if (bitmap == null || bitmap.width <= 0 || bitmap.height <= 0) {
            // When screenshot API is waiting for next frame, do NOT spam taps!
            return VisionAnalysisResult(
                screenType = DetectedScreen.UNKNOWN_OR_BACKGROUND,
                confidence = 0.0f,
                statusMessage = "Waiting for live display buffer..."
            )
        }

        val width = bitmap.width
        val height = bitmap.height

        fun getPixelSafe(nx: Float, ny: Float): Int {
            val px = (nx * width).toInt().coerceIn(0, width - 1)
            val py = (ny * height).toInt().coerceIn(0, height - 1)
            return bitmap.getPixel(px, py)
        }

        fun hasRegionBrightness(startX: Float, endX: Float, startY: Float, endY: Float, minLuminance: Int): Boolean {
            var brightCount = 0
            val samples = 9
            val stepX = (endX - startX) / 3f
            val stepY = (endY - startY) / 3f
            for (ix in 0..2) {
                for (iy in 0..2) {
                    val color = getPixelSafe(startX + ix * stepX, startY + iy * stepY)
                    val r = Color.red(color)
                    val g = Color.green(color)
                    val b = Color.blue(color)
                    val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                    if (lum >= minLuminance) brightCount++
                }
            }
            return brightCount >= 3
        }

        // 1. Encounter Detection via 4 key landmarks:
        // A) Flee icon in top-left (x: 0.06..0.15, y: 0.06..0.13)
        val hasFleeIcon = hasRegionBrightness(0.06f, 0.15f, 0.06f, 0.13f, 150)

        // B) Berry button in bottom-left (x: 0.06..0.16, y: 0.82..0.92)
        val hasBerryIcon = hasRegionBrightness(0.06f, 0.16f, 0.82f, 0.92f, 130)

        // C) Ball switch tray in bottom-right (x: 0.84..0.94, y: 0.82..0.92)
        val hasBallDrawer = hasRegionBrightness(0.84f, 0.94f, 0.82f, 0.92f, 130)

        // D) Bottom Center Pokéball resting area (x: 0.45..0.55, y: 0.76..0.84)
        var ballScore = 0
        for (i in 0..4) {
            val px = 0.46f + i * 0.02f
            val py = 0.79f
            val col = getPixelSafe(px, py)
            val r = Color.red(col)
            val g = Color.green(col)
            val b = Color.blue(col)
            // Pokéball (red/white), Great Ball (blue), Ultra Ball (black/yellow/white)
            val isBallColor = (r > 160 && g < 120 && b < 120) || // red
                    (r > 180 && g > 180 && b > 180) || // white
                    (b > 140 && r < 120) || // blue
                    (r > 160 && g > 140 && b < 80) // yellow
            if (isBallColor) ballScore++
        }
        val hasBall = ballScore >= 2

        // If at least 2 encounter landmarks are confirmed, we are 100% on the Catch Screen
        val encounterScore = (if (hasFleeIcon) 1 else 0) +
                (if (hasBerryIcon) 1 else 0) +
                (if (hasBallDrawer) 1 else 0) +
                (if (hasBall) 1 else 0)

        if (encounterScore >= 2) {
            return VisionAnalysisResult(
                screenType = DetectedScreen.ENCOUNTER_CATCH,
                confidence = 0.95f,
                targetCoordinates = Offset(width * 0.50f, height * 0.38f),
                fleeButtonCoordinates = Offset(width * 0.11f, height * 0.09f),
                ballPositionCoordinates = Offset(width * 0.50f, height * 0.79f),
                exitButtonCoordinates = Offset(width * 0.11f, height * 0.09f),
                statusMessage = "Wild Pokémon locked in reticle. Ready for calibrated throw."
            )
        }

        // 2. PokéStop Photo Disc Screen
        // - Close X button at bottom center (x ~ 0.50, y ~ 0.92)
        // - Disc area in center with sky / photo disc colors
        val centerDiscPixel = getPixelSafe(0.50f, 0.45f)
        val bottomCenterPixel = getPixelSafe(0.50f, 0.92f)
        val isCloseButtonPresent = isDarkOrCloseIcon(bottomCenterPixel)

        if (isCloseButtonPresent && isBlueOrPurpleDisc(centerDiscPixel)) {
            return VisionAnalysisResult(
                screenType = DetectedScreen.POKESTOP_DISC,
                confidence = 0.89f,
                targetCoordinates = Offset(width * 0.50f, height * 0.45f),
                exitButtonCoordinates = Offset(width * 0.50f, height * 0.92f),
                statusMessage = "PokéStop disc ready for spinning gesture"
            )
        }

        // 3. Catch Success / Checkmark Dialog Screen:
        if (isGreenCheckmark(bottomCenterPixel)) {
            return VisionAnalysisResult(
                screenType = DetectedScreen.CATCH_SUCCESS_DIALOG,
                confidence = 0.88f,
                targetCoordinates = Offset(width * 0.50f, height * 0.92f),
                exitButtonCoordinates = Offset(width * 0.50f, height * 0.92f),
                statusMessage = "Capture confirmed: Auto-advancing dialog"
            )
        }

        // 4. Overworld Map
        return VisionAnalysisResult(
            screenType = DetectedScreen.POKEMON_GO_MAP,
            confidence = 0.80f,
            targetCoordinates = Offset(width * 0.50f, height * 0.58f),
            statusMessage = "Overworld map: Monitoring spawns and stops"
        )
    }

    private fun isBlueOrPurpleDisc(color: Int): Boolean {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val isBlue = b > 130 && b > r && b > g
        val isPurple = r > 100 && b > 110 && g < 120
        return isBlue || isPurple
    }

    private fun isDarkOrCloseIcon(color: Int): Boolean {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        return luminance < 80 || (r > 180 && g > 180 && b > 180)
    }

    private fun isGreenCheckmark(color: Int): Boolean {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return g > 140 && g > (r + 30) && g > (b + 30)
    }
}

