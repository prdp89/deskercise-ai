package com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch

import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.GlobalSettings
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivityBridge
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveController
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.Classifiers.TorsoStretchInPositionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.Classifiers.TorsoStretchInPositionTwoClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.Classifiers.TorsoStretchRepetitionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.Classifiers.TorsoStretchRepetitionInProgressClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.Classifiers.TorsoStretchStartClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel.Companion._messageSF
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel.Companion.shared
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel.Companion.states
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * The Controller manages when the different states are transitioned based on the model's flag and classifier's results
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

open class TorsoStretchController(private val activity: MainActivityBridge) : MoveController() {

    init {
        var initial = true
        resetState()
        _exercisesStartButtonStateSF.value = true

        viewModelScope.launch {
            TorsoStretchModel.goodRepCounterModelSF.collect { value ->
                _goodRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            TorsoStretchModel.badRepCounterModelSF.collect { value ->
                _badRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            TorsoStretchModel.currentRepCounterModelSF.collect { value ->
                _currentRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            _totalRepCounterSF.value = shared.repetitions * 2
        }
    }

    override fun drawnJoints(): List<BodyPart> {
        return if (model.getShared().currentSide == Move.Side.left) {
            listOf(
                BodyPart.LEFT_ELBOW,
                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_KNEE,
            )
        } else {
            listOf(
                BodyPart.RIGHT_ELBOW,
                BodyPart.RIGHT_SHOULDER,
                BodyPart.RIGHT_KNEE,
            )
        }
    }

    override fun drawnJointPairs(): List<Pair<BodyPart, BodyPart>> {
        return listOf()
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
                TorsoStretchModel.InitialState::class -> initialProcessObservation()
                TorsoStretchModel.CalibrationState::class -> calibrationProcessObservation()
                TorsoStretchModel.StartState::class -> startProcessObservation()
                TorsoStretchModel.StartStateTwo::class -> startProcessObservationTwo()
                TorsoStretchModel.RepetitionState::class -> repetitionProcessObservation()
                TorsoStretchModel.RepetitionInitialState::class -> repetitionInitialProcessObservation()

                TorsoStretchModel.RepetitionInProgressState::class -> repetitionInProgressProcessObservation()
                TorsoStretchModel.RepetitionCompletedState::class -> repetitionCompletedProcessObservation()
            }

            // update drawn keypoints
            drawnJoints()
            drawnJointPairs()
            _updateDrawnKeyPointsSF.value = true
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
            val newModel = TorsoStretchModel()
//            TorsoStretchModel.firstLeftRepetition = true
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
        }
    }

    override fun cleanState() {
        _exerciseStateDisplaySF.value = ""
        _exerciseInstructionSF.value = ""
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
        _currentRepetitionState.value = 0
        TorsoStretchModel.repetitions = model.getShared().remainingRepetitions
        model.getShared().remainingRepetitionsRight = model.getShared().repetitions
        model.getShared().remainingRepetitionsLeft = model.getShared().repetitions

        enter(TorsoStretchModel.CalibrationState())
        // reset
        model.getShared().updateToRightSide()
        _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal

        TorsoStretchModel._goodRepCounterModelSF.value = 0
        TorsoStretchModel._badRepCounterModelSF.value = 0
        TorsoStretchModel._currentRepCounterModelSF.value = 0
        _exerciseInstructionSF.value = _messageSF.value

        // update drawn keypoints
        drawnJoints()
        drawnJointPairs()
        _updateDrawnKeyPointsSF.value = true
    }

    override suspend fun calibrationProcessObservation() {
        _currentRepetitionState.value = 0

        isObservingCalibrationState = true
        val thresholdToBeInFrame = 100L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value

        while (isObservingCalibrationState && !didUserSkipSF.value) {
            waitIfPaused()
            val result = TorsoStretchStartClassifier.check(person)
            if (result) {
                val diff = System.currentTimeMillis() - startTimeOfBeingInFrame
                if (diff >= thresholdToBeInFrame) {
                    isObservingCalibrationState = false
                    if (Move.currentMoveState!!::class == TorsoStretchModel.CalibrationState::class) {
                        enter(TorsoStretchModel.StartState())
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
        if (model.isShowValidFrame) {
            activity.updateDrawnKeyPoints()
        }

        isObservingStartProcess = true
        val thresholdToBeInFrame = 400L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value

        delay(50)
        yield()
        while (isObservingStartProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = TorsoStretchInPositionClassifier.check(person)
            if (result) {
                _currentRepetitionState.value = 2
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == TorsoStretchModel.StartState::class) {
                        if (model.getShared().firstRepetition) {
                            if (enter(TorsoStretchModel.InPositionState())) {
                                _displayCountersSF.value = true
                                _exerciseInstructionSF.value = _messageSF.value

                                for (i in TorsoStretchModel.countdownDuration.toInt() downTo 0) {
                                    waitIfPaused()
                                    _exerciseInstructionSF.value = _messageSF.value + "\n" + i
                                    delay(1000L) // this delay causes the coroutine to return the value to the UI
                                }
                                enter(TorsoStretchModel.StartStateTwo())
                            }
                        } else {
                            if (enter(TorsoStretchModel.InPositionState())) {
                                enter(TorsoStretchModel.StartStateTwo())
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

    private suspend fun startProcessObservationTwo() {
        _currentRepetitionState.value = 2

        isObservingStartProcess = true
        val thresholdToBeInFrame = 400L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        delay(2000L)

        yield()
        while (isObservingStartProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = TorsoStretchInPositionTwoClassifier.check(person)
            if (result) {
                delay(2000)
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == TorsoStretchModel.StartStateTwo::class) {
                        enter(TorsoStretchModel.RepetitionState())
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
        _currentRepetitionState.value = 2
        // Check if right exercise is done
        if (model.getShared().rightRepetitionDone) {
            model.getShared().remainingRepetitionsRight -= 1
            if (model.getShared().remainingRepetitionsRight <= 0) {
                model.getShared().updateToLeftSide()
                _gifDirectionSF.value = Commons.Direction.LEFT.ordinal

//                TorsoStretchModel.firstLeftRepetition = true
                // update drawn keypoints
                drawnJoints()
                drawnJointPairs()
                _updateDrawnKeyPointsSF.value = true
                if (model.isShowValidFrame) {
                    activity.updateDrawnKeyPoints()
                }
                enter(TorsoStretchModel.StartState())
                model.getShared().rightRepetitionDone = false
                return
            }
            enter(TorsoStretchModel.RepetitionInitialState())
            model.getShared().rightRepetitionDone = false
            return
        }
        model.getShared().firstRepetition = false
        // Check if left exercise is done
        if (model.getShared().leftRepetitionDone) {
            model.getShared().remainingRepetitionsLeft -= 1
            if (model.getShared().remainingRepetitionsLeft <= 0) {
                model.getShared().exerciseCompleted = true
                model.getShared().leftRepetitionDone = false
                model.getShared().rightRepetitionDone = false
                model.getShared().currentSide = Move.Side.left
                _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal
                model.firstRepetition = true
                if (model.isShowValidFrame) {
                    activity.updateDrawnKeyPoints()
                }

                enter(TorsoStretchModel.EndState())
                model.getShared().updateToRightSide()
                _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal

                _exerciseInstructionSF.value = ""
                _displayCountersSF.value = false
                delay(100)
                yield()
                exitState()
                return
            }
            enter(TorsoStretchModel.RepetitionInitialState())
            model.getShared().leftRepetitionDone = false
            return
        }
        enter(TorsoStretchModel.RepetitionInitialState())
    }

    override suspend fun repetitionInitialProcessObservation() {
        _currentRepetitionState.value = 3
        if (model.isShowValidFrame) {
            activity.updateDrawnKeyPoints()
        }

        isObservingRepetitionInitialProcess = true
        _exerciseInstructionSF.value = _messageSF.value
        yield()
        val thresholdToBeInFrame = 100L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        while (isObservingRepetitionInitialProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = TorsoStretchRepetitionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    waitIfPaused()
                    isObservingRepetitionInitialProcess = false
                    enter(TorsoStretchModel.RepetitionInProgressState())
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
        val thresholdToBeInFrame: Long = (TorsoStretchModel.duration * 1000).toLong()
        var startTimeOfBeingInFrame = System.currentTimeMillis()
        var remainingTime: Long
        yield()

        isObservingRepetitionInProgressProcess = true
        _exerciseInstructionSF.value = _messageSF.value

        model.waitingFrame = 0
        var lastFewFramesResults: MutableList<Boolean> = mutableListOf()
        var lastResult: Boolean = true

        model.getShared().goodFrames = 0
        model.getShared().badFrames = 0
        delay(50)
        yield()
        while (isObservingRepetitionInProgressProcess && !didUserSkipSF.value) {
            val newStartTimeOfBeingInFrame = waitIfPaused(startTimeOfBeingInFrame)
            if (newStartTimeOfBeingInFrame != null) {
                startTimeOfBeingInFrame = newStartTimeOfBeingInFrame
            }
            yield()
            remainingTime =
                thresholdToBeInFrame - (System.currentTimeMillis() - startTimeOfBeingInFrame)
            var result = TorsoStretchRepetitionInProgressClassifier.check(person)
            model.waitingFrame += 1

            if (remainingTime <= 0) {
                yield()
                _currentRepetitionState.value = 2
                TorsoStretchModel.RepetitionInProgressState()
                    .updateInstructions(result, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    TorsoStretchModel._repetitionStatusColorModelSF.value
                yield()
                _repetitionStatusColorSF.value = 0
                isObservingRepetitionInProgressProcess = false
                if (Move.currentMoveState!!::class == TorsoStretchModel.RepetitionInProgressState::class) {
                    if (model.getShared().currentSide == Move.Side.right) {
                        model.getShared().rightRepetitionDone = true
                    } else {
                        model.getShared().leftRepetitionDone = true
                    }
                    if (model.isShowValidFrame) {
                        activity.updateDrawnKeyPoints()
                    }
                    enter(TorsoStretchModel.RepetitionCompletedState())
                    _exerciseInstructionSF.value = _messageSF.value
                }
            } else {
                yield()
                if (model.waitingFrame >= 10000) {
                    // To reduce the flickering of results
                    lastFewFramesResults.add(result)
                    val trueCount = lastFewFramesResults.count { it }
                    val trueRate: Float =
                        (trueCount.toFloat() / lastFewFramesResults.size.toFloat())
                    result = (trueRate >= 0.5)
                    lastResult = result
                    yield()
                    TorsoStretchModel.RepetitionInProgressState()
                        .updateInstructions(lastResult, remainingTime)
                    _exerciseInstructionSF.value = _messageSF.value
                    _repetitionStatusColorSF.value =
                        TorsoStretchModel._repetitionStatusColorModelSF.value

                    lastFewFramesResults = mutableListOf()
                    model.waitingFrame = 0
                } else {
                    lastFewFramesResults.add(result)
                    TorsoStretchModel.RepetitionInProgressState()
                        .updateInstructions(lastResult, remainingTime)
                    yield()
                    _exerciseInstructionSF.value = _messageSF.value
                    _repetitionStatusColorSF.value =
                        TorsoStretchModel._repetitionStatusColorModelSF.value
                }
            }
        }
        model.getShared().goodFrames = 0
        model.getShared().badFrames = 0
        yield()
    }

    override suspend fun repetitionCompletedProcessObservation() {
        var flag = true

        if (model.getShared().remainingRepetitionsLeft == 1) {
            _exerciseInstructionSF.value = ""
            _currentRepetitionState.value = 2
            enter(TorsoStretchModel.RepetitionState())
            return
        }
        while (AudioManagerService.tts.isSpeaking) {
            delay(100)
        }

        delay(100)
        yield()

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 4

        yield()
        while (flag) {
            val resultInPosition = TorsoStretchInPositionClassifier.check(person)
            val resultRepetition = TorsoStretchRepetitionClassifier.check(person)
            if (resultInPosition && !resultRepetition) {
                enter(TorsoStretchModel.RepetitionState())
                flag = false
            }
        }
        yield()
    }

    companion object {
        var model: Move = shared
    }
}
