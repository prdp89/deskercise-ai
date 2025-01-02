package com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch

import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.ValidFramePosition
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.GlobalSettings
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivityBridge
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveController
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.Classifiers.OverheadTricepsStretchInPositionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.Classifiers.OverheadTricepsStretchInPositionTwoClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.Classifiers.OverheadTricepsStretchRepetitionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.Classifiers.OverheadTricepsStretchRepetitionInProgressClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.Classifiers.OverheadTricepsStretchStartClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.OverheadTricepsStretchModel.Companion._messageSF
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.OverheadTricepsStretchModel.Companion.shared
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.OverheadTricepsStretchModel.Companion.states
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * The Controller manages when the different states are transitioned based on the model's flag and classifier's results
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

open class OverheadTricepsStretchController(private val activity: MainActivityBridge) : MoveController() {

    init {
        var initial = true
        resetState()
        _exercisesStartButtonStateSF.value = true

        viewModelScope.launch {
            OverheadTricepsStretchModel.goodRepCounterModelSF.collect { value ->
                _goodRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            OverheadTricepsStretchModel.badRepCounterModelSF.collect { value ->
                _badRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            OverheadTricepsStretchModel.currentRepCounterModelSF.collect { value ->
                _currentRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            _totalRepCounterSF.value = shared.repetitions * 2
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

    override fun validFramePosition(): ValidFramePosition? {
        return  when (_currentRepetitionState.value) {
            2, 3 -> {
                if (model.getShared().exerciseCompleted) {
                    null
                } else {
                    if (model.getShared().currentSide == Move.Side.right) {
                        val leftPositionValidFrame = 0.3 * 480
                        val rightPositionValidFrame = 480
                        val topPositionValidFrame = 0
                        val bottomPositionValidFrame = 0.6 * 640
                        ValidFramePosition(
                            left = leftPositionValidFrame.toFloat(),
                            top = topPositionValidFrame.toFloat(),
                            right = rightPositionValidFrame.toFloat(),
                            bottom = bottomPositionValidFrame.toFloat()
                        )
                    } else {
                        val leftPositionValidFrame = 0
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
            }

            4 -> {
                if (model.getShared().currentSide == Move.Side.right) {
                    val leftPositionValidFrame = 0
                    val rightPositionValidFrame = 0.3 * 480
                    val topPositionValidFrame = 0
                    val bottomPositionValidFrame = 0.6 * 640
                    ValidFramePosition(
                        left = leftPositionValidFrame.toFloat(),
                        top = topPositionValidFrame.toFloat(),
                        right = rightPositionValidFrame.toFloat(),
                        bottom = bottomPositionValidFrame.toFloat()
                    )
                } else {
                    val leftPositionValidFrame = 0.7 * 480
                    val rightPositionValidFrame = 480
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
                OverheadTricepsStretchModel.InitialState::class -> initialProcessObservation()
                OverheadTricepsStretchModel.CalibrationState::class -> calibrationProcessObservation()
                OverheadTricepsStretchModel.StartState::class -> startProcessObservation()
                OverheadTricepsStretchModel.StartStateTwo::class -> startProcessObservationTwo()
                OverheadTricepsStretchModel.RepetitionState::class -> repetitionProcessObservation()
                OverheadTricepsStretchModel.RepetitionInitialState::class -> repetitionInitialProcessObservation()

                OverheadTricepsStretchModel.RepetitionInProgressState::class -> repetitionInProgressProcessObservation()
                OverheadTricepsStretchModel.RepetitionCompletedState::class -> repetitionCompletedProcessObservation()
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
            val newModel = OverheadTricepsStretchModel()
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
        OverheadTricepsStretchModel.repetitions = model.getShared().remainingRepetitions
        model.getShared().remainingRepetitionsRight = model.getShared().repetitions
        model.getShared().remainingRepetitionsLeft = model.getShared().repetitions

        enter(OverheadTricepsStretchModel.CalibrationState())
        // reset
        model.getShared().updateToRightSide()
        _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal

        OverheadTricepsStretchModel._goodRepCounterModelSF.value = 0
        OverheadTricepsStretchModel._badRepCounterModelSF.value = 0
        OverheadTricepsStretchModel._currentRepCounterModelSF.value = 0
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
            val result = OverheadTricepsStretchStartClassifier.check(person)
            if (result) {
                delay(1000)
                val diff = System.currentTimeMillis() - startTimeOfBeingInFrame
                if (diff >= thresholdToBeInFrame) {
                    isObservingCalibrationState = false
                    if (Move.currentMoveState!!::class == OverheadTricepsStretchModel.CalibrationState::class) {
                        enter(OverheadTricepsStretchModel.StartState())
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
        _currentRepetitionState.value = 2
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
            val result = OverheadTricepsStretchInPositionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == OverheadTricepsStretchModel.StartState::class) {
                        enter(OverheadTricepsStretchModel.StartStateTwo())
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
        _currentRepetitionState.value = 3

        isObservingStartProcess = true
        val thresholdToBeInFrame = 400L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        delay(2000L)

        yield()
        while (isObservingStartProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = OverheadTricepsStretchInPositionTwoClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == OverheadTricepsStretchModel.StartStateTwo::class) {
                        if (model.getShared().firstRepetition) {
                            if (enter(OverheadTricepsStretchModel.InPositionState())) {
                                _displayCountersSF.value = true
                                _exerciseInstructionSF.value = _messageSF.value

                                for (i in OverheadTricepsStretchModel.countdownDuration.toInt() downTo 0) {
                                    waitIfPaused()
                                    _exerciseInstructionSF.value = _messageSF.value + "\n" + i
                                    delay(1000L) // this delay causes the coroutine to return the value to the UI
                                }
                                enter(OverheadTricepsStretchModel.RepetitionState())
                            }
                        } else {
                            if (enter(OverheadTricepsStretchModel.InPositionState())) {
                                enter(OverheadTricepsStretchModel.RepetitionState())
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
        // Check if right exercise is done
        if (model.getShared().rightRepetitionDone) {
            model.getShared().remainingRepetitionsRight -= 1
            if (model.getShared().remainingRepetitionsRight <= 0) {
                model.getShared().updateToLeftSide()
                _gifDirectionSF.value = Commons.Direction.LEFT.ordinal
                // update drawn keypoints
                drawnJoints()
                drawnJointPairs()
                _updateDrawnKeyPointsSF.value = true
                if (model.isShowValidFrame) {
                    activity.updateDrawnKeyPoints()
                }
                enter(OverheadTricepsStretchModel.StartState())
                model.getShared().rightRepetitionDone = false
                return
            }
            enter(OverheadTricepsStretchModel.RepetitionInitialState())
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
                enter(OverheadTricepsStretchModel.EndState())
                model.getShared().updateToRightSide()
                _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal
                _exerciseInstructionSF.value = ""
                _displayCountersSF.value = false
                delay(100)
                yield()
                exitState()
                return
            }
            enter(OverheadTricepsStretchModel.RepetitionInitialState())
            model.getShared().leftRepetitionDone = false
            return
        }
        enter(OverheadTricepsStretchModel.RepetitionInitialState())
    }

    override suspend fun repetitionInitialProcessObservation() {
        _currentRepetitionState.value = 4
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
            val result = OverheadTricepsStretchRepetitionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    waitIfPaused()
                    isObservingRepetitionInitialProcess = false
                    enter(OverheadTricepsStretchModel.RepetitionInProgressState())
                }
            } else {
                startTimeOfBeingInFrame = System.currentTimeMillis()
            }

            yield()
        }
        yield()
    }

    override suspend fun repetitionInProgressProcessObservation() {
        _currentRepetitionState.value = 4
        val thresholdToBeInFrame: Long = (OverheadTricepsStretchModel.duration * 1000).toLong()
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
            var result = OverheadTricepsStretchRepetitionInProgressClassifier.check(person)
            model.waitingFrame += 1

            if (result) {
                enter(OverheadTricepsStretchModel.RepetitionInProgressState())
            }

            if (remainingTime <= 0) {
                yield()
                OverheadTricepsStretchModel.RepetitionInProgressState()
                    .updateInstructions(result, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    OverheadTricepsStretchModel._repetitionStatusColorModelSF.value
                yield()
                _repetitionStatusColorSF.value = 0
                isObservingRepetitionInProgressProcess = false
                if (Move.currentMoveState!!::class == OverheadTricepsStretchModel.RepetitionInProgressState::class) {
                    if (enter(OverheadTricepsStretchModel.RepetitionCompletedState())) {
                        _exerciseInstructionSF.value = _messageSF.value
                        yield()
                        delay(3000)
                        enter(OverheadTricepsStretchModel.RepetitionState())
                    }
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
                    OverheadTricepsStretchModel.RepetitionInProgressState()
                        .updateInstructions(lastResult, remainingTime)
                    _exerciseInstructionSF.value = _messageSF.value
                    _repetitionStatusColorSF.value =
                        OverheadTricepsStretchModel._repetitionStatusColorModelSF.value

                    lastFewFramesResults = mutableListOf()
                    model.waitingFrame = 0
                } else {
                    lastFewFramesResults.add(result)
                    OverheadTricepsStretchModel.RepetitionInProgressState()
                        .updateInstructions(lastResult, remainingTime)
                    yield()
                    _exerciseInstructionSF.value = _messageSF.value
                    _repetitionStatusColorSF.value =
                        OverheadTricepsStretchModel._repetitionStatusColorModelSF.value
                }
            }
        }
        model.getShared().goodFrames = 0
        model.getShared().badFrames = 0
        yield()
    }

    override suspend fun repetitionCompletedProcessObservation() {
        var flag = true
        _currentRepetitionState.value = 4
        if (model.getShared().remainingRepetitionsLeft == 1) {
            _exerciseInstructionSF.value = ""
            _currentRepetitionState.value = 3
            enter(OverheadTricepsStretchModel.RepetitionState())
            return
        }
        _exerciseInstructionSF.value = _messageSF.value
        yield()
        while (flag) {
            val resultInPosition = OverheadTricepsStretchInPositionTwoClassifier.check(person)
            val resultRepetition = OverheadTricepsStretchRepetitionClassifier.check(person)
            if (resultInPosition && !resultRepetition) {
                enter(OverheadTricepsStretchModel.RepetitionState())
                flag = false
            }
        }
        yield()
    }

    companion object {
        var model: Move = shared
    }
}
