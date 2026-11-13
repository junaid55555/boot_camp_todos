package com.fitnessmarket.bootcamptodo.ui.photo

import android.graphics.Bitmap
import android.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class WorkoutPhotoDecodeTest {

    private val dir = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "decode-test")
        .apply { mkdirs() }

    @After
    fun tearDown() {
        dir.deleteRecursively()
    }

    /** A landscape 400 x 200 JPEG, optionally tagged as needing a rotation (as camera apps do). */
    private fun jpeg(name: String, exifOrientation: Int? = null): File {
        val file = File(dir, name)
        val bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        if (exifOrientation != null) {
            ExifInterface(file.path).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation.toString())
                saveAttributes()
            }
        }
        return file
    }

    @Test
    fun withoutExif_keepsOrientation() {
        val bitmap = decodeWorkoutPhoto(jpeg("plain.jpg"), targetPx = 200)!!

        assertEquals(400, bitmap.width)
        assertEquals(200, bitmap.height)
    }

    @Test
    fun downscales_butNotBelowTheTargetSize() {
        val bitmap = decodeWorkoutPhoto(jpeg("big.jpg"), targetPx = 50)!!

        val shorterSide = minOf(bitmap.width, bitmap.height)
        assertTrue("expected downscaling, got $shorterSide", shorterSide < 200)
        assertTrue("expected at least 50, got $shorterSide", shorterSide >= 50)
        assertEquals("aspect ratio kept", 2f, bitmap.width.toFloat() / bitmap.height, 0.01f)
    }

    @Test
    fun exifRotate90_isTurnedUpright() {
        val bitmap = decodeWorkoutPhoto(jpeg("rot90.jpg", ExifInterface.ORIENTATION_ROTATE_90), targetPx = 200)!!

        assertEquals(200, bitmap.width)
        assertEquals(400, bitmap.height)
    }

    @Test
    fun exifRotate270_isTurnedUpright() {
        val bitmap = decodeWorkoutPhoto(jpeg("rot270.jpg", ExifInterface.ORIENTATION_ROTATE_270), targetPx = 200)!!

        assertEquals(200, bitmap.width)
        assertEquals(400, bitmap.height)
    }

    @Test
    fun exifRotate180_keepsLandscape() {
        val bitmap = decodeWorkoutPhoto(jpeg("rot180.jpg", ExifInterface.ORIENTATION_ROTATE_180), targetPx = 200)!!

        assertEquals(400, bitmap.width)
        assertEquals(200, bitmap.height)
    }

    @Test
    fun missingFile_isNull() {
        assertNull(decodeWorkoutPhoto(File(dir, "missing.jpg"), targetPx = 50))
    }

    @Test
    fun notAnImage_isNull() {
        val file = File(dir, "not-an-image.jpg").apply { writeText("hello") }

        assertNull(decodeWorkoutPhoto(file, targetPx = 50))
    }

    @Test
    fun emptyFile_isNull() {
        // What a cancelled camera can leave behind.
        val file = File(dir, "empty.jpg").apply { createNewFile() }

        assertNull(decodeWorkoutPhoto(file, targetPx = 50))
    }
}
