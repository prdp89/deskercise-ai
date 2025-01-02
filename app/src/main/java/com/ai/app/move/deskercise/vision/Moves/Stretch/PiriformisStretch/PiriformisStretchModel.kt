package com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch

import android.util.Log
import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.TextToSpeechText
import com.ai.app.move.deskercise.services.UIInstructionText
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveState
import com.ai.app.move.deskercise.vision.Moves.Stretch.Stretch
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PiriformisStretchModel : Stretch(
    listOf(
        InitialState(),
        CalibrationState(),
        InPositionState(),
        StartState(),
        StartStateTwo(),
        RepetitionState(),
        RepetitionInitialState(),
        RepetitionInProgressState(),
        RepetitionCompletedState(),
        ExerciseEndState(),
    ),
    12,
    "Piriformis Stretch",
    1,
    duration,
    "Images/ezgif-5-1775e022d7-gif-png",
) {
    override var remainingRepetitions: Int = repetitions
    override var remainingDuration: Float = duration
    override var startTimeLeft: Float = 3F
    override var repetitionDurationLeft: Float = duration
    override var leftCompleted: Boolean = false
    override var rightCompleted: Boolean = false
    override var lastWristAnkleDistance: Double = 0.0
    override var isSet: Boolean = true

    override var repetitionIsGood = false
    override var leftRepetitionResults: MutableList<Boolean> = mutableListOf()
    override var rightRepetitionResults: MutableList<Boolean> = mutableListOf()
    override var currentRepetitionGood: Boolean = false

    companion object {
        var shared: PiriformisStretchModel = PiriformisStretchModel()
        var repetitions: Int = 3
        var duration: Float = 5.1F
        var currentSide: Side = Side.right
        var waitingFrame: Int = 0

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
            shared = PiriformisStretchModel()
        }

        lateinit var states: List<MoveState>
    }

    init {
        states = super.move_states
    }

    override fun resetStateMachine() = resetSharedStateMachine()

    override fun getShared(): Move = shared

    override fun welcomeMessage() =
        AudioManagerService.speakText(text = TextToSpeechText.welcomeMessagePiriformisStretch)

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
            get() = "PiriformisStretchInitial"
    }

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
            AudioManagerService.speakText(TextToSpeechText.piriformisStretchCalibrationState)
        }

        override val description: String
            get() = "PiriformisStretchCalibration"
    }

    internal class StartState : MoveState() {
        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is InPositionState -> true
                is RepetitionInitialState -> true
                is RepetitionInProgressState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            if (shared.currentSide == Side.right) {
                _messageSF.value = UIInstructionText.turnToLeftSide
                AudioManagerService.speakText(TextToSpeechText.turnToRightSide)
            } else {
                _messageSF.value = UIInstructionText.torsoStretchStartStateLeftSide
                    // "Cross your right leg over the left leg."
                AudioManagerService.speakText(
                    TextToSpeechText.torsoStretchStartStateLeftSide,
                    //"Cross your right leg over the left leg.",
                    speakTillComplete = true)
            }
        }

        override val description: String
            get() = "PiriformisStretchStart"
    }

    internal class StartStateTwo : MoveState() {

        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is TorsoStretchModel.InitialState -> true
                is TorsoStretchModel.InPositionState -> true
                is TorsoStretchModel.RepetitionState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            if (TorsoStretchModel.shared.currentSide == Side.left) {
                TorsoStretchModel._messageSF.value = UIInstructionText.torsoStretchStartStateRightSide
                AudioManagerService.speakText(text = TextToSpeechText.torsoStretchStartStateRightSide)
            } else {
                TorsoStretchModel._messageSF.value = UIInstructionText.torsoStretchStartStateLeftSide
                AudioManagerService.speakText(text = TextToSpeechText.torsoStretchStartStateRightSide)
            }
        }

        override val description: String
            get() = "PiriformisStretchStartTwo"
    }

    // Arm Position Ready
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
            get() = "PiriformisStretchInPosition"
    }

    internal class RepetitionState : MoveState() {
        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionInitialState -> true
                is ExerciseEndState -> true
                is StartState -> true
                else -> false
            }
        }

        override val description: String
            get() = "PiriformisStretchRepetition"
    }

    internal class RepetitionInitialState : MoveState() {
        override fun isValidNextState(state: Any): Boolean {
            return when (state) {
                is InitialState -> true
                is RepetitionInProgressState -> true
                else -> false
            }
        }

        override fun didEnter(from: Any?) {
            _messageSF.value = UIInstructionText.bendForwardAtHips
            AudioManagerService.speakText(text = TextToSpeechText.bendForwardAtHips)
        }

        override val description: String
            get() = "PiriformisStretchRepetitionInitial"
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
            if (result && remainingTime < 9500) {
                Log.d("tag", "good: $remainingTime")
                _messageSF.value = UIInstructionText.holdThere
                _repetitionStatusColorModelSF.value = 1
            } else {
                Log.d("tag", "bad: $remainingTime")
                _messageSF.value = UIInstructionText.stretchMore
                _repetitionStatusColorModelSF.value = 2
            }
        }

        override fun didEnter(from: Any?) {
            AudioManagerService.playStretchGoalReachedSFX()
        }

        override val description: String
            get() = "PiriformisStretchInProgress"
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

        override fun didEnter(from: Any?) {
            val model = shared
            model.currentRepetitionGood = (model.goodFrames > model.badFrames - 1)

            if (model.currentRepetitionGood) {
                _messageSF.value = UIInstructionText.goodJobNowRelax
                AudioManagerService.speakText(text = TextToSpeechText.goodWithRelax)
                model.currentGoodCount += 1
                goodReps += 1
                totalReps += 1
                _goodRepCounterModelSF.value = model.currentGoodCount
                _currentRepCounterModelSF.value = totalReps.toInt()
            } else {
                _messageSF.value = UIInstructionText.tryHarderNextTimeNowRelax
                AudioManagerService.speakText(text = TextToSpeechText.stretchMoreWithRelax)
                model.currentBadCount += 1
                totalReps += 1
                _badRepCounterModelSF.value = model.currentBadCount
                _currentRepCounterModelSF.value = totalReps.toInt()
            }

            if (shared.currentSide == Side.right) {
                model.rightRepetitionResults.add(model.currentRepetitionGood)
            } else {
                model.leftRepetitionResults.add(model.currentRepetitionGood)
            }

            model.repetitionResults.add(model.currentRepetitionGood)
            model.currentRepetitionGood = false
            model.goodFrames = 0
            model.badFrames = 0
        }

        override val description: String
            get() = "PiriformisStretchCompleted"
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
            _messageSF.value = ""
            Commons.playGoodOrBadExerciseAudio(goodReps, totalReps)
        }

        override val description: String
            get() = "PiriformisStretchEnd"
    }
}
