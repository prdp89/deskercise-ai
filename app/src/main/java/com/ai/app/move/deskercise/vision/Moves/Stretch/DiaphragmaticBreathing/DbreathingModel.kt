package com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing

import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.services.Commons.Companion.playGoodOrBadExerciseAudio
import com.ai.app.move.deskercise.services.TextToSpeechText
import com.ai.app.move.deskercise.services.UIInstructionText
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveState
import com.ai.app.move.deskercise.vision.Moves.Stretch.Stretch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * The model file contains variables and functions that the controller will manipulate and utilize
 */

open class DbreathingModel : Stretch(
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
    thisId = 14,
    thisName = "Relaxation breath",
    thisType = 1,
    thisDuration = repetitionDuration,
    "Images/DiaphramaticBreathing",
) {

    companion object {
        var shared: DbreathingModel = DbreathingModel()
        var repetitions: Int = 2
        var duration: Float = 3.5F

        var repetitionDuration: Float = 6.5F

        var bufferInterval: Double = 3.0
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
            shared = DbreathingModel()
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
//        AudioManagerService.speakText(text = "Relaxation breath")
        AudioManagerService.speakText(text = TextToSpeechText.welcomeMessageDbreathing)
    }

    class InitialState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is CalibrationState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            _messageSF.value = UIInstructionText.dbreathingInitialState
        }

        override val description: String
            get() = "DbreathingInitial"
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
//            _messageSF.value = "Relaxation breath"
//            AudioManagerService.speakText("Please ensure your shoulders and face are inside the frame")
            _messageSF.value = UIInstructionText.dbreathingName
            AudioManagerService.speakText(TextToSpeechText.seatedCalibrationWithArmsAndElbows)
        }

        override val description: String
            get() = "DbreathingCalibration"
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
            get() = "DbreathingStart"
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
//                _messageSF.value = "Get Ready"
//                AudioManagerService.speakText(text = "Please standby")
                _messageSF.value = UIInstructionText.getReady
                AudioManagerService.speakText(text = TextToSpeechText.exerciseIsStarting)
            }
        }

        override val description: String
            get() = "DbreathingInPosition"
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

        override fun didEnter(from: Any?) {
            AudioManagerService.speakText(text = TextToSpeechText.dbreathingRepetitionState)
            _messageSF.value = UIInstructionText.dbreathingRepetitionState
        }

        override val description: String
            get() = "DbreathingRepetition"
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

        fun updateInstruction() {
            AudioManagerService.speakText(text = "Hold Breath")
        }

        override fun didEnter(from: Any?) {
            if (shared.currentSide == Side.right) {
//                AudioManagerService.speakText(text = "Breathe in")
//                _messageSF.value = "Breathe in"
                AudioManagerService.speakText(TextToSpeechText.dbreathingRepetitionInitialStateRightSide)
                _messageSF.value = UIInstructionText.dbreathingRepetitionInitialStateRightSide
            } else {
//                AudioManagerService.speakText(text = "Breathe out")
//                _messageSF.value = "Breathe out"
                AudioManagerService.speakText(
                    TextToSpeechText.dbreathingRepetitionInitialStateLeftSide
                )
                _messageSF.value = UIInstructionText.dbreathingRepetitionInitialStateLeftSide
            }
        }

        override val description: String
            get() = "DbreathingInitial"
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
            if (remainingTime <= 0) {
                _messageSF.value = ""
            } else {
                if (result) {
                    _messageSF.value = "Good job\n${((remainingTime) / 1000L).toInt()}"
                    _repetitionStatusColorModelSF.value = 1
                } else {
                    _messageSF.value = "Please stay in frame\n${((remainingTime) / 1000L).toInt()}"
                    _repetitionStatusColorModelSF.value = 2
                }
            }
        }

        override fun didEnter(from: Any?) {
            AudioManagerService.playStretchGoalReachedSFX()
            Timber.d(">> RepetitionCompletedState")
            val model = shared
//            model.repetitionDurationRemaining = DbreathingModel.repetitionDuration
        }

        override val description: String
            get() = "DbreathingInProgress"
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
            Timber.d(">> RepetitionCompletedState")
            val model = shared
            model.currentRepetitionGood = (model.goodFrames > model.badFrames)
            totalReps += 1
            if (shared.currentSide == Side.left) {
                if (model.currentRepetitionGood) {
                    AudioManagerService.speakText(text = TextToSpeechText.good)
                    _messageSF.value = UIInstructionText.good
                    model.currentGoodCount += 1
                    _goodRepCounterModelSF.value = model.currentGoodCount
                    goodReps += 1
                } else {
//                    AudioManagerService.speakText(text = "Please stay in frame")
//                    _messageSF.value = "Please stay in frame"
                    AudioManagerService.speakText(text = TextToSpeechText.dbreathingRepetitionCompletedState)
                    _messageSF.value = UIInstructionText.dbreathingRepetitionCompletedState
                    model.currentBadCount += 1
                    _badRepCounterModelSF.value = model.currentBadCount
                }
            }

            if (model.getShared().currentSide == Side.right) {
                model.getShared().rightRepetitionResults.add(shared.currentRepetitionGood)
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
            get() = "DbreathingCompleted"
    }

    internal class EndState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            _messageSF.value = UIInstructionText.dbreathingEndState
            AudioManagerService.speakText(TextToSpeechText.dbreathingEndState)
//            _messageSF.value = "Relaxation breath Completed"
//            AudioManagerService.speakText("Relaxation breath Completed")
            playGoodOrBadExerciseAudio(goodReps, totalReps)
        }

        override val description: String
            get() = "DbreathingEnd"
    }
}
