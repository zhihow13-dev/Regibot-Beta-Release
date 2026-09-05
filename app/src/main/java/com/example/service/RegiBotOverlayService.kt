package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.engine.RegiBotEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegiBotOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        if (Settings.canDrawOverlays(this)) {
            initFloatingView()
        }
    }

    private fun startAsForeground() {
        val channelId = "regibot_overlay_channel"
        val channelName = "RegiBot Automation HUD"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows floating status controller"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("RegiBot Active")
            .setContentText("Autonomous overlay controller running")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun initFloatingView() {
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            // Programmatically construct the compact floating pill
            val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutParamsType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 40
                y = 150
            }

            val pillLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(24, 16, 24, 16)
                setBackgroundColor(Color.parseColor("#E6101720")) // dark translucent obsidian
                gravity = Gravity.CENTER_VERTICAL
                elevation = 16f
            }

            // Status Indicator dot
            val dot = View(this).apply {
                val size = 24
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(0, 0, 16, 0)
                }
                setBackgroundColor(Color.parseColor("#00E5FF")) // Cyan neon
            }
            pillLayout.addView(dot)

            val statusText = TextView(this).apply {
                text = "RegiBot"
                setTextColor(Color.parseColor("#F0F6FC"))
                textSize = 13f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(0, 0, 16, 0)
            }
            pillLayout.addView(statusText)

            val statsText = TextView(this).apply {
                text = "0 / 0"
                setTextColor(Color.parseColor("#00E5FF"))
                textSize = 12f
                setPadding(0, 0, 16, 0)
            }
            pillLayout.addView(statsText)

            // Play / Pause toggle
            val toggleBtn = TextView(this).apply {
                text = "PAUSE"
                setTextColor(Color.parseColor("#FFB300"))
                textSize = 11f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(12, 6, 12, 6)
                setBackgroundColor(Color.parseColor("#293B4E"))
                setOnClickListener {
                    RegiBotEngine.toggleEngine()
                }
            }
            pillLayout.addView(toggleBtn)

            // Instant Throw / Quick Catch Action button
            val throwBtn = TextView(this).apply {
                val btnParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(12, 0, 0, 0)
                }
                layoutParams = btnParams
                text = "⚡ THROW"
                setTextColor(Color.parseColor("#00E5FF"))
                textSize = 11f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(12, 6, 12, 6)
                setBackgroundColor(Color.parseColor("#153B50"))
                setOnClickListener {
                    RegiBotEngine.triggerInstantCatchThrow()
                }
            }
            pillLayout.addView(throwBtn)

            // Draggable touch listener
            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f

            pillLayout.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(view, params)
                        true
                    }
                    else -> false
                }
            }

            windowManager?.addView(pillLayout, params)
            floatingView = pillLayout

            // Observe engine updates
            serviceScope.launch {
                RegiBotEngine.stateFlow.collectLatest { state ->
                    statusText.text = state.displayName
                    if (state.isActivelyExecuting) {
                        dot.setBackgroundColor(Color.parseColor("#00E676"))
                        toggleBtn.text = "PAUSE"
                    } else {
                        dot.setBackgroundColor(Color.parseColor("#FFB300"))
                        toggleBtn.text = "START"
                    }
                }
            }

            serviceScope.launch {
                RegiBotEngine.telemetryFlow.collectLatest { stats ->
                    statsText.text = "C:${stats.catchesCount} S:${stats.spinsCount}"
                }
            }

        } catch (e: Exception) {
            Log.e("RegiBotOverlayService", "Error showing floating window", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let {
            windowManager?.removeView(it)
            floatingView = null
        }
    }

    companion object {
        fun startOverlay(context: Context) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, RegiBotOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stopOverlay(context: Context) {
            val intent = Intent(context, RegiBotOverlayService::class.java)
            context.stopService(intent)
        }
    }
}
