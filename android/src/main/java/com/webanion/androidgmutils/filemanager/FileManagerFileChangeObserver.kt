package com.webanion.androidgmutils.filemanager

import com.webanion.androidgmutils.BuildConfig
import android.net.Uri
import android.os.FileObserver
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.util.Log
import android.webkit.MimeTypeMap
import android.os.FileObserver.*

class FileManagerFileChangeObserver(
    private val watchPath: String,
    private val onEventCallback: (
        eventName: String,
        fileName: String,
        filePath: String,
        fileUri: String,
        isDir: Boolean,
        size: Long,
        modification: Long,
        fileType: String,
        extname: String
    ) -> Unit
) : FileObserver(
    watchPath,
    MOVED_TO
) {
    private val TAG = "AGUFileManager"
    private var watching = false

    companion object {
        private val executorLock = Any()
        private var _executor: ExecutorService = Executors.newFixedThreadPool(4)

        fun getExecutor(): ExecutorService {
            synchronized(executorLock) {
                if (_executor.isShutdown) {
                    _executor = Executors.newFixedThreadPool(4)
                }
                return _executor
            }
        }

        fun execute(task: Runnable) {
            getExecutor().execute(task)
        }

        fun shutdownExecutor() {
            synchronized(executorLock) {
                if (!_executor.isShutdown) {
                    _executor.shutdownNow()
                }
            }
        }
    }

    fun isWatching(): Boolean = watching

    // Convert event int to human-readable string
    private fun eventToString(event: Int): String {
        val events = mutableListOf<String>()
        if (event and ACCESS != 0) events.add("ACCESS")
        if (event and MODIFY != 0) events.add("MODIFY")
        if (event and ATTRIB != 0) events.add("ATTRIB")
        if (event and CLOSE_WRITE != 0) events.add("CLOSE_WRITE")
        if (event and CLOSE_NOWRITE != 0) events.add("CLOSE_NOWRITE")
        if (event and OPEN != 0) events.add("OPEN")
        if (event and MOVED_FROM != 0) events.add("MOVED_FROM")
        if (event and MOVED_TO != 0) events.add("MOVED_TO")
        if (event and CREATE != 0) events.add("CREATE")
        if (event and DELETE != 0) events.add("DELETE")
        if (event and DELETE_SELF != 0) events.add("DELETE_SELF")
        if (event and MOVE_SELF != 0) events.add("MOVE_SELF")
        return if (events.isEmpty()) "UNKNOWN($event)" else events.joinToString(" | ")
    }

    override fun startWatching() {
        if (BuildConfig.DEBUG) Log.d(TAG, "Observer started for path: $watchPath")
        super.startWatching()
        watching = true
    }

    override fun stopWatching() {
        if (BuildConfig.DEBUG) Log.d(TAG, "Observer Stopped for path: $watchPath")
        super.stopWatching()
        watching = false
    }

    override fun onEvent(event: Int, path: String?) {
        if (path == null) return

        if (BuildConfig.DEBUG) Log.d(TAG, "Event $event (${eventToString(event)}) triggered for path: $path")

        try {
            val fullFile = File(watchPath, path)
            if (!fullFile.exists()) {
                if (BuildConfig.DEBUG) Log.d(TAG, "File no longer exists: ${fullFile.absolutePath}")
                return
            }

            val fileName = fullFile.name
            val filePath = fullFile.absolutePath
            val fileUri = Uri.fromFile(fullFile).toString()
            val isDir = fullFile.isDirectory
            val modification = fullFile.lastModified()
            val eventName = eventToString(event)
            val extname = if (fullFile.extension.isNullOrBlank()) "noex" else fullFile.extension.lowercase()
            val fileType = if (extname == "noex") {
                "application/octet-stream"
            } else {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extname) ?: "application/octet-stream"
            }

            // Compute size asynchronously using shared executor
            execute(Runnable {
                try {
                    val size = FileManagerUtils.getFileOrFolderSizeInBytes(fullFile)
                    onEventCallback(eventName, fileName, filePath, fileUri, isDir, size, modification, fileType, extname)
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Error processing file event for $filePath", e)
                }
            })
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error handling file event for path: $path", e)
        }
    }
}
