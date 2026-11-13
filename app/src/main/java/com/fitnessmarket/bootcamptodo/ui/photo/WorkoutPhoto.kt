package com.fitnessmarket.bootcamptodo.ui.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Shows a stored workout photo, cropped to fill [modifier]'s size. Decodes off the main thread, downscaled
 * so the shorter side is at least [targetSize], and turned upright from the EXIF orientation.
 * Shows a plain placeholder while loading or when the file is missing.
 */
@Composable
fun WorkoutPhoto(file: File, contentDescription: String?, targetSize: Dp, modifier: Modifier = Modifier) {
    val targetPx = with(LocalDensity.current) { targetSize.roundToPx() }
    val bitmap by produceState<ImageBitmap?>(null, file, targetPx) {
        value = withContext(Dispatchers.IO) { decodeWorkoutPhoto(file, targetPx)?.asImageBitmap() }
    }

    // Described on the container, so screen readers find it while loading too.
    Box(
        modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                role = Role.Image
            },
    ) {
        bitmap?.let {
            Image(it, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

/** Decodes [file] with its shorter side at least [targetPx] (when the photo is that big), upright. Null if unreadable. */
internal fun decodeWorkoutPhoto(file: File, targetPx: Int): Bitmap? {
    if (!file.isFile) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (minOf(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= targetPx) sampleSize *= 2
    val bitmap = BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
        ?: return null

    val degrees = when (exifOrientation(file)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> return bitmap
    }
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun exifOrientation(file: File): Int = runCatching {
    ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
}.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
