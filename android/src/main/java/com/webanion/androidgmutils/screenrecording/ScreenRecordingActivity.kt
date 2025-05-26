package com.webanion.androidgmutils.screenrecording

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.content.Intent
import android.widget.LinearLayout
import android.view.*
import android.graphics.Color

class ScreenRecordingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            (resources.displayMetrics.heightPixels * 0.4).toInt()
        )

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 100, 50, 100)
            gravity = Gravity.CENTER
        }

        val btn = Button(this).apply {
            text = "START EBMSR"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.TRANSPARENT)
            contentDescription = "START EBMSR Button"
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            setOnClickListener {
                startMediaProjection()
            }
        }

        layout.addView(btn)

        setContentView(layout)
    }

    private fun startMediaProjection() {
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as android.media.projection.MediaProjectionManager
        val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(permissionIntent, ScreenRecordingListenerModule.SCREEN_RECORD_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ScreenRecordingListenerModule.SCREEN_RECORD_REQUEST_CODE && data != null) {
            ScreenRecorderSingleton.setActivity(this)
            try {
                ScreenRecorderSingleton.startRecording(data, resultCode)
            } catch (e: Exception) {
              // Log Error
            }
            finish()
        }
    }
}
