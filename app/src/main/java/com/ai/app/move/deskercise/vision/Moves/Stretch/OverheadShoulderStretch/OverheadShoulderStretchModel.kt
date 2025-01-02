package com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch

import android.os.Build
import androidx.annotation.RequiresApi
import com.ai.app.move.deskercise.BuildConfig
import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.services.Commons.Companion.playGoodOrBadExerciseAudio
import com.ai.app.move.deskercise.services.TextToSpeechText
import com.ai.app.move.deskercise.services.UIInstructionText
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveState
import com.ai.app.move.deskercise.vision.Moves.Stretch.Stretch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The model file contains variables and functions that the controller will manipulate and utilize
 */

open class OverheadShoulderStretchModel : Stretch(
    listOf(
        InitialState(),
        CalibrationState(),
        StartState(),
        InPositionState(),
        RepetitionState(),
        RepetitionInitialState(),
        RepetitionInProgressState(),
        RepetitionCompletedState(),
        ExerciseEndState(),
    ),
    3,
    "Overhead Shoulder Stretch",
    1,
    duration,
    "Images/OverheadShoulder",
    thisIsShowValidFrame = BuildConfig.DRAW_VALID_BOX
) {

    override var remainingRepetitions: Int = repetitions
    override var remainingDuration: Float = duration
    override var startTimeLeft: Float = 3F
    override var repetitionDurationLeft: Float = duration
    override var firstRepetition: Boolean = true
    override var lastRepetition: Boolean = false

    override var currentLeftAngle: Double = 0.0
    override var currentRightAngle: Double = 0.0

    override var liveLeftAngle: Double = 0.0
    override var liveRightAngle: Double = 0.0

    override var repetitionIsGood = false
    override var repetitionResults: MutableList<Boolean> = mutableListOf()
    override var currentRepetitionGood: Boolean = false

    companion object {
        var shared: OverheadShoulderStretchModel = OverheadShoulderStretchModel()
        var goodAngleThreshold: Double = 140.0
        var repetitions: Int = 3
        var duration: Float = 5.1F
        var bufferInterval: Double = 3.0

        var _messageSF = MutableStateFlow("")
        val messageSF = _messageSF.asStateFlow()

        var _repetitionStatusColorModelSF =
            MutableStateFlow(0) // 0 - default color, 1 - green color, 2 - red color
        val repetitionStatusColorModelSF = _repetitionStatusColorModelSF.asStateFlow()

        var _goodRepCounterModelSF = MutableStateFlow(0)
        val goodRepCounterModelSF = _goodRepCounterModelSF.asStateFlow()

        var _badRepCounterModelSF = MutableStateFlow(0)
        val badRepCounterModelSF = _badRepCounterModelSF.asStateFlow()

        var _currentRepCounterModelSF = MutableStateFlow(0)
        val currentRepCounterModelSF = _currentRepCounterModelSF.asStateFlow()

        var _totalRepCounterModelSF = MutableStateFlow(0)
        val totalRepCounterModelSF = _totalRepCounterModelSF.asStateFlow()

        fun resetSharedStateMachine() {
            shared = OverheadShoulderStretchModel()
        }

        lateinit var states: List<MoveState>
    }

    init {
        states = super.move_states
    }

    override fun resetStateMachine() {
        resetSharedStateMachine()
    }

    override fun getShared(): Move = shared

    override fun welcomeMessage() {
        AudioManagerService.speakText(text = TextToSpeechText.welcomeMessageOverheadShoulderStretch)
    }

    class InitialState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is CalibrationState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            _messageSF.value = UIInstructionText.startingStretch
        }

        override val description: String
            get() = "OverheadShoulderStretchInitial"
    }

    // State for calibration
    class CalibrationState : MoveState() {
        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is StartState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            _messageSF.value = UIInstructionText.overheadShoulderStretchCalibrationState
            AudioManagerService.speakText(TextToSpeechText.seatedCalibrationWithArmsAndHeadspace)
        }

        override val description: String
            get() = "OverheadShoulderStretchCalibration"
    }

    internal class InPositionState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            _messageSF.value = UIInstructionText.getReady

            if (shared.firstRepetition) {
                AudioManagerService.speakText(text = TextToSpeechText.exerciseIsStarting)
            }
        }

        override val description: String
            get() = "OverheadShoulderStretchInPosition"
    }

    // Prompt to get into starting position
    internal class StartState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is InPositionState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            _messageSF.value = UIInstructionText.overheadShoulderStretchStartState
            AudioManagerService.speakText(text = TextToSpeechText.lockFingersTogetherAndPlaceThemOnHead)
        }

        override val description: String
            get() = "OverheadShoulderStretchStart"
    }

    // Hands are in position on the head
    internal class RepetitionState : MoveState() {
        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionInitialState -> true
                is ExerciseEndState -> true
                else -> false
            }
        }

        override val description: String
            get() = "OverheadShoulderStretchRepetition"
    }

    // Hands have exceeded the predefined threshold
    internal class RepetitionInitialState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionInProgressState -> true
                is ExerciseEndState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            _messageSF.value = UIInstructionText.overheadShoulderStretchRepetitionInitialState
            AudioManagerService.speakText(text = TextToSpeechText.upwardsArmStretchWithHandsLockedTogether)
        }

        override val description: String
            get() = "OverheadShoulderStretchRepetitionInitial"
    }

    internal class RepetitionInProgressState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionCompletedState -> true
                else -> false
            }
        }

        fun updateInstructions(result: Boolean, remainingTime: Long) {
            if (result) {
                _messageSF.value =
                    "${UIInstructionText.stretchMore}\n${((remainingTime) / 1000L).toInt()}"
                _repetitionStatusColorModelSF.value = 1
            } else {
                _messageSF.value =
                    "${UIInstructionText.stretchMore}\n${((remainingTime) / 1000L).toInt()}"
                _repetitionStatusColorModelSF.value = 2
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun didEnter(from: Any?) {
            AudioManagerService.playStretchGoalReachedSFX()
        }

        override val description: String
            get() = "OverheadShoulderStretchInProgress"
    }

    class RepetitionCompletedState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionState -> true
                is StartState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            val model = shared
            model.currentRepetitionGood = (model.goodFrames > model.badFrames)
            _messageSF.value =
                UIInstructionText.sideStretchRepetitionCompletedState
            if (shared.remainingRepetitions > 0) {
                if (model.currentRepetitionGood) {
                    AudioManagerService.speakText(text = TextToSpeechText.goodJobAndReturnToStartPosition)
                    model.currentGoodCount += 1
                    goodReps += 1
                    totalReps += 1
                    _goodRepCounterModelSF.value = model.currentGoodCount
                    _currentRepCounterModelSF.value = totalReps.toInt()
                } else {
                    AudioManagerService.speakText(text = TextToSpeechText.stretchMoreAndReturnToStartPosition)
                    model.currentBadCount += 1
                    totalReps += 1
                    _badRepCounterModelSF.value = model.currentBadCount
                    _currentRepCounterModelSF.value = totalReps.toInt()
                }
            }
            model.repetitionResults.add(model.currentRepetitionGood)
            model.currentRepetitionGood = false
            model.goodFrames = 0
            model.badFrames = 0
            shared.remainingRepetitions -= 1
            if (shared.remainingRepetitions == 0) {
                shared.lastRepetition = true
            }
        }

        override val description: String
            get() = "OverheadShoulderStretchCompleted"
    }

    internal class ExerciseEndState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            AudioManagerService.playStretchCompletionSFX()
            playGoodOrBadExerciseAudio(goodReps, totalReps)
        }

        override val description: String
            get() = "OverheadShoulderStretchEnd"
    }
}
