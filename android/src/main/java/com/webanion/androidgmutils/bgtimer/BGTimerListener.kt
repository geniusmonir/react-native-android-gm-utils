package com.webanion.androidgmutils.bgtimer

import com.webanion.androidgmutils.BuildConfig
import com.webanion.androidgmutils.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class BGTimerListener : Service() {

    private val TAG = "BGTimer"
    private val TEMP_NOTIFICATION_ID = 102
    private val CHANNEL_ID = "EBM_NOTIFICATION_CH"
    private val CHANNEL_NAME = "EBM_NOTIFICATION_CH_NAME"
    private val CHANNEL_GROUP = "EBM_NOTIFICATION_CH_GROUP"

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Log.d(TAG, "BGTimerListener Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStartCommand triggered")

        startTemporaryForeground(intent)

        return START_STICKY
    }

    private fun startTemporaryForeground(intent: Intent?) {
        val iconName = "notification_icon"
        val iconResId = applicationContext.resources.getIdentifier(
            iconName,
            "drawable",
            applicationContext.packageName
        )

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE)
                channel.lightColor = Color.BLUE
                channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                manager.createNotificationChannel(channel)
            }

            Notification.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle("Updating...")
                .setOngoing(true)
                .setSmallIcon(if (iconResId != 0) iconResId else R.drawable.icon)
                .setGroup(CHANNEL_GROUP)
                .build()
        } else {
            // Pre-O fallback
            Notification.Builder(applicationContext)
                .setContentTitle("Updating...")
                .setSmallIcon(if (iconResId != 0) iconResId else R.drawable.icon)
                .setGroup(CHANNEL_GROUP)
                .build()
        }

        startForeground(TEMP_NOTIFICATION_ID, notification)

        // Remove notification after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            stopForeground(true)
            if (BuildConfig.DEBUG) Log.d(TAG, "Temporary foreground notification removed")
        }, 3000)
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { super.onDestroy() }
}
