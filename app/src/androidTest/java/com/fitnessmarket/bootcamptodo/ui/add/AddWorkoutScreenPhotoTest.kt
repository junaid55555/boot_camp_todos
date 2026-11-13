package com.fitnessmarket.bootcamptodo.ui.add

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.core.app.ActivityOptionsCompat
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.data.FilePhotoStore
import com.fitnessmarket.bootcamptodo.data.RoomWorkoutRepository
import com.fitnessmarket.bootcamptodo.data.WorkoutDatabase
import com.fitnessmarket.bootcamptodo.domain.AddWorkoutUseCase
import com.fitnessmarket.bootcamptodo.domain.Workout
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Clock

/**
 * The add screen's camera flow, end to end except for the camera app itself: a fake [ActivityResultRegistry]
 * plays the camera, writing a real JPEG through our FileProvider URI.
 */
@RunWith(AndroidJUnit4::class)
class AddWorkoutScreenPhotoTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var db: WorkoutDatabase
    private lateinit var photoStore: FilePhotoStore
    private lateinit var photosBefore: Set<String>
    private val camera = FakeCamera()
    private var savedAndClosed = false

    private fun str(id: Int) = rule.activity.getString(id)

    @Before
    fun setUp() {
        val context = rule.activity
        db = Room.inMemoryDatabaseBuilder(context, WorkoutDatabase::class.java).build()
        // The real folder: FileProvider only shares filesDir/photos.
        photoStore = FilePhotoStore(File(context.filesDir, "photos"))
        photosBefore = photoStore.listFileNames().toSet()

        val clock = Clock.systemDefaultZone()
        val viewModel = AddWorkoutViewModel(
            AddWorkoutUseCase(RoomWorkoutRepository(db.workoutDao()), clock),
            photoStore,
            clock,
        )
        val registryOwner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry = camera
        }
        rule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
                BootCampTodoTheme {
                    AddWorkoutScreen(
                        viewModel = viewModel,
                        photoFile = photoStore::file,
                        onBack = { savedAndClosed = true },
                    )
                }
            }
        }
    }

    @After
    fun tearDown() {
        db.close()
        (photoStore.listFileNames() - photosBefore).forEach(photoStore::delete)
    }

    @Test
    fun takenPhoto_isPreviewed_andSavedWithTheWorkout() {
        fillValidForm()

        rule.onNodeWithText(str(R.string.add_photo)).performScrollTo().performClick()

        val uri = camera.launches.single()
        assertEquals("content", uri.scheme)
        assertEquals("${rule.activity.packageName}.fileprovider", uri.authority)
        val photoName = takenPhotoNames().single()
        assertTrue("camera output should be on disk", photoStore.file(photoName).length() > 0)
        rule.onNodeWithContentDescription(str(R.string.workout_photo)).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText(str(R.string.add_photo)).assertDoesNotExist()

        save()

        assertEquals(photoName, storedWorkout().photoFileName)
    }

    @Test
    fun cancelledCamera_leavesTheFormWithoutAPhoto() {
        camera.nextResult = CameraResult.Cancelled
        fillValidForm()

        rule.onNodeWithText(str(R.string.add_photo)).performScrollTo().performClick()

        rule.onNodeWithText(str(R.string.add_photo)).performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription(str(R.string.workout_photo)).assertDoesNotExist()
        save()
        assertNull(storedWorkout().photoFileName)
    }

    @Test
    fun retake_replacesThePhoto() {
        fillValidForm()
        rule.onNodeWithText(str(R.string.add_photo)).performScrollTo().performClick()
        rule.onNodeWithText(str(R.string.retake_photo)).performScrollTo().performClick()

        assertEquals(2, camera.launches.size)
        val secondPhoto = camera.launches.last().lastPathSegment
        save()
        assertEquals(secondPhoto, storedWorkout().photoFileName)
    }

    @Test
    fun removedPhoto_isNotSaved() {
        fillValidForm()
        rule.onNodeWithText(str(R.string.add_photo)).performScrollTo().performClick()

        rule.onNodeWithText(str(R.string.remove_photo)).performScrollTo().performClick()

        rule.onNodeWithText(str(R.string.add_photo)).performScrollTo().assertIsDisplayed()
        save()
        assertNull(storedWorkout().photoFileName)
    }

    @Test
    fun noCameraApp_showsAMessage_andKeepsTheForm() {
        camera.nextResult = CameraResult.NoCameraApp
        fillValidForm()

        rule.onNodeWithText(str(R.string.add_photo)).performScrollTo().performClick()

        rule.onNodeWithText(str(R.string.no_camera_app)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.add_photo)).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Leg day").assertIsDisplayed()
    }

    private fun fillValidForm() {
        rule.onNodeWithText(str(R.string.field_name)).performTextInput("Leg day")
        rule.onNodeWithText(str(R.string.field_duration)).performTextInput("50")
    }

    private fun save() {
        rule.onNodeWithText(str(R.string.save)).performScrollTo().performClick()
        rule.waitUntil(timeoutMillis = 5_000) { savedAndClosed }
    }

    private fun storedWorkout(): Workout =
        runBlocking { RoomWorkoutRepository(db.workoutDao()).observeWorkouts().first().single() }

    private fun takenPhotoNames() = photoStore.listFileNames() - photosBefore

    private enum class CameraResult { Taken, Cancelled, NoCameraApp }

    /** Plays the camera app for ActivityResultContracts.TakePicture. */
    private inner class FakeCamera : ActivityResultRegistry() {
        val launches = mutableListOf<Uri>()
        var nextResult = CameraResult.Taken

        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            if (nextResult == CameraResult.NoCameraApp) throw ActivityNotFoundException("no camera in this test")
            val uri = input as Uri
            launches += uri
            val taken = nextResult == CameraResult.Taken
            if (taken) {
                // Written through the content URI, like a real camera app: this proves FileProvider grants write access.
                rule.activity.contentResolver.openOutputStream(uri)!!.use {
                    Bitmap.createBitmap(64, 48, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.JPEG, 90, it)
                }
            }
            dispatchResult(requestCode, taken)
        }
    }
}
