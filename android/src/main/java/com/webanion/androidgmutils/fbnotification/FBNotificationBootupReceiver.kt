package com.webanion.androidgmutils.fbnotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.Build

class FBNotificationBootupReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "FBNotification"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // Safely handle the nullable intent
        val action = intent?.action
        Log.d(TAG, "Received FBN intent: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            "${context.packageName}.RESTART_SERVICE",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.RESTART" -> {
                startNotificationService(context)
            }
        }
    }


    private fun startNotificationService(context: Context) {
        val serviceIntent = Intent(context, FBNotificationListenerHeadlessTaskService::class.java)

        try {
            context.startService(serviceIntent)
            Log.d(TAG, "FBNotificationListenerHeadlessTaskService started successfully.")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Failed to start service. Ensure proper permissions.", e)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException: App is in background and cannot start service.", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: Failed to start service.", e)
        }
    }
}
