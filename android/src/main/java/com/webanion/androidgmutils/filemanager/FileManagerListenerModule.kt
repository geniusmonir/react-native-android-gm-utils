package com.webanion.androidgmutils.filemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.Arguments
import android.net.Uri
import android.util.Log
import java.io.File

class FileManagerListenerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = NAME

    companion object {
        const val TAG = "AGUFileManager"
        const val NAME = "FileManagerListenerModule"
        const val PERMISSION_REQUEST_CODE = 1001
    }

    @ReactMethod
    fun hasPermission(promise: Promise) {
        val context = reactApplicationContext
        val isGranted = FileManagerObserverManager.hasStoragePermission(context)
        promise.resolve(isGranted)
    }

    @ReactMethod
    fun requestPermission(promise: Promise) {
        val activity = currentActivity
        if (activity == null) {
            promise.reject("NO_ACTIVITY", "Current activity is null")
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val context = reactApplicationContext
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                  context.startActivity(intent)
                  promise.resolve(true)
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_REQUEST_CODE
                )
                promise.resolve(true)
            }
        } catch (e: Exception) {
            promise.reject("REQUEST_PERMISSION_ERROR", "Failed to request permission: ${e.message}", e)
        }
    }

    @ReactMethod
    fun areObserversRunning(promise: Promise) {
        try {
            val isRunning = FileManagerObserverManager.areObserversRunning()
            promise.resolve(isRunning)
        } catch (e: Exception) {
            promise.reject("OBSERVER_CHECK_ERROR", "Failed to check observer status", e)
        }
    }

    @ReactMethod
    fun startObserversByPaths(pathsArray: ReadableArray, promise: Promise) {
        val context = reactApplicationContext
        try {
            val filePaths = mutableListOf<File>()
            val invalidPaths = mutableListOf<String>()

            for (i in 0 until pathsArray.size()) {
                val pathString = pathsArray.getString(i)
                if (pathString != null) {
                    val file = File(pathString)
                    if (file.exists() && file.isDirectory && file.canRead()) {
                        filePaths.add(file)
                    } else {
                        invalidPaths.add(pathString)
                    }
                }
            }

            if (filePaths.isEmpty()) {
                if (invalidPaths.isNotEmpty()) {
                    promise.reject("INVALID_PATHS", "All provided paths are invalid or inaccessible: ${invalidPaths.joinToString()}")
                } else {
                    promise.reject("NO_PATHS", "No valid paths provided to observe")
                }
                return
            }

            FileManagerObserverManager.startObservers(filePaths, context)

            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("START_OBSERVERS_ERROR", "Failed to start observers", e)
        }
    }

    @ReactMethod
    fun checkAndStopObservers(promise: Promise) {
        try {
            FileManagerObserverManager.checkAndStopObservers()
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("OBSERVER_STOP_FAILED", e)
        }
    }

    @ReactMethod
    fun getStorageRoots(promise: Promise) {
        val context = reactApplicationContext
        try {
            if (!FileManagerObserverManager.hasStoragePermission(context)) {
                promise.reject("PERMISSION_DENIED", "Storage permission not granted")
                return
            }
            val sroots = FileManagerObserverManager.getStorageRoots(context)

            val srootsArr = Arguments.createArray()

            sroots.forEach { root ->
                srootsArr.pushString(root.absolutePath)
            }

            promise.resolve(srootsArr)
        } catch (e: Exception) {
            promise.reject("STORAGE_ROOT_EXTRACTION_ERROR", "Failed to get storage roots", e)
        }
    }

    @ReactMethod
    fun scanStorageTree(promise: Promise) {
        val context = reactApplicationContext
        if (!FileManagerObserverManager.hasStoragePermission(context)) {
            promise.reject("PERMISSION_DENIED", "Storage permission not granted")
            return
        }

        FileManagerObserverManager.scanStorageTreeAsync(context) { json ->
            promise.resolve(FileManagerUtils.jsonToWritableMap(json))
        }
    }

    @ReactMethod
    fun scanAllFiles(promise: Promise) {
        val context = reactApplicationContext
        if (!FileManagerObserverManager.hasStoragePermission(context)) {
            promise.reject("PERMISSION_DENIED", "Storage permission not granted")
            return
        }

        FileManagerObserverManager.scanAllFilesAsync(context) { json ->
            promise.resolve(FileManagerUtils.jsonToWritableMap(json))
        }
    }

    @ReactMethod
    fun setAGUSPValue(key: String, value: String, promise: Promise) {
        try {
            FileManagerUtils.setAGUSPValue(reactApplicationContext, key, value)
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("SET_AGUSP_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getAGUSPValue(key: String, promise: Promise) {
        try {
            val value = FileManagerUtils.getAGUSPValue(reactApplicationContext, key)
            promise.resolve(value)
        } catch (e: Exception) {
            promise.reject("GET_AGUSP_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getDefaultObserverPaths(promise: Promise) {
        try {
            val files = FileManagerUtils.getDefaultObserverPath(reactApplicationContext)
            val filesArr = Arguments.createArray()

            files.forEach { file ->
                filesArr.pushString(file.absolutePath)
            }

            promise.resolve(filesArr)
        } catch (e: Exception) {
            promise.reject("DEFAULT_PATHS_ERROR", e.message, e)
        }
    }
}
