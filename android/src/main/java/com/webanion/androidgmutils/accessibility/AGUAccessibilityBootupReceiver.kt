package com.webanion.androidgmutils.accessibility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

class AGUAccessibilityBootupReceiver : BroadcastReceiver() {
    companion object {
      private const val TAG = "AGUAccessibility"
    }
    override fun onReceive(context: Context, intent: Intent) {
        // Safely handle the nullable intent
        val action = intent?.action
        Log.d(TAG, "Received AGU intent: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            "${context.packageName}.RESTART_ACCESSIBILITY_SERVICE",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.RESTART" -> {
              // Start the service first
              context.startService(Intent(context, AGUAccessibilityListener::class.java))

              // Force reconnect after a delay to ensure service is ready
              Handler(Looper.getMainLooper()).postDelayed({
                  try {
                      Log.d(TAG, "Force reconnecting accessibility service after boot/restart")
                      AGUAccessibilityListener.forceReconnect()
                  } catch (e: Exception) {
                      Log.e(TAG, "Failed to force reconnect after boot", e)
                  }
              }, 10000) // 10 second delay
            }
        }
    }
}
