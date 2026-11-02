package com.touchguard.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.touchguard.ai.data.local.TouchGuardDatabase

class TouchGuardApplication : Application() {

    lateinit var database: TouchGuardDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = TouchGuardDatabase.getInstance(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_PROTECTION,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_PROTECTION = "touchguard_protection_channel"
        lateinit var instance: TouchGuardApplication
            private set
    }
}
