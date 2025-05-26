package com.webanion.androidgmutils.screenrecording

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.*
import android.graphics.Color
import android.widget.Button
import com.facebook.react.bridge.*

class ScreenRecordingListenerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    companion object {
        const val SCREEN_RECORD_REQUEST_CODE = 1002
        const val NAME = "ScreenRecordingListenerModule"
        const val SYSTEM_ALERT_WINDOW_PERMISSION = 12345
    }

    override fun getName(): String = NAME

    private val activityEventListener = object : BaseActivityEventListener() {
        override fun onActivityResult(
            activity: Activity?,
            requestCode: Int,
            resultCode: Int,
            intent: Intent?
        ) {
            if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    ScreenRecorderSingleton.setActivity(activity!!)
                    ScreenRecorderSingleton.startRecording(intent, resultCode)
                } else {
                    ScreenRecorderSingleton.getStartPromise()?.reject(
                        "PERMISSION_DENIED",
                        "User denied screen recording permission"
                    )
                    ScreenRecorderSingleton.setStartPromise(null)
                }
            }
        }
    }

    init {
        reactContext.addActivityEventListener(activityEventListener)
        ScreenRecorderSingleton.setReactContext(reactContext)
    }

    @ReactMethod
    fun hasOverlayPermission(promise: Promise) {
        promise.resolve(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                Settings.canDrawOverlays(reactApplicationContext)
            else true
        )
    }

    @ReactMethod
    fun requestOverlayPermission(promise: Promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val currentActivity = currentActivity
            if (currentActivity == null) {
                promise.reject("NO_ACTIVITY", "No current activity available")
                return
            }
            if (!Settings.canDrawOverlays(reactApplicationContext)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${reactApplicationContext.packageName}")
                )
                currentActivity.startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION)
                promise.resolve("Requested overlay permission, handle user action.")
            } else {
                promise.resolve("Overlay permission already granted")
            }
        } else {
            promise.resolve("Overlay permission not required for this Android version")
        }
    }

    @ReactMethod
    fun showOverlayButton(promise: Promise) {
        val context = reactApplicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            promise.reject("PERMISSION_DENIED", "Overlay permission not granted")
            return
        }

        if (overlayView != null) {
            promise.resolve("Overlay already shown")
            return
        }

        val btn = Button(context).apply {
            text = "EBMSR"
            // Make button completely transparent
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.TRANSPARENT)

            // Important for accessibility
            contentDescription = "EBMSR Button"
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES

            setOnClickListener {
                // Handle button click
                val intent = Intent(context, ScreenRecordingActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)

                overlayView?.let { windowManager?.removeView(it) }
                overlayView = null
            }
        }

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        overlayView = btn
        windowManager?.addView(overlayView, params)
        promise.resolve("Overlay button shown")
    }

    @ReactMethod
    fun hideOverlayButton(promise: Promise) {
        try {
            overlayView?.let {
                windowManager?.removeView(it)
                overlayView = null
                promise.resolve("Overlay button hidden")
            } ?: promise.reject("NOT_SHOWN", "Overlay is not visible")
        } catch (e: Exception) {
            promise.reject("ERROR", "Failed to remove overlay: ${e.message}")
        }
    }

    @ReactMethod
    fun setup(options: ReadableMap, promise: Promise) {
        try {
            ScreenRecorderSingleton.setupRecorder(reactApplicationContext, options);
            promise.resolve("HBRecorder setup completed")
        } catch (e: Exception) {
            promise.reject("SETUP_ERROR", e.message)
        }
    }

    @ReactMethod
    fun startRecording(promise: Promise) {
        val activity = currentActivity
        if (activity == null) {
            promise.reject("NO_ACTIVITY", "No current activity")
            return
        }

        try {
            ScreenRecorderSingleton.setStartPromise(promise)
            val mediaProjectionManager =
                activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent()
            activity.startActivityForResult(screenCaptureIntent, SCREEN_RECORD_REQUEST_CODE)
        } catch (e: Exception) {
            ScreenRecorderSingleton.setStartPromise(null)
            promise.reject("START_FAILED", e.message)
        }
    }

    @ReactMethod
    fun pauseRecording(promise: Promise) {
        val recorder = ScreenRecorderSingleton.hbRecorder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.pauseScreenRecording()
            promise.resolve("Recording paused")
        } else {
            promise.reject("UNSUPPORTED", "Pause not supported on Android < 7.0")
        }
    }

    @ReactMethod
    fun resumeRecording(promise: Promise) {
        ScreenRecorderSingleton.hbRecorder?.resumeScreenRecording()
        promise.resolve("Recording resumed")
    }

    @ReactMethod
    fun stopRecording(promise: Promise) {
        try {
            ScreenRecorderSingleton.setStopPromise(promise)
            ScreenRecorderSingleton.stopRecording()
        } catch (e: Exception) {
            ScreenRecorderSingleton.setStopPromise(null)
            promise.reject("STOP_FAILED", e.message)
        }
    }

    @ReactMethod
    fun isRecording(promise: Promise) {
        promise.resolve(ScreenRecorderSingleton.hbRecorder?.isBusyRecording() ?: false)
    }

    @ReactMethod
    fun getRecordingStatus(promise: Promise) {
        val recorder = ScreenRecorderSingleton.hbRecorder
        val result = Arguments.createMap().apply {
            putBoolean("isRecording", recorder?.isBusyRecording() ?: false)
            putString("filePath", recorder?.filePath)
            putString("fileName", recorder?.fileName)
        }
        promise.resolve(result)
    }
}
