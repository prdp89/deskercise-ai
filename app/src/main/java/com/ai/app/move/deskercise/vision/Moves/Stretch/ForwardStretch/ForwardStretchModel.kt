package com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch

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

open class ForwardStretchModel : Stretch(
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
    10,
    "Forward Stretch",
    1,
    duration,
    "Images/ForwardStretch",
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
        var shared: ForwardStretchModel = ForwardStretchModel()
        var repetitions: Int = 3
        var duration: Float = 5.1F
        var bufferInterval: Double = 3.0

        var _messageSF = MutableStateFlow("")
        val messageSF = _messageSF.asStateFlow()

        var _timeSF = MutableStateFlow("")
        val timeSF = _timeSF.asStateFlow()

        var _repetitionStatusColorModelSF =
            MutableStateFlow(0) // 0 - default color, 1 - green color, 2 - red color
        val repetitionStatusColorModelSF = _repetitionStatusColorModelSF.asStateFlow()
        val repetitionAngleThreshold = 60

        var _goodRepCounterModelSF = MutableStateFlow(0)
        val goodRepCounterModelSF = _goodRepCounterModelSF.asStateFlow()

        var _badRepCounterModelSF = MutableStateFlow(0)
        val badRepCounterModelSF = _badRepCounterModelSF.asStateFlow()

        var _currentRepCounterModelSF = MutableStateFlow(0)
        val currentRepCounterModelSF = _currentRepCounterModelSF.asStateFlow()

        var _totalRepCounterModelSF = MutableStateFlow(0)
        val totalRepCounterModelSF = _totalRepCounterModelSF.asStateFlow()

        var _currentRepetitionState = MutableStateFlow(0)
        val currentRepetitionState = _currentRepetitionState.asStateFlow()

        fun resetSharedStateMachine() {
            shared = ForwardStretchModel()
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
        AudioManagerService.speakText(text = TextToSpeechText.welcomeMessageForwardStretch)
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
            get() = "ForwardStretchInitial"
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
            _messageSF.value = UIInstructionText.forwardStretchCalibrationState
            AudioManagerService.speakText(TextToSpeechText.turnToRightSide)
        }

        override val description: String
            get() = "ForwardStretchCalibration"
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
            get() = "ForwardStretchInPosition"
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
            _messageSF.value = UIInstructionText.forwardStretchStartState
            AudioManagerService.speakText(text = TextToSpeechText.startForwardStretch)
        }

        override val description: String
            get() = "ForwardStretchStart"
    }

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
            get() = "ForwardStretchRepetition"
    }

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
            _messageSF.value = UIInstructionText.forwardStretchRepetitionInitialState
            AudioManagerService.speakText(text = TextToSpeechText.repetitionInitialForwardStretch)
        }

        override val description: String
            get() = "ForwardStretchRepetitionInitial"
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
                _messageSF.value = UIInstructionText.holdThere
                _timeSF.value = "\n${((remainingTime) / 1000L).toInt()}"
                _repetitionStatusColorModelSF.value = 1
            } else {
                _messageSF.value = UIInstructionText.stretchMore
                _timeSF.value = "\n${((remainingTime) / 1000L).toInt()}"
                _repetitionStatusColorModelSF.value = 2
            }
        }

        override fun didEnter(from: Any?) {
            AudioManagerService.playStretchGoalReachedSFX()
        }

        override val description: String
            get() = "ForwardStretchInProgress"
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

            totalReps += 1
            if (model.currentRepetitionGood) {
                _messageSF.value = UIInstructionText.goodJobNowRelax
                AudioManagerService.speakText(text = TextToSpeechText.goodWithRelax)
                model.currentGoodCount += 1
                goodReps += 1
                _goodRepCounterModelSF.value = model.currentGoodCount
                _currentRepCounterModelSF.value =
                    model.currentGoodCount + model.currentBadCount
            } else {
                _messageSF.value = UIInstructionText.tryHarderNextTimeNowRelax
                AudioManagerService.speakText(text = TextToSpeechText.stretchMoreWithRelax)
                model.currentBadCount += 1
                _badRepCounterModelSF.value = model.currentBadCount
                _currentRepCounterModelSF.value =
                    model.currentGoodCount + model.currentBadCount
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
            get() = "ForwardStretchCompleted"
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
            get() = "ExerciseEndState"
    }
}
