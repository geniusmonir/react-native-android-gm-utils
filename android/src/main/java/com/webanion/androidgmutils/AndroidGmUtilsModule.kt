package com.webanion.androidgmutils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class AndroidGmUtilsModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return NAME
    }

    @ReactMethod
    fun isBatteryOptimizationEnabled(promise: Promise) {
        val context = reactApplicationContext
        val packageName = context.packageName
        val powerManager = context.getSystemService(PowerManager::class.java)

        if (powerManager != null) {
            val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName)
            promise.resolve(!isIgnoringBatteryOptimizations)
        } else {
            promise.reject("POWER_MANAGER_ERROR", "PowerManager service not available")
        }
    }

    @ReactMethod
    fun requestDisableBatteryOptimization(promise: Promise) {
        val context = reactApplicationContext
        val packageName = context.packageName
        val powerManager = context.getSystemService(PowerManager::class.java)

        // First check if already disabled
        if (powerManager != null && powerManager.isIgnoringBatteryOptimizations(packageName)) {
            promise.resolve(true)
            return
        }

        // If not disabled, show the disable dialog
        val intent = Intent().apply {
            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            data = Uri.parse("package:$packageName")
        }

        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this flag
                context.startActivity(intent)
                promise.resolve(true)
            } else {
                promise.reject("INTENT_ERROR", "Unable to resolve intent for battery optimization settings")
            }
          } catch (e: Exception) {
              promise.reject("INTENT_FAILED", "Failed to open battery optimization settings: ${e.message}")
        }
    }

    @ReactMethod
    fun openBackgroundAutoStartSettings(promise: Promise) {
        try {
            val context = reactApplicationContext
            val manufacturer = Build.MANUFACTURER.lowercase()

            when {
                manufacturer.contains("xiaomi") -> openXiaomiSettings(context)
                manufacturer.contains("huawei") -> openHuaweiSettings(context)
                manufacturer.contains("honor") -> openHonorSettings(context)
                manufacturer.contains("oppo") -> openOppoRealmeSettings(context)
                manufacturer.contains("realme") -> openOppoRealmeSettings(context)
                manufacturer.contains("vivo") -> openVivoSettings(context)
                manufacturer.contains("oneplus") -> openOnePlusSettings(context)
                manufacturer.contains("samsung") -> openSamsungSettings(context)
                else -> openGenericSettings(context)
            }
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("AUTO_START_ERROR", "Failed to open settings: ${e.message}")
        }
    }

    private fun openXiaomiSettings(context: Context) {
        Intent().apply {
            setClassName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this flag
            context.startActivity(this)
        }
    }

    private fun openHuaweiSettings(context: Context) {
        Intent().apply {
            setClassName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this flag
            context.startActivity(this)
        }
    }

    private fun openHonorSettings(context: Context) {
        Intent().apply {
            setClassName(
                "com.hihonor.systemmanager",
                "com.hihonor.systemmanager.optimize.process.ProtectActivity"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this flag
            context.startActivity(this)
        }
    }

    private fun openOppoRealmeSettings(context: Context) {
        Intent().apply {
            setClassName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this flag
            context.startActivity(this)
        }
    }

    private fun openVivoSettings(context: Context) {
        Intent().apply {
            setClassName(
                "com.vivo.abe",
                "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this flag
            context.startActivity(this)
        }
    }

    private fun openOnePlusSettings(context: Context) {
        Intent().apply {
            setClassName(
                "com.oneplus.security",
                "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this flag
            context.startActivity(this)
        }
    }

    private fun openSamsungSettings(context: Context) {
        Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this flag
            context.startActivity(this)
        }
    }

    private fun openGenericSettings(context: Context) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this flag
            context.startActivity(this)
        }
    }

    companion object {
        const val NAME = "AndroidGmUtils"
        private const val REQUEST_CODE_DISABLE_BATTERY_OPTIMIZATION = 1001
    }
}
