package com.touchguard.ai.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.touchguard.ai.TouchGuardApplication
import com.touchguard.ai.core.device.DeviceProfiler
import com.touchguard.ai.core.model.DeadZoneConfig
import com.touchguard.ai.core.model.DeadZoneMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DeadZoneOverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private val activeOverlayViews = mutableListOf<View>()
    private var floatingEmergencyButton: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        observeDeadZones()
    }

    private fun observeDeadZones() {
        serviceScope.launch {
            val db = (applicationContext as TouchGuardApplication).database
            db.deadZoneDao().getAllFlow().collectLatest { entities ->
                val configs = entities.map { it.toConfig() }
                rebuildOverlays(configs)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun rebuildOverlays(zones: List<DeadZoneConfig>) {
        if (!hasOverlayPermission()) {
            Toast.makeText(this, "Overlay permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        clearAllOverlays()

        val wm = windowManager ?: return
        val profile = DeviceProfiler.profileDevice(this)
        val screenW = profile.screenWidthPx
        val screenH = profile.screenHeightPx

        val blockingZones = zones.filter { it.isEnabled && it.mode == DeadZoneMode.BLOCK_TOUCH }

        if (blockingZones.isEmpty()) {
            removeEmergencyButton()
            return
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        blockingZones.forEach { zone ->
            val zoneLeft = (zone.leftRatio * screenW).toInt()
            val zoneTop = (zone.topRatio * screenH).toInt()
            val zoneWidth = ((zone.rightRatio - zone.leftRatio) * screenW).toInt().coerceAtLeast(10)
            val zoneHeight = ((zone.bottomRatio - zone.topRatio) * screenH).toInt().coerceAtLeast(10)

            val params = WindowManager.LayoutParams(
                zoneWidth,
                zoneHeight,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = zoneLeft
                y = zoneTop
            }

            // Create protective shield view that consumes touch events
            val shieldView = FrameLayout(this).apply {
                val backgroundDrawable = GradientDrawable().apply {
                    setColor(Color.parseColor("#33FF1744")) // Semi-transparent hazard red
                    setStroke(2, Color.parseColor("#FF1744"))
                    cornerRadius = 8f
                }
                background = backgroundDrawable

                val label = TextView(context).apply {
                    text = "SHIELD: ${zone.name}"
                    textSize = 9f
                    setTextColor(Color.WHITE)
                    setPadding(8, 4, 8, 4)
                }
                addView(label)

                // SWALLOW ALL TOUCHES: Prevents ghost clicks from reaching underlying window
                setOnTouchListener { _, event ->
                    // Vibrate or log incident if needed
                    true // Consumed
                }
            }

            try {
                wm.addView(shieldView, params)
                activeOverlayViews.add(shieldView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Attach Floating Emergency Killswitch
        attachEmergencyButton(wm, overlayType)
    }

    private fun attachEmergencyButton(wm: WindowManager, overlayType: Int) {
        if (floatingEmergencyButton != null) return

        val killParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = 24
            y = 120
        }

        val btn = TextView(this).apply {
            text = "⚡ KILL SHIELD"
            textSize = 11f
            setTextColor(Color.WHITE)
            setPadding(24, 16, 24, 16)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E60A0E1A"))
                setStroke(3, Color.parseColor("#00F0FF"))
                cornerRadius = 24f
            }
            setOnClickListener {
                stopSelf()
                Toast.makeText(context, "Touch Guard Overlays Killed", Toast.LENGTH_SHORT).show()
            }
        }

        try {
            wm.addView(btn, killParams)
            floatingEmergencyButton = btn
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeEmergencyButton() {
        val wm = windowManager ?: return
        floatingEmergencyButton?.let {
            try {
                wm.removeView(it)
            } catch (_: Exception) {}
            floatingEmergencyButton = null
        }
    }

    private fun clearAllOverlays() {
        val wm = windowManager ?: return
        activeOverlayViews.forEach { view ->
            try {
                wm.removeView(view)
            } catch (_: Exception) {}
        }
        activeOverlayViews.clear()
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearAllOverlays()
        removeEmergencyButton()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START_OVERLAYS = "com.touchguard.ai.action.START_OVERLAYS"
        const val ACTION_STOP_OVERLAYS = "com.touchguard.ai.action.STOP_OVERLAYS"

        fun start(context: Context) {
            val intent = Intent(context, DeadZoneOverlayService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, DeadZoneOverlayService::class.java)
            context.stopService(intent)
        }
    }
}
