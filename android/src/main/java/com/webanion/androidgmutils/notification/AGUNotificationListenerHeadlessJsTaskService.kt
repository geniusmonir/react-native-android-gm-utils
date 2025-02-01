package com.webanion.androidgmutils.notification

import android.content.Intent
import android.os.Bundle
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import javax.annotation.Nullable

class AGUNotificationListenerHeadlessJsTaskService : HeadlessJsTaskService() {

    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig? {
        val extras: Bundle? = intent?.extras
        return extras?.let {
            HeadlessJsTaskConfig(
                "AGU_NOTIFICATION_LISTENER_HEADLESS_TASK",  // Task name
                Arguments.fromBundle(extras),                // Arguments from the bundle
                15000,                                       // Timeout for the task
                true                                         // Allow foreground tasks
            )
        }
    }
}
