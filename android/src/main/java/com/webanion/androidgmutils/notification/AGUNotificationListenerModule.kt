package com.webanion.androidgmutils.notification

import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class AGUNotificationListenerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return NAME
    }

    @ReactMethod
    fun addNumbers(a: Double, b: Double, promise: Promise) {
      promise.resolve(a + b)
    }

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

    companion object {
      const val NAME = "AGUNotificationListenerModule"
    }
}
