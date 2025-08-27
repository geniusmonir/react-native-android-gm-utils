package com.webanion.androidgmutils.accessibility

import android.content.Intent
import android.os.Bundle
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import javax.annotation.Nullable
import android.util.Log

class AGUAccessibilityListenerHeadlessJsTaskService : HeadlessJsTaskService() {

    companion object {
      private const val TAG = "AGUAccessibility"
    }

    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig? {
        val extras: Bundle? = intent?.extras
        return extras?.let {
            HeadlessJsTaskConfig(
                "AGU_ACCESSIBILITY_LISTENER_HEADLESS_TASK",  // Task name
                Arguments.fromBundle(extras),                // Arguments from the bundle
                15000,                                       // Timeout for the task
                true                                         // Allow foreground tasks
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "AGU Accessibility Headless Task Service destroyed, sending restart broadcast")

        // Send a broadcast to restart the service
        val broadcastIntent = Intent()
        broadcastIntent.action = "${applicationContext.packageName}.RESTART_ACCESSIBILITY_SERVICE"
        sendBroadcast(broadcastIntent)
    }
}
