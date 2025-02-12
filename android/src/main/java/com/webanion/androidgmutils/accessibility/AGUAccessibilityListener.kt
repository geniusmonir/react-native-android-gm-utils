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
import java.util.*

class AGUAccessibilityListener : AccessibilityService() {

    companion object {
        private const val TAG = "AGUAccessibilityListener"
        private const val THROTTLE_INTERVAL = 1000L // 1 second
        private var storageDuration = 5 * 60 * 1000L

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
        Log.d(TAG, "Accessibility service connected")
        configureService()
        storageStartTime = System.currentTimeMillis()
    }

    private fun configureService() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
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
            when (it.eventType) {
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    processWindowContent()
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    private fun processWindowContent() {
        val rootNode = rootInActiveWindow ?: return
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
        val directory = File(applicationContext.getExternalFilesDir(null), "osrc")
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
