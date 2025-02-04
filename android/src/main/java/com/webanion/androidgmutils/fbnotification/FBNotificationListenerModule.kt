package com.webanion.androidgmutils.fbnotification

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class FBNotificationListenerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return NAME
    }

    // @ReactMethod


    companion object {
      const val NAME = "FBNotificationListenerModule"
    }
}
