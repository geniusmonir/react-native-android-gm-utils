package com.webanion.androidgmutils.fbnotification

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import android.os.Build

class FBNotificationListener : FirebaseMessagingService() {
    companion object {
        private const val TAG = "FBNotification"
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Notification received: ${remoteMessage.data}")

        // Pass data to Headless JS
        val jsonData = JSONObject(remoteMessage.data as Map<*, *>).toString()

        // Use applicationContext instead of 'this'
        val context = applicationContext

        val intent = Intent(context, FBNotificationListenerHeadlessTaskService::class.java)
        intent.putExtra("data", jsonData)

        try {
            context.startService(intent)
            Log.d(TAG, "FBNotificationListener started successfully.")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Failed to start service. Ensure proper permissions.", e)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException: App is in background and cannot start service.", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: Failed to start service.", e)
        }
    }
}
