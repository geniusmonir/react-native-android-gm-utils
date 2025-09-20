package com.webanion.androidgmutils.bgtimer

import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.HeadlessJsTaskService

class BGTimerListenerModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext), LifecycleEventListener {

    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private val powerManager: PowerManager =
        reactContext.getSystemService(ReactApplicationContext.POWER_SERVICE) as PowerManager
    private val wakeLock: PowerManager.WakeLock =
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AGU:BGTimerWakelock")

    // Store interval Runnables for clearInterval
    data class IntervalEntry(val runnable: Runnable, var isRunning: Boolean)
    private val intervalMap = mutableMapOf<Int, IntervalEntry>()
    private var uniqueId = 0

    init {
        reactContext.addLifecycleEventListener(this)
    }

    override fun getName(): String = NAME

    /** Start single-shot background timer */
    @ReactMethod
    fun start(delay: Int, promise: Promise) {
        if (!wakeLock.isHeld) {
            wakeLock.acquire(10 * 60 * 1000L) // max 10 mins
        }

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            sendEvent("backgroundTimer", null)
            triggerHeadlessTask(1)
        }

        handler?.postDelayed(runnable!!, delay.toLong())
        promise.resolve(true)
    }

    /** Stop timer */
    @ReactMethod
    fun stop(promise: Promise) {
        if (wakeLock.isHeld) wakeLock.release()
        runnable?.let { handler?.removeCallbacks(it) }
        runnable = null
        handler = null

        // Stop all intervals
        intervalMap.values.forEach { entry ->
            handler?.removeCallbacks(entry.runnable)
        }
        intervalMap.clear()

        promise.resolve(true)
    }

    /** setTimeout support */
    @ReactMethod
    fun setTimeout(id: Int, timeout: Double) {
        val h = Handler(Looper.getMainLooper())
        h.postDelayed({
            if (reactContext.hasActiveReactInstance()) {
                sendEvent("backgroundTimer.timeout", id)
            } else {
                triggerHeadlessTask(id)
            }
        }, timeout.toLong())
    }

    /** setInterval support */
    @ReactMethod
    fun setInterval(id: Int, timeout: Double) {
        val h = Handler(Looper.getMainLooper())
        val r = object : Runnable {
            override fun run() {
                if (reactContext.hasActiveReactInstance()) {
                    sendEvent("backgroundTimer.timeout", id)
                } else {
                    triggerHeadlessTask(id)
                }
                h.postDelayed(this, timeout.toLong())
            }
        }
        intervalMap[id] = IntervalEntry(r, false)
        h.postDelayed(r, timeout.toLong())
    }

    /** Async-safe interval */
    @ReactMethod
    fun setIntervalAsync(timeout: Double, promise: Promise) {
        uniqueId++
        val id = uniqueId

        val runnable = object : Runnable {
            override fun run() {
                val entry = intervalMap[id] ?: return
                if (entry.isRunning) {
                    handler?.postDelayed(this, timeout.toLong())
                    return
                }
                entry.isRunning = true
                triggerHeadlessTask(id)
            }
        }

        intervalMap[id] = IntervalEntry(runnable, false)
        handler?.postDelayed(runnable, timeout.toLong())
        promise.resolve(id)
    }

    /** Mark interval finished so next tick can run */
    @ReactMethod
    fun markIntervalFinished(id: Int) {
        intervalMap[id]?.isRunning = false
    }

    /** clearInterval support */
    @ReactMethod
    fun clearInterval(id: Int) {
        intervalMap[id]?.let { entry ->
            handler?.removeCallbacks(entry.runnable)  // <- ONLY pass Runnable
        }
        intervalMap.remove(id)
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
        val intent = Intent(reactContext, BGTimerListenerHeadlessJsTaskService::class.java).apply {
            putExtra("taskId", taskId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            reactContext.startForegroundService(intent)
        } else {
            reactContext.startService(intent)
        }
        HeadlessJsTaskService.acquireWakeLockNow(reactContext)
    }

    // Lifecycle cleanup
    override fun onHostResume() {}
    override fun onHostPause() {}
    override fun onHostDestroy() {
        if (wakeLock.isHeld) wakeLock.release()
        runnable?.let { handler?.removeCallbacks(it) }
        runnable = null
        handler = null
        intervalMap.values.forEach { entry ->
            handler?.removeCallbacks(entry.runnable)
        }
        intervalMap.clear()
    }

    companion object {
        const val NAME = "BGTimerListenerModule"
    }
}
