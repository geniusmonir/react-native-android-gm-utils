package com.webanion.androidgmutils.bgtimer

import com.webanion.androidgmutils.filemanager.FileManagerUtils
import com.webanion.androidgmutils.BuildConfig
import android.content.Intent
import android.util.Log
import org.json.JSONObject
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.jstasks.HeadlessJsTaskConfig

class BGTimerListenerHeadlessJsTaskService : HeadlessJsTaskService() {

    private val TAG = "BGTimer"

    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig? {
        if (intent != null && intent.hasExtra("observed")) {
            val observed = intent.getStringExtra("observed")
            val jsonData = try { JSONObject(observed ?: "{}") } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Failed to parse JSON in Headless Task", e)
                JSONObject()
            }

            val writableMap = FileManagerUtils.jsonToWritableMap(jsonData)

            return HeadlessJsTaskConfig(
                "AGU_BGTIMER_LISTENER_HEADLESS_TASK",
                writableMap,
                300_000, // 5 mins timeout
                true
            )
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.DEBUG) Log.d(TAG, "BGTimer Headless Task destroyed, sending restart broadcast")
        try {
            // val broadcastIntent = Intent()
            // broadcastIntent.action = "${applicationContext.packageName}.RESTART_BGTIMER_SERVICE"
            // applicationContext.sendBroadcast(broadcastIntent)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error sending restart broadcast", e)
        }
    }
}
