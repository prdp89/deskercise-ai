package com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch

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

open class NeckStretchModel : Stretch(
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
    thisId = 6,
    thisName = "Neck Stretch",
    thisType = 1,
    thisDuration = repetitionDuration,
    "Images/NeckStretch",
) {

    companion object {
        var shared: NeckStretchModel = NeckStretchModel()
        var repetitions: Int = 3
        var duration: Float = 5.1F

        var repetitionDuration: Float = 5.1F

        var bufferInterval: Double = 3.0

        var repetitionNeckToEarAngleThreshold = 55
        var repetitionInProgressNeckToEarAngleThreshold = 55
        var positionCountdownDuration = 3

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
            shared = NeckStretchModel()
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

    override var updateCount: Int = 0

    init {
        states = super.move_states
    }

    override fun resetStateMachine() {
        resetSharedStateMachine()
    }

    override fun getShared(): Move = shared

    override fun welcomeMessage() {
        AudioManagerService.speakText(text = TextToSpeechText.welcomeMessageNeckStretch)
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
            get() = "NeckStretchInitial"
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
            _messageSF.value = UIInstructionText.moveBackwards
            AudioManagerService.speakText(TextToSpeechText.seatedCalibrationWithFaceShoulderElbow)
        }

        override val description: String
            get() = "NeckStretchCalibration"
    }

    internal class StartState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is InPositionState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            _messageSF.value = UIInstructionText.neckStretchStartState
            AudioManagerService.speakText(text = TextToSpeechText.lookStraightAndKeepArmsAtSide)
        }

        override val description: String
            get() = "NeckStretchStart"
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
            if (shared.firstRepetition) {
                _messageSF.value = UIInstructionText.getReady
                AudioManagerService.speakText(text = TextToSpeechText.exerciseIsStarting)
            }
        }

        override val description: String
            get() = "NeckStretchInPosition"
    }

    internal class RepetitionState : MoveState() {
        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionInitialState -> true
                is EndState -> true
                else -> false
            }
        }

        override val description: String
            get() = "NeckStretchRepetition"
    }

    internal class RepetitionInitialState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionInProgressState -> true
                is EndState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            if (shared.currentSide == Side.right) {
                AudioManagerService.speakText(text = TextToSpeechText.leanHeadAndRollHeadToRight)
                _messageSF.value = UIInstructionText.neckStretchRepetitionInitialStateRightSide
            } else {
                AudioManagerService.speakText(text = TextToSpeechText.rollHeadToLeft)
                _messageSF.value = UIInstructionText.neckStretchRepetitionInitialStateLeftSide
            }
        }

        override val description: String
            get() = "NeckStretchRepetitionInitial"
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
//            model.repetitionDurationRemaining = NeckStretchModel.repetitionDuration
        }

        override val description: String
            get() = "NeckStretchRepetitionInProgress"
    }

    class RepetitionCompletedState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            val model = shared
            model.currentRepetitionGood = (model.goodFrames > model.badFrames)

            totalReps += 1
            if (model.currentRepetitionGood) {
                AudioManagerService.speakText(text = TextToSpeechText.goodWithRelax)
                _messageSF.value =
                    UIInstructionText.good + " " + UIInstructionText.neckStretchRepetitionCompletedState
                model.currentGoodCount += 1
                goodReps += 1
            } else {
                AudioManagerService.speakText(text = TextToSpeechText.stretchMoreWithRelax)
                _messageSF.value =
                    UIInstructionText.tryToStretchMore + " " + UIInstructionText.neckStretchRepetitionCompletedState
                model.currentBadCount += 1
            }

            if (model.getShared().currentSide == Side.right) {
                model.getShared().rightRepetitionResults.add(shared.currentRepetitionGood)
                model.getShared().repetitionResults.add(shared.currentRepetitionGood)
            } else {
                model.getShared().leftRepetitionResults.add(shared.currentRepetitionGood)
                model.getShared().repetitionResults.add(shared.currentRepetitionGood)
            }
            // reset for next rep
            model.currentRepetitionGood = false
            model.goodFrames = 0
            model.badFrames = 0

            shared.remainingRepetitions -= 1
            if (shared.remainingRepetitions == 0) {
                shared.lastRepetition = true
            }
        }

        override val description: String
            get() = "NeckStretchRepetitionCompleted"
    }

    internal class EndState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is InPositionState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            AudioManagerService.playStretchCompletionSFX()
            playGoodOrBadExerciseAudio(goodReps, totalReps)
        }

        override val description: String
            get() = "NeckStretchEnd"
    }
}
