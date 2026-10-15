package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import com.example.model.DestinationState
import com.example.model.GameTarget
import com.example.model.ManagedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

sealed class FileSelectionResult {
    data class Success(val file: ManagedFile) : FileSelectionResult()
    data class Error(val message: String) : FileSelectionResult()
}

sealed class InjectResult {
    data class Success(val verifiedBytes: Long, val fileName: String) : InjectResult()
    data class Failure(val reason: String) : InjectResult()
}

sealed class RemoveResult {
    data class Success(val fileName: String) : RemoveResult()
    data class Failure(val reason: String) : RemoveResult()
}

class StorageManager(private val context: Context) {

    companion object {
        const val MAX_MANAGED_FILES = 10
        const val BUFFER_SIZE = 64 * 1024 // 64 KB buffer for streaming I/O
    }

    /**
     * Inspects a content URI selected by the user, validates size <= 250MB,
     * non-empty, and checks against current managed files.
     */
    fun processSelectedUri(
        uri: Uri,
        existingFiles: List<ManagedFile>
    ): FileSelectionResult {
        if (existingFiles.size >= MAX_MANAGED_FILES) {
            return FileSelectionResult.Error("Limit reached: You can manage a maximum of $MAX_MANAGED_FILES files.")
        }

        val contentResolver = context.contentResolver
        var displayName = "file_${System.currentTimeMillis()}"
        var sizeBytes = -1L

        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                if (cursor.moveToFirst()) {
                    if (nameIndex != -1 && !cursor.isNull(nameIndex)) {
                        displayName = cursor.getString(nameIndex)
                    }
                    if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                        sizeBytes = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            return FileSelectionResult.Error("Unable to inspect selected file: ${e.localizedMessage ?: "Read failure"}")
        }

        // Fallback size determination if cursor did not return it
        if (sizeBytes <= 0) {
            try {
                contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                    val len = afd.length
                    if (len > 0) {
                        sizeBytes = len
                    }
                }
            } catch (_: Exception) {
                // Ignore and keep sizeBytes
            }
        }

        // Validation 1: Size bounds
        if (sizeBytes == 0L) {
            return FileSelectionResult.Error("File '$displayName' is empty (0 bytes). Empty files cannot be injected.")
        }

        if (sizeBytes > ManagedFile.MAX_FILE_SIZE_BYTES) {
            val formatted = ManagedFile.formatBytes(sizeBytes)
            return FileSelectionResult.Error("File '$displayName' ($formatted) exceeds the 250 MB limit. Please select a file under 250 MB.")
        }

        // Validation 2: Duplicate check
        val duplicate = existingFiles.any { it.displayName.equals(displayName, ignoreCase = true) || it.sourceUri == uri.toString() }
        if (duplicate) {
            return FileSelectionResult.Error("A file with the name '$displayName' is already in your managed files list.")
        }

        // Take persistable URI permission if permitted
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
            // Some providers don't support persistable flags; temporary access is still preserved for the current session
        }

        val managed = ManagedFile(
            id = UUID.randomUUID().toString(),
            displayName = displayName,
            sourceUri = uri.toString(),
            sizeBytes = if (sizeBytes > 0) sizeBytes else 0L,
            formattedSize = if (sizeBytes > 0) ManagedFile.formatBytes(sizeBytes) else "Unknown size",
            statusMessage = "Ready to inject"
        )

        return FileSelectionResult.Success(managed)
    }

    /**
     * Inspects user-selected tree Uri for destination folder.
     * Verifies write access, checks for Android 11+ Scoped Storage restrictions on Android/data.
     */
    fun evaluateDestinationTree(
        treeUri: Uri,
        target: GameTarget
    ): DestinationState {
        val cr = context.contentResolver

        // Take persistable write permissions
        try {
            cr.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: Exception) {
            // Some trees might not grant persistable permission
        }

        val docTree = try {
            DocumentFile.fromTreeUri(context, treeUri)
        } catch (e: Exception) {
            return DestinationState(
                targetFolderPath = target.targetSubPath,
                treeUriString = treeUri.toString(),
                folderDisplayName = "Invalid Folder",
                isAccessible = false,
                isVerifiedTarget = false,
                isScopedStorageRestricted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R,
                statusMessage = "Cannot parse folder tree: ${e.localizedMessage}"
            )
        }

        if (docTree == null || !docTree.exists()) {
            return DestinationState(
                targetFolderPath = target.targetSubPath,
                treeUriString = treeUri.toString(),
                folderDisplayName = "Unavailable",
                isAccessible = false,
                isVerifiedTarget = false,
                isScopedStorageRestricted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R,
                statusMessage = "Selected folder does not exist or access was revoked."
            )
        }

        val folderName = docTree.name ?: "Custom Destination"
        val canWrite = docTree.canWrite()

        // Check path correlation
        val uriStr = treeUri.toString()
        val containsGamePackage = uriStr.contains(target.packageName, ignoreCase = true) ||
                uriStr.contains("freefire", ignoreCase = true)

        // Attempt active write verification
        var writeVerified = false
        if (canWrite) {
            try {
                val testFile = docTree.createFile("text/plain", ".perm_check_${System.currentTimeMillis()}")
                if (testFile != null && testFile.exists()) {
                    writeVerified = true
                    testFile.delete()
                }
            } catch (_: Exception) {
                writeVerified = false
            }
        }

        val isRestricted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !containsGamePackage

        val message = when {
            writeVerified && containsGamePackage ->
                "Verified: Write access granted to ${target.displayName} folder."
            writeVerified ->
                "Folder accessible: Write permission confirmed for '$folderName'."
            !canWrite && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                "Access Blocked: Android 11+ Scoped Storage policy blocks direct access to '${target.targetSubPath}'."
            else ->
                "Write permission denied by Android OS for this directory."
        }

        return DestinationState(
            targetFolderPath = target.targetSubPath,
            treeUriString = treeUri.toString(),
            folderDisplayName = folderName,
            isAccessible = writeVerified,
            isVerifiedTarget = containsGamePackage,
            isScopedStorageRestricted = !writeVerified && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R,
            statusMessage = message
        )
    }

    /**
     * Performs a real streaming byte-by-byte file copy to the verified destination tree.
     * Reports live progress and strictly verifies copied byte size upon completion.
     */
    suspend fun injectFile(
        file: ManagedFile,
        destTreeUriString: String?,
        target: GameTarget,
        onProgress: (bytesCopied: Long, totalBytes: Long) -> Unit
    ): InjectResult = withContext(Dispatchers.IO) {
        if (destTreeUriString.isNullOrBlank()) {
            return@withContext InjectResult.Failure(
                "Destination folder not configured. Scoped storage requires an authorized target directory."
            )
        }

        val destUri = Uri.parse(destTreeUriString)
        val destTree = try {
            DocumentFile.fromTreeUri(context, destUri)
        } catch (e: Exception) {
            return@withContext InjectResult.Failure("Failed to resolve destination folder: ${e.localizedMessage}")
        }

        if (destTree == null || !destTree.canWrite()) {
            return@withContext InjectResult.Failure(
                "Write access denied. Android Scoped Storage blocks writes to this directory."
            )
        }

        val sourceUri = Uri.parse(file.sourceUri)
        val contentResolver = context.contentResolver

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            inputStream = contentResolver.openInputStream(sourceUri)
                ?: return@withContext InjectResult.Failure("Cannot open source file for reading.")

            // Handle filename collisions safely
            val targetFileName = file.displayName
            val existing = destTree.findFile(targetFileName)
            if (existing != null && existing.exists()) {
                // If this is an existing file, attempt safe deletion to refresh
                try {
                    existing.delete()
                } catch (_: Exception) {
                    // If deletion fails, proceed to create or throw
                }
            }

            val targetDocFile = destTree.createFile("application/octet-stream", targetFileName)
                ?: return@withContext InjectResult.Failure(
                    "Android OS rejected file creation in destination folder. Storage may be restricted or full."
                )

            outputStream = contentResolver.openOutputStream(targetDocFile.uri)
                ?: return@withContext InjectResult.Failure("Failed to open destination file for writing.")

            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            var totalBytesCopied = 0L
            val expectedSize = file.sizeBytes

            var lastReportTime = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesCopied += bytesRead

                val now = System.currentTimeMillis()
                if (now - lastReportTime > 80 || totalBytesCopied == expectedSize) {
                    lastReportTime = now
                    onProgress(totalBytesCopied, expectedSize)
                }
            }

            outputStream.flush()

            // Verify copied size
            val actualCopiedLength = targetDocFile.length()
            val verified = if (actualCopiedLength > 0) actualCopiedLength else totalBytesCopied

            if (expectedSize > 0 && verified != expectedSize) {
                return@withContext InjectResult.Failure(
                    "Size mismatch verification failed: expected $expectedSize bytes, wrote $verified bytes."
                )
            }

            InjectResult.Success(
                verifiedBytes = verified,
                fileName = targetDocFile.name ?: targetFileName
            )

        } catch (e: Exception) {
            InjectResult.Failure("Streaming copy error: ${e.localizedMessage ?: e.javaClass.simpleName}")
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { outputStream?.close() } catch (_: Exception) {}
        }
    }

    /**
     * Removes ONLY the file specifically placed and tracked by this application.
     */
    suspend fun removeInjectedFile(
        file: ManagedFile,
        destTreeUriString: String?
    ): RemoveResult = withContext(Dispatchers.IO) {
        if (destTreeUriString.isNullOrBlank()) {
            return@withContext RemoveResult.Failure("Destination folder path is not set.")
        }

        val destUri = Uri.parse(destTreeUriString)
        val destTree = DocumentFile.fromTreeUri(context, destUri)
            ?: return@withContext RemoveResult.Failure("Cannot access destination tree.")

        val targetName = file.destinationFileName ?: file.displayName
        val targetFile = destTree.findFile(targetName)

        if (targetFile == null || !targetFile.exists()) {
            // Already removed or missing
            return@withContext RemoveResult.Success(targetName)
        }

        try {
            val deleted = targetFile.delete()
            if (deleted) {
                RemoveResult.Success(targetName)
            } else {
                RemoveResult.Failure("Android OS denied deletion of '$targetName'. Check folder permissions.")
            }
        } catch (e: Exception) {
            RemoveResult.Failure("Deletion failed: ${e.localizedMessage}")
        }
    }
}
