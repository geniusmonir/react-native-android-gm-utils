package com.webanion.androidgmutils.notification

import android.graphics.BitmapFactory
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.app.Notification
import android.util.Log
import java.util.ArrayList
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.content.Context
import java.lang.Exception
import android.graphics.drawable.Icon
import android.graphics.drawable.BitmapDrawable

class AGUNotification(
    context: Context,
    sbn: StatusBarNotification,
    listener: String
) {
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
    var groupedMessages: ArrayList<AGUGroupedNotification> = ArrayList()
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
            app = if (packageName.isNullOrEmpty()) "Unknown App" else packageName
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
            Log.d(TAG, "The notification received has no data")
        }
    }

    private fun getPropertySafely(notification: Notification, propKey: String): String {
        return try {
            val propCharSequence = notification.extras.getCharSequence(propKey)
            propCharSequence?.toString()?.trim() ?: ""
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Error")
            ""
        }
    }

    private fun getGroupedNotifications(notification: Notification): ArrayList<AGUGroupedNotification> {
        val result = ArrayList<AGUGroupedNotification>()
        try {
            val lines = notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)

            if (lines != null && lines.isNotEmpty()) {
                for (line in lines) {
                    if (!TextUtils.isEmpty(line)) {
                        val groupedNotification = AGUGroupedNotification(this, line)
                        result.add(groupedNotification)
                    }
                }
            }

            return result
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Error")
            return result
        }
    }

    private fun getNotificationIcon(context: Context, notification: Notification): String {
        return try {
            val iconBitmap = notification.largeIcon
            if (iconBitmap == null) {
                Log.d(TAG, "Large icon is null")
                return ""
            }

            // Convert Bitmap to Base64
            val outputStream = ByteArrayOutputStream()
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            val result = Base64.encodeToString(byteArray, Base64.DEFAULT)

            // Return the Base64 string with the data URI scheme
            if (result.isEmpty()) result else "data:image/png;base64,$result"
        } catch (e: Exception) {
            Log.d(TAG, "Error loading large icon: ${e.message}")
            ""
        }
    }

    private fun getNotificationLargeIcon(context: Context, notification: Notification): String {
        return try {
            val iconBitmap = notification.largeIcon
            if (iconBitmap == null) {
                Log.d(TAG, "Large icon is null")
                return ""
            }

            // Convert Bitmap to Base64
            val outputStream = ByteArrayOutputStream()
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            val result = Base64.encodeToString(byteArray, Base64.DEFAULT)

            // Return the Base64 string with the data URI scheme
            if (result.isEmpty()) result else "data:image/png;base64,$result"
        } catch (e: Exception) {
            Log.d(TAG, "Error loading large icon: ${e.message}")
            ""
        }
    }

    private fun getNotificationImage(notification: Notification): String {
        return try {
            if (!notification.extras.containsKey(Notification.EXTRA_PICTURE)) return ""

            val imageBitmap = notification.extras.get(Notification.EXTRA_PICTURE) as Bitmap

            val options = BitmapFactory.Options()
            options.inSampleSize = calculateInSampleSize(options, 100, 100)
            options.inJustDecodeBounds = false
            val outputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)

            val result = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

            if (result.isNotEmpty()) "data:image/png;base64,$result" else result
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Error")
            ""
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                && (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}
