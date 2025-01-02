package com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch

import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.GlobalSettings
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivityBridge
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveController
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.Classifiers.NeckStretchInPositionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.Classifiers.NeckStretchRepetitionInProgressClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.Classifiers.NeckStretchStartClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.NeckStretchModel.Companion._messageSF
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.NeckStretchModel.Companion.shared
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.NeckStretchModel.Companion.states
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * The Controller manages when the different states are transitioned based on the model's flag and classifier's results
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

open class NeckStretchController(private val activity: MainActivityBridge) : MoveController() {

    init {
        var initial = true
        resetState()
        _exercisesStartButtonStateSF.value = true

        viewModelScope.launch {
            NeckStretchModel.goodRepCounterModelSF.collect { value ->
                _goodRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            NeckStretchModel.badRepCounterModelSF.collect { value ->
                _badRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            NeckStretchModel.currentRepCounterModelSF.collect { value ->
                _currentRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            _totalRepCounterSF.value = shared.repetitions * 2
        }
    }

    override fun drawnJoints(): List<BodyPart> {
        if (model.getShared().currentSide == Move.Side.right) {
            return listOf(
                BodyPart.RIGHT_EAR,
                BodyPart.LEFT_EAR,

                BodyPart.NECK,

                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_ELBOW,
                BodyPart.RIGHT_SHOULDER,
                BodyPart.RIGHT_ELBOW,

            )
        } else {
            return listOf(
                BodyPart.LEFT_EAR,
                BodyPart.RIGHT_EAR,
                BodyPart.NECK,

                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_ELBOW,
                BodyPart.RIGHT_SHOULDER,
                BodyPart.RIGHT_ELBOW,
            )
        }
    }

    override fun drawnJointPairs(): List<Pair<BodyPart, BodyPart>> {
        if (model.getShared().currentSide == Move.Side.right) {
            return listOf(
                Pair(BodyPart.RIGHT_EAR, BodyPart.NECK),
                Pair(BodyPart.LEFT_EAR, BodyPart.NECK),
                Pair(BodyPart.LEFT_SHOULDER, BodyPart.NECK),
                Pair(BodyPart.RIGHT_SHOULDER, BodyPart.NECK),

                Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
                Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
            )
        } else {
            return listOf(
                Pair(BodyPart.LEFT_EAR, BodyPart.NECK),
                Pair(BodyPart.RIGHT_EAR, BodyPart.NECK),
                Pair(BodyPart.LEFT_SHOULDER, BodyPart.NECK),
                Pair(BodyPart.RIGHT_SHOULDER, BodyPart.NECK),

                Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
                Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
            )
        }
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
                NeckStretchModel.InitialState::class -> initialProcessObservation()
                NeckStretchModel.CalibrationState::class -> calibrationProcessObservation()
                NeckStretchModel.StartState::class -> startProcessObservation()
                NeckStretchModel.RepetitionState::class -> repetitionProcessObservation()
                NeckStretchModel.RepetitionInitialState::class -> repetitionInitialProcessObservation()
                NeckStretchModel.RepetitionInProgressState::class -> repetitionInProgressProcessObservation()
                NeckStretchModel.RepetitionCompletedState::class -> repetitionCompletedProcessObservation()
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
            // consider adding a reset function within model
            val newModel = NeckStretchModel()
            model.getShared().repetitionResults = newModel.repetitionResults
            model.getShared().rightRepetitionResults = newModel.rightRepetitionResults
            model.getShared().leftRepetitionResults = newModel.leftRepetitionResults
            model.remainingDuration = newModel.remainingDuration
            model.startTimeLeft = newModel.startTimeLeft
            model.repetitionDurationLeft = newModel.repetitionDurationLeft
            model.getShared().firstRepetition = newModel.firstRepetition
            model.getShared().lastRepetition = newModel.lastRepetition
            model.getShared().repetitionDurationRemaining = newModel.repetitionDurationRemaining

            model.instructed = newModel.instructed
            model.firstExercise = newModel.firstExercise
            model.exerciseCompleted = newModel.exerciseCompleted
            model.currentGoodCount = newModel.currentGoodCount
            model.currentBadCount = newModel.currentBadCount
            model.goodFrames = newModel.goodFrames
            model.badFrames = newModel.badFrames
            model.getShared().leftRepetitionDone = newModel.leftRepetitionDone
            model.getShared().rightRepetitionDone = newModel.rightRepetitionDone
            model.getShared().rightCompleted = newModel.rightCompleted
            model.getShared().leftCompleted = newModel.leftCompleted
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

        if (shared.currentSide == Move.Side.left) {
            _gifDirectionSF.value = Commons.Direction.LEFT.ordinal
        } else {
            _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal
        }
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

        NeckStretchModel.repetitions = model.getShared().remainingRepetitions

        enter(NeckStretchModel.CalibrationState())
        // reset
        model.getShared().updateToRightSide()

        // update drawn keypoints
        drawnJoints()
        drawnJointPairs()
        _updateDrawnKeyPointsSF.value = true

        NeckStretchModel._goodRepCounterModelSF.value = 0
        NeckStretchModel._badRepCounterModelSF.value = 0
        NeckStretchModel._currentRepCounterModelSF.value = 0
        _currentRepetitionState.value = 0
        model.getShared().remainingRepetitionsLeft = model.getShared().repetitions
        model.getShared().remainingRepetitionsRight = model.getShared().repetitions
        _exerciseInstructionSF.value = _messageSF.value
    }

    override suspend fun calibrationProcessObservation() {
        isObservingCalibrationState = true
        val thresholdToBeInFrame = 400L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 0

        while (isObservingCalibrationState && !didUserSkipSF.value) {
            waitIfPaused()
            val result = NeckStretchStartClassifier.check(person)
            if (result) {
                val diff = System.currentTimeMillis() - startTimeOfBeingInFrame
                if (diff >= thresholdToBeInFrame) {
                    isObservingCalibrationState = false
                    if (Move.currentMoveState!!::class == NeckStretchModel.CalibrationState::class) {
                        enter(NeckStretchModel.StartState())
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
        val thresholdToBeInFrame = 100L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 1

        delay(50)
        yield()
        while (isObservingStartProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = NeckStretchInPositionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == NeckStretchModel.StartState::class) {
                        if (enter(NeckStretchModel.InPositionState())) {
                            if (model.getShared().firstRepetition) {
                                _displayCountersSF.value = true
                                _exerciseInstructionSF.value = _messageSF.value

                                for (i in NeckStretchModel.positionCountdownDuration downTo 0) {
                                    waitIfPaused()
                                    _exerciseInstructionSF.value = _messageSF.value + "\n" + i
                                    delay(1000L) // this delay causes the coroutine to return the value to the UI
                                }
                                enter(NeckStretchModel.RepetitionState())
                                _currentRepetitionState.value = 3
                            }
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
        if (model.getShared().rightCompleted) {
            model.getShared().remainingRepetitionsRight -= 1
            if (model.getShared().remainingRepetitionsRight == 0) {
                model.getShared().currentSide = Move.Side.left
                _gifDirectionSF.value = Commons.Direction.LEFT.ordinal
                activity.updateDrawnKeyPoints()
                model.getShared().rightCompleted = false
                enter(NeckStretchModel.RepetitionInitialState())
                return
            }
            delay(300)
            enter(NeckStretchModel.RepetitionInitialState())
            model.getShared().rightCompleted = false
            return
        }

        if (model.getShared().leftCompleted) {
            model.getShared().remainingRepetitionsLeft -= 1
            if (model.getShared().remainingRepetitionsLeft == 0) {
                model.getShared().exerciseCompleted = true

                enter(NeckStretchModel.EndState())
                _exerciseInstructionSF.value = ""
                _displayCountersSF.value = false

                delay(100)
                yield()

                exitState()

                model.getShared().currentSide = Move.Side.right
                _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal
                activity.updateDrawnKeyPoints()

                return
            }
            enter(NeckStretchModel.RepetitionInitialState())
            model.getShared().leftCompleted = false
            return
        }
        enter(NeckStretchModel.RepetitionInitialState())
    }

    var flag = false
    override suspend fun repetitionInitialProcessObservation() {
        _currentRepetitionState.value = 3

        isObservingRepetitionInitialProcess = true
        val thresholdToBeInFrame = 100L
        val startTimeOfBeingInFrame = System.currentTimeMillis()
        _exerciseInstructionSF.value = _messageSF.value
        yield()

        while (isObservingRepetitionInitialProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = NeckStretchInPositionClassifier.check(person)
            if (result) {
                flag = true
            }
            if (flag) {
                val result2 = NeckStretchRepetitionInProgressClassifier.check(person)
                if (result2) {
                    if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                        isObservingRepetitionInitialProcess = false
                        delay(1000)
                        enter(NeckStretchModel.RepetitionInProgressState())
                        flag = false
                    }
                }
            }

            yield()
        }
        yield()
    }

    override suspend fun repetitionInProgressProcessObservation() {
        _currentRepetitionState.value = 3
        val thresholdToBeInFrame: Long = (NeckStretchModel.duration * 1000).toLong()
        var startTimeOfBeingInFrame = System.currentTimeMillis()
        var remainingTime: Long
        yield()

        isObservingRepetitionInProgressProcess = true
        _exerciseInstructionSF.value = _messageSF.value

        model.waitingFrame = 0
        var lastFewFramesResults: MutableList<Boolean> = mutableListOf()
        var lastResult: Boolean = true

        while (isObservingRepetitionInProgressProcess && !didUserSkipSF.value) {
            val newStartTimeOfBeingInFrame = (startTimeOfBeingInFrame)
            if (newStartTimeOfBeingInFrame != null) {
                startTimeOfBeingInFrame = newStartTimeOfBeingInFrame
            }
            yield()
            remainingTime =
                thresholdToBeInFrame - (System.currentTimeMillis() - startTimeOfBeingInFrame)
            var result = NeckStretchRepetitionInProgressClassifier.check(person)
            model.waitingFrame += 1
            yield()

            if (remainingTime <= 0) {
                NeckStretchModel.RepetitionInProgressState().updateInstructions(result, 0)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    NeckStretchModel._repetitionStatusColorModelSF.value
                delay(100)
                yield()
                _exerciseInstructionSF.value = ""
                _repetitionStatusColorSF.value = 0
                delay(100)
                yield()
                isObservingRepetitionInProgressProcess = false
                if (Move.currentMoveState!!::class == NeckStretchModel.RepetitionInProgressState::class) {
//                            if(enter(NeckStretchModel.RepetitionCompletedState())){
//                                _exerciseInstructionSF.value = NeckStretchModel._messageSF.value
//                                yield()
//                                delay(3000)

                    // based on side mark that side completed
                    if (model.getShared().currentSide == Move.Side.right) {
                        model.getShared().rightCompleted = true
                    } else {
                        model.getShared().leftCompleted = true
                    }

                    enter(NeckStretchModel.RepetitionCompletedState())
                    _exerciseInstructionSF.value = _messageSF.value

                    // UPDATE
                    _currentRepCounterSF.value += 1
                    _goodRepCounterSF.value = Move.goodReps.toInt()
                    _badRepCounterSF.value = _currentRepCounterSF.value - Move.goodReps.toInt()
                    yield()
                }
            } else {
                yield()
                lastFewFramesResults.add(result)
                if (model.waitingFrame >= 100) {
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
                NeckStretchModel.RepetitionInProgressState()
                    .updateInstructions(lastResult, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    NeckStretchModel._repetitionStatusColorModelSF.value
            }
        }
        yield()
    }

    override suspend fun repetitionCompletedProcessObservation() {
        var flag1 = true
        _currentRepetitionState.value = 4
        _exerciseInstructionSF.value = _messageSF.value
        while (AudioManagerService.tts.isSpeaking) {
            delay(100) // Time for Text-To-Speech to complete w/o being interrupted by next instruction
        }
        delay(50)

        if (model.getShared().leftCompleted && model.getShared().rightCompleted) {
            enter(NeckStretchModel.RepetitionState())
            return
        }
        while (flag1) {
            waitIfPaused()
            yield()
            val result = NeckStretchInPositionClassifier.check(person)
            if (result) {
                flag = true
                flag1 = false
            }
            if (flag) {
                enter(NeckStretchModel.RepetitionState())
                yield()
                return
            }
        }
        yield()
    }

    companion object {
        var model: Move = shared
    }
}
