package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Path
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RegiBotAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isServiceActive = true
        Log.d(TAG, "RegiBot Accessibility Service connected successfully")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString()
        if (!pkg.isNullOrBlank() && (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED)) {
            currentForegroundPackage = pkg
            isPokemonGoForeground = pkg == POKEMON_GO_PACKAGE
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "RegiBot Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
            isServiceActive = false
        }
        Log.d(TAG, "RegiBot Accessibility Service destroyed")
    }

    fun getScreenDimensions(): Pair<Float, Float> {
        val dm = resources.displayMetrics
        return Pair(dm.widthPixels.toFloat(), dm.heightPixels.toFloat())
    }

    suspend fun captureScreen(): Bitmap? = suspendCancellableCoroutine { continuation ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor, object : TakeScreenshotCallback {
                    override fun onSuccess(screenshotResult: ScreenshotResult) {
                        try {
                            val hardwareBuffer = screenshotResult.hardwareBuffer
                            val colorSpace = screenshotResult.colorSpace
                            val bmp = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)
                            hardwareBuffer.close()
                            if (bmp != null) {
                                val softwareBmp = bmp.copy(Bitmap.Config.ARGB_8888, false)
                                continuation.resume(softwareBmp)
                            } else {
                                continuation.resume(null)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error converting screenshot hardwareBuffer: ${e.message}")
                            continuation.resume(null)
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        Log.w(TAG, "takeScreenshot failed with code: $errorCode")
                        continuation.resume(null)
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Exception calling takeScreenshot: ${e.message}")
                continuation.resume(null)
            }
        } else {
            continuation.resume(null)
        }
    }

    fun dispatchTapGesture(x: Float, y: Float, onResult: ((Boolean) -> Unit)? = null): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 50L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onResult?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                onResult?.invoke(false)
            }
        }, null)
    }

    fun dispatchPathGesture(path: Path, durationMs: Long = 400L, onResult: ((Boolean) -> Unit)? = null): Boolean {
        val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs.coerceIn(100L, 1200L))
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onResult?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                onResult?.invoke(false)
            }
        }, null)
    }

    companion object {
        private const val TAG = "RegiBotAccessService"
        const val POKEMON_GO_PACKAGE = "com.nianticlabs.pokemongo"

        @Volatile
        var instance: RegiBotAccessibilityService? = null

        @Volatile
        var isServiceActive: Boolean = false

        @Volatile
        var currentForegroundPackage: String = ""

        @Volatile
        var isPokemonGoForeground: Boolean = false

        fun isAccessibilitySettingsEnabled(context: Context): Boolean {
            val serviceName = "${context.packageName}/${RegiBotAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val component = colonSplitter.next()
                if (component.equals(serviceName, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }
}
