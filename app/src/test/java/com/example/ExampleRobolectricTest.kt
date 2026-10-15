package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.FileRepository
import com.example.model.FileStatus
import com.example.model.GameTarget
import com.example.model.ManagedFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FF DATA INJECTOR", appName)
    }

    @Test
    fun testFileRepositoryPersistence() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = FileRepository(context)

        val testFile = ManagedFile(
            id = "test-123",
            displayName = "patch_v1.dat",
            sourceUri = "content://media/external/file/123",
            sizeBytes = 15 * 1024 * 1024L,
            status = FileStatus.INJECTED,
            destinationFileName = "patch_v1.dat"
        )

        repository.saveManagedFiles(listOf(testFile))
        val loaded = repository.loadManagedFiles()

        assertEquals(1, loaded.size)
        assertEquals("test-123", loaded[0].id)
        assertEquals("patch_v1.dat", loaded[0].displayName)
        assertEquals(FileStatus.INJECTED, loaded[0].status)
    }

    @Test
    fun testGameTargetPackageIdentifier() {
        val targets = GameTarget.defaultTargets()
        val standardFF = targets.first { it.id == "freefire_std" }

        assertEquals("com.dts.freefireth", standardFF.packageName)
        assertEquals("Android/data/com.dts.freefireth/files/", standardFF.targetSubPath)
        assertTrue(standardFF.isPrimary)
    }
}
