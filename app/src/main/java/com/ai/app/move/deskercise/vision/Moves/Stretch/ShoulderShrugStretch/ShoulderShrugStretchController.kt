package com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch

import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.services.GlobalSettings
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivityBridge
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveController
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.Classifiers.ShoulderShrugStretchInPositionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.Classifiers.ShoulderShrugStretchRepetitionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.Classifiers.ShoulderShrugStretchRepetitionInProgressClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.Classifiers.ShoulderShrugStretchStartClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.ShoulderShrugStretchModel.Companion._messageSF
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.ShoulderShrugStretchModel.Companion.shared
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.ShoulderShrugStretchModel.Companion.states
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * The Controller manages when the different states are transitioned based on the model's flag and classifier's results
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

open class ShoulderShrugStretchController(private val activity: MainActivityBridge) :
    MoveController() {

    init {
        resetState()
        _exercisesStartButtonStateSF.value = true

        viewModelScope.launch {
            ShoulderShrugStretchModel.goodRepCounterModelSF.collect { value ->
                _goodRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            ShoulderShrugStretchModel.badRepCounterModelSF.collect { value ->
                _badRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            ShoulderShrugStretchModel.currentRepCounterModelSF.collect { value ->
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
            BodyPart.NOSE,
            BodyPart.NECK,

        )
    }

    override fun drawnJointPairs(): List<Pair<BodyPart, BodyPart>> {
        return listOf(
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.NECK),
            Pair(BodyPart.NECK, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.NECK, BodyPart.NOSE),
            Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER),
            Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
            Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),

        )
    }

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
                ShoulderShrugStretchModel.InitialState::class -> initialProcessObservation()
                ShoulderShrugStretchModel.CalibrationState::class -> calibrationProcessObservation()
                ShoulderShrugStretchModel.StartState::class -> startProcessObservation()
                ShoulderShrugStretchModel.RepetitionState::class -> repetitionProcessObservation()
                ShoulderShrugStretchModel.RepetitionInitialState::class -> repetitionInitialProcessObservation()
                ShoulderShrugStretchModel.RepetitionInProgressState::class -> repetitionInProgressProcessObservation()
                ShoulderShrugStretchModel.RepetitionCompletedState::class -> repetitionCompletedProcessObservation()
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
            val newModel = ShoulderShrugStretchModel()
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
        _currentRepetitionState.value = 1
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
        // Starting Strech Logic
        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 0
        // reset
        _goodRepCounterSF.value = 0
        _badRepCounterSF.value = 0
        _currentRepCounterSF.value = 0
        ShoulderShrugStretchModel._goodRepCounterModelSF.value = 0
        ShoulderShrugStretchModel._badRepCounterModelSF.value = 0
        ShoulderShrugStretchModel._currentRepCounterModelSF.value = 0
        model.getShared().currentGoodCount = 0
        model.getShared().currentBadCount = 0
        model.getShared().remainingRepetitions = model.getShared().repetitions
        enter(ShoulderShrugStretchModel.CalibrationState())
    }

    override suspend fun calibrationProcessObservation() {
        isObservingCalibrationState = true
        val thresholdToBeInFrame = 100L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 0

        while (isObservingCalibrationState && !didUserSkipSF.value) {
            waitIfPaused()
            val result = ShoulderShrugStretchStartClassifier.check(person)
            if (result) {
                val diff = System.currentTimeMillis() - startTimeOfBeingInFrame
                if (diff >= thresholdToBeInFrame) {
                    isObservingCalibrationState = false
                    if (Move.currentMoveState!!::class == ShoulderShrugStretchModel.CalibrationState::class) {
                        enter(ShoulderShrugStretchModel.StartState())
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
        _currentRepetitionState.value = 1

        isObservingStartProcess = true
        val thresholdToBeInFrame = 50L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value

        delay(50)
        yield()
        while (isObservingStartProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = ShoulderShrugStretchInPositionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == ShoulderShrugStretchModel.StartState::class) {
                        if (enter(ShoulderShrugStretchModel.InPositionState())) {
                            if (model.getShared().firstRepetition) {
                                _displayCountersSF.value = true
                                _exerciseInstructionSF.value = _messageSF.value
                                for (i in 3 downTo 0) {
                                    waitIfPaused()
                                    _exerciseInstructionSF.value = _messageSF.value + "\n" + i
                                    delay(1000L) // this delay causes the coroutine to return the value to the UI
                                }
                            }
                            enter(ShoulderShrugStretchModel.RepetitionState())
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

            enter(ShoulderShrugStretchModel.ExerciseEndState())
            _exerciseInstructionSF.value = ""
            _displayCountersSF.value = false
            delay(100)
            yield()

            // Check Queue
            exitState()

            return
        } else {
            enter(ShoulderShrugStretchModel.RepetitionInitialState())
            return
        }
    }

    override suspend fun repetitionInitialProcessObservation() {
        _currentRepetitionState.value = 3

        isObservingRepetitionInitialProcess = true
        val thresholdToBeInFrame = 50L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        yield()

        while (isObservingRepetitionInitialProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = ShoulderShrugStretchRepetitionClassifier.check(person)
            if (result) {
                waitIfPaused()
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingRepetitionInitialProcess = false
                    enter(ShoulderShrugStretchModel.RepetitionInProgressState())
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
        val thresholdToBeInFrame: Long = (ShoulderShrugStretchModel.duration * 1000).toLong()
        var startTimeOfBeingInFrame = System.currentTimeMillis()
        var remainingTime: Long
        yield()

        isObservingRepetitionInProgressProcess = true
        model.waitingFrame = 0
        var lastFewFramesResults: MutableList<Boolean> = mutableListOf()
        var lastResult: Boolean = true

        _exerciseInstructionSF.value = _messageSF.value

        while (isObservingRepetitionInProgressProcess && !didUserSkipSF.value) {
            val newStartTimeOfBeingInFrame = waitIfPaused(startTimeOfBeingInFrame)
            if (newStartTimeOfBeingInFrame != null) {
                startTimeOfBeingInFrame = newStartTimeOfBeingInFrame
            }
            yield()
            remainingTime =
                thresholdToBeInFrame - (System.currentTimeMillis() - startTimeOfBeingInFrame)
            var result = ShoulderShrugStretchRepetitionInProgressClassifier.check(person)
            model.waitingFrame += 1

            if (result) {
                enter(ShoulderShrugStretchModel.RepetitionInProgressState())
            }

            if (remainingTime <= 0) {
                yield()
                ShoulderShrugStretchModel.RepetitionInProgressState()
                    .updateInstructions(result, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    ShoulderShrugStretchModel._repetitionStatusColorModelSF.value
                _repetitionStatusColorSF.value = 0
                yield()
                isObservingRepetitionInProgressProcess = false
                if (Move.currentMoveState!!::class == ShoulderShrugStretchModel.RepetitionInProgressState::class) {
                    if (enter(ShoulderShrugStretchModel.RepetitionCompletedState())) {
                        model.waitingFrame = 0
                    }
                }
            } else {
                yield()
                lastFewFramesResults.add(result)

                if (model.waitingFrame >= 5000) {
                    // To reduce the flickering of results
                    val trueCount = lastFewFramesResults.count { it }
                    val trueRate: Float =
                        (trueCount.toFloat() / lastFewFramesResults.size.toFloat())
                    result = (trueRate >= 0.5)
                    lastResult = result

                    yield()

                    lastFewFramesResults = mutableListOf()
                    model.waitingFrame = 0
                }
                ShoulderShrugStretchModel.RepetitionInProgressState()
                    .updateInstructions(lastResult, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    ShoulderShrugStretchModel._repetitionStatusColorModelSF.value
                yield()
            }
        }
        yield()
    }

    override suspend fun repetitionCompletedProcessObservation() {
        var flag = true
        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 4
        yield()

        delay(ShoulderShrugStretchModel.bufferInterval.toLong() * 1000)

        if (model.getShared().firstRepetition) {
            model.getShared().firstRepetition = false
        }
        if (model.getShared().lastRepetition) {
            enter(ShoulderShrugStretchModel.RepetitionState())
            return
        }
        while (flag) {
            val resultInPosition = ShoulderShrugStretchInPositionClassifier.check(person)
            val resultRepetition = ShoulderShrugStretchRepetitionClassifier.check(person)
            if (resultInPosition && !resultRepetition) {
                enter(ShoulderShrugStretchModel.RepetitionState())
                flag = false
            }
        }
        yield()
    }

    companion object {
        var model: Move = shared
    }
}
