package com.webanion.androidgmutils.screenrecording

import android.content.Intent
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import android.util.Log

class ScreenRecordingHeadlessTaskService : HeadlessJsTaskService() {
    companion object {
        private const val TAG = "ScreenRecording"
    }

    override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
        val extras = intent.extras
        val bundle = extras?.getBundle("data")
        val srEventsData = bundle?.let { Arguments.fromBundle(it) } ?: Arguments.createMap()

        return HeadlessJsTaskConfig(
            "SCREEN_RECORDING_LISTENER_HEADLESS_TASK",
            srEventsData,
            300000, // timeout in ms
            true // optional: defines whether or not the task is allowed in foreground
        )
    }
}
