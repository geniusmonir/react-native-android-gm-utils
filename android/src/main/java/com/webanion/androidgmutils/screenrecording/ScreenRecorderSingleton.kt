package com.webanion.androidgmutils.screenrecording

import android.app.Activity
import android.content.Intent
import android.content.Context
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.hbisoft.hbrecorder.HBRecorder
import com.hbisoft.hbrecorder.HBRecorderListener
import com.facebook.react.HeadlessJsTaskService

object ScreenRecorderSingleton : HBRecorderListener {
    var hbRecorder: HBRecorder? = null
    var currentActivity: Activity? = null
    private var startPromise: Promise? = null
    private var stopPromise: Promise? = null
    private var reactContext: ReactApplicationContext? = null

    fun setReactContext(context: ReactApplicationContext) {
        reactContext = context
    }

    private fun handleEvent(params: WritableMap?) {
        reactContext?.let { ctx ->
            val serviceIntent = Intent(ctx, ScreenRecordingHeadlessTaskService::class.java);

            serviceIntent.putExtra("event", "SREvents")

            val bundle = params?.let { Arguments.toBundle(it) }

            if (bundle != null) {
                serviceIntent.putExtra("data", bundle)
            }


            HeadlessJsTaskService.acquireWakeLockNow(ctx)
            ctx.startService(serviceIntent)
        }
    }

    fun setupRecorder(context:Context, options: ReadableMap){
        hbRecorder = HBRecorder(context, this);

        hbRecorder?.let { recorder ->
          if (options.hasKey("mic")) recorder.isAudioEnabled(options.getBoolean("mic")) // audio
          if (options.hasKey("hdVideo")) recorder.recordHDVideo(options.getBoolean("hdVideo")) // quality
          if (options.hasKey("path")) recorder.setOutputPath(options.getString("path"))
          if (options.hasKey("fileName")) recorder.setFileName(options.getString("fileName"))


          if (options.hasKey("audioBitrate")) recorder.setAudioBitrate(options.getInt("audioBitrate"))
          if (options.hasKey("audioSampleRate")) recorder.setAudioSamplingRate(options.getInt("audioSampleRate"))
          if (options.hasKey("notificationTitle")) recorder.setNotificationTitle(options.getString("notificationTitle"))
          if (options.hasKey("notificationDescription")) recorder.setNotificationDescription(options.getString("notificationDescription"))

          if (options.hasKey("maxFileSize")) recorder.setMaxFileSize(options.getDouble("maxFileSize").toLong()) // in bytes
          if (options.hasKey("maxDuration")) recorder.setMaxDuration(options.getInt("maxDuration")) // in seconds



          if (options.hasKey("fps") || options.hasKey("bitrate") ||
              options.hasKey("videoEncoder") || options.hasKey("outputFormat") || options.hasKey("audioSource")) {
              recorder.enableCustomSettings()
              if (options.hasKey("audioSource")) recorder.setAudioSource(options.getString("audioSource"))
              if (options.hasKey("fps")) recorder.setVideoFrameRate(options.getInt("fps")) // videoFrameRate
              if (options.hasKey("bitrate")) recorder.setVideoBitrate(options.getInt("bitrate")) // videoBitrate
              if (options.hasKey("videoEncoder")) recorder.setVideoEncoder(options.getString("videoEncoder"))
              if (options.hasKey("outputFormat")) recorder.setOutputFormat(options.getString("outputFormat"))
          }
      }
    }


    fun setActivity(activity: Activity) {
        currentActivity = activity
    }

    fun setStartPromise(promise: Promise?) {
        startPromise = promise
    }

    fun getStartPromise(): Promise? {
        return startPromise
    }

    fun setStopPromise(promise: Promise?) {
        stopPromise = promise
    }

    fun startRecording(intent: Intent, resultCode: Int) {
        if (hbRecorder == null || currentActivity == null) {
            val errorMap = Arguments.createMap().apply {
                putString("status", "error")
                putString("reason", "HBRecorder or Activity is not set up")
            }
            handleEvent(errorMap)
            return
        }

        try {
          hbRecorder?.startScreenRecording(intent, resultCode)
        } catch(e: Exception) {
          val errorMap = Arguments.createMap().apply {
              putString("status", "error")
              putString("reason", e.message)
          }

          handleEvent(errorMap)
        }
    }

    fun stopRecording() {
        hbRecorder?.stopScreenRecording()
    }

    fun isRecording(): Boolean = hbRecorder?.isBusyRecording == true

    fun pause() {
        hbRecorder?.pauseScreenRecording()
    }

    fun resume() {
        hbRecorder?.resumeScreenRecording()
    }

    fun isBusyRecording(): Boolean = hbRecorder?.isBusyRecording ?: false

    fun getFilePath(): String? = hbRecorder?.filePath

    fun getFileName(): String? = hbRecorder?.fileName

    override fun HBRecorderOnStart() {
      val resultMap = Arguments.createMap().apply {
          putString("status", "started")
          putString("filePath", hbRecorder?.filePath)
          putString("fileName", hbRecorder?.fileName)
      }


      startPromise?.resolve(resultMap)
      startPromise = null

      val eventMap = Arguments.createMap().apply {
          putString("status", "started")
          putString("filePath", hbRecorder?.filePath)
          putString("fileName", hbRecorder?.fileName)
      }

      handleEvent(eventMap)
    }

    override fun HBRecorderOnComplete() {
      val resultMap = Arguments.createMap().apply {
          putString("status", "completed")
          putString("filePath", hbRecorder?.filePath)
          putString("fileName", hbRecorder?.fileName)
      }

      stopPromise?.resolve(resultMap)
      stopPromise = null

      val eventMap = Arguments.createMap().apply {
          putString("status", "completed")
          putString("filePath", hbRecorder?.filePath)
          putString("fileName", hbRecorder?.fileName)
      }

      handleEvent(eventMap)
    }

    override fun HBRecorderOnError(errorCode: Int, reason: String?) {
      val errorMap = Arguments.createMap().apply {
          putString("status", "error")
          putString("filePath", hbRecorder?.filePath)
          putString("fileName", hbRecorder?.fileName)
          putInt("errorCode", errorCode)
          putString("reason", reason)
      }

      stopPromise?.reject("RECORDING_ERROR", "Error $errorCode: $reason")
      stopPromise = null

      handleEvent(errorMap)
    }

    override fun HBRecorderOnPause() {
        val resultMap = Arguments.createMap().apply {
            putString("status", "paused")
            putString("filePath", hbRecorder?.filePath)
            putString("fileName", hbRecorder?.fileName)
        }

        handleEvent(resultMap)
    }

    override fun HBRecorderOnResume() {
      val resultMap = Arguments.createMap().apply {
          putString("status", "resumed")
          putString("filePath", hbRecorder?.filePath)
          putString("fileName", hbRecorder?.fileName)
      }

      handleEvent(resultMap)
    }
}
