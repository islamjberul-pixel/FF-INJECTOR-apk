package com.example

import com.example.data.StorageManager
import com.example.model.FileStatus
import com.example.model.GameTarget
import com.example.model.ManagedFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMaxFileSizeConstraint() {
        val maxExpected = 250L * 1024L * 1024L // 250 MB in bytes
        assertEquals(maxExpected, ManagedFile.MAX_FILE_SIZE_BYTES)
    }

    @Test
    fun testMaxManagedFilesConstraint() {
        assertEquals(10, StorageManager.MAX_MANAGED_FILES)
    }

    @Test
    fun testFormatBytes() {
        assertEquals("0 B", ManagedFile.formatBytes(0))
        assertEquals("500 B", ManagedFile.formatBytes(500))
        assertEquals("1.0 KB", ManagedFile.formatBytes(1024))
        assertEquals("10.0 MB", ManagedFile.formatBytes(10 * 1024 * 1024L))
        assertEquals("250.0 MB", ManagedFile.formatBytes(250 * 1024 * 1024L))
    }

    @Test
    fun testOfficialDestinationFolderSubPath() {
        assertEquals(
            "Android/data/com.dts.freefireth/files/",
            GameTarget.DEFAULT_DESTINATION_SUBPATH
        )
    }

    @Test
    fun testFileStatusTransitions() {
        val file = ManagedFile(
            id = "test-1",
            displayName = "config.dat",
            sourceUri = "content://test",
            sizeBytes = 2048
        )
        assertEquals(FileStatus.READY, file.status)

        val injecting = file.copy(status = FileStatus.INJECTING, progress = 0.5f)
        assertEquals(FileStatus.INJECTING, injecting.status)
        assertEquals(0.5f, injecting.progress)

        val injected = injecting.copy(status = FileStatus.INJECTED, progress = 1f)
        assertEquals(FileStatus.INJECTED, injected.status)
    }
}
