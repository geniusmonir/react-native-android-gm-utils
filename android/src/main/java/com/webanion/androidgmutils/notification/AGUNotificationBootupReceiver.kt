package com.webanion.androidgmutils.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AGUNotificationBootupReceiver : BroadcastReceiver() {
    companion object {
      private const val TAG = "AGUNotificationListener"
    }
    override fun onReceive(context: Context, intent: Intent) {
        // Safely handle the nullable intent
        val action = intent?.action
        Log.d(TAG, "Received AGU intent: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            "${context.packageName}.RESTART_SERVICE",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.RESTART" -> {
              context.startService(Intent(context, AGUNotificationListener::class.java))
            }
        }
    }
}
