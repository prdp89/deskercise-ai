package com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch

import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.ValidFramePosition
import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.services.GlobalSettings
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivityBridge
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveController
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.Classifiers.OverheadShoulderStretchInPositionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.Classifiers.OverheadShoulderStretchRepetitionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.Classifiers.OverheadShoulderStretchRepetitionInProgressClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.Classifiers.OverheadShoulderStretchStartClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.OverheadShoulderStretchModel.Companion._messageSF
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.OverheadShoulderStretchModel.Companion.shared
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.OverheadShoulderStretchModel.Companion.states
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * The Controller manages when the different states are transitioned based on the model's flag and classifier's results
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

open class OverheadShoulderStretchController(private val activity: MainActivityBridge) : MoveController() {

    init {
        resetState()
        _exercisesStartButtonStateSF.value = true

        viewModelScope.launch {
            OverheadShoulderStretchModel.goodRepCounterModelSF.collect { value ->
                _goodRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            OverheadShoulderStretchModel.badRepCounterModelSF.collect { value ->
                _badRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            OverheadShoulderStretchModel.currentRepCounterModelSF.collect { value ->
                _currentRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            _totalRepCounterSF.value = shared.repetitions
        }
    }

    override fun drawnJoints(): List<BodyPart> {
        return listOf(
            BodyPart.LEFT_SHOULDER,
            BodyPart.LEFT_ELBOW,
            BodyPart.LEFT_WRIST,
            BodyPart.RIGHT_SHOULDER,
            BodyPart.RIGHT_ELBOW,
            BodyPart.RIGHT_WRIST,
        )
    }

    override fun drawnJointPairs(): List<Pair<BodyPart, BodyPart>> {
        return listOf(
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER),
            Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
            Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
        )
    }

    override fun refreshRateOfKeyPointsInMs(): Long? {
        return GlobalSettings.getRefreshRateOfKeyPointsInMs()
    }

    override fun validFramePosition(): ValidFramePosition? {
        return when(_currentRepetitionState.value) {
            2, 3, 4 -> {
                if (model.getShared().exerciseCompleted) {
                    null
                } else {
                    val leftPositionValidFrame = 0.3 * 480
                    val rightPositionValidFrame = 0.7 * 480
                    val topPositionValidFrame = 0
                    val bottomPositionValidFrame = 0.6 * 640
                    ValidFramePosition(
                        left = leftPositionValidFrame.toFloat(),
                        top = topPositionValidFrame.toFloat(),
                        right = rightPositionValidFrame.toFloat(),
                        bottom = bottomPositionValidFrame.toFloat()
                    )
                }
            }
            else -> null
        }
    }

    override fun executeState() {
        viewModelScope.launch {
            processObservation()
        }
    }

    override fun exitState() {
        viewModelScope.launch {
            _exerciseCompletedSF.value = true
            cleanState()
        }
    }

    override fun userSkip() {
        viewModelScope.launch {
            _didUserSkipSF.value = true
            cleanState()
        }
    }

    override fun userSkipReset() {
        viewModelScope.launch {
            _didUserSkipSF.value = false
            cleanState()
        }
    }

    override fun userExit() {
        viewModelScope.launch {
            _didUserExitSF.value = true
            _didUserSkipSF.value = true
            cleanState()
        }
    }

    override fun welcome() {
        viewModelScope.launch {
            _goodRepCounterSF.value = 0
            _badRepCounterSF.value = 0
            _currentRepCounterSF.value = 0
            yield()
            model.welcomeMessage()
            delay(100)
        }
    }

    override fun updateState() {
        viewModelScope.launch {
            super.updateState()
            // get index of controller state
            val currentStateIndex = getStateIndexOfMatchingCurrentState()
            if (currentStateIndex < 0 || currentStateIndex >= states.lastIndex) {
                Move.currentMoveState = states.first()
            } else {
                Move.currentMoveState = states[currentStateIndex + 1]
            }
            processObservation()
        }
    }

    override suspend fun processObservation() {
        super.processObservation()
        if (!didUserSkipSF.value) {
            while (activity.isStartingExerciseFragmentVisible()) {
                yield()
            }
            when (Move.currentMoveState!!::class) {
                OverheadShoulderStretchModel.InitialState::class -> initialProcessObservation()
                OverheadShoulderStretchModel.CalibrationState::class -> calibrationProcessObservation()
                OverheadShoulderStretchModel.StartState::class -> startProcessObservation()
                OverheadShoulderStretchModel.RepetitionState::class -> repetitionProcessObservation()
                OverheadShoulderStretchModel.RepetitionInitialState::class -> repetitionInitialProcessObservation()
                OverheadShoulderStretchModel.RepetitionInProgressState::class -> repetitionInProgressProcessObservation()
                OverheadShoulderStretchModel.RepetitionCompletedState::class -> repetitionCompletedProcessObservation()
            }
        }
    }

    final override fun resetState() {
        viewModelScope.launch {
            cleanState()
            Move.currentMoveState = states[0]
            _exerciseInstructionSF.value = ""

            shared.firstRepetition = true

            // setRepetitions
            model = shared

            // consider adding a reset function within model
            Move.goodReps = 0F
            Move.totalReps = 0F
            val newModel = OverheadShoulderStretchModel()
            model.getShared().repetitionResults = newModel.repetitionResults
            model.remainingDuration = newModel.remainingDuration
            model.startTimeLeft = newModel.startTimeLeft
            model.repetitionDurationLeft = newModel.repetitionDurationLeft
            model.getShared().firstRepetition = newModel.firstRepetition
            model.getShared().lastRepetition = newModel.lastRepetition

            model.instructed = newModel.instructed
            model.firstExercise = newModel.firstExercise
            model.exerciseCompleted = newModel.exerciseCompleted
            model.currentGoodCount = newModel.currentGoodCount
            model.currentBadCount = newModel.currentBadCount
            model.goodFrames = newModel.goodFrames
            model.badFrames = newModel.badFrames
        }
    }

    override fun cleanState() {
        _exerciseStateDisplaySF.value = ""
        _exerciseInstructionSF.value = ""
        isObservingCalibrationState = false
        isObservingStartProcess = false
        isObservingRepetitionInitialProcess = false
        isObservingRepetitionInProgressProcess = false
    }

    private fun getStateIndexOfMatchingCurrentState(): Int {
        for (idx in 0 until states.count()) {
            if (states[idx]::class == Move.currentMoveState!!::class) {
                return idx
            }
        }
        return -1
    }

    override suspend fun initialProcessObservation() {
        yield()
        // Starting Stretch Logic
        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 0
        // reset
        OverheadShoulderStretchModel._goodRepCounterModelSF.value = 0
        OverheadShoulderStretchModel._badRepCounterModelSF.value = 0
        OverheadShoulderStretchModel._currentRepCounterModelSF.value = 0
        model.getShared().currentGoodCount = 0
        model.getShared().currentBadCount = 0
        model.getShared().remainingRepetitions = model.getShared().repetitions
        enter(OverheadShoulderStretchModel.CalibrationState())
    }

    override suspend fun calibrationProcessObservation() {
        isObservingCalibrationState = true
        val thresholdToBeInFrame = 100L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 0
        while (isObservingCalibrationState && !didUserSkipSF.value) {
            waitIfPaused()
            val result = OverheadShoulderStretchStartClassifier.check(person)
            if (result) {
                val diff = System.currentTimeMillis() - startTimeOfBeingInFrame
                if (diff >= thresholdToBeInFrame) {
                    isObservingCalibrationState = false
                    if (Move.currentMoveState!!::class == OverheadShoulderStretchModel.CalibrationState::class) {
                        enter(OverheadShoulderStretchModel.StartState())
                    }
                }
            } else {
                startTimeOfBeingInFrame = System.currentTimeMillis()
            }
            yield()
        }

        yield()
    }

    override suspend fun startProcessObservation() {
        isObservingStartProcess = true
        val thresholdToBeInFrame = 50L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 2
        if (model.isShowValidFrame) {
            activity.updateDrawnKeyPoints()
        }

        delay(50)
        yield()
        while (isObservingStartProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = OverheadShoulderStretchInPositionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == OverheadShoulderStretchModel.StartState::class) {
                        if (enter(OverheadShoulderStretchModel.InPositionState())) {
                            if (model.getShared().firstRepetition) {
                                _displayCountersSF.value = true
                                _exerciseInstructionSF.value = _messageSF.value
                                for (i in 3 downTo 0) {
                                    waitIfPaused()
                                    _exerciseInstructionSF.value = _messageSF.value + "\n" + i
                                    delay(1000L) // this delay causes the coroutine to return the value to the UI
                                }
                            }
                            enter(OverheadShoulderStretchModel.RepetitionState())
                        }
                    }
                    yield()
                }
            } else {
                startTimeOfBeingInFrame = System.currentTimeMillis()
            }
        }
        yield()
    }

    override suspend fun repetitionProcessObservation() {
        if (model.getShared().remainingRepetitions <= 0) {
            model.exerciseCompleted = true

            enter(OverheadShoulderStretchModel.ExerciseEndState())
            _exerciseInstructionSF.value = ""
            _displayCountersSF.value = false
            delay(100)
            yield()

            // Check Queue
            exitState()

            return
        } else {
            enter(OverheadShoulderStretchModel.RepetitionInitialState())
            return
        }
    }

    override suspend fun repetitionInitialProcessObservation() {
        _currentRepetitionState.value = 3

        isObservingRepetitionInitialProcess = true
        val thresholdToBeInFrame = 100L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        yield()
        while (isObservingRepetitionInitialProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = OverheadShoulderStretchRepetitionClassifier.check(person)
            if (result) {
                waitIfPaused()
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingRepetitionInitialProcess = false
                    enter(OverheadShoulderStretchModel.RepetitionInProgressState())
                }
            } else {
                startTimeOfBeingInFrame = System.currentTimeMillis()
            }
            yield()
        }

        yield()
    }

    override suspend fun repetitionInProgressProcessObservation() {
        _currentRepetitionState.value = 3
        val thresholdToBeInFrame: Long = (OverheadShoulderStretchModel.duration * 1000).toLong()
        var startTimeOfBeingInFrame = System.currentTimeMillis()
        var remainingTime: Long
        yield()

        isObservingRepetitionInProgressProcess = true

        _exerciseInstructionSF.value = _messageSF.value
        while (isObservingRepetitionInProgressProcess && !didUserSkipSF.value) {
            val newStartTimeOfBeingInFrame = waitIfPaused(startTimeOfBeingInFrame)
            if (newStartTimeOfBeingInFrame != null) {
                startTimeOfBeingInFrame = newStartTimeOfBeingInFrame
            }
            yield()
            remainingTime =
                thresholdToBeInFrame - (System.currentTimeMillis() - startTimeOfBeingInFrame)
            val result = OverheadShoulderStretchRepetitionInProgressClassifier.check(person)
            if (result) {
                enter(OverheadShoulderStretchModel.RepetitionInProgressState())
            }

            if (remainingTime <= 0) {
                yield()
                OverheadShoulderStretchModel.RepetitionInProgressState()
                    .updateInstructions(result, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    OverheadShoulderStretchModel._repetitionStatusColorModelSF.value
                yield()
                _repetitionStatusColorSF.value = 0
                isObservingRepetitionInProgressProcess = false
                if (Move.currentMoveState!!::class == OverheadShoulderStretchModel.RepetitionInProgressState::class) {
                    enter(OverheadShoulderStretchModel.RepetitionCompletedState())
                }
            } else {
                yield()
                OverheadShoulderStretchModel.RepetitionInProgressState()
                    .updateInstructions(result, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    OverheadShoulderStretchModel._repetitionStatusColorModelSF.value
            }
        }

        yield()
    }

    override suspend fun repetitionCompletedProcessObservation() {
        var flag = true
        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 4
        if (model.getShared().lastRepetition) {
            _exerciseInstructionSF.value = ""
            _currentRepetitionState.value = 1
            if (model.isShowValidFrame) {
                activity.updateDrawnKeyPoints()
            }
            enter(OverheadShoulderStretchModel.RepetitionState())
            return
        }

        yield()

        while (AudioManagerService.tts.isSpeaking) {
            delay(100) // Time for Text-To-Speech to complete w/o being interrupted by next instruction
        }

        if (model.getShared().firstRepetition) {
            model.getShared().firstRepetition = false
        }

        _currentRepetitionState.value = 4
        _exerciseInstructionSF.value = _messageSF.value

        while (AudioManagerService.tts.isSpeaking) {
            delay(100) // Time for Text-To-Speech to complete w/o being interrupted by next instruction
        }

        enter(OverheadShoulderStretchModel.StartState())

        yield()
    }

    companion object {
        var model: Move = shared
    }
}
