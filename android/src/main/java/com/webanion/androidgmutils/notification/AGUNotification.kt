package com.webanion.androidgmutils.notification

import android.app.Notification
import androidx.core.app.NotificationCompat
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.lang.Exception

class AGUNotification(context: Context, sbn: StatusBarNotification, listener: String) {
    companion object {
        private const val TAG = "AGUNotificationListener"
    }

    var listener: String = listener
    var app: String = ""
    var title: String = ""
    var titleBig: String = ""
    var text: String = ""
    var subText: String = ""
    var summaryText: String = ""
    var bigText: String = ""
    var groupedMessages: ArrayList<AGUGroupedNotification> = arrayListOf()
    var audioContentsURI: String = ""
    var imageBackgroundURI: String = ""
    var extraInfoText: String = ""
    var icon: String = ""
    var image: String = ""
    var time: String = ""
    var iconLarge: String = ""

    init {
        val notification = sbn.notification

        if (notification != null && notification.extras != null) {
            val packageName = sbn.packageName

            time = sbn.postTime.toString()
            app = if (TextUtils.isEmpty(packageName)) "Unknown App" else packageName
            title = getPropertySafely(notification, Notification.EXTRA_TITLE)
            titleBig = getPropertySafely(notification, Notification.EXTRA_TITLE_BIG)
            text = getPropertySafely(notification, Notification.EXTRA_TEXT)
            subText = getPropertySafely(notification, Notification.EXTRA_SUB_TEXT)
            summaryText = getPropertySafely(notification, Notification.EXTRA_SUMMARY_TEXT)
            bigText = getPropertySafely(notification, Notification.EXTRA_BIG_TEXT)
            audioContentsURI = getPropertySafely(notification, Notification.EXTRA_AUDIO_CONTENTS_URI)
            imageBackgroundURI = getPropertySafely(notification, Notification.EXTRA_BACKGROUND_IMAGE_URI)
            extraInfoText = getPropertySafely(notification, Notification.EXTRA_INFO_TEXT)
            iconLarge = getNotificationLargeIcon(context, notification)
            icon = getNotificationIcon(context, notification)
            image = getNotificationImage(notification)
            groupedMessages = getGroupedNotifications(notification)
        } else {
            // Log.d(TAG, "The notification received has no data")
        }
    }

    private fun getPropertySafely(notification: Notification, propKey: String): String {
        return try {
            val propCharSequence = notification.extras.getCharSequence(propKey)
            propCharSequence?.toString()?.trim() ?: ""
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Error in getPropertySafely")
            ""
        }
    }

    private fun getGroupedNotifications(notification: Notification): ArrayList<AGUGroupedNotification> {
        val result = arrayListOf<AGUGroupedNotification>()

        try {
            // Check if the notification uses the MessagingStyle template
            val template = notification.extras.getString("android.template")
            if (template == "android.app.Notification\$MessagingStyle") {
                // Extract messages using MessagingStyle
                val messagingStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
                if (messagingStyle != null) {
                    for (message in messagingStyle.messages) {
                        result.add(AGUGroupedNotification(this, message.text.toString()))
                    }
                    // Log.d(TAG, "Extracted ${result.size} grouped messages using MessagingStyle")
                } else {
                    // Log.d(TAG, "No grouped messages found in MessagingStyle")
                }
            } else {
                // Fallback to EXTRA_TEXT_LINES and EXTRA_BIG_TEXT
                val lines = notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                if (lines != null) {
                    for (line in lines) {
                        if (!TextUtils.isEmpty(line)) {
                            result.add(AGUGroupedNotification(this, line))
                        }
                    }
                    // Log.d(TAG, "Extracted ${result.size} grouped messages using EXTRA_TEXT_LINES")
                } else {
                    // Log.d(TAG, "No grouped messages found in EXTRA_TEXT_LINES")
                }

                if (result.isEmpty()) {
                    val bigText = notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                    if (!TextUtils.isEmpty(bigText)) {
                        result.add(AGUGroupedNotification(this, bigText!!))
                        // Log.d(TAG, "Extracted 1 grouped message using EXTRA_BIG_TEXT")
                    } else {
                        // Log.d(TAG, "No grouped messages found in EXTRA_BIG_TEXT")
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error in getGroupedNotifications: ${e.message}")
        }

        return result
    }
    private fun getNotificationIcon(context: Context, notification: Notification): String {
        return try {
            val iconInstance = notification.getSmallIcon()
            val iconDrawable: Drawable? = iconInstance.loadDrawable(context)
            if (iconDrawable == null) {
                // Log.d(TAG, "Small icon drawable is null")
                return ""
            }

            val iconBitmap = (iconDrawable as? BitmapDrawable)?.bitmap
            if (iconBitmap == null) {
                // Log.d(TAG, "Small icon bitmap is null")
                return ""
            }

            val outputStream = ByteArrayOutputStream()
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            val result = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            if (TextUtils.isEmpty(result)) result else "data:image/png;base64,$result"
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Error in getNotificationIcon")
            ""
        }
    }

    private fun getNotificationLargeIcon(context: Context, notification: Notification): String {
        return try {
            val iconInstance = notification.getLargeIcon()
            val iconDrawable: Drawable? = iconInstance?.loadDrawable(context)
            if (iconDrawable == null) {
                // Log.d(TAG, "Large icon drawable is null")
                return ""
            }

            val iconBitmap = (iconDrawable as? BitmapDrawable)?.bitmap
            if (iconBitmap == null) {
                // Log.d(TAG, "Large icon bitmap is null")
                return ""
            }

            val outputStream = ByteArrayOutputStream()
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            val result = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            if (TextUtils.isEmpty(result)) result else "data:image/png;base64,$result"
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Error in getNotificationLargeIcon")
            ""
        }
    }

    private fun getNotificationImage(notification: Notification): String {
        return try {
            if (!notification.extras.containsKey(Notification.EXTRA_PICTURE)) return ""

            val imageBitmap = notification.extras.get(Notification.EXTRA_PICTURE) as? Bitmap ?: return ""

            val options = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(this, 100, 100)
                inJustDecodeBounds = false
            }

            val outputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)

            val result = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            if (TextUtils.isEmpty(result)) result else "data:image/png;base64,$result"
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Error in getNotificationImage")
            ""
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}
