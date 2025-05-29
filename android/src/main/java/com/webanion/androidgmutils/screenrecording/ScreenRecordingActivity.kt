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

        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)

        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.clearFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.TRANSPARENT)
            setOnTouchListener { _, _ -> true }
        }

        val btn = Button(this).apply {
            text = "START EBMSR"
            contentDescription = "START EBMSR Button"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.TRANSPARENT)
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

    override fun onBackPressed() {
        // Consume back button press
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Consume all touches
        return true
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
