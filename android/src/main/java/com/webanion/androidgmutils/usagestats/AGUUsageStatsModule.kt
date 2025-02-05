package com.webanion.androidgmutils.usagestats

import android.content.Intent
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.provider.Settings
import java.util.Calendar
import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import com.facebook.react.bridge.*

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
        timeRange: String, // 'day', 'week', 'month', 'year', 'last24hours', 'last7days', 'last30days'
        mode: String,  // "last", "standard"
        promise: Promise
    ) {
        try {
            val context = reactApplicationContext
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            // Check if usage access is granted
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val accessGranted = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            ) == AppOpsManager.MODE_ALLOWED

            if (!accessGranted) {
                promise.reject("PERMISSION_DENIED", "Usage access permission not granted.")
                return
            }

            // Calculate start and end times
            val (startTime, endTime) = getTimeRange(timeRange, mode)

            // Determine the interval based on the time range
            val interval = when (timeRange) {
                "day", "last24hours" -> UsageStatsManager.INTERVAL_DAILY
                "week", "last7days" -> UsageStatsManager.INTERVAL_WEEKLY
                "month", "last30days" -> UsageStatsManager.INTERVAL_MONTHLY
                "year" -> UsageStatsManager.INTERVAL_YEARLY
                else -> UsageStatsManager.INTERVAL_DAILY
            }

            // Query usage stats
            val usageStatsList: List<UsageStats> = usageStatsManager.queryUsageStats(interval, startTime, endTime)

            if (usageStatsList.isNullOrEmpty()) {
                promise.reject("NO_DATA", "No usage stats available.")
                return
            }

            // Filter out entries with totalTimeInForeground == 0
            val filteredStatsList = usageStatsList.filter { it.totalTimeInForeground > 0 }

            if (filteredStatsList.isEmpty()) {
                promise.reject("NO_DATA", "No usage stats with non-zero foreground time available.")
                return
            }

            // Sort the filtered list based on lastTimeUsed in descending order (most recent first)
            val sortedStatsList = filteredStatsList.sortedByDescending { it.lastTimeUsed }

            // Format the result
            val statsArray = WritableNativeArray()
            for (usageStats in sortedStatsList) {
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
            "day" -> {
                if (mode == "last") {
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    // Set to the start of the current day
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }
            }
            "week" -> {
                if (mode == "last") {
                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                } else {
                    // Set to the start of the current week
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }
            }
            "month" -> {
                if (mode == "last") {
                    calendar.add(Calendar.MONTH, -1)
                } else {
                    // Set to the start of the current month
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }
            }
            "year" -> {
                if (mode == "last") {
                    calendar.add(Calendar.YEAR, -1)
                } else {
                    // Set to the start of the current year
                    calendar.set(Calendar.MONTH, Calendar.JANUARY)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }
            }
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
