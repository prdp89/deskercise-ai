package com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch

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

open class SideStretchModel : Stretch(
    listOf(
        InitialState(),
        CalibrationState(),
        StartState(),
        InPositionState(),
        RepetitionState(),
        RepetitionInitialState(),
        RepetitionArmRaisedState(),
        RepetitionInProgressState(),
        RepetitionCompletedState(),
        EndState(),
    ),
    thisId = 4,
    thisName = "Side Stretch",
    thisType = 1,
    thisDuration = repetitionDuration,
    "Images/SideStretch",
) {

    companion object {
        var shared: SideStretchModel = SideStretchModel()
        var repetitions: Int = 3
        const val repetitionArmAngleThreshold: Double = 95.0
        var positionCountdownDuration = 3
        var repetitionDuration: Float = 5.1F
        var bufferInterval: Double = 3.0

        var duration: Float = 5.1F

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
            shared = SideStretchModel()
        }

        lateinit var states: List<MoveState>
    }

    override var remainingRepetitions: Int = repetitions
    override var remainingDuration: Float = duration
    override var startTimeLeft: Float = 3F
    override var repetitionDurationLeft: Float = duration
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
        AudioManagerService.speakText(text = TextToSpeechText.welcomeMessageSideStretch)
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
            get() = "SideStretchInitial"
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
            _messageSF.value = TextToSpeechText.seatedCalibrationWithArmsAndHeadspace
            AudioManagerService.speakText(TextToSpeechText.seatedCalibrationWithArmsAndHeadspace)
        }

        override val description: String
            get() = "SideStretchCalibration"
    }

    internal class StartState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is InPositionState -> true
                is RepetitionInitialState -> true
                else -> false
            }
        }

        override val description: String
            get() = "SideStretchStart"
    }

    internal class InPositionState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionState -> true
                is RepetitionInitialState -> true
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
            get() = "SideStretchInPosition"
    }

    internal class RepetitionState : MoveState() {
        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionInitialState -> true
                is StartState -> true
                is EndState -> true
                else -> false
            }
        }

        override val description: String
            get() = "SideStretchRepetition"
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
                AudioManagerService.speakText(TextToSpeechText.sideStretchRepetitionInitialStateRightSide)
                _messageSF.value = UIInstructionText.sideStretchRepetitionInitialStateRightSide
            } else {
                AudioManagerService.speakText(TextToSpeechText.sideStretchRepetitionInitialStateLeftSide)
                _messageSF.value = UIInstructionText.sideStretchRepetitionInitialStateLeftSide
            }
        }

        override val description: String
            get() = "SideStretchRepetitionInitial"
    }

    internal class RepetitionArmRaisedState : MoveState() {
        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionInProgressState -> true
                is RepetitionInitialState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            if (shared.currentSide == Side.right) {
                _messageSF.value = TextToSpeechText.bendYourBodyToLeft
                AudioManagerService.speakText(text = TextToSpeechText.bendYourBodyToLeft)
            } else {
                _messageSF.value = TextToSpeechText.bendYourBodyToRight
                AudioManagerService.speakText(text = TextToSpeechText.bendYourBodyToRight)
            }
        }

        override val description: String
            get() = "SideStretchRepetitionArmRaised"
    }

    internal class RepetitionInProgressState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionState -> true
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
        }

        override val description: String
            get() = "SideStretchRepetitionInProgress"
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
            _messageSF.value = UIInstructionText.sideStretchRepetitionCompletedState
            totalReps += 1
            if (model.currentRepetitionGood) {
                AudioManagerService.speakText(text = TextToSpeechText.goodJobAndReturnToStartPosition)
                model.currentGoodCount += 1
                goodReps += 1
            } else {
                AudioManagerService.speakText(text = TextToSpeechText.stretchMoreAndReturnToStartPosition)
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
            get() = "SideStretchRepetitionCompleted"
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
            get() = "SideStretchEnd"
    }
}
