package com.webanion.androidgmutils.filemanager

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import org.json.JSONArray
import org.json.JSONObject
import android.content.Context
import java.io.File
import android.util.Log

object FileManagerUtils {
    const val DEFAULT_OBSERVER_PATHS_KEY = "defaultPaths"
    private const val PREFS_NAME = "AGUSharedPrefs"

    // Convert JSONObject → WritableMap
    @JvmStatic
    fun jsonToWritableMap(json: JSONObject): WritableMap {
        val map = Arguments.createMap()
        json.keys().forEach { key ->
            try {
                val value = json.get(key)
                when (value) {
                    is JSONObject -> map.putMap(key, jsonToWritableMap(value))
                    is JSONArray -> map.putArray(key, jsonToWritableArray(value))
                    is Boolean -> map.putBoolean(key, value)
                    is Int -> map.putInt(key, value)
                    is Long -> map.putDouble(key, value.toDouble())
                    is Double -> map.putDouble(key, value)
                    is String -> map.putString(key, value)
                    JSONObject.NULL -> map.putNull(key)
                    else -> map.putString(key, value.toString())
                }
            } catch (e: Exception) {
                // Skip invalid keys
            }
        }
        return map
    }

    // Convert JSONArray → WritableArray
    @JvmStatic
    fun jsonToWritableArray(jsonArray: JSONArray): WritableArray {
        val array = Arguments.createArray()
        for (i in 0 until jsonArray.length()) {
            try {
                val value = jsonArray.get(i)
                when (value) {
                    is JSONObject -> array.pushMap(jsonToWritableMap(value))
                    is JSONArray -> array.pushArray(jsonToWritableArray(value))
                    is Boolean -> array.pushBoolean(value)
                    is Int -> array.pushInt(value)
                    is Long -> array.pushDouble(value.toDouble())
                    is Double -> array.pushDouble(value)
                    is String -> array.pushString(value)
                    JSONObject.NULL -> array.pushNull()
                    else -> array.pushString(value.toString())
                }
            } catch (e: Exception) {
                // Skip invalid values
            }
        }
        return array
    }

    // Convert List<File> → WritableArray
    @JvmStatic
    fun filesToWritableArray(files: List<File>): WritableArray {
        val array = Arguments.createArray()
        files.forEach { f ->
            try {
                val obj = Arguments.createMap()
                obj.putString("path", f.absolutePath)
                obj.putString("name", f.name)
                obj.putBoolean("isDir", f.isDirectory)
                obj.putDouble("size", if (f.isFile) f.length().toDouble() else getFileOrFolderSizeInBytes(f).toDouble())
                obj.putDouble("modification", f.lastModified().toDouble())
                array.pushMap(obj)
            } catch (e: Exception) {
                // Skip invalid files
            }
        }
        return array
    }

    // Get File or Folder Size
    @JvmStatic
    fun getFileOrFolderSizeInBytes(file: File): Long {
        if (!file.exists()) return 0L

        var size: Long = 0
        if (file.isFile) {
            return file.length()
        }

        file.listFiles()?.forEach { f ->
            size += if (f.isDirectory) getFileOrFolderSizeInBytes(f) else f.length()
        }
        return size
    }

    @JvmStatic
    fun setAGUSPValue(context: Context, key: String, value: String) {
        try {
            val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit().putString(key, value).apply()
        } catch (e: Exception) {
            Log.e("FileManagerUtils", "Failed to set value for $key: ${e.message}")
        }
    }

    @JvmStatic
    fun getAGUSPValue(context: Context, key: String): String? {
        return try {
            val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPrefs.getString(key, null)
        } catch (e: Exception) {
            Log.e("FileManagerUtils", "Failed to get value for $key: ${e.message}")
            null
        }
    }

    @JvmStatic
    fun getDefaultObserverPath(context: Context): List<File> {
        val validFiles = mutableListOf<File>()
        try {
            val jsonString = getAGUSPValue(context, DEFAULT_OBSERVER_PATHS_KEY)
            if (!jsonString.isNullOrEmpty()) {
                val jsonArray = JSONArray(jsonString)
                val updatedArray = JSONArray()  // will store only valid paths

                for (i in 0 until jsonArray.length()) {
                    val path = jsonArray.optString(i)
                    val file = File(path)
                    if (file.exists() && file.isDirectory && file.canRead()) {
                        validFiles.add(file)
                        updatedArray.put(path) // keep valid path
                    }
                }

                // Update stored paths to remove invalid ones
                setAGUSPValue(context, DEFAULT_OBSERVER_PATHS_KEY, updatedArray.toString())
            }
        } catch (e: Exception) {
            Log.e("FileManagerUtils", "Error retrieving default observer paths: ${e.message}")
        }
        return validFiles
    }
}
