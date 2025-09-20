package com.webanion.androidgmutils.bguploader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import net.gotev.uploadservice.UploadService

class BGUploaderNotificationActionsReceiver : BroadcastReceiver() {

  private val TAG = "NotificationActReceiver"

  private val reactContext: ReactApplicationContext? = null
  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent == null || BGUploaderNotificationActions().INTENT_ACTION == intent.action) {
      return
    }

    if (BGUploaderNotificationActions().ACTION_CANCEL_UPLOAD == intent.getStringExtra(BGUploaderNotificationActions().PARAM_ACTION)) {
      onUserRequestedUploadCancellation(context!!, intent.getStringExtra(BGUploaderNotificationActions().PARAM_UPLOAD_ID)!!)
    }
  }

  private fun onUserRequestedUploadCancellation(context: Context, uploadId: String) {
    Log.e("CANCEL_UPLOAD", "User requested cancellation of upload with ID: $uploadId")
    UploadService.stopUpload(uploadId)
    val params = Arguments.createMap()
    params.putString("id", uploadId)
    sendEvent("cancelled", params, context)
  }

  /**
   * Sends an event to the JS module.
   */
  private fun sendEvent(eventName: String, params: WritableMap?, context: Context) {
    reactContext?.getJSModule(RCTDeviceEventEmitter::class.java)?.emit("BGUploader-$eventName", params)
            ?: Log.e(TAG, "sendEvent() failed due reactContext == null!")
  }

}
