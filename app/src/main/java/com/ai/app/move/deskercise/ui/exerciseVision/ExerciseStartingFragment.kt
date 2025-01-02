package com.ai.app.move.deskercise.ui.exerciseVision

import android.content.res.AssetManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseFragment
import com.ai.app.move.deskercise.services.Commons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import timber.log.Timber

/**
 * ExerciseStartingFragment contains the logic to handle the introduction screen before the exercise begins
 */

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val EXERCISE_NAME_PARAM = "EXERCISE_NAME"
private const val EXERCISE_IMAGES_RELATIVE_PATH_PARAM = "EXERCISE_IMAGES_RELATIVE_PATH"
private const val EXERCISE_NUMBER_PARAM = "EXERCISE_NUMBER"
private const val EXERCISE_SETS = "HI"
private const val FIRST_INSTANCE_PARAM = "FIRST_INSTANCE"
private const val DIALOGUE_POPUP = "DISPLAY_POP_UP"

class ExerciseStartingFragment : BaseFragment() {
    private var exerciseName: String? = null
    private var exerciseNumber: String? = null
    private var exerciseImageFolderRelativePath: String? = null
    private var sets: String? = null
    private lateinit var fragmentView: View
    var firstExercise: Boolean? = false
    private var isPaused: Boolean = false
    private val popUp = ReminderFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            // e.g. exerciseName = "Overhead Shoulder Stretch"
            exerciseName = it.getString(EXERCISE_NAME_PARAM)
            exerciseNumber = it.getString(EXERCISE_NUMBER_PARAM)
            sets = it.getString(EXERCISE_SETS)
            // e.g. exerciseImageFolderRelativePath = "Images/OverheadShoulderGif"
            exerciseImageFolderRelativePath = it.getString(EXERCISE_IMAGES_RELATIVE_PATH_PARAM)
            firstExercise = it.getBoolean(FIRST_INSTANCE_PARAM)
            Timber.d(">> bundle $it")
        }
    }

    override fun onStart() {
        super.onStart()
        isPaused = false

        if (firstExercise == true && !popUp.isAdded) {
            popUp.show((activity as AppCompatActivity).supportFragmentManager, DIALOGUE_POPUP)
        }

        val textView =
            fragmentView.findViewById<View>(R.id.starting_instruction_exercise_name) as TextView
        textView.text = exerciseName

        val number =
            fragmentView.findViewById<View>(R.id.starting_instruction_exercise_number) as TextView
        number.text = "0$exerciseNumber"
        val exset = fragmentView.findViewById<View>(R.id.sets) as TextView
        if (exerciseName?.startsWith("Diaphr") == true || exerciseName?.startsWith("Relaxa") == true) {
            exset.text = "2 sets"
            val exerciseType = fragmentView.findViewById<View>(R.id.exercise_type) as TextView
            exerciseType.text = "Cool down "
        } else {
            exset.text = "3 sets"
        }
        exerciseImageFolderRelativePath?.let { displayGif(it) }
    }

    private fun displayGif(relative_folder_path_string: String) {
        lifecycleScope.launch {
            val imageView = fragmentView.findViewById<View>(R.id.starting_image_view) as ImageView
            val assetManager: AssetManager = resources.assets
            Commons.displayGifOnImageView(
                relative_folder_path_string,
                imageView,
                assetManager,
                Commons.Direction.LEFT,
                this@ExerciseStartingFragment,
            )

            val countdownTimerTxtView =
                fragmentView.findViewById(R.id.starting_instruction_exercise_countdown_timer) as TextView
            var timer = 5

            if (firstExercise == true) {
                timer = 20
            }

            for (i in timer downTo 0) {
                // if on Pause, wait indefinitely

                countdownTimerTxtView.text = "$i sec"
                delay(1000L)
            }
            if (popUp.isVisible) {
                // Make sure dismiss isn't already called as it will cause an exception.
                popUp.dismissNow()
            }
            Timber.d(">> count down end")
            requireActivity().supportFragmentManager.beginTransaction().remove(this@ExerciseStartingFragment).commitAllowingStateLoss()
        }
    }

    suspend fun waitIfPaused() {
        while (isPaused) {
            yield()
        }
    }

    override fun onStop() {
        super.onStop()
        isPaused = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_exercise_starting, container, false)
        return fragmentView
    }
}
