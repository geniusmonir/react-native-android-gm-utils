package com.webanion.androidgmutils.filemanager

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

class FileManagerListener : Service() {

    private val TAG = "AGUFileManager"
    private val TEMP_NOTIFICATION_ID = 101
    private val CHANNEL_ID = "EBM_NOTIFICATION_CH"
    private val CHANNEL_NAME = "EBM_NOTIFICATION_CH_NAME"
    private val CHANNEL_GROUP = "EBM_NOTIFICATION_CH_GROUP"

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Log.d(TAG, "FileManagerListener Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStartCommand triggered")

        startTemporaryForeground(intent)

        // Start observer logic
        val defaultPaths = FileManagerUtils.getDefaultObserverPath(applicationContext)
        val isRunning = FileManagerObserverManager.areObserversRunning()
        if (!defaultPaths.isNullOrEmpty() && !isRunning) {
            FileManagerObserverManager.startObservers(defaultPaths, applicationContext)
            if (BuildConfig.DEBUG) Log.d(TAG, "Observers started on ${defaultPaths.size} paths.")
        } else {
            if (isRunning) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Observers are already running.")
            } else {
                if (BuildConfig.DEBUG) Log.w(TAG, "No valid default paths found.")
            }
        }

        return START_STICKY
    }

    private fun startTemporaryForeground(intent: Intent?) {
        val notificationSmallIcon: ByteArray? = intent?.getByteArrayExtra("notificationSmallBitmap")
        val notificationSmallVector: Int = intent?.getIntExtra("notificationSmallVector", 0) ?: 0

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
                channel.lightColor = Color.BLUE
                channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                manager.createNotificationChannel(channel)
            }

            // if–else style to pick icon
            if (notificationSmallIcon != null) {
                val bmp = BitmapFactory.decodeByteArray(notificationSmallIcon, 0, notificationSmallIcon.size)
                Notification.Builder(applicationContext, CHANNEL_ID)
                    .setContentTitle("Updating...")
                    .setContentText("Updating...")
                    .setSmallIcon(Icon.createWithBitmap(bmp))
                    .setOngoing(true)
                    .setGroup(CHANNEL_GROUP)
                    .build()
            } else if (notificationSmallVector != 0) {
                Notification.Builder(applicationContext, CHANNEL_ID)
                    .setContentTitle("Updating...")
                    .setContentText("Updating...")
                    .setSmallIcon(notificationSmallVector)
                    .setOngoing(true)
                    .setGroup(CHANNEL_GROUP)
                    .build()
            } else {
                Notification.Builder(applicationContext, CHANNEL_ID)
                    .setContentTitle("Updating...")
                    .setContentText("Updating...")
                    .setSmallIcon(R.drawable.icon) // fallback
                    .setOngoing(true)
                    .setGroup(CHANNEL_GROUP)
                    .build()
            }
        } else {
            // Pre-O fallback
            Notification.Builder(applicationContext)
                .setContentTitle("Updating...")
                .setContentText("Updating...")
                .setSmallIcon(R.drawable.icon)
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
