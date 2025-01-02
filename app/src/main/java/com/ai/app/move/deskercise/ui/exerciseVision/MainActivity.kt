/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package com.ai.app.move.deskercise.ui.exerciseVision

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.app.AppStorage
import com.ai.app.move.deskercise.data.ArrayListQueue
import com.ai.app.move.deskercise.data.Device
import com.ai.app.move.deskercise.data.Exercise
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.databinding.ActivityMainBinding
import com.ai.app.move.deskercise.databinding.ActivityMainBinding.inflate
import com.ai.app.move.deskercise.ml.*
import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.services.CameraSource
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.Commons.Companion.displayGifOnImageView
import com.ai.app.move.deskercise.services.Commons.Companion.mirrorGifOnImageView
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.StartProgramViewModel
import com.ai.app.move.deskercise.ui.exerciseMenu.RecyclerViewAdapterAvailableExercise
import com.ai.app.move.deskercise.ui.exerciseSummary.ExerciseSummaryFragment
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveController
import com.ai.app.move.deskercise.vision.MoveController.Companion._isPausedSF
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingController
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.ForwardStretchController
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.ForwardStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.HzShoulderStretchController
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.HzShoulderStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.NeckStretchController
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.NeckStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.OverheadShoulderStretchController
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.OverheadShoulderStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.OverheadTricepsStretchController
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.OverheadTricepsStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.PiriformisStretchController
import com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.PiriformisStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.ShoulderShrugStretchController
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.ShoulderShrugStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.SideStretchController
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.SideStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchController
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.UpperTrapStretchController
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.UpperTrapStretchModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.InputStream

/**
 * MainActivity contains the logic to handle the executing of all exercises
 * including:
 * iterating through all exercises,
 * managing the StartingFragment,
 * managing the SummaryFragment,
 * managing the skip and exit button
 */

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), MainActivityBridge {

    //    var modelPos = 1 //Thunder
    private val viewModel by viewModel<StartProgramViewModel>()
    private val appStorage: AppStorage by inject()
    private lateinit var binding: ActivityMainBinding // using viewBinding
    private val fragmentManager: FragmentManager = supportFragmentManager
    private val starting_exercise_fragment_tag = "STARTING_EXERCISE_FRAGMENT"
    private val summary_exercise_fragment_tag = "SUMMARY_EXERCISE_FRAGMENT"
    var firstExercise = true
    var firstInstance = true
    private var _skipQueueSF = MutableStateFlow(0)
    private val skipQueueSF = _skipQueueSF.asStateFlow()
    private var isNotInMiddleOfSkipping = true
    private var currentAnimator: Animator? = null
    private var shortAnimationDuration: Int = 0
    private var exerciseList: ArrayList<Exercise> = ArrayList()
    var pointCount = Int

    /** A [SurfaceView] for camera preview.   */
    private lateinit var surfaceView: SurfaceView

    /** Default pose estimation model is 1 (MoveNet Thunder)
     * 0 == MoveNet Lightning model
     **/
    private var modelPos = 0 // Lightning

    /** Default device is CPU */
    private var device = Device.CPU
    var person: Person? = null
    private var cameraSource: CameraSource? = null
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                openCamera()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                ErrorDialog.newInstance(getString(R.string.tfe_pe_request_permission))
                    .show(supportFragmentManager, FRAGMENT_DIALOG)
            }
        }

    init {
        instance = this
    }

    private fun zoomImageFromThumb(thumbView: View, imageResId: Int) {
        // If there's an animation in progress, cancel it immediately and
        // proceed with this one.
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.

        val assetManager: AssetManager = resources.assets
        lifecycleScope.launch {
            if (moveController.model.imagesFolderPath.isNotEmpty()) {
                binding.expandedImage.visibility = View.VISIBLE

                displayGifOnImageView(
                    moveController.model.imagesFolderPath,
                    binding.expandedImage,
                    assetManager,
                    Commons.Direction.RIGHT,
                    this@MainActivity,
                )
            } else {
                binding.expandedImage.setImageResource(imageResId)
            }
        }

        // Calculate the s tarting and ending bounds for the zoomed-in image.
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the
        // container view. Set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBoundsInt)
        binding.relativeLayout.getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        // Using the "center crop" technique, adjust the start bounds to be the
        // same aspect ratio as the final bounds. This prevents unwanted
        // stretching during the animation. Calculate the start scaling factor.
        // The end scaling factor is always 1.0.
        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally.
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically.
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it positions the zoomed-in view in the place of the
        // thumbnail.
        thumbView.alpha = 0f

        animateZoomToLargeImage(startBounds, finalBounds, startScale)

        setDismissLargeImageAnimation(thumbView, startBounds, startScale)
    }

    private fun animateZoomToLargeImage(startBounds: RectF, finalBounds: RectF, startScale: Float) {
        binding.expandedImage.visibility = View.VISIBLE
        binding.skipButton.visibility = View.GONE
        binding.exitButton.visibility = View.GONE
        binding.silhouetteImage.visibility = View.GONE

        // Set the pivot point for SCALE_X and SCALE_Y transformations to the
        // top-left corner of the zoomed-in view. The default is the center of
        // the view.
        binding.expandedImage.pivotX = 0f
        binding.expandedImage.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties: X, Y, SCALE_X, and SCALE_Y.
        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    binding.expandedImage,
                    View.X,
                    startBounds.left,
                    finalBounds.left,
                ),
            ).apply {
                with(
                    ObjectAnimator.ofFloat(
                        binding.expandedImage,
                        View.Y,
                        startBounds.top,
                        finalBounds.top,
                    ),
                )
                with(ObjectAnimator.ofFloat(binding.expandedImage, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(binding.expandedImage, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }
    }

    private fun setDismissLargeImageAnimation(
        thumbView: View,
        startBounds: RectF,
        startScale: Float,
    ) {
        // When the zoomed-in image is tapped, it zooms down to the original
        // bounds and shows the thumbnail instead of the expanded image.
        binding.expandedImage.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning and sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(
                    ObjectAnimator.ofFloat(
                        binding.expandedImage,
                        View.X,
                        startBounds.left,
                    ),
                ).apply {
                    with(ObjectAnimator.ofFloat(binding.expandedImage, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(binding.expandedImage, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(binding.expandedImage, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        thumbView.alpha = 1f
                        binding.expandedImage.visibility = View.GONE
                        binding.skipButton.visibility = View.VISIBLE
                        binding.exitButton.visibility = View.VISIBLE
                        binding.silhouetteImage.visibility = View.VISIBLE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        thumbView.alpha = 1f
                        binding.expandedImage.visibility = View.GONE
                        binding.skipButton.visibility = View.VISIBLE
                        binding.exitButton.visibility = View.VISIBLE
                        binding.silhouetteImage.visibility = View.VISIBLE
                        currentAnimator = null
                    }
                })
                start()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = inflate(layoutInflater)
        setContentView(binding.root)
        registerEvents()
        context = applicationContext
        AudioManagerService(applicationContext)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        surfaceView = findViewById(R.id.surfaceView)

        firstInstance = true

        movesList = RecyclerViewAdapterAvailableExercise.listOfSelectedMovesShared
        moveQueue = ArrayListQueue<Move>()
        for (move in movesList) {
            moveQueue.enqueue(move)
        }

        if (moveQueue.count > 0) {
            // need to dequeue elsewhere too
            moveController = getControllerBasedOfModel(moveQueue.dequeue()!!)
        } else {
            // error
            Toast.makeText(this, "No Moves selected", Toast.LENGTH_LONG).show()
            finish()
        }

        // When Back Button pressed
        instance!!.onBackPressedDispatcher.addCallback(this) {
            showExitConfirmation(
                {
                    moveController._didUserExitSF.value = true
                    finishAndRemoveTask()
                },
            )
        }

        if (!isCameraPermissionGranted()) requestPermission()
    }

    private fun registerEvents() {
        viewModel.pointLiveData.observe(this) { points ->
            binding.tvPoint.text = if (points == 0) {
                ""
            } else {
                buildString {
                    append("\uD83C\uDFC6+")
                    append(points.toString())
                    append("points")
                }
            }
        }
    }

    private suspend fun restartMoveActivity() {
        firstInstance = true
        firstExercise = false

        moveController._exerciseCompletedSF.value = false
        yield()
        onStart()
        yield()
    }

    private fun getControllerBasedOfModel(mv: Move): MoveController {
        moveController = when (mv::class.java) {
            NeckStretchModel::class.java -> NeckStretchController(this)
            OverheadShoulderStretchModel::class.java -> OverheadShoulderStretchController(this)
            ForwardStretchModel::class.java -> ForwardStretchController(this)
            SideStretchModel::class.java -> SideStretchController(this)
            PiriformisStretchModel::class.java -> PiriformisStretchController(this)
            ShoulderShrugStretchModel::class.java -> ShoulderShrugStretchController(this)
            TorsoStretchModel::class.java -> TorsoStretchController(this)
            UpperTrapStretchModel::class.java -> UpperTrapStretchController(this)
            OverheadTricepsStretchModel::class.java -> OverheadTricepsStretchController(this)
            HzShoulderStretchModel::class.java -> HzShoulderStretchController(this)
            DbreathingModel::class.java -> DbreathingController(this)
            else -> OverheadShoulderStretchController(this)
        }
        moveController.model = mv

        return moveController
    }

    private fun getNextMoveController(): Boolean {
        return if (moveQueue.count > 0) {
            moveController = getControllerBasedOfModel(moveQueue.dequeue()!!)
            true
        } else {
            false
        }
    }

    override suspend fun isStartingExerciseFragmentVisible(): Boolean {
        val myFragment: ExerciseStartingFragment? =
            fragmentManager.findFragmentByTag(starting_exercise_fragment_tag) as ExerciseStartingFragment?
        val isVisible = myFragment != null && myFragment.isVisible
        _isPausedSF.value = isVisible
        delay(100)
        return isVisible
    }

    suspend fun isPopUpFragmentVisible(): Boolean {
        val myFragment: ReminderFragment? =
            fragmentManager.findFragmentByTag(DIALOGUE_POPUP) as ReminderFragment?
        val isVisible = myFragment != null && myFragment.isVisible
        _isPausedSF.value = isVisible
        delay(100)
        return isVisible
    }

    private fun isSummaryExerciseFragmentVisible(): Boolean {
        val myFragment: ExerciseSummaryFragment? =
            fragmentManager.findFragmentByTag(summary_exercise_fragment_tag) as ExerciseSummaryFragment?
        val isVisible = myFragment != null && myFragment.isVisible
        _isPausedSF.value = isVisible
        // delay(100)
        return isVisible
    }

    private fun clearAllRepCountsForAllExercises() {
        binding.tvPoint.text = ""
        Move.goodReps = 0F
        Move.totalReps = 0F
    }

    override fun onStart() {
        super.onStart()
        _isPausedSF.value = false

        if (cameraSource == null) {
            openCamera()
        }

        // Restart visualization of Key Points
        updateDrawnKeyPoints()

        if (firstInstance) {
            openExerciseStartingFragment()
//            lifecycleScope.launch{
//                yield()
//                if (isPopUpFragmentVisible()){
//                    while (isPopUpFragmentVisible()){
//                        yield()
//                    }
//                }
//            }

            lifecycleScope.launch {
                // waiting for camera to detect && check if fragment is closed
                yield()

                if (isStartingExerciseFragmentVisible() && !isPopUpFragmentVisible()) {
                    delay(50)
                    yield()

                    moveController.welcome()
                    while (isStartingExerciseFragmentVisible()) {
                        yield()
                    }
                }

                delay(50)
                yield()

                lifecycleScope.launch {
                    while (person == null || isStartingExerciseFragmentVisible() || _isPausedSF.value || !isCameraPermissionGranted() || isPopUpFragmentVisible()) {
                        delay(50)
                        yield()
                    }

                    // Pre Exercise Stretch
                    moveController.initialView()

                    // if SkipState then
                    moveController.executeState()
                }
            }
        }

        if (!firstInstance) return
        firstInstance = false

        lifecycleScope.launch {
            binding.ivSquareGif.setOnClickListener {
                zoomImageFromThumb(surfaceView, R.color.deskGreen)
            }

            shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

            moveController.gifDirectionSF.collect { value ->
                if (value == 1) {
                    // set Left
                    mirrorGifOnImageView(binding.ivSquareGif, Commons.Direction.LEFT)
                    mirrorGifOnImageView(binding.silhouetteImage, Commons.Direction.LEFT)
                } else {
                    // set Right
                    mirrorGifOnImageView(binding.ivSquareGif, Commons.Direction.RIGHT)
                    mirrorGifOnImageView(binding.silhouetteImage, Commons.Direction.RIGHT)
                }
                yield()
            }
        }
        lifecycleScope.launch {
            moveController.currentRepetitionState.collect { value ->
                val assetManager: AssetManager = resources.assets
                Log.d("TAG", "$value")

                if (moveController.model.imagesFolderPath.isNotEmpty()) {
                    var relative_folder_path = moveController.model.imagesFolderPath
                    if (relative_folder_path.endsWith("/")) {
                        relative_folder_path = relative_folder_path.dropLast(1)
                    }
                    var state = value
                    if (value == 0) {
                        state = 1
                    }

                    val animationImageStream: InputStream =
                        (assetManager.open("$relative_folder_path/${relative_folder_path.substring(7)}-$state.png"))
                    if (animationImageStream != null) {
                        val d = Drawable.createFromStream(animationImageStream, null)
                        binding.ivSquareGif.setImageDrawable(d)
                    }
                } else {
                    binding.ivSquareGif.visibility = View.GONE
                }
                if (value == 0) {
                    if (moveController.model.name == "Piriformis Stretch") {
                        if (!isStartingExerciseFragmentVisible()) {
                            binding.silhouetteImage.visibility = View.VISIBLE
                            binding.silhouetteImage.setImageResource(R.drawable.half_person)
                        }
                    } else if (moveController.model.name == "Relaxation breath") {
                        if (!isStartingExerciseFragmentVisible()) {
                            binding.greentored.visibility = View.GONE
                            binding.greentorednext.visibility = View.GONE
                            binding.silhouetteImage.visibility = View.GONE
                            binding.silhouetteImage.setImageResource(R.drawable.dummy_image)
                        }
                    } else if (moveController.model.name == "Forward Stretch") {
                        if (!isStartingExerciseFragmentVisible()) {
                            binding.silhouetteImage.visibility = View.GONE
                            binding.silhouetteImage.setImageResource(R.drawable.seated_person)
                        }
                    } else {
                        binding.silhouetteImage.visibility = View.VISIBLE
                        binding.silhouetteImage.setImageResource(R.drawable.half_person)
                    }
                } else if (value == 1) {
                    if (moveController.model.name == "Torso Stretch") {
                        binding.silhouetteImage.visibility = View.VISIBLE
                        binding.silhouetteImage.setImageResource(R.drawable.seated_person)
                    } else {
                        binding.silhouetteImage.visibility = View.GONE
                    }
                } else {
                    binding.silhouetteImage.visibility = View.GONE
                }

                if (moveController.model.name == "Relaxation breath") {
                    binding.greentored.visibility = View.GONE
                    binding.greentorednext.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            moveController.exerciseInstructionSF.collect { value ->
                if (value.isNotEmpty()) {
                    if (value.startsWith("Get Ready!\n0")) {
                        binding.instruction.visibility = View.VISIBLE
                        binding.pill.visibility = View.VISIBLE
                        binding.instruction.text = "Get Ready"
                    } else if (value.startsWith("Get Ready\n0")) {
                        binding.instruction.visibility = View.VISIBLE
                        binding.pill.visibility = View.VISIBLE
                        binding.instruction.text = "Get Ready"
                    } else if (value.startsWith("Get Ready")) {
                        binding.instruction.visibility = View.VISIBLE
                        binding.pill.visibility = View.VISIBLE
                        binding.instruction.text = value
                        for (i in 3 downTo 0) {
                            // if on Pause, wait indefinitely
                            waitIfPaused()
                            binding.time.text = "$i sec"
                            delay(1000L)
                        }
                    } else if (value.startsWith("Hold there")) {
                        binding.instruction.visibility = View.VISIBLE
                        binding.pill.visibility = View.VISIBLE

                        for (i in 5 downTo 0) {
                            // if on Pause, wait indefinitely
                            waitIfPaused()
                            binding.time.text = "$i sec"

                            delay(1000L)
                        }
                    } else if (value.startsWith("Stretch more")) {
                        binding.instruction.visibility = View.VISIBLE
                        binding.pill.visibility = View.VISIBLE

                        for (i in 5 downTo 0) {
                            // if on Pause, wait indefinitely
                            waitIfPaused()
                            binding.time.text = "$i sec"

                            delay(1000L)
                        }
                    } else if (value.startsWith("Breathe in")) {
                        binding.instruction.visibility = View.VISIBLE
                        binding.pill.visibility = View.VISIBLE
                        binding.instruction.text = "Breathe in"

                        for (i in 3 downTo 0) {
                            // if on Pause, wait indefinitely
                            waitIfPaused()
                            binding.time.text = "$i sec"

                            delay(1000L)
                        }
                    } else if (value.startsWith("Breathe out")) {
                        binding.instruction.visibility = View.VISIBLE
                        binding.pill.visibility = View.VISIBLE
                        binding.instruction.text = "Breathe out"

                        for (i in 3 downTo 0) {
                            // if on Pause, wait indefinitely
                            waitIfPaused()
                            binding.time.text = "$i sec"

                            delay(1000L)
                        }
                    } else if (value.startsWith("Hold breath")) {
                        delay(10L)
                        binding.instruction.visibility = View.VISIBLE
                        binding.pill.visibility = View.VISIBLE
                        binding.instruction.text = "Hold Breath"

                        for (i in 2 downTo 0) {
                            // if on Pause, wait indefinitely
                            waitIfPaused()
                            binding.time.text = "$i sec"
                            delay(1000L)
                        }
                    }
//

                    else {
                        binding.instruction.visibility = View.VISIBLE
                        binding.pill.visibility = View.GONE
                        binding.instruction.text = value
                    }
                } else {
                    binding.instruction.visibility = View.GONE
                    binding.pill.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            moveController.repetitionStatusColorSF.collect { value ->
                when (value) {
                    1 -> {
                        // color green
                        binding.pill.visibility = View.VISIBLE
                        binding.instruction.text = "Hold there"
                        binding.pill.background = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.circle_green,
                            null,
                        )
                        binding.clock.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.blackclock,
                                null,
                            ),
                        )
                    }

                    2 -> {
                        delay(250)
                        binding.pill.visibility = View.VISIBLE
                        binding.instruction.text = "Stretch more"
                        // color red
                        binding.pill.background = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.circle_red,
                            null,
                        )
                        binding.clock.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.blackclock,
                                null,
                            ),
                        )
                    }

                    else -> {
                        binding.pill.visibility = View.VISIBLE
                        binding.pill.background = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.inverse_color_circle,
                            null,
                        )
                        binding.clock.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.clock,
                                null,
                            ),
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            moveController.updateDrawnKeyPointsSF.collect { value ->
                moveController._updateDrawnKeyPointsSF.value = false
                updateDrawnKeyPoints()
            }
        }

        lifecycleScope.launch {
            moveController.displayCountersSF.collect { value ->
                if (value) {
                    binding.countersLayout.visibility = View.VISIBLE
                } else {
                    binding.pill.visibility = View.GONE
                    binding.countersLayout.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            moveController.midInstructionTimerSF.collect { value ->
                if (value.isNotEmpty() && moveController.exerciseCompletedSF.value) {
                    binding.midInstructionTimer.visibility = View.VISIBLE
                    binding.midInstructionTimer.text = value
                } else {
                    binding.midInstructionTimer.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            moveController.midInstructionMessageSF.collect { value ->
                if (value.isNotEmpty()) {
                    binding.midInstructionMessage.visibility = View.VISIBLE
                } else {
                    binding.midInstructionMessage.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            moveController.goodRepCounterSF.collect { value ->
                binding.goodRepCounterValue.text = "$value"
                viewModel.updateGoodStep(value)
            }
        }

        lifecycleScope.launch {
            moveController.badRepCounterSF.collect { value ->
                binding.badRepCounterValue.text = "$value"
                viewModel.updateBadStep(value)
            }
        }

        lifecycleScope.launch {
            moveController.currentRepCounterSF.collect { value ->
                binding.currentRepCounter.text = "$value"
            }
        }

        lifecycleScope.launch {
            moveController.totalRepCounterSF.collect { value ->
                binding.totalRepCounter.text = "$value"
            }
        }

        lifecycleScope.launch {
            moveController.exerciseCompletedSF.collect { value ->
                if (value) {
                    closeStartingExerciseFragment()
                    // StretchCompleted message
                    moveController.endView()
                    overrideExerciseData()

                    Timber.d(">> exerciseCompletedSF exercise list=$exerciseList")
                    recalculatePointBaseOnResult()
                    waitIfPaused()
                    yield()

                    // check if other exercises are left in queue and then redirect to them
                    if (moveController.exerciseCompletedSF.value) {
                        if (getNextMoveController()) {
                            moveController.resetState()
                            restartMoveActivity()
                        } else {
                            // OPEN SUMMARY PAGE

                            openExerciseSummaryFragment()
                            AudioManagerService.playGoodJobCompletingAllExercisesNarration()
                            removeCameraPreviewAndButtons()
                            yield()
//                        delay(5000)

//                        closeSummaryExerciseFragment()
//                        openLeaderboardFragment()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            moveController.didUserExitSF.collect { value ->
                if (value) {
                    closeStartingExerciseFragment()
                    AudioManagerService.stopAll()
                    AudioManagerService.exit()
                    finish()

                    // turn off camera
                    removeCameraPreviewAndButtons()
                    clearAllRepCountsForAllExercises()
                    yield()
                }
            }
        }

        lifecycleScope.launch {
            moveController.didUserSkipSF.collect { value ->
                if (value) {
                    yield()
                }
            }
        }

        lifecycleScope.launch {
            skipQueueSF.collect { value ->
                // While Skip Queue.Count > 0 & NotAtSummaryPage & Not in middle of skipping
                while (skipQueueSF.value > 0 && isNotInMiddleOfSkipping) {
                    isNotInMiddleOfSkipping = false
                    overrideExerciseData()

                    Timber.d(">> skipQueueSF exercise list: $exerciseList")

                    // if summary page is visible then UserSkip = 0
                    if (isSummaryExerciseFragmentVisible()) {
                        _skipQueueSF.value = 0
                        isNotInMiddleOfSkipping = true
                        removeCameraPreviewAndButtons()
                        clearAllRepCountsForAllExercises()
                        break
                    }

                    AudioManagerService.stopAll()

                    moveController.userSkip()

                    yield()
                    if (isStartingExerciseFragmentVisible()) {
                        yield()

                        closeStartingExerciseFragment()
                    } else if (getNextMoveController()) {
                        restartMoveActivity()
                    } else if (!getNextMoveController()) {
                        openExerciseSummaryFragment()

                        AudioManagerService.playGoodJobCompletingAllExercisesNarration()
                        removeCameraPreviewAndButtons()
                        clearAllRepCountsForAllExercises()
                        yield()
                    } else {
                        removeCameraPreviewAndButtons()
                    }

                    if (!isSummaryExerciseFragmentVisible()) {
                        moveController.userSkipReset()
                        _skipQueueSF.value -= 1
                        isNotInMiddleOfSkipping = true
                    } else {
                        moveController._didUserSkipSF.value = true
                        isNotInMiddleOfSkipping = false
                    }
                    yield()
                }
            }
        }

        // skipButtonBind
        binding.skipButton.setOnClickListener {
            _skipQueueSF.value += 1
        }

        binding.exitButton.setOnClickListener {
            lifecycleScope.launch {
                showExitConfirmation(
                    {
                        moveController.userExit()
                        finish()
                    },
                )
            }
        }
    }
    private fun recalculatePointBaseOnResult(){
        var totalPoints = 0
        val goodPoint = appStorage.getGoodReps()
        val badPoint = appStorage.getBadReps()
        exerciseList.forEach { exercise ->
            totalPoints += exercise.countBad * badPoint + exercise.countGood * goodPoint
        }
        binding.tvPoint.text = if (totalPoints == 0) {
            ""
        } else {
            buildString {
                append("\uD83C\uDFC6+")
                append(totalPoints.toString())
                append("points")
            }
        }
    }
    private fun showExitConfirmation(onConfirm: () -> Unit) {
        val alert = AlertDialog.Builder(this@MainActivity)
        alert.setTitle(getString(R.string.exit_program))
        alert.setMessage(getString(R.string.msg_confirm_exit_exercise))
        alert.setNegativeButton(getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.cancel()
            onConfirm()
        }
        alert.setPositiveButton(getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.cancel()
        }
        alert.show()
    }

    private fun overrideExerciseData() {
        // Fix the 3 rep exercises: shoulder shrug, overhead shoulder, forward stretch
        val totalReps =
            if (moveController.model.id == 14 || moveController.model.id == 8 || moveController.model.id == 3 || moveController.model.id == 10) { // meant Relaxation breath
                moveController.model.getShared().repetitions
            } else {
                moveController.model.getShared().repetitions * 2
            }
        exerciseList.firstOrNull { it.exerciseId == moveController.model.id }?.let { exercise ->
            // should update data
            exercise.apply {
                countGood = moveController.model.getShared().repetitionResults.count { it }
                countBad = moveController.model.getShared().repetitionResults.count { !it }
                completedReps = moveController.model.getShared().repetitionResults.size
                this.totalReps = totalReps
            }
        } ?: kotlin.run {
            // just add new
            exerciseList.add(
                Exercise(
                    moveController.model.id,
                    moveController.model.getShared().repetitionResults.count { it },
                    moveController.model.getShared().repetitionResults.count { !it },
                    moveController.model.getShared().repetitionResults.size,
                    totalReps,
                ),
            )
        }
    }

    private fun removeCameraPreviewAndButtons() {
        // turn off camera
        cameraSource?.close()
        cameraSource = null
        binding.skipButton.visibility = View.GONE
        binding.exitButton.visibility = View.GONE
        binding.instruction.visibility = View.GONE
        binding.surfaceView.visibility = View.GONE
    }

    private fun openExerciseStartingFragment() {
        // Open a fragment
        val arguments = Bundle()
        arguments.putString("EXERCISE_NAME", moveController.model.name)
        arguments.putString("EXERCISE_NUMBER", moveController.model.getShared().order)
        arguments.putString("EXERCISE_IMAGES_RELATIVE_PATH", moveController.model.imagesFolderPath)
        arguments.putBoolean("FIRST_INSTANCE", firstExercise)
        val exerciseStartFragment = ExerciseStartingFragment()
        exerciseStartFragment.arguments = arguments
        val ft: FragmentTransaction = fragmentManager.beginTransaction()

        ft.replace(R.id.relative_layout, exerciseStartFragment, starting_exercise_fragment_tag)
        ft.commit()
    }

    private fun openExerciseSummaryFragment() {
        Log.d("TAG", "open summary")
        Log.d("TAG", "moveList = $movesList")
        Log.d("TAG", "exerciseList, $exerciseList")

        val exerciseSummaryFragment = ExerciseSummaryFragment()

        val arguments = Bundle()
        arguments.putSerializable("moveList", movesList)
        arguments.putSerializable("exerciseList", exerciseList)
        exerciseSummaryFragment.arguments = arguments
        binding.bottomPanel.visibility = View.GONE
        binding.toolbar.visibility = View.GONE
        val ft: FragmentTransaction = fragmentManager.beginTransaction()
        // ft.replace(R.id.frame_content, exerciseSummaryFragment, summary_exercise_fragment_tag)
        ft.replace(R.id.frame_layout, exerciseSummaryFragment, summary_exercise_fragment_tag)
        ft.commit()
    }

    private suspend fun closeStartingExerciseFragment() {
        if (isStartingExerciseFragmentVisible()) {
            // CLOSE STARTING FRAGMENT IF NEED BE
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            val myFragment: ExerciseStartingFragment? =
                fragmentManager.findFragmentByTag(starting_exercise_fragment_tag) as ExerciseStartingFragment?
            if (myFragment != null) {
                fragmentTransaction.remove(myFragment)
            }
            fragmentTransaction.commit()
        }
    }

    override fun onResume() {
        cameraSource?.resume()
        super.onResume()
        // Start the button listener
        _isPausedSF.value = false
    }

    override fun onPause() {
        // Pause Audio
        AudioManagerService.stopAll()
        _isPausedSF.value = true
        cameraSource?.close()
        cameraSource = null
        super.onPause()
    }

    // check if permission is granted or not.
    private fun isCameraPermissionGranted(): Boolean {
        return checkPermission(
            Manifest.permission.CAMERA,
            Process.myPid(),
            Process.myUid(),
        ) == PackageManager.PERMISSION_GRANTED
    }

    // open camera
    private fun openCamera() {
        if (isCameraPermissionGranted()) {
            if (cameraSource == null) {
                cameraSource =
                    CameraSource(
                        surfaceView,
                        object : CameraSource.CameraSourceListener {
                            override fun onFPSListener(fps: Int) {
//                            tvFPS.text = getString(R.string.tfe_pe_tv_fps, fps)
                            }

                            override fun onDetectedInfo(
                                _person: Person,
                                poseLabels: List<Pair<String, Float>>?,
                            ) {
                                person = _person
                                moveController.onUpdatePerson(person!!)
                            }
                        },
                    ).apply {
                        prepareCamera()
                    }
                lifecycleScope.launch(Dispatchers.Main) {
                    cameraSource?.initCamera()
                }
            }
            createPoseEstimator()
            updateDrawnKeyPoints()
        }
    }

    override fun updateDrawnKeyPoints() {
        cameraSource?.updateDrawnKeyPoints(
            moveController.drawnJoints(),
            moveController.drawnJointPairs(),
            moveController.refreshRateOfKeyPointsInMs(),
            moveController.confidenceLevel(),
            myIsDrawValidFrame = moveController.model.isShowValidFrame,
            myValidFramePosition = moveController.validFramePosition()
        )
    }

    suspend fun waitIfPaused() {
        while (_isPausedSF.value) {
            yield()
        }
    }

    private fun createPoseEstimator() {
        // For MoveNet MultiPose, hide score and disable pose classifier as the model returns
        // multiple Person instances.
        val poseDetector = when (modelPos) {
            0 -> {
                // MoveNet Lightning (SinglePose)
                MoveNet.create(this, device, ModelType.Lightning)
            }

            else -> {
                null
            }
        }
        poseDetector?.let { detector ->
            cameraSource?.setDetector(detector)
        }
    }

    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA,
            ),
            -> {
                // You can use the API that requires the permission.
                openCamera()
            }

            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA,
                )
            }
        }
    }

    fun True(view: View) {}

    /**
     * Shows an error message dialog.
     */
    class ErrorDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(activity)
                .setMessage(requireArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    // do nothing
                }
                .create()

        companion object {
            @JvmStatic
            val ARG_MESSAGE = "message"

            @JvmStatic
            fun newInstance(message: String): ErrorDialog = ErrorDialog().apply {
                arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
            }
        }
    }

    companion object {
        const val FRAGMENT_DIALOG = "dialog"
        private const val DIALOGUE_POPUP = "DISPLAY_POP_UP"

        // Todo: Address the memory leak warning and refactor code
        @SuppressLint("StaticFieldLeak")
        var instance: MainActivity? = null

        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null

        lateinit var moveController: MoveController
        lateinit var moveQueue: ArrayListQueue<Move>
        lateinit var movesList: ArrayList<Move>
    }
}
