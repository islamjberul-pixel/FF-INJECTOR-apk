package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.FileStatus
import com.example.model.ManagedFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class FileRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ff_data_injector_prefs", Context.MODE_PRIVATE)
    private val storageFile = File(context.filesDir, "managed_files.json")

    fun loadManagedFiles(): List<ManagedFile> {
        val list = mutableListOf<ManagedFile>()
        if (!storageFile.exists()) return list

        try {
            val jsonStr = storageFile.readText()
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val statusStr = obj.optString("status", FileStatus.READY.name)
                val status = try {
                    FileStatus.valueOf(statusStr)
                } catch (_: Exception) {
                    FileStatus.READY
                }

                val sizeBytes = obj.optLong("sizeBytes", 0L)
                val managed = ManagedFile(
                    id = obj.getString("id"),
                    displayName = obj.getString("displayName"),
                    sourceUri = obj.getString("sourceUri"),
                    sizeBytes = sizeBytes,
                    formattedSize = ManagedFile.formatBytes(sizeBytes),
                    status = status,
                    progress = if (status == FileStatus.INJECTED) 1f else 0f,
                    statusMessage = obj.optString("statusMessage", "Restored"),
                    destinationFileName = if (obj.has("destinationFileName")) obj.getString("destinationFileName") else null,
                    injectedTimestamp = if (obj.has("injectedTimestamp")) obj.getLong("injectedTimestamp") else null,
                    verifiedByteSize = if (obj.has("verifiedByteSize")) obj.getLong("verifiedByteSize") else null
                )
                list.add(managed)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveManagedFiles(files: List<ManagedFile>) {
        try {
            val array = JSONArray()
            for (file in files) {
                val obj = JSONObject().apply {
                    put("id", file.id)
                    put("displayName", file.displayName)
                    put("sourceUri", file.sourceUri)
                    put("sizeBytes", file.sizeBytes)
                    put("status", file.status.name)
                    put("statusMessage", file.statusMessage)
                    file.destinationFileName?.let { put("destinationFileName", it) }
                    file.injectedTimestamp?.let { put("injectedTimestamp", it) }
                    file.verifiedByteSize?.let { put("verifiedByteSize", it) }
                }
                array.put(obj)
            }
            val tempFile = File(storageFile.parentFile, "${storageFile.name}.tmp")
            tempFile.writeText(array.toString())
            if (tempFile.exists()) {
                tempFile.renameTo(storageFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveSelectedGameId(gameId: String) {
        prefs.edit().putString("selected_game_id", gameId).apply()
    }

    fun getSelectedGameId(): String {
        return prefs.getString("selected_game_id", "freefire_std") ?: "freefire_std"
    }

    fun saveDestinationTreeUri(uriString: String?, displayName: String?) {
        prefs.edit()
            .putString("dest_tree_uri", uriString)
            .putString("dest_display_name", displayName)
            .apply()
    }

    fun getSavedDestinationTreeUri(): String? {
        return prefs.getString("dest_tree_uri", null)
    }

    fun getSavedDestinationDisplayName(): String? {
        return prefs.getString("dest_display_name", null)
    }
}
