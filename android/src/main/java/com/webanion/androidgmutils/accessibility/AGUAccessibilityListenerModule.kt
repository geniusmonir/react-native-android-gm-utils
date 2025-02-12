package com.webanion.androidgmutils.accessibility

import android.content.Intent
import android.provider.Settings
import android.net.Uri
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.text.TextUtils
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityManager
import com.facebook.react.bridge.Promise

class AGUAccessibilityListenerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return NAME
    }

    @ReactMethod
    fun isAccessibilityServiceEnabled(promise: Promise) {
        try {
            val context = reactApplicationContext
            val expectedComponentName = ComponentName(context, AGUAccessibilityListener::class.java)

            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedComponentName.flattenToString(), ignoreCase = true)) {
                    promise.resolve(true) // Service is enabled
                    return
                }
            }

            promise.resolve(false) // Service is disabled
        } catch (e: Exception) {
            promise.reject("ERROR", "Failed to check accessibility service status", e)
        }
    }

    @ReactMethod
    fun requestAccessibilityPermission() {
        val context = reactApplicationContext
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @ReactMethod
    fun setAccessibilityStorageDuration(minutes: Double) {
        val durationMs = (minutes * 60 * 1000).toLong() // Convert minutes to milliseconds
        AGUAccessibilityListener.setAccessibilityStorageDuration(durationMs) // Minutes
    }

    companion object {
      const val NAME = "AGUAccessibilityListenerModule"
    }
}
