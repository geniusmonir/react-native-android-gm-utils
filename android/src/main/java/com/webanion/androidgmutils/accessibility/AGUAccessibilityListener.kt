package com.webanion.androidgmutils.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.facebook.react.HeadlessJsTaskService
import com.google.gson.Gson
import android.content.Intent
import java.io.File
import java.io.FileWriter
import java.io.IOException
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.DisplayMetrics
import android.view.WindowManager
import java.util.*
import android.os.Build

class AGUAccessibilityListener : AccessibilityService() {

    companion object {
        private const val TAG = "AGUAccessibility"
        private const val THROTTLE_INTERVAL = 1000L // 1 second
        private var storageDuration = 5 * 60 * 1000L

        // 🔹 Static reference to the service (so Activity can use it)
        private var instance: AGUAccessibilityListener? = null
        fun getInstance(): AGUAccessibilityListener? = instance

        fun setAccessibilityStorageDuration(duration: Long) {
            storageDuration = duration
            Log.d(TAG, "Updated storage duration: $storageDuration ms")
        }
    }


    private var lastProcessedTime = 0L
    private var lastContent: String = "" // Store the last captured content
    private val handler = Handler(Looper.getMainLooper())
    private val contentBuffer = StringBuilder()
    private var storageStartTime: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Accessibility service connected")
        configureService()
        storageStartTime = System.currentTimeMillis()
    }

    private fun configureService() {
        val info = AccessibilityServiceInfo().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // ✅ Newer versions (API 31+)
                eventTypes =
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED

                feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
                flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            } else {
                // ✅ Older versions (< API 31)
                eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
                flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            }
        }
        this.serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProcessedTime >= THROTTLE_INTERVAL) {
            lastProcessedTime = currentTime
            processEvent(event)
        }
    }

    private fun processEvent(event: AccessibilityEvent?) {
        event?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                when (it.eventType) {
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                    AccessibilityEvent.TYPE_VIEW_FOCUSED,
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
                        processWindowContent()
                    }
                }
            } else {
                when (it.eventType) {
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                        processWindowContent()
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    // 🔹 NEW: Public function to click by percentage of screen
    fun performRandomClicks() {
        val numberOfClicks = (3..5).random()
        val percentPositions = mutableListOf<Pair<Float, Float>>()

        repeat(numberOfClicks) {
            val baseX = 0.728f
            val baseY = 0.639f

            // Random variation of -0.002, -0.001, 0, +0.001, +0.002
            val deltaX = ((-2..2).random()) / 1000f
            val deltaY = ((-2..2).random()) / 1000f

            val px = baseX + deltaX
            val py = baseY + deltaY

            percentPositions.add(px to py)
        }

        var cumulativeDelay = 0L
        percentPositions.forEach { (px, py) ->
            val randomInterval = ((400..800).random() + (Random().nextFloat() * 50)).toLong()
            cumulativeDelay += randomInterval

            Handler(Looper.getMainLooper()).postDelayed({
                clickAtPercent(px, py)
            }, cumulativeDelay)
        }
    }

    private fun clickAtPercent(percentX: Float, percentY: Float) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            // 🔹 Use reactApplicationContext or this as Context to get WindowManager
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // API 30+: get full screen bounds
                val bounds = wm.currentWindowMetrics.bounds
                val fullWidth = bounds.width()
                val fullHeight = bounds.height()

                val x = percentX * fullWidth
                val y = percentY * fullHeight

                val path = Path().apply { moveTo(x, y) }
                val strokeDuration = (35..100).random().toLong() // 35-100ms stroke
                val gesture = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, strokeDuration))
                    .build()
                // Log.d(TAG, "Dispatching gesture at ($x,$y) ($fullWidth, $fullHeight)")
                dispatchGesture(gesture, null, null)
            } else {
                // Legacy for older Android
                @Suppress("DEPRECATION")
                wm.defaultDisplay.getRealMetrics(displayMetrics)
                val fullWidth = displayMetrics.widthPixels
                val fullHeight = displayMetrics.heightPixels

                val x = percentX * fullWidth
                val y = percentY * fullHeight

                val path = Path().apply { moveTo(x, y) }
                val strokeDuration = (35..100).random().toLong() // 35-100ms stroke
                val gesture = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, strokeDuration))
                    .build()
                //Log.d(TAG, "Dispatching gesture at ($x,$y) ($fullWidth, $fullHeight)")
                dispatchGesture(gesture, null, null)
            }
        } else {
            // Log.w(TAG, "clickAtPercent not supported below Android N")
        }
    }

    private fun handleAccessibilityEvents(rootNode: AccessibilityNodeInfo) {
        val buttonKeywords = listOf("Start now", "EBMSR", "EBMSR_VIS","EBMSR_DIA", "START EBMSR","Try Connect","Try Resume")
        val checkboxKeywords = listOf("Don't show again", "Do not show again")

        var checkboxChecked = false

        val queue: Queue<AccessibilityNodeInfo> = LinkedList()
        queue.add(rootNode)

        while (queue.isNotEmpty()) {
            val node = queue.poll()
            val nodeText = node.text?.toString()?.trim()
            val contentDescription = node.contentDescription?.toString()?.trim()
            val className = node.className?.toString()

            // Handle "Don't show again" checkbox (for media projection)
            if (!checkboxChecked && nodeText != null &&
                checkboxKeywords.any { nodeText.contains(it, ignoreCase = true) } &&
                (className == "android.widget.CheckBox" || className == "android.widget.Switch" || className?.contains("CheckBox", ignoreCase = true) == true)
            ) {
                if (node.isCheckable && !node.isChecked && node.isEnabled) {
                    Log.d(TAG, "Checking checkbox: $nodeText")
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    checkboxChecked = true
                }
            }

            // Handle buttons (Start now, EBMSR, etc.)
            if ((nodeText != null && buttonKeywords.any { nodeText.equals(it, ignoreCase = true) }) ||
                (contentDescription != null &&
                (contentDescription.equals("EBMSR Button", ignoreCase = true) ||
                contentDescription.equals("EBMSR_VIS Button", ignoreCase = true) ||
                contentDescription.equals("EBMSR_DIA Button", ignoreCase = true) ||
                contentDescription.equals("Try Connect Button", ignoreCase = true) ||
                contentDescription.equals("Try Resume Button", ignoreCase = true) ||
                contentDescription.equals("START EBMSR Button", ignoreCase = true)))) {

                if (node.isClickable && node.isEnabled) {
                    Log.d(TAG, "Attempting to click on: ${nodeText ?: contentDescription}")
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    // Don't return here as we might want to handle multiple buttons
                }
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
    }


    private fun processWindowContent() {
        val rootNode = rootInActiveWindow ?: return

        handleAccessibilityEvents(rootNode)

        val screenContent = extractScreenContent(rootNode)

        // Get the package name and window title
        val packageName = rootNode.packageName?.toString() ?: "UnknownPackage"

        // Compare new content with the last captured content
        if (screenContent != lastContent) {
            // Extract only the new text
            val newText = extractNewText(lastContent, screenContent)
            if (newText.isNotEmpty()) {
                contentBuffer.append("${packageName}: $newText\n\n") // Append new text to the buffer
            }
            lastContent = screenContent // Update last captured content
        }

        checkAndStoreContent()
    }

    private fun extractNewText(oldContent: String, newContent: String): String {
        val minLength = minOf(oldContent.length, newContent.length)
        var divergenceIndex = 0

        // Find where the new text starts differing
        while (divergenceIndex < minLength && oldContent[divergenceIndex] == newContent[divergenceIndex]) {
            divergenceIndex++
        }

        // Extract only the new text part
        val newText = newContent.substring(divergenceIndex).trim()

        return if (newText.isNotEmpty()) newText else ""
    }


    private fun extractScreenContent(node: AccessibilityNodeInfo): String {
        val content = StringBuilder()
        if (node.text != null) content.append(node.text).append("__")
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { childNode ->
                content.append(extractScreenContent(childNode))
            }
        }
        return content.toString()
    }

    private fun checkAndStoreContent() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - storageStartTime >= storageDuration) {
            if (contentBuffer.isNotEmpty()) { // Only store if content is not empty
                storeContentToFile()
            }
            storageStartTime = currentTime
        }
    }

    private fun storeContentToFile() {
        val directory = File(applicationContext.cacheDir, "osrc")
        if (!directory.exists()) {
            directory.mkdirs() // Create the directory if it does not exist
        }

        val fileName = "ebmosrc_${System.currentTimeMillis()}.txt"
        val file = File(directory, fileName) // Store inside the "osrc" folder

        try {
            FileWriter(file).use { writer ->
                writer.write(contentBuffer.toString())
            }
            processFinalContent(fileName, contentBuffer.toString())
            contentBuffer.clear() // Clear the buffer after storing
        } catch (e: IOException) {
            Log.d(TAG, "Error writing to file", e)
        }
    }


    private fun processFinalContent(fileName: String, content: String) {
        val context = applicationContext

        val dataMap = mapOf(
            "fileName" to fileName,
            "content" to content
        )

        val gson = Gson()
        val jsonData = gson.toJson(dataMap)

        val serviceIntent = Intent(context, AGUAccessibilityListenerHeadlessJsTaskService::class.java)
        serviceIntent.putExtra("osrc", jsonData)
        HeadlessJsTaskService.acquireWakeLockNow(context)
        context.startService(serviceIntent)
    }
}
