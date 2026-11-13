package com.fitnessmarket.bootcamptodo.domain

/** Workout photos, addressed by file name only so the storage location can change. */
interface PhotoStore {
    /** A fresh, unused file name for the camera to write to. Creates no file. */
    fun newFileName(): String

    /** Deletes the photo; does nothing if it doesn't exist. */
    fun delete(fileName: String)

    fun listFileNames(): List<String>
}
