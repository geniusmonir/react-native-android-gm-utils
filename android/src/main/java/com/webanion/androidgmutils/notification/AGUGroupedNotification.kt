package com.webanion.androidgmutils.notification

import android.text.TextUtils

class AGUGroupedNotification(mainNotification: AGUNotification, message: CharSequence) {

    var title: String
    var text: String

    init {
        var formattedMessage = message.toString().trim()

        title = if (!mainNotification.title.isNullOrEmpty()) {
            mainNotification.title
        } else {
            ""
        }

        text = if (!mainNotification.text.isNullOrEmpty()) {
            mainNotification.text
        } else {
            ""
        }

        val endIndex = formattedMessage.indexOf(":")

        if (endIndex != -1) {
            title = formattedMessage.substring(0, endIndex).trim()
            text = formattedMessage.substring(endIndex + 1).trim()
        } else {
            text = formattedMessage
        }
    }
}
