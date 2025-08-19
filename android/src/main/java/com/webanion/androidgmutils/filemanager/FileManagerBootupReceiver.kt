package com.webanion.androidgmutils.filemanager

import com.webanion.androidgmutils.BuildConfig
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.Build

class FileManagerBootupReceiver : BroadcastReceiver() {
    private val TAG = "AGUFileManager"

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "${context.packageName}.RESTART_SERVICE",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.RESTART" -> {
                startFileManagerService(context)
            }
        }
    }

    private fun startFileManagerService(context: Context) {
        val serviceIntent = Intent(context, FileManagerListener::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "FileManagerListener started successfully.")
        } catch (e: SecurityException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "SecurityException: Failed to start service. Ensure proper permissions.", e)
        } catch (e: IllegalStateException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "IllegalStateException: App is in background and cannot start service.", e)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Exception: Failed to start service.", e)
        }
    }
}
