package com.webanion.androidgmutils.notification

import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.Arguments
import com.google.gson.Gson

class AGUNotificationListenerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
      const val NAME = "AGUNotificationListenerModule"
    }

    override fun getName(): String = NAME

    @ReactMethod
    fun getPermissionStatus(promise: Promise) {
        if (reactApplicationContext == null) {
            promise.resolve("unknown")
        } else {
            val packageName = reactApplicationContext.packageName
            val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(reactApplicationContext)
            if (enabledPackages.contains(packageName)) {
                promise.resolve("authorized")
            } else {
                promise.resolve("denied")
            }
        }
    }

    @ReactMethod
    fun requestPermission() {
        if (reactApplicationContext != null) {
            val intent = Intent()
            intent.action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            reactApplicationContext.startActivity(intent)
        }
    }

    @ReactMethod
    fun getCurrentNotifications(promise: Promise) {
        try {
            // Check if the notification listener service is running
            val service = AGUNotificationListener.instance
            if (service == null) {
                promise.reject("SERVICE_NOT_RUNNING", "Notification listener service is not running")
                return
            }

            // Get active notifications
            val notifications = service.getCurrentNotifications()
            val gson = Gson()
            val notificationsArray: WritableArray = Arguments.createArray()

            for (sbn in notifications) {
                // Ensure notification has valid data
                sbn.notification?.let { notification ->
                    if (notification.extras != null) {
                        // Use AGUNotification to process the notification, matching processNotification
                        val agnNotification = AGUNotification(reactApplicationContext, sbn, "active")
                        val serializedNotification = gson.toJson(agnNotification)
                        notificationsArray.pushString(serializedNotification)
                    }
                }
            }

            promise.resolve(notificationsArray)
        } catch (e: Exception) {
            promise.reject("ERROR", "Failed to fetch notifications: ${e.message}")
        }
    }
}
