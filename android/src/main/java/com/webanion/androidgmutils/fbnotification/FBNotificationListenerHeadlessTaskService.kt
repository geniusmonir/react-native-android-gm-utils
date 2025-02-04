package com.webanion.androidgmutils.fbnotification

import android.content.Intent
import android.util.Log
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import org.json.JSONObject

class FBNotificationListenerHeadlessTaskService : HeadlessJsTaskService() {

    companion object {
        private const val TAG = "FBNotification"
    }


    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig? {
        if (intent != null && intent.hasExtra("data")) {
            val data = intent.getStringExtra("data")
            val jsonData =
                    try {
                        JSONObject(data ?: "{}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Invalid JSON data received: $data", e)
                        JSONObject()
                    }

            // Log.d(TAG, "Starting Headless Task with data: $jsonData")

            // Convert JSONObject to WritableMap
            val writableMap = Arguments.createMap()
            jsonData.keys().forEach { key -> writableMap.putString(key, jsonData.getString(key)) }

            return HeadlessJsTaskConfig(
                    "FB_NOTIFICATION_LISTENER_HEADLESS_TASK", // Task name registered in index.js
                    writableMap, // Pass WritableMap to JS
                    300000, // Timeout (5 minutes)
                    true // Allow execution while the device is in foreground
            )
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "FBN Headless Task Service destroyed, sending restart broadcast")

        // Send a broadcast to restart the service
        val broadcastIntent = Intent()
        broadcastIntent.action = "${applicationContext.packageName}.RESTART_SERVICE"
        sendBroadcast(broadcastIntent)
    }
}
