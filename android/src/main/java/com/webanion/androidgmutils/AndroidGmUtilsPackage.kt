package com.webanion.androidgmutils

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.webanion.androidgmutils.notification.AGUNotificationListenerModule
import com.webanion.androidgmutils.fbnotification.FBNotificationListenerModule
import com.webanion.androidgmutils.usagestats.AGUUsageStatsModule
import com.webanion.androidgmutils.accessibility.AGUAccessibilityListenerModule
import com.webanion.androidgmutils.screenrecording.ScreenRecordingListenerModule
import com.webanion.androidgmutils.filemanager.FileManagerListenerModule


class AndroidGmUtilsPackage : ReactPackage {
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    return listOf(
            AndroidGmUtilsModule(reactContext),
            AGUNotificationListenerModule(reactContext),
            FBNotificationListenerModule(reactContext),
            AGUUsageStatsModule(reactContext),
            AGUAccessibilityListenerModule(reactContext),
            ScreenRecordingListenerModule(reactContext),
            FileManagerListenerModule(reactContext)
        )
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    return emptyList()
  }
}
