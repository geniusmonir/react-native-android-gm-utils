package com.webanion.androidgmutils.notification

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.gson.Gson
import com.facebook.react.HeadlessJsTaskService

class AGUNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "AGUNotificationListener"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        processNotification(sbn, "posted")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        processNotification(sbn, "removed")
    }

    private fun processNotification(sbn: StatusBarNotification?, action: String) {
        sbn?.notification?.let { notification ->
            // Log.d(TAG, "Notification $action event fired")

            if (notification.extras == null) {
                // Log.d(TAG, "The notification received has no data")
                return
            }

            val context = applicationContext // or getApplicationContext()
            val serviceIntent = Intent(context, AGUNotificationListenerHeadlessJsTaskService::class.java)

            val agnNotification = AGUNotification(context, sbn, action)
            val gson = Gson()
            val serializedNotification = gson.toJson(agnNotification)

            serviceIntent.putExtra("notification", serializedNotification)

            HeadlessJsTaskService.acquireWakeLockNow(context)

            context.startService(serviceIntent)
        }
    }
}
