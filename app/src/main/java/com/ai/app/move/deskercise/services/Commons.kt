package com.ai.app.move.deskercise.services

import android.content.res.AssetManager
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.ui.exerciseVision.ExerciseStartingFragment
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Contains all the Global Settings based on the email of the user using the application at the moment
 */
internal class GlobalSettings {
    companion object {

        // / Gets the cached default confidence value to be used to detect `Hand Poses` from `Vision`.
        internal fun getDefaultConfidence(): Float {
//            val defaults = UserDefaults.standard
//            val defaultConfidence = defaults.float(forKey = getDefaultEmail() + "ConfidenceValue")
            val defaultConfidence =
                0F // Todo: Change Logic to use SHAREDPREF similar to IOS when backend is implemented
            return  0.3f
        }

        // Used in the CommonClassifier during the startingStretch check
        internal fun getDefaultCommonClassifierConfidence(): Float =
            0.3F

        internal fun getRefreshRateOfKeyPointsInMs(): Long = 50L
    }
}

// / General rule of thumb is to keep Text-To-Speech to a maximum of 1 sentence.
class TextToSpeechText {
    // Todo: If multiple languages need in the future, consider moving constant strings to strings.xml
    companion object {
        // Welcome Message - played during preview video
        internal const val welcomeMessageDbreathing : String = "Relaxation breath"
        internal const val welcomeMessageOverheadTricepsStretch: String = "Overhead Triceps Stretch"
        internal const val welcomeMessagePiriformisStretch: String = "Piriformis Stretch"
        internal const val welcomeMessageHorizontalShoulder: String = "Horizontal shoulder stretch"
        internal const val welcomeMessageSideStretch: String = "Side Stretch"
        internal const val welcomeMessageTorsoStretch: String = "Torso Stretch"
        internal const val welcomeMessageOverheadShoulderStretch: String =
            "Overhead Shoulder Stretch"
        internal const val welcomeMessageNeckStretch: String = "Neck Stretch"
        internal const val welcomeMessageShoulderShrugStretch: String = "Shoulder Shrug Stretch"
        internal const val welcomeMessageForwardStretch: String = "Forward Stretch"
        internal const val welcomeMessageUpperTrapStretch: String = "Upper Trap Stretch"

        // General Instructions
        internal const val bendForwardAtHips = "Keep back straight and bend forward at the hips"
        internal const val bendYourBodyToRight: String = "Bend your body to the right"
        internal const val bendYourBodyToLeft: String = "Bend your body to the left"
        internal const val returnToYourStarting: String = "Return to your starting position"
        internal const val sideStretchRepetitionCompletedState: String = "Return to your starting position"
        internal const val seatedCalibrationWithArmsAndHeadspace: String =
            "While seated, ensure that your arms are visible and that there is ample space above your head"
        internal const val seatedCalibrationForwardStretch: String =
            "While seated, face 90 degrees to your right and adjust your position such that your body, arms and hips can be seen in the frame"
        internal const val startForwardStretch: String =
            "Raise your arms to shoulder level."
        internal const val seatedCalibrationWithArmsAndElbows: String =
            "While seated, ensure that your face and arms are visible"
        internal const val seatedCalibrationWithWristAndHeadspace: String =
            "While seated, adjust your position such that your face, shoulders, elbows and wrist are clearly visible in the frame"
        internal const val moveBackwards: String = "Move backwards so your body is in the frame"
        internal const val lockFingersTogetherAndPlaceThemOnHead: String =
            "Lock your fingers together and put your hands on your head"
        internal const val armsRelaxedBySide: String = "Leave your arms relaxed by your side"
        internal const val upwardsArmStretchWithHandsLockedTogether: String =
            "With your hands locked together, stretch your arms upwards"
        internal const val repetitionInitialForwardStretch: String =
            "Tuck your chin in and round your back"
        internal const val shoulderShrugStretch: String =
            "Raise both your shoulders at once up toward your ears"
        internal const val exerciseIsStarting: String = "Please Standby, Exercise starting"
        internal const val seatedCalibrationWithFaceShoulderElbow: String =
            "While seated, adjust your position such that your face, shoulders and elbows are in the frame"
        internal const val leanHeadAndRollHeadToRight: String =
            "Tilt your head to the right "
        internal const val rollHeadToLeft: String = "Tilt your head to the left "
        internal const val lookStraightAndKeepArmsAtSide: String =
            "Look straight and keep your arms by your side."
        internal const val seatedCalibrationHeadShouldersKnees: String =
            "While seated, adjust your position such that your head, arms and knees are clearly visible in the frame"
        internal const val turnToRightSide: String = "Turn ninety degrees to the right"
        internal const val turnToLeftSide: String = "Turn ninety degrees to the left"

        internal const val stretchMoreWithRelax: String = "Try to stretch more! Now relax."
        internal const val good: String = "Good"
        internal const val goodWithRelax: String = "Good! Now relax"
        internal const val goodJobWithRelax: String = "Good job! Now relax"
        internal const val goodExercise: String = "Good job on completing the exercise"
        internal const val badExercise: String = "Exercise completed, try harder next time"
        internal const val goodJobCompletingAllExercises: String =
            "Good job on completing all exercises! Here is a summary of your results."
        internal const val goodJobAndReturnToStartPosition: String =
            "Good job! Now return to your starting position"
        internal const val stretchMoreAndReturnToStartPosition: String =
            "Try to stretch more! Now return to your starting position"

        // Dbreathing Audio Instructions
        internal const val dbreathingRepetitionState : String = "Please sit upright"
        internal const val dbreathingRepetitionInitialStateRightSide : String = "Breathe in"
        internal const val dbreathingRepetitionInitialStateLeftSide : String = "Breathe out"
        internal const val dbreathingRepetitionCompletedState : String = "Please stay in frame"
        internal const val dbreathingEndState : String = "Relaxation breath Completed"

        // Horizontal Shoulder Stretch Audio Instruction
        internal const val hshoulderRepetitionInitialStateRightSide: String =
            "Straighten your right arm and pull it across your chest with your left hand"
        internal const val hshoulderRepetitionInitialStateLeftSide: String =
            "Straighten your left arm and pull it across your chest with your right hand"

        // Side Stretch Audio Instruction
        internal const val sideStretchRepetitionInitialStateRightSide: String =
            "Straighten your right arm above your head and bend to the left"
        internal const val sideStretchRepetitionInitialStateLeftSide: String =
            "Straighten your left arm above your head and bend to the right"

        internal const val overheadTricepsStartStateRightSide : String =
            "Rest your right palm on your back and point your elbow to the ceiling"
        internal const val overheadTricepsStartStateLeftSide : String =
            "Rest your left palm on your back and point your elbow to the ceiling"
        internal const val overheadTricepsStartStateTwoRightSide : String =
            "Place your left hand on your right elbow"
        internal const val overheadTricepsStartStateTwoLeftSide : String =
            "Place your right hand on your left elbow"
        internal const val overheadTricepsRepetitionInitialStateRightSide: String =
            "Pull your right elbow towards the back of your head"
        internal const val overheadTricepsRepetitionInitialStateLeftSide: String =
            "Pull your left elbow towards the back of your head"

        // Torso Stretch Audio Instructions
        internal const val torsoStretchRepetitionInitialStateRightSide: String =
            "Twist your upper body towards the left, and look over your shoulder"
        internal const val torsoStretchRepetitionInitialStateLeftSide: String =
            "Twist your upper body towards the right, and look over your shoulder"
        internal const val torsoStretchStartStateRightSide: String =
            "Cross your left leg over your right leg"
        internal const val torsoStretchStartStateLeftSide: String =
            "Cross your right leg over your left leg"

        // UpperTrap Stretch Audio Instructions
        internal const val upperTrapStretchCalibrationState: String =
            "While seated, adjust your position such that your face, shoulders and elbows are clearly visible in the frame"
        internal const val upperTrapStretchStartStateRightSide: String =
            "Place your right hand on the top of your head"
        internal const val upperTrapStretchStartStateLeftSide: String =
            "Place your left hand on the top of your head"
        internal const val upperTrapStretchRepetitionInitialStateRightSide: String =
            "Pull your head towards your right shoulder"
        internal const val upperTrapStretchRepetitionInitialStateLeftSide: String =
            "Pull your head towards your left shoulder"
        internal const val upperTrapStretchRepetitionCompletedState: String =
            "Return to your starting position"
        internal const val piriformisStretchCalibrationState: String =
            "While seated, move backwards so your shoulders and legs are visible in the frame"
    }
}

class UIInstructionText {
    // Todo: If multiple languages need in the future, consider moving constant strings to strings.xml
    companion object {
        internal const val seatedCalibrationWithFaceShoulderElbows: String =
            "While seated, adjust your position such that your face, shoulders and elbows are clearly visible in the frame"
        internal const val seatedCalibrationWithFaceShoulderElbowWrist: String =
            "While seated, adjust your position such that your face, shoulders, elbows and wrist are clearly visible in the frame"
        internal const val seatedCalibrationWithArmsAndHeadspace: String =
            "While seated, ensure that your arms are visible and that there is ample space above your head"
        internal const val moveBackwards: String = "Move backwards"
        internal const val bendForwardAtHips = "Keep back straight and bend forward at the hips"
        internal const val returnToYourStarting: String = "Return to your starting position"
        internal const val sideStretchRepetitionCompletedState: String = "Return to your starting position"
        internal const val startingStretch: String = "Starting exercise..."
        internal const val getReady: String = "Get Ready!"
        internal const val holdThere: String = "Hold there"
        internal const val stretchMore: String = "Stretch more!"
        internal const val goodJobNowRelax: String = "Good job! Now relax!"
        internal const val stretchMoreWithRelax: String = "Try to stretch more! Now relax."
        internal const val tryHarderNextTimeNowRelax: String = "Try harder next time! Now relax!"
        internal const val stretchCompleted: String = "Exercise Completed!"

        internal const val good: String = "Good!"
        internal const val tryToStretchMore: String = "Try to stretch more!"

        internal const val dbreathingName : String = "Relaxation breath"
        internal const val dbreathingInitialState : String = "Breathing exercise"
        internal const val dbreathingRepetitionState: String = "Sit upright"
        internal const val dbreathingRepetitionInitialStateRightSide : String = "Breathe in"
        internal const val dbreathingRepetitionInitialStateLeftSide : String = "Breathe out"
        internal const val dbreathingRepetitionCompletedState : String = "Please stay in frame"
        internal const val dbreathingEndState : String = "Relaxation breath Completed"


        internal const val hshoulderRepetitionInitialStateRightSide: String =
            "Straighten your right arm and pull it across your chest with left hand"
        internal const val hshoulderRepetitionInitialStateLeftSide: String =
            "Straighten your left arm and pull it across your chest with right hand"


        internal const val overheadTricepsStartStateRightSide : String =
            "Rest your right palm on your back and point your elbow to the ceiling"
        internal const val overheadTricepsStartStateLeftSide : String =
            "Rest your left palm on your back and point your elbow to the ceiling"
        internal const val overheadTricepsStartStateTwoRightSide : String =
            "Place your left hand on your right elbow"
        internal const val overheadTricepsStartStateTwoLeftSide : String =
            "Place your right hand on your left elbow"
        internal const val overheadTricepsRepetitionInitialStateRightSide: String =
            "Pull your right elbow towards the back of your head"
        internal const val overheadTricepsRepetitionInitialStateLeftSide: String =
            "Pull your left elbow towards the back of your head"

        internal const val forwardStretchCalibrationState: String =
            "Turn right and move backwards"
        internal const val forwardStretchStartState: String =
            "Raise your arms to shoulder level"
        internal const val forwardStretchRepetitionInitialState: String =
            "Tuck your chin in and round your back"

        internal const val neckStretchStartState: String =
            "Look straight and keep your elbows to your side"
        internal const val neckStretchRepetitionInitialStateRightSide: String =
            "Tilt your head to the right"
        internal const val neckStretchRepetitionInitialStateLeftSide: String =
            "Tilt your head to the left"
        internal const val neckStretchRepetitionCompletedState: String =
            "Return to your starting position"

        internal const val overheadShoulderStretchCalibrationState: String =
            "While seated, move backwards such that there is ample space above your head"
        internal const val overheadShoulderStretchStartState: String =
            "Lock your fingers together and put your hands on your head"
        internal const val overheadShoulderStretchRepetitionInitialState: String =
            "With your hands locked together, stretch your arms upwards"
        internal const val overheadShoulderStretchRepetitionCompletedStateGoodRep: String =
            "Good job! Now bring your hands back to your head"
        internal const val overheadShoulderStretchRepetitionCompletedStateBadRep: String =
            "Try harder next time! Now bring your hands back to your head"

        internal const val shoulderShrugStretchStartState: String = "Relax your arms by your side"
        internal const val shoulderShrugStretchRepetitionInitialState: String =
            "Raise both your shoulders at once up towards your ears"
        internal const val shoulderShrugStretchRepetitionCompletedStateGoodRep: String =
            "Return to your starting position"
        internal const val shoulderShrugStretchRepetitionCompletedStateBadRep: String =
            "Return to your starting position"

        internal const val sideStretchRepetitionInitialStateRightSide: String =
            "Straighten your right arm above your head and bend to the left"
        internal const val sideStretchRepetitionInitialStateLeftSide: String =
            "Straighten your left arm above your head and bend to the right"

        internal const val turnToRightSide: String = "Turn ninety degrees to the right"
        internal const val turnToLeftSide: String = "Turn ninety degrees to the left"
        internal const val torsoStretchRepetitionInitialStateRightSide: String =
            "Twist your upper body towards the left"
        internal const val torsoStretchRepetitionInitialStateLeftSide: String =
            "Twist your upper body towards the right"
        internal const val torsoStretchStartStateRightSide: String =
            "Cross your left leg over your right leg"
        internal const val torsoStretchStartStateLeftSide: String =
            "Cross your right leg over your left leg"
        internal const val torsoStretchRepetitionCompletedState: String =
            "Return to your starting position"

        internal const val upperTrapStretchStartStateRightSide: String =
            "Place your right hand on the top of your head"
        internal const val upperTrapStretchStartStateLeftSide: String =
            "Place your left hand on the top of your head"
        internal const val upperTrapStretchRepetitionInitialStateRightSide: String =
            "Pull your head towards your right shoulder"
        internal const val upperTrapStretchRepetitionInitialStateLeftSide: String =
            "Pull your head towards your left shoulder"
        internal const val upperTrapStretchRepetitionCompletedState: String =
            "Return to your starting position"
    }
}

class Commons {
    enum class Direction {
        RIGHT,
        LEFT,
    }

    companion object {

        fun playGoodOrBadExerciseAudio(goodReps: Float, totalReps: Float) {
            val fraction: Float = goodReps / totalReps
            if (fraction < 0.5) {
                AudioManagerService.speakText(text = TextToSpeechText.badExercise)
            } else {
                AudioManagerService.speakText(text = TextToSpeechText.goodExercise)
            }
        }

        fun mirrorGifOnImageView(gifImageView: ImageView, direction: Direction = Direction.RIGHT) {
            if (direction == Direction.LEFT) {
                // mirror based on direction
                gifImageView.scaleX = 1.0F
            } else {
                gifImageView.scaleX = -1.0F
            }
        }

        fun displayGifOnImageView(
            relative_folder_path_string: String,
            gifImageView: ImageView,
            resources_assets: AssetManager,
            direction: Direction = Direction.LEFT,
            exerciseStartingFragment: ExerciseStartingFragment,
        ) {
            var relative_folder_path = relative_folder_path_string
            if (relative_folder_path == null || relative_folder_path.filter { !it.isWhitespace() } == "") {
                return
            }

            // Added as android version 8 had issues with assetManager if path ended with backslash
            // Removes Backslash at the end
            if (relative_folder_path.endsWith("/")) {
                relative_folder_path = relative_folder_path.dropLast(1)
            }

            val assetManager: AssetManager = resources_assets

            val animationImagesFolderPath: Array<out String>? =
                assetManager.list(relative_folder_path)
            if (animationImagesFolderPath != null) {
                when {
                    animationImagesFolderPath[0].startsWith("Torso") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.torso)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("Side") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.ss)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("UpperTrap") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.trap)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("Diaphramatic") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.dbreath)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("OverheadShoulder") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.overheadshoulder)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("OverheadTricep") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.overheadtricep)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("HorizontalShoulder") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.horizontalshoulder)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("Forward") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.forward)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("ShoulderShrug") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.shouldershrug)
                            .into(gifImageView)
                    }
                    else -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.neck)
                            .into(gifImageView)
                    }
                }
            }
        }

        @ExperimentalCoroutinesApi
        fun displayGifOnImageView(
            relative_folder_path_string: String,
            gifImageView: ImageView,
            resources_assets: AssetManager,
            direction: Direction = Direction.LEFT,
            exerciseStartingFragment: MainActivity,
        ) {
            var relative_folder_path = relative_folder_path_string
            if (relative_folder_path == null || relative_folder_path.filter { !it.isWhitespace() } == "") {
                return
            }

            // Added as android version 8 had issues with assetManager if path ended with backslash
            // Removes Backslash at the end
            if (relative_folder_path.endsWith("/")) {
                relative_folder_path = relative_folder_path.dropLast(1)
            }

            val assetManager: AssetManager = resources_assets

            val animationImagesFolderPath: Array<out String>? =
                assetManager.list(relative_folder_path)

//            val path = Paths.get("").toAbsolutePath().toString()
//            println("Working Directory = $path")

            if (animationImagesFolderPath != null) {
                when {
                    animationImagesFolderPath[0].startsWith("Torso") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.torso)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("Side") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.ss)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("UpperTrap") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.trap)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("Diaphramatic") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.dbreath)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("OverheadShoulder") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.overheadshoulder)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("OverheadTricep") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.overheadtricep)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("HorizontalShoulder") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.horizontalshoulder)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("Forward") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.forward)
                            .into(gifImageView)
                    }
                    animationImagesFolderPath[0].startsWith("ShoulderShrug") -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.shouldershrug)
                            .into(gifImageView)
                    }
                    else -> {
                        Glide.with(exerciseStartingFragment)
                            .load(R.drawable.neck)
                            .into(gifImageView)
                    }
                }
            }
        }

        fun crossFade(vw: View) {
            vw.apply {
                // Set the content view to 0% opacity but visible, so that it is visible
                // (but fully transparent) during the animation.
                alpha = 0f
                visibility = View.VISIBLE

                val shortAnimationDuration =
                    resources.getInteger(android.R.integer.config_shortAnimTime)

                // Animate the content view to 100% opacity, and clear any animation
                // listener set on the view.
                animate()
                    .alpha(1f)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(null)
            }
        }

        // / Populate scores of the KeyPoints based on the joints requested
        fun getKeyPointsAboveConfidenceThreshold(
            observation: List<KeyPoint>,
            joints: List<BodyPart>,
        ): MutableList<KeyPoint> {
            val points: MutableList<KeyPoint> = mutableListOf()

            for (joint in joints) {
                var point: KeyPoint?

                try {
                    point = observation.first { it.bodyPart == joint }
                } catch (e: Exception) {
                    continue
                }

                if (point == null) {
                    continue
                }

                if (point.score > GlobalSettings.getDefaultConfidence()) {
                    points.add(point)
                }
            }

            return points
        }
    }
}
