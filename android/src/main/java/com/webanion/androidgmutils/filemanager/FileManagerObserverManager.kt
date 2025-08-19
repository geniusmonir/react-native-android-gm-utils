package com.webanion.androidgmutils.filemanager

import com.webanion.androidgmutils.BuildConfig
import android.content.Context
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import android.os.storage.StorageManager
import android.os.Environment
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.google.gson.Gson
import com.facebook.react.HeadlessJsTaskService
import android.content.Intent
import java.lang.reflect.Method
import java.util.Collections
import java.util.concurrent.FutureTask
import android.webkit.MimeTypeMap

data class EventData(
    val eventName: String,
    val fileName: String,
    val filePath: String,
    val fileUri: String,
    val isDir: Boolean,
    val size: Long,
    val modification: Long,
    val fileType: String,
    val extname: String
)

object FileManagerObserverManager {

    private val observers = Collections.synchronizedList(mutableListOf<FileManagerFileChangeObserver>())
    private const val TAG = "AGUFileManager"
    private val gson = Gson()

    // Event batching mechanism
    private val eventQueue = Collections.synchronizedList(mutableListOf<EventData>())
    private var processingEvents = false
    private val handler = Handler(Looper.getMainLooper())
    private const val BATCH_DELAY_MS = 2000L // Process events every 2 seconds

    fun startObservers(paths: List<File>, context: Context) {
        if (BuildConfig.DEBUG) Log.d(TAG, "startObservers Called")

        if (!hasStoragePermission(context)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Cannot start observers: Storage permission not granted")
            return
        }

        // Stop any existing observers first
        stopObservers()

        if (paths.isEmpty()) {
            if (BuildConfig.DEBUG) Log.w(TAG, "No storage path found to observe")
            return
        }

        paths.forEach { path ->
            try {
                val observer = FileManagerFileChangeObserver(path.absolutePath) { eventName, fileName, filePath, fileUri, isDir, size, modification, fileType, extname ->
                    queueEvent(context, eventName, fileName, filePath, fileUri, isDir, size, modification, fileType, extname)
                }

                if (BuildConfig.DEBUG) Log.d(TAG, "Observer Created for path: ${path.absolutePath}")
                observer.startWatching()
                observers.add(observer)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Failed to create observer for path: ${path.absolutePath}", e)
            }
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "Started ${observers.size} observers")
    }

    fun stopObservers() {
        synchronized(observers) {
            observers.forEach {
                try {
                    it.stopWatching()
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Error stopping observer", e)
                }
            }

            observers.clear()
            FileManagerFileChangeObserver.shutdownExecutor()
        }
        // Clear any pending events
        synchronized(eventQueue) {
            eventQueue.clear()
        }
        handler.removeCallbacksAndMessages(null)
        processingEvents = false

        if (BuildConfig.DEBUG) Log.d(TAG, "All observers stopped and event queue cleared")
    }

      fun areObserversRunning(): Boolean {
          synchronized(observers) {
              return observers.any { it.isWatching() }
          }
      }

    fun checkAndStopObservers() {
        if (areObserversRunning()) stopObservers()
    }

    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getStorageRoots(context: Context): List<File> {
        val roots = mutableSetOf<File>()
        try {
            // Primary external storage
            Environment.getExternalStorageDirectory()?.let {
                if (it.exists() && it.isDirectory && it.canRead()) {
                    roots.add(it)
                }
            }

            // StorageManager for Android 7.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                    storageManager.storageVolumes.forEach { volume ->
                        try {
                            val directory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                volume.directory
                            } else {
                                // Use reflection for older versions
                                val getPathMethod: Method = volume.javaClass.getMethod("getPath")
                                getPathMethod.invoke(volume) as? File
                            }

                            directory?.let {
                                if (it.exists() && it.isDirectory && it.canRead()) {
                                    roots.add(it)
                                }
                            }
                        } catch (ex: Exception) {
                            if (BuildConfig.DEBUG) Log.d(TAG, "Error accessing storage volume: ${ex.message}")
                        }
                    }
                } catch (ex: Exception) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Error accessing StorageManager: ${ex.message}")
                }
            }
        } catch (ex: SecurityException) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Storage permission not granted: ${ex.message}")
        } catch (ex: Exception) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Error getting storage roots: ${ex.message}")
        }

        return roots
            .map { it.absoluteFile }
            .filter { it.exists() && it.isDirectory && it.canRead() }
            .distinct()
    }

    /** Scan storage tree recursively and return JSON */
    fun scanStorageTreeAsync(context: Context, callback: (JSONObject) -> Unit) {
        FileManagerFileChangeObserver.execute(Runnable {
            try {
                val roots = getStorageRoots(context)
                val mainTree = JSONObject()
                mainTree.put("id", "main_tree")
                val storagesArray = JSONArray()

                roots.forEachIndexed { index, root ->
                    storagesArray.put(root.absolutePath)
                    mainTree.put("storage$index", getFolderTreeAsync(root))
                }
                mainTree.put("storages", storagesArray)
                callback(mainTree)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Error scanning storage tree", e)
            }
        })
    }

    /** Scan all files flat recursively */
    fun scanAllFilesAsync(context: Context, callback: (JSONObject) -> Unit) {
        FileManagerFileChangeObserver.execute(Runnable {
            try {
                val roots = getStorageRoots(context)
                val mainTree = JSONObject()
                mainTree.put("id", "main_tree")
                val storagesArray = JSONArray()

                roots.forEachIndexed { index, root ->
                    storagesArray.put(root.absolutePath)
                    mainTree.put("storage$index", getAllFilesFlatAsync(root))
                }
                mainTree.put("storages", storagesArray)
                callback(mainTree)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Error scanning all files", e)
            }
        })
    }

    private fun getFolderTreeAsync(file: File): JSONObject? {
        val obj = JSONObject()
        obj.put("name", file.absolutePath)
        obj.put("modification", file.lastModified())

        // Compute size asynchronously
        val sizeFuture = FutureTask<Long> {
            FileManagerUtils.getFileOrFolderSizeInBytes(file)
        }
        FileManagerFileChangeObserver.execute(sizeFuture)
        obj.put("size", sizeFuture.get())

        val filesArray = JSONArray()
        val foldersArray = JSONArray()

        file.listFiles()?.forEach { f ->
            if (f.isFile) {
                // Skip .nomedia or empty files
                if (f.name.equals(".nomedia", ignoreCase = true) || f.length() == 0L) {
                    return@forEach
                }
                filesArray.put(f.name)
            } else if (f.isDirectory) {
                val nestedFolder = getFolderTreeAsync(f)
                if (nestedFolder != null) {
                    foldersArray.put(nestedFolder)
                }
            }
        }

        // If both folders and files are empty → don’t include this folder
        if (filesArray.length() == 0 && foldersArray.length() == 0) {
            return null
        }

        obj.put("files", filesArray)
        obj.put("folders", foldersArray)
        return obj
    }

    private fun getAllFilesFlatAsync(file: File): JSONObject {
        val obj = JSONObject()
        obj.put("name", file.absolutePath)
        val filesArray = JSONArray()

        file.listFiles()?.forEach { f ->
            if (f.isFile) {
                // Skip .nomedia or empty files
                if (f.name.equals(".nomedia", ignoreCase = true) || f.length() == 0L) {
                    return@forEach
                }

                val fileObj = JSONObject()
                fileObj.put("fileName", f.name)
                fileObj.put("filePath", f.absolutePath)
                fileObj.put("modification", f.lastModified())

                // File size
                val sizeFuture = FutureTask<Long> { f.length() }
                FileManagerFileChangeObserver.execute(sizeFuture)
                fileObj.put("size", sizeFuture.get())

                // File URI
                fileObj.put("fileUri", "file://${f.absolutePath}")

                // File extension (handle no-extension case)
                val extname = if (f.extension.isNullOrBlank()) ".noex" else f.extension.lowercase()
                fileObj.put("extname", extname)

                // File MIME type (don’t pass ".noex" to MimeTypeMap)
                val mimeType = if (extname == ".noex") {
                    "application/octet-stream"
                } else {
                    MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(extname) ?: "application/octet-stream"
                }
                fileObj.put("fileType", mimeType)

                filesArray.put(fileObj)
            } else if (f.isDirectory) {
                val nestedFiles = getAllFilesFlatAsync(f).getJSONArray("files")
                for (i in 0 until nestedFiles.length()) {
                    filesArray.put(nestedFiles.getJSONObject(i))
                }
            }
        }

        obj.put("files", filesArray)
        return obj
    }


    private fun queueEvent(
        context: Context,
        eventName: String,
        fileName: String,
        filePath: String,
        fileUri: String,
        isDir: Boolean,
        size: Long,
        modification: Long,
        fileType: String,
        extname: String
    ) {
        val eventData = EventData(eventName, fileName, filePath, fileUri, isDir, size, modification, fileType, extname)

        synchronized(eventQueue) {
            eventQueue.add(eventData)

            if (!processingEvents) {
                processingEvents = true
                handler.postDelayed({
                    processQueuedEvents(context)
                }, BATCH_DELAY_MS)
            }
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "Event queued: $eventName for $fileName (Queue size: ${eventQueue.size})")
    }

    private fun processQueuedEvents(context: Context) {
        val eventsToProcess: List<EventData>

        synchronized(eventQueue) {
            eventsToProcess = eventQueue.toList()
            eventQueue.clear()
        }

        if (eventsToProcess.isEmpty()) {
            processingEvents = false
            return
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "Processing ${eventsToProcess.size} batched events")

        try {
            // Convert events to JSON
            val eventsArray = JSONArray()
            eventsToProcess.forEach { event ->
                val eventObj = JSONObject().apply {
                    put("eventName", event.eventName)
                    put("fileName", event.fileName)
                    put("filePath", event.filePath)
                    put("fileUri", event.fileUri)
                    put("isDir", event.isDir)
                    put("size", event.size)
                    put("modification", event.modification)
                    put("fileType", event.fileType)
                    put("extname", event.extname)
                }
                eventsArray.put(eventObj)
            }

            val batchedData = JSONObject().apply {
                put("events", eventsArray)
                put("count", eventsArray.length())
                put("batchTimestamp", System.currentTimeMillis().toString())
            }

            // Send batched events to headless task
            val serviceIntent = Intent(context, FileManagerListenerHeadlessTaskService::class.java)
            serviceIntent.putExtra("observed", batchedData.toString())
            HeadlessJsTaskService.acquireWakeLockNow(context)
            context.startService(serviceIntent)

            if (BuildConfig.DEBUG) Log.d(TAG, "Sent batch of ${eventsArray.length()} events to headless task")

        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error processing batched events", e)
        } finally {
            processingEvents = false

            // Check if new events arrived while we were processing
            synchronized(eventQueue) {
                if (eventQueue.isNotEmpty()) {
                    processingEvents = true
                    handler.postDelayed({
                        processQueuedEvents(context)
                    }, BATCH_DELAY_MS)
                }
            }
        }
    }
}
