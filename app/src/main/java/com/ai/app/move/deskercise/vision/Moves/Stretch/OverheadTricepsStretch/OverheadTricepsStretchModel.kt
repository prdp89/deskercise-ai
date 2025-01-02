package com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch

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

open class OverheadTricepsStretchModel : Stretch(
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
    2,
    "Overhead Triceps Stretch",
    1,
    repetitionDuration,
    "Images/OverheadTricep",
    thisIsShowValidFrame = BuildConfig.DRAW_VALID_BOX
) {

    //    var currentStateDescription: String = InitialState().description

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

    companion object {
        var shared: OverheadTricepsStretchModel = OverheadTricepsStretchModel()
        var repetitions: Int = 3
        var duration: Float = 5.1F
        var countdownDuration: Float = 3F

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
            shared = OverheadTricepsStretchModel()
        }

        lateinit var states: List<MoveState>
    }

    override var updateCount: Int = 0

    init {
        states = super.move_states
    }

    override fun resetStateMachine() {
        resetSharedStateMachine()
    }

    override fun getShared(): Move = shared

    override fun welcomeMessage() {
        AudioManagerService.speakText(text = TextToSpeechText.welcomeMessageOverheadTricepsStretch)
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
            get() = "OverheadTricepsStretchInitial"
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
//            _messageSF.value = "While seated, ensure that your arms are visible and that there is ample space above your head"
//            AudioManagerService.speakText(
//                "While seated, ensure that your arms are visible and that there is ample space above your head",
//                speakTillComplete = true,
//            )
            _messageSF.value = UIInstructionText.seatedCalibrationWithArmsAndHeadspace
            AudioManagerService.speakText(
                TextToSpeechText.seatedCalibrationWithArmsAndHeadspace,
                speakTillComplete = true,
            )
        }

        override val description: String
            get() = "OverheadTricepsStretchCalibration"
    }

    internal class InPositionState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionState -> true
                is RepetitionInitialState -> true
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
            get() = "OverheadTricepsStretchInPosition"
    }

    // Prompt to get into starting position
    internal class StartState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is InPositionState -> true
                is RepetitionInitialState -> true
                is RepetitionInProgressState -> true
                is StartStateTwo -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            if (shared.currentSide == Side.right) {
                _messageSF.value =
                    UIInstructionText.overheadTricepsStartStateRightSide
                AudioManagerService.speakText(
                    text = TextToSpeechText.overheadTricepsStartStateRightSide,
                    speakTillComplete = false,
                )
            } else {
                _messageSF.value = UIInstructionText.overheadTricepsStartStateLeftSide
                    // "Rest your left palm on your back and point your elbow to the ceiling"
                AudioManagerService.speakText(
                    text = TextToSpeechText.overheadTricepsStartStateLeftSide,
                    //"Rest your left palm on your back and point your elbow to the ceiling",
                    speakTillComplete = false,
                )
            }
        }

        override val description: String
            get() = "OverheadTricepsStretchStart"
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
                _messageSF.value = UIInstructionText.overheadTricepsStartStateTwoRightSide
                AudioManagerService.speakText(
                    TextToSpeechText.overheadTricepsStartStateTwoRightSide,
                    speakTillComplete = true,
                    )
            } else {
                _messageSF.value = UIInstructionText.overheadTricepsStartStateTwoLeftSide
                AudioManagerService.speakText(
                    TextToSpeechText.overheadTricepsStartStateTwoLeftSide,
                    speakTillComplete = true,
                    )
            }
        }

        override val description: String
            get() = "OverheadTricepsStretchTwo"
    }

    // Hands are in position on the head
    internal class RepetitionState : MoveState() {
        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionInitialState -> true
                is EndState -> true
                is StartState -> true
                else -> false
            }
        }

        override val description: String
            get() = "OverheadTricepsStretchRepetition"
    }

    // Hands have exceeded the predefined threshold
    internal class RepetitionInitialState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is RepetitionInitialState -> true
                is RepetitionInProgressState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            if (shared.currentSide == Side.right) {
                _messageSF.value = UIInstructionText.overheadTricepsRepetitionInitialStateRightSide
                    // "Pull your right elbow towards the back of your head."
                AudioManagerService.speakText(
                    text = TextToSpeechText.overheadTricepsRepetitionInitialStateRightSide,
                    // "Pull your right elbow towards the back of your head.",
                    speakTillComplete = true,
                )
            } else {
                _messageSF.value = UIInstructionText.overheadTricepsRepetitionInitialStateLeftSide
                    // "Pull your left elbow towards the back of your head."
                AudioManagerService.speakText(
                    text = TextToSpeechText.overheadTricepsRepetitionInitialStateLeftSide,
                    // "Pull your left elbow towards the back of your head.",
                    speakTillComplete = true,
                )
            }
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
            get() = "OverheadTricepsStretchInProgress"
    }

    class RepetitionCompletedState : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionState -> true
                else -> false
            }
        }

        fun updateInstructions() {
            _repetitionStatusColorModelSF.value = 0
        }

        fun updateInstruction() {
            AudioManagerService.speakText(TextToSpeechText.returnToYourStarting)
            _messageSF.value = UIInstructionText.returnToYourStarting
        }

        override fun didEnter(from: Any?) {
            val model = shared
            model.currentRepetitionGood = (model.goodFrames > model.badFrames)

            if (model.currentRepetitionGood) {
                AudioManagerService.speakText(TextToSpeechText.goodWithRelax)
                _messageSF.value = UIInstructionText.good + " Now relax."
                model.currentGoodCount += 1
                goodReps += 1
                totalReps += 1
                _goodRepCounterModelSF.value = model.currentGoodCount
                _currentRepCounterModelSF.value = totalReps.toInt()
            } else {
                AudioManagerService.speakText(TextToSpeechText.stretchMoreWithRelax)
                _messageSF.value = UIInstructionText.stretchMoreWithRelax
                model.currentBadCount += 1
                totalReps += 1
                _badRepCounterModelSF.value = model.currentBadCount
                _currentRepCounterModelSF.value = totalReps.toInt()
                model.currentRepetitionGood = false
            }

            if (model.getShared().currentSide == Side.right) {
                model.getShared().rightRepetitionDone = true
                model.rightRepetitionResults.add(shared.currentRepetitionGood)
                model.getShared().repetitionResults.add(shared.currentRepetitionGood)
            } else {
                model.getShared().leftRepetitionDone = true
                model.getShared().leftRepetitionResults.add(shared.currentRepetitionGood)
                model.getShared().repetitionResults.add(shared.currentRepetitionGood)
            }
            // reset for next rep

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
            get() = "OverheadTricepsStretchCompleted"
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
            get() = "OverheadTricepsStretchEnd"
    }
}
