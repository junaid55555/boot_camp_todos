package com.fitnessmarket.bootcamptodo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FilePhotoStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dir by lazy { File(tempFolder.root, "photos") }
    private val store by lazy { FilePhotoStore(dir) }

    @Test
    fun `new file names are unique jpgs and the folder exists`() {
        val first = store.newFileName()
        val second = store.newFileName()

        assertNotEquals(first, second)
        assertTrue(first.endsWith(".jpg"))
        assertTrue(dir.isDirectory)
        assertEquals(File(dir, first), store.file(first))
    }

    @Test
    fun `lists stored files`() {
        val name = store.newFileName().also { store.file(it).writeText("x") }

        assertEquals(listOf(name), store.listFileNames())
    }

    @Test
    fun `list is empty when the folder does not exist yet`() {
        assertEquals(emptyList<String>(), store.listFileNames())
    }

    @Test
    fun `delete removes the file and ignores a missing one`() {
        val name = store.newFileName().also { store.file(it).writeText("x") }

        store.delete(name)
        store.delete("missing.jpg")

        assertFalse(store.file(name).exists())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `names that would leave the folder are rejected`() {
        store.file("../workouts.db")
    }
}
