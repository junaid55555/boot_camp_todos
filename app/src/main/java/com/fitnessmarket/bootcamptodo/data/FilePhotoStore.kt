package com.fitnessmarket.bootcamptodo.data

import com.fitnessmarket.bootcamptodo.domain.PhotoStore
import java.io.File
import java.util.UUID

/** Photos as JPEG files in one private folder (`filesDir/photos`), shared with the camera via FileProvider. */
class FilePhotoStore(private val dir: File) : PhotoStore {

    override fun newFileName(): String {
        dir.mkdirs()
        return "${UUID.randomUUID()}.jpg"
    }

    override fun delete(fileName: String) {
        file(fileName).delete()
    }

    override fun listFileNames(): List<String> = dir.listFiles()?.filter { it.isFile }?.map { it.name }.orEmpty()

    /** The file for [fileName]; rejects names that would point outside the folder. */
    fun file(fileName: String): File {
        require(fileName.isNotEmpty() && File(fileName).name == fileName && fileName != "..") {
            "Not a plain file name: $fileName"
        }
        return File(dir, fileName)
    }
}
