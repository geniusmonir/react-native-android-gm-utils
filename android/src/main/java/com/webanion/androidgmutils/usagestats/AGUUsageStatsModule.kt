package com.webanion.androidgmutils.usagestats

import android.content.Intent
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.provider.Settings
import java.util.Calendar
import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap

class AGUUsageStatsModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return NAME
    }

    @ReactMethod
    fun hasUsageAccess(promise: Promise) {
        val context = reactApplicationContext
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        val granted = mode == AppOpsManager.MODE_ALLOWED
        promise.resolve(granted)
    }

    @ReactMethod
    fun requestUsageAccess() {
        val context = reactApplicationContext
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @ReactMethod
    fun getAppUsageStats(
        timeRange: String, // day week month year last24hours last7days last30days
        mode: String,  // "last" or "standard"
        promise: Promise
    ) {
        try {
            val context = reactApplicationContext
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            val (startTime, endTime) = getTimeRange(timeRange, mode)
            val usageStatsList: List<UsageStats> = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime
            )

            if (usageStatsList.isNullOrEmpty()) {
                promise.reject("NO_DATA", "No usage stats available. Ensure the permission is granted.")
                return
            }

            val statsArray = WritableNativeArray()
            for (usageStats in usageStatsList) {
                val statsMap = WritableNativeMap().apply {
                    putString("packageName", usageStats.packageName)
                    putDouble("totalTimeInForeground", usageStats.totalTimeInForeground.toDouble())
                    putDouble("lastTimeUsed", usageStats.lastTimeUsed.toDouble())
                }
                statsArray.pushMap(statsMap)
            }
            promise.resolve(statsArray)

        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }

    private fun getTimeRange(timeRange: String, mode: String): Pair<Long, Long> {
    val calendar = Calendar.getInstance()
    val endTime = System.currentTimeMillis()

    when (timeRange) {
        "day" -> calendar.add(Calendar.DAY_OF_YEAR, if (mode == "last") -1 else 0)
        "week" -> calendar.set(Calendar.DAY_OF_WEEK, if (mode == "last") Calendar.MONDAY else calendar.firstDayOfWeek)
        "month" -> calendar.set(Calendar.DAY_OF_MONTH, if (mode == "last") 1 else calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
        "year" -> calendar.set(Calendar.DAY_OF_YEAR, if (mode == "last") 1 else calendar.getActualMinimum(Calendar.DAY_OF_YEAR))
        "last24hours" -> calendar.add(Calendar.HOUR_OF_DAY, -24)
        "last7days" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
        "last30days" -> calendar.add(Calendar.DAY_OF_YEAR, -30)
    }

    val startTime = calendar.timeInMillis
        return Pair(startTime, endTime)
    }


    companion object {
      const val NAME = "AGUUsageStatsModule"
    }
}
