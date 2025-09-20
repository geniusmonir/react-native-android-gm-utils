package com.webanion.androidgmutils.bgtimer

import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import org.json.JSONObject
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.HeadlessJsTaskService

class BGTimerListenerModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext), LifecycleEventListener {

    private val powerManager: PowerManager =
        reactContext.getSystemService(ReactApplicationContext.POWER_SERVICE) as PowerManager
    private val wakeLock: PowerManager.WakeLock =
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AGU:BGTimerWakelock")

    data class IntervalEntry(val runnable: Runnable, val handler: Handler, var isRunning: Boolean)
    private val intervalMap = mutableMapOf<Int, IntervalEntry>()
    private var uniqueId = 0

    init {
        reactContext.addLifecycleEventListener(this)
    }

    override fun getName(): String = NAME

    /** Generate or return provided ID */
    private fun getOrGenerateId(providedId: Int): Int {
        return if (providedId == 0) {
            uniqueId++
            uniqueId
        } else {
            providedId
        }
    }

    /** Start single-shot background timer (returns ID) */
    @ReactMethod
    fun start(delay: Int, id: Int, promise: Promise) {
        if (!wakeLock.isHeld) wakeLock.acquire(10 * 60 * 1000L)

        val handler = Handler(Looper.getMainLooper())
        val intervalId = getOrGenerateId(id)

        val runnable = Runnable {
            sendEvent("backgroundTimer", intervalId)
            triggerHeadlessTask(intervalId)
        }

        intervalMap[intervalId] = IntervalEntry(runnable, handler, false)
        handler.postDelayed(runnable, delay.toLong())
        promise.resolve(intervalId)
    }

    /** Stop all timers */
    @ReactMethod
    fun stop(promise: Promise) {
        if (wakeLock.isHeld) wakeLock.release()
        intervalMap.values.forEach { entry ->
            entry.handler.removeCallbacks(entry.runnable)
        }
        intervalMap.clear()
        promise.resolve(true)
    }

    /** setTimeout support (returns ID) */
    @ReactMethod
    fun setTimeout(timeout: Double, id: Int, promise: Promise) {
        val handler = Handler(Looper.getMainLooper())
        val intervalId = getOrGenerateId(id)

        val runnable = Runnable {
            if (reactContext.hasActiveReactInstance()) {
                sendEvent("backgroundTimer.timeout", intervalId)
            } else {
                triggerHeadlessTask(intervalId)
            }
        }

        intervalMap[intervalId] = IntervalEntry(runnable, handler, false)
        handler.postDelayed(runnable, timeout.toLong())
        promise.resolve(intervalId)
    }

    /** setInterval support (returns ID) */
    @ReactMethod
    fun setInterval(timeout: Double, id: Int, promise: Promise) {
        val handler = Handler(Looper.getMainLooper())
        val intervalId = getOrGenerateId(id)

        val runnable = object : Runnable {
            override fun run() {
                if (reactContext.hasActiveReactInstance()) {
                    sendEvent("backgroundTimer.timeout", intervalId)
                } else {
                    triggerHeadlessTask(intervalId)
                }
                handler.postDelayed(this, timeout.toLong())
            }
        }

        intervalMap[intervalId] = IntervalEntry(runnable, handler, false)
        handler.postDelayed(runnable, timeout.toLong())
        promise.resolve(intervalId)
    }

    /** Async-safe interval (returns ID) */
    @ReactMethod
    fun setIntervalAsync(timeout: Double, id: Int, promise: Promise) {
        if (!wakeLock.isHeld) wakeLock.acquire()
        val handler = Handler(Looper.getMainLooper())
        val intervalId = getOrGenerateId(id)

        val runnable = object : Runnable {
            override fun run() {
                val entry = intervalMap[intervalId] ?: return
                if (entry.isRunning) {
                    handler.postDelayed(this, timeout.toLong())
                    return
                }
                entry.isRunning = true

                if (reactContext.hasActiveReactInstance()) {
                    sendEvent("backgroundTimer.timeout", intervalId)
                } else {
                    triggerHeadlessTask(intervalId)
                }
            }
        }

        intervalMap[intervalId] = IntervalEntry(runnable, handler, false)
        handler.postDelayed(runnable, timeout.toLong())
        promise.resolve(intervalId)
    }

    /** Mark interval finished so next tick can run */
    @ReactMethod
    fun markIntervalFinished(id: Int) {
        intervalMap[id]?.isRunning = false
    }

    /** clearInterval / clearTimeout support */
    @ReactMethod
    fun clearInterval(id: Int) {
        intervalMap[id]?.let { entry ->
            entry.handler.removeCallbacks(entry.runnable)
        }
        intervalMap.remove(id)

        // Release wake lock if nothing is left
        if (intervalMap.isEmpty() && wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    /** Send event to JS */
    private fun sendEvent(eventName: String, params: Any?) {
        if (reactContext.hasActiveReactInstance()) {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        }
    }

    /** Trigger HeadlessJS task (foreground service if needed) */
    private fun triggerHeadlessTask(taskId: Int) {
        val taskData = JSONObject().apply {
            put("taskId", taskId)
            put("taskTimestamp", System.currentTimeMillis().toString())
        }

        val serviceIntent = Intent(reactContext, BGTimerListenerHeadlessJsTaskService::class.java).apply {
            putExtra("observed", taskData.toString())
        }

        HeadlessJsTaskService.acquireWakeLockNow(reactContext)
        reactContext.startService(serviceIntent)
    }

    // Lifecycle cleanup
    override fun onHostResume() {}
    override fun onHostPause() {}
    override fun onHostDestroy() {
        if (wakeLock.isHeld) wakeLock.release()
        intervalMap.values.forEach { entry ->
            entry.handler.removeCallbacks(entry.runnable)
        }
        intervalMap.clear()
    }

    companion object {
        const val NAME = "BGTimerListenerModule"
    }
}
