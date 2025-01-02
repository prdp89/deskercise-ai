package com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch

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

open class TorsoStretchModel : Stretch(
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
    thisId = 7,
    thisName = "Torso Stretch",
    thisType = 1,
    thisDuration = repetitionDuration,
    "Images/TorsoStretch"
) {

    companion object {
        var shared: TorsoStretchModel = TorsoStretchModel()
        var repetitions: Int = 3
        var repetitionDuration: Float = 5.1F
        var bufferInterval: Double = 3.0

        var duration: Float = 5.1F
        var countdownDuration: Float = 3F

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
            shared = TorsoStretchModel()
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

    init {
        states = super.move_states
    }

    override fun resetStateMachine() {
        resetSharedStateMachine()
    }

    override fun getShared(): Move = shared

    override fun welcomeMessage() {
        AudioManagerService.speakText(text = TextToSpeechText.welcomeMessageTorsoStretch)
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
            get() = "TorsoStretchInitial"
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
            _messageSF.value = TextToSpeechText.seatedCalibrationHeadShouldersKnees
            AudioManagerService.speakText(TextToSpeechText.seatedCalibrationHeadShouldersKnees)
        }

        override val description: String
            get() = "TorsoStretchCalibration"
    }

    internal class StartState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is InPositionState -> true
                is RepetitionState -> true
                is StartStateTwo -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            if (shared.currentSide == Side.right) {
                _messageSF.value = UIInstructionText.turnToRightSide
                AudioManagerService.speakText(text = TextToSpeechText.turnToRightSide)
            } else {
                _messageSF.value = UIInstructionText.turnToLeftSide
                AudioManagerService.speakText(text = TextToSpeechText.turnToLeftSide)
            }
        }

        override val description: String
            get() = "TorsoStretchStart"
    }

    internal class StartStateTwo : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is InPositionState -> true
                is RepetitionState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            if (shared.currentSide == Side.right) {
                _messageSF.value = UIInstructionText.torsoStretchStartStateRightSide
                AudioManagerService.speakText(text = TextToSpeechText.torsoStretchStartStateRightSide)
            } else {
                _messageSF.value = UIInstructionText.torsoStretchStartStateLeftSide
                AudioManagerService.speakText(text = UIInstructionText.torsoStretchStartStateLeftSide)
            }
        }

        override val description: String
            get() = "TorsoStretchStartTwo"
    }

    internal class InPositionState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionState -> true
                is StartStateTwo -> true
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
            get() = "TorsoStretchInPosition"
    }

    internal class RepetitionState : MoveState() {
        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is StartState -> true
                is RepetitionInitialState -> true
                is EndState -> true
                else -> false
            }
        }

        override val description: String
            get() = "TorsoStretchRepetition"
    }

    // Hands have exceeded the predefined threshold
    internal class RepetitionInitialState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true

                is RepetitionInProgressState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            if (shared.currentSide == Side.right) {
                _messageSF.value = UIInstructionText.torsoStretchRepetitionInitialStateRightSide
                AudioManagerService.speakText(TextToSpeechText.torsoStretchRepetitionInitialStateRightSide)
            } else {
                _messageSF.value = UIInstructionText.torsoStretchRepetitionInitialStateLeftSide
                AudioManagerService.speakText(TextToSpeechText.torsoStretchRepetitionInitialStateLeftSide)
            }
        }

        override val description: String
            get() = "TorsoStretchRepetitionInitial"
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
            model.repetitionDurationRemaining = repetitionDuration
        }

        override val description: String
            get() = "TorsoStretchRepetitionInProgress"
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
            model.isSet = false
            model.goodMoreThanBad = model.goodFrames > model.badFrames
            _messageSF.value = UIInstructionText.sideStretchRepetitionCompletedState

            if (model.goodMoreThanBad) {
                AudioManagerService.speakText(text = TextToSpeechText.goodJobAndReturnToStartPosition)
                model.currentRepetitionGood = true
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
                model.currentRepetitionGood = false
            }
            if (model.getShared().currentSide == Side.right) {
                model.getShared().rightRepetitionDone = true
                model.getShared().rightRepetitionResults.add(shared.currentRepetitionGood)
                model.getShared().repetitionResults.add(shared.currentRepetitionGood)
            } else {
                model.getShared().leftRepetitionDone = true
                model.getShared().leftRepetitionResults.add(shared.currentRepetitionGood)
                model.getShared().repetitionResults.add(shared.currentRepetitionGood)
            }
            model.repetitionDurationRemaining = repetitionDuration
            model.currentRepetitionGood = false
            model.goodFrames = 0
            model.badFrames = 0

            shared.remainingRepetitions -= 1
            if (shared.remainingRepetitions == 0) {
                shared.lastRepetition = true
            }
        }

        override val description: String
            get() = "TorsoStretchRepetitionCompleted"
    }

    internal class EndState : MoveState() {

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
            get() = "TorsoStretchExerciseEnd"
    }
}
