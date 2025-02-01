package com.webanion.androidgmutils.notification

import android.text.TextUtils
import android.util.Log

class AGUGroupedNotification(mainNotification: AGUNotification, message: CharSequence) {

    var title: String = ""
    var text: String = ""

    init {
        val formattedMessage = message.toString().trim()

        title = if (!TextUtils.isEmpty(mainNotification.title)) mainNotification.title else ""
        text = if (!TextUtils.isEmpty(mainNotification.text)) mainNotification.text else ""

        val endIndex = formattedMessage.indexOf(":")

        if (endIndex != -1) {
            title = formattedMessage.substring(0, endIndex).trim()
            text = formattedMessage.substring(endIndex + 1).trim()
        } else {
            text = formattedMessage
        }

        // Debug logging using Log.d
        Log.d("AGUNotificationListener", "Title: $title, Text: $text")
    }
}
