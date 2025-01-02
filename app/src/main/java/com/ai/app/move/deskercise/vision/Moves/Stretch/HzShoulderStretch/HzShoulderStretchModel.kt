package com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch

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

open class HzShoulderStretchModel : Stretch(
    listOf(
        InitialState(),
        CalibrationState(),
        StartState(),
        InPositionState(),
        RepetitionState(),
        RepetitionInitialState(),
        RepetitionInProgressState(),
        RepetitionCompletedState(),
        EndState(),
    ),
    thisId = 13,
    thisName = "Horizontal Shoulder Stretch",
    thisType = 1,
    thisDuration = repetitionDuration,
    "Images/HorizontalShoulderStretch",
    thisIsShowValidFrame =  true//BuildConfig.DRAW_VALID_BOX
) {

    companion object {
        var shared: HzShoulderStretchModel = HzShoulderStretchModel()
        var repetitions: Int = 3
        var duration: Float = 5.1F

        var repetitionDuration: Float = 5.1F

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
            shared = HzShoulderStretchModel()
        }

        lateinit var states: List<MoveState>
    }

    override var remainingRepetitions: Int = repetitions
    override var remainingDuration: Float = duration
    override var startTimeLeft: Float = 3F
    override var repetitionDurationLeft: Float = duration
    override var firstRepetition: Boolean = true
    override var lastRepetition: Boolean = false
    override var isSet: Boolean = false

    override var repetitionIsGood = false
    override var leftRepetitionResults: MutableList<Boolean> = mutableListOf()
    override var rightRepetitionResults: MutableList<Boolean> = mutableListOf()
    override var currentRepetitionGood: Boolean = false

    override var updateCount: Int = 0

    init {
        states = super.move_states
    }

    override fun resetStateMachine() {
        resetSharedStateMachine()
    }

    override fun getShared(): Move = shared

    override fun welcomeMessage() {
//        AudioManagerService.speakText("Horizontal shoulder stretch")
        AudioManagerService.speakText(TextToSpeechText.welcomeMessageHorizontalShoulder)
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
            get() = "HzShoulderStretchInitial"
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
//            _messageSF.value =
//                "While seated, adjust your position such that your face, shoulders, elbows and wrist are clearly visible in the frame"
//
//            AudioManagerService.speakText(
//                "While seated, adjust your position such that your face, shoulders, elbows and wrist are clearly visible in the frame"
//            )
            _messageSF.value = UIInstructionText.seatedCalibrationWithFaceShoulderElbowWrist

            AudioManagerService.speakText(
                TextToSpeechText.seatedCalibrationWithWristAndHeadspace
            )
        }

        override val description: String
            get() = "HzShoulderStretchCalibration"
    }

    internal class StartState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is InPositionState -> true
                is RepetitionInitialState -> true
                is RepetitionState -> true
                is RepetitionCompletedState -> true
                else -> false
            }
        }

        override val description: String
            get() = "HzShoulderStretchStart"
    }

    internal class InPositionState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is StartState -> true
                is InitialState -> true
                is RepetitionState -> true
                is RepetitionInitialState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            if (shared.firstRepetition) {
                _messageSF.value = UIInstructionText.getReady
                AudioManagerService.speakText(
                    text = TextToSpeechText.exerciseIsStarting,
                    speakTillComplete = false,
                )
            }
        }

        override val description: String
            get() = "HzShoulderStretchInPosition"
    }

    internal class RepetitionState : MoveState() {
        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InPositionState -> true
                is RepetitionInitialState -> true
                is RepetitionCompletedState -> true
                is EndState -> true
                is StartState -> true
                else -> false
            }
        }

        override val description: String
            get() = "HzShoulderStretchRepetition"
    }

    internal class RepetitionInitialState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is InPositionState -> true
                is RepetitionInProgressState -> true
                is StartState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            if (shared.currentSide == Side.right) {
                AudioManagerService.speakText(
                    text = TextToSpeechText.hshoulderRepetitionInitialStateRightSide,
                    speakTillComplete = true,
                )
               _messageSF.value = UIInstructionText.hshoulderRepetitionInitialStateRightSide
            } else {
                AudioManagerService.speakText(
                    text = TextToSpeechText.hshoulderRepetitionInitialStateLeftSide,
                    speakTillComplete = true,
                )
              _messageSF.value = UIInstructionText.hshoulderRepetitionInitialStateLeftSide
            }
        }

        override val description: String
            get() = "HzShoulderStretchRepetitionInitial"
    }

    internal class RepetitionInProgressState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionCompletedState -> true
                is StartState -> true
                else -> false
            }
        }

        fun updateInstructions(result: Boolean, remainingTime: Long) {
            if (result) {
                _messageSF.value =
                    "${UIInstructionText.holdThere}\n${((remainingTime) / 1000L).toInt()}"
                _repetitionStatusColorModelSF.value = 1
            } else {
                _messageSF.value =
                    "${UIInstructionText.stretchMore}\n${((remainingTime) / 1000L).toInt()}"
                _repetitionStatusColorModelSF.value = 2
            }
        }

        override fun didEnter(from: Any?) {
            AudioManagerService.playStretchGoalReachedSFX()
            val model = shared
//            model.repetitionDurationRemaining = HzShoulderStretchModel.repetitionDuration
        }

        override val description: String
            get() = "HzShoulderStretchRepetitionInProgress"
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

        fun updateInstructions() {
            AudioManagerService.speakText(text = TextToSpeechText.sideStretchRepetitionCompletedState)
            _messageSF.value = UIInstructionText.sideStretchRepetitionCompletedState
        }

        override fun didEnter(from: Any?) {
            val model = shared
            model.currentRepetitionGood = (model.goodFrames > model.badFrames)
            totalReps += 1

            if (model.currentRepetitionGood) {
                AudioManagerService.speakText(text = TextToSpeechText.goodJobWithRelax)
                _messageSF.value = UIInstructionText.goodJobNowRelax
                model.currentGoodCount += 1
                goodReps += 1
            } else {
                AudioManagerService.speakText(text = TextToSpeechText.stretchMoreWithRelax)
                _messageSF.value = UIInstructionText.stretchMoreWithRelax
                model.currentBadCount += 1
            }

            if (model.getShared().currentSide == Side.right) {
                model.rightRepetitionResults.add(shared.currentRepetitionGood)
                model.getShared().repetitionResults.add(shared.currentRepetitionGood)
            } else {
                model.getShared().leftRepetitionResults.add(shared.currentRepetitionGood)
                model.getShared().repetitionResults.add(shared.currentRepetitionGood)
            }
            // reset for next rep

            model.currentRepetitionGood = false
            model.goodFrames = 0
            model.badFrames = 0
        }

        override val description: String
            get() = "HzShoulderStretchRepetitionCompleted"
    }

    internal class EndState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionState -> true
                is StartState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            AudioManagerService.playStretchCompletionSFX()
            playGoodOrBadExerciseAudio(goodReps, totalReps)
        }

        override val description: String
            get() = "HzShoulderStretchEnd"
    }
}
