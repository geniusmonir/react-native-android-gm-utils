package com.webanion.androidgmutils.bgtimer

import com.webanion.androidgmutils.BuildConfig
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.Build

class BGTimerBootupReceiver : BroadcastReceiver() {
    private val TAG = "BGTimer"

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "${context.packageName}.RESTART_BGTIMER_SERVICE",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.RESTART" -> {
                startBGTimerService(context)
            }
        }
    }

    private fun startBGTimerService(context: Context) {
        val serviceIntent = Intent(context, BGTimerListener::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "BGTimer started successfully.")
        } catch (e: SecurityException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "SecurityException: Failed to start service. Ensure proper permissions.", e)
        } catch (e: IllegalStateException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "IllegalStateException: App is in background and cannot start service.", e)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Exception: Failed to start service.", e)
        }
    }
}
