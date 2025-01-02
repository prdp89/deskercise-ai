package com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch

import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.services.GlobalSettings
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivityBridge
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveController
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.Classifiers.ForwardStretchInPositionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.Classifiers.ForwardStretchRepetitionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.Classifiers.ForwardStretchRepetitionInProgressClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.Classifiers.ForwardStretchStartClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.ForwardStretchModel.Companion._messageSF
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.ForwardStretchModel.Companion.shared
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.ForwardStretchModel.Companion.states
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * The Controller manages when the different states are transitioned based on the model's flag and classifier's results
 *
 * NOTE: All RIGHT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.RIGHT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

open class ForwardStretchController(private val activity: MainActivityBridge) : MoveController() {

    init {
        resetState()
        _exercisesStartButtonStateSF.value = true

        viewModelScope.launch {
            ForwardStretchModel.goodRepCounterModelSF.collect { value ->
                _goodRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            ForwardStretchModel.badRepCounterModelSF.collect { value ->
                _badRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            ForwardStretchModel.currentRepCounterModelSF.collect { value ->
                _currentRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            _totalRepCounterSF.value = shared.repetitions
        }
    }

    override fun drawnJoints(): List<BodyPart> {
        return listOf(
            BodyPart.RIGHT_EAR,
            BodyPart.RIGHT_SHOULDER,
            BodyPart.RIGHT_ELBOW,
            BodyPart.RIGHT_HIP,
        )
    }

    override fun drawnJointPairs(): List<Pair<BodyPart, BodyPart>> {
        return listOf(
            Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
        )
    }

    // Reduce the framerate of the key points as for this exercise the keypoints are very jittery
    override fun refreshRateOfKeyPointsInMs(): Long? {
        return GlobalSettings.getRefreshRateOfKeyPointsInMs()
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
            if (!didUserSkipSF.value) {
                yield()
                model.welcomeMessage()
                delay(100)
            }
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
                ForwardStretchModel.InitialState::class -> initialProcessObservation()
                ForwardStretchModel.CalibrationState::class -> calibrationProcessObservation()
                ForwardStretchModel.StartState::class -> startProcessObservation()
                ForwardStretchModel.RepetitionState::class -> repetitionProcessObservation()
                ForwardStretchModel.RepetitionInitialState::class -> repetitionInitialProcessObservation()
                ForwardStretchModel.RepetitionInProgressState::class -> repetitionInProgressProcessObservation()
                ForwardStretchModel.RepetitionCompletedState::class -> repetitionCompletedProcessObservation()
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

            Move.goodReps = 0F
            Move.totalReps = 0F

            // todo: consider adding a reset function within model
            val newModel = ForwardStretchModel()
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
        _currentRepetitionState.value = 1
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
        _currentRepetitionState.value = 1
        // reset
        ForwardStretchModel._goodRepCounterModelSF.value = 0
        ForwardStretchModel._badRepCounterModelSF.value = 0
        ForwardStretchModel._currentRepCounterModelSF.value = 0
        model.getShared().currentGoodCount = 0
        model.getShared().currentBadCount = 0
        model.getShared().remainingRepetitions = model.getShared().repetitions
        enter(ForwardStretchModel.CalibrationState())
    }

    override suspend fun calibrationProcessObservation() {
        isObservingCalibrationState = true
        val thresholdToBeInFrame = 100L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 1
        while (isObservingCalibrationState && !didUserSkipSF.value) {
            waitIfPaused()
            val result = ForwardStretchStartClassifier.check(person)
            if (result) {
                val diff = System.currentTimeMillis() - startTimeOfBeingInFrame
                if (diff >= thresholdToBeInFrame) {
                    isObservingCalibrationState = false
                    if (Move.currentMoveState!!::class == ForwardStretchModel.CalibrationState::class) {
                        enter(ForwardStretchModel.StartState())
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
        val thresholdToBeInFrame = 0L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 2

        delay(50)
        yield()
        while (isObservingStartProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = ForwardStretchInPositionClassifier.check(person)

            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == ForwardStretchModel.StartState::class) {
                        if (enter(ForwardStretchModel.InPositionState())) {
                            if (model.getShared().firstRepetition) {
                                _displayCountersSF.value = true
                                _exerciseInstructionSF.value = _messageSF.value
                                for (i in 3 downTo 0) {
                                    waitIfPaused()
                                    _countdownSF.value = _messageSF.value + "\n" + i
                                    delay(1000L) // this delay causes the coroutine to return the value to the UI
                                }
                            }
                            enter(ForwardStretchModel.RepetitionState())
                            _currentRepetitionState.value = 3
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

            enter(ForwardStretchModel.ExerciseEndState())
            _exerciseInstructionSF.value = ""
            _displayCountersSF.value = false
            delay(100)
            yield()

            exitState()

            return
        } else {
            enter(ForwardStretchModel.RepetitionInitialState())
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
            val result = ForwardStretchRepetitionClassifier.check(person)
            if (result) {
                waitIfPaused()
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingRepetitionInitialProcess = false
                    enter(ForwardStretchModel.RepetitionInProgressState())
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

        val thresholdToBeInFrame: Long = (ForwardStretchModel.duration * 1000).toLong()
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
            val result = ForwardStretchRepetitionInProgressClassifier.check(person)
            if (result) {
                enter(ForwardStretchModel.RepetitionInProgressState())
            }

            if (remainingTime <= 0) {
                yield()
                ForwardStretchModel.RepetitionInProgressState()
                    .updateInstructions(result, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    ForwardStretchModel._repetitionStatusColorModelSF.value
                yield()
                _repetitionStatusColorSF.value = 0
                isObservingRepetitionInProgressProcess = false
                if (Move.currentMoveState!!::class == ForwardStretchModel.RepetitionInProgressState::class) {
                    enter(ForwardStretchModel.RepetitionCompletedState())
                }
            } else {
                yield()
                ForwardStretchModel.RepetitionInProgressState()
                    .updateInstructions(result, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    ForwardStretchModel._repetitionStatusColorModelSF.value
            }
        }

        yield()
    }

    override suspend fun repetitionCompletedProcessObservation() {
        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 4
        yield()

        delay(ForwardStretchModel.bufferInterval.toLong() * 1000)

        if (model.getShared().firstRepetition) {
            model.getShared().firstRepetition = false
        }
        if (!model.getShared().lastRepetition) {
            if (!ForwardStretchInPositionClassifier.check(person)) {
                // Go to start state only if its not the last repetition and not in position for the next rep
                enter(ForwardStretchModel.StartState())
            } else {
                // Go to repetition state if not last repetition and in position for next rep
                enter(ForwardStretchModel.RepetitionState())
            }
        } else {
            // Go to repetition state if its last rep regardless of whether user is in position
            enter(ForwardStretchModel.RepetitionState())
        }
        yield()
    }

    companion object {
        var model: Move = shared
    }
}
