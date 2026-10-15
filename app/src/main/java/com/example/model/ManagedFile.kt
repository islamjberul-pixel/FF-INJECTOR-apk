package com.example.model

import java.util.Locale

data class ManagedFile(
    val id: String,
    val displayName: String,
    val sourceUri: String,
    val sizeBytes: Long,
    val formattedSize: String = formatBytes(sizeBytes),
    val status: FileStatus = FileStatus.READY,
    val progress: Float = 0f,
    val statusMessage: String = "Ready to inject",
    val destinationFileName: String? = null,
    val injectedTimestamp: Long? = null,
    val verifiedByteSize: Long? = null
) {
    companion object {
        const val MAX_FILE_SIZE_BYTES: Long = 250L * 1024L * 1024L // 250 MB

        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
                else -> "$bytes B"
            }
        }
    }
}
