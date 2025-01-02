package com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch

import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.ValidFramePosition
import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.GlobalSettings
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivityBridge
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveController
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.Classifiers.HzShoulderStretchInPositionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.Classifiers.HzShoulderStretchRepetitionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.Classifiers.HzShoulderStretchRepetitionInProgressClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.Classifiers.HzShoulderStretchStartClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.HzShoulderStretchModel.Companion._messageSF
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.HzShoulderStretchModel.Companion.shared
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.HzShoulderStretchModel.Companion.states
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * The Controller manages when the different states are transitioned based on the model's flag and classifier's results
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

open class HzShoulderStretchController(private val activity: MainActivityBridge) : MoveController() {

    init {
        resetState()
        _exercisesStartButtonStateSF.value = true

        viewModelScope.launch {
            HzShoulderStretchModel.goodRepCounterModelSF.collect { value ->
                _goodRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            HzShoulderStretchModel.badRepCounterModelSF.collect { value ->
                _badRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            HzShoulderStretchModel.currentRepCounterModelSF.collect { value ->
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
        return when(_currentRepetitionState.value) {
            2, 3 -> {
                if (model.getShared().exerciseCompleted) {
                    null
                } else {
                    if (model.getShared().currentSide == Move.Side.right) {
                        val leftPositionValidFrame = 0
                        val rightPositionValidFrame = 0.5 * 480
                        val topPositionValidFrame = 0.4 * 640
                        val bottomPositionValidFrame = 0.75 * 640
                        ValidFramePosition(
                            left = leftPositionValidFrame.toFloat(),
                            top = topPositionValidFrame.toFloat(),
                            right = rightPositionValidFrame.toFloat(),
                            bottom = bottomPositionValidFrame.toFloat()
                        )
                    } else {
                        val leftPositionValidFrame = 0.5 * 480
                        val rightPositionValidFrame = 480
                        val topPositionValidFrame = 0.4 * 640
                        val bottomPositionValidFrame = 0.75 * 640
                        ValidFramePosition(
                            left = leftPositionValidFrame.toFloat(),
                            top = topPositionValidFrame.toFloat(),
                            right = rightPositionValidFrame.toFloat(),
                            bottom = bottomPositionValidFrame.toFloat()
                        )
                    }
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
                HzShoulderStretchModel.InitialState::class -> initialProcessObservation()
                HzShoulderStretchModel.CalibrationState::class -> calibrationProcessObservation()
                HzShoulderStretchModel.StartState::class -> startProcessObservation()
                HzShoulderStretchModel.InPositionState::class -> inPositionObservation()
                HzShoulderStretchModel.RepetitionState::class -> repetitionProcessObservation()
                HzShoulderStretchModel.RepetitionInitialState::class -> repetitionInitialProcessObservation()
                HzShoulderStretchModel.RepetitionInProgressState::class -> repetitionInProgressProcessObservation()
                HzShoulderStretchModel.RepetitionCompletedState::class -> repetitionCompletedProcessObservation()
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
            val newModel = HzShoulderStretchModel()
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

        HzShoulderStretchModel.repetitions = model.getShared().remainingRepetitions
        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 0
        // reset
        model.getShared().updateToRightSide()
        _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal

        enter(HzShoulderStretchModel.CalibrationState())
        HzShoulderStretchModel._goodRepCounterModelSF.value = 0
        HzShoulderStretchModel._badRepCounterModelSF.value = 0
        HzShoulderStretchModel._currentRepCounterModelSF.value = 0
        model.getShared().remainingRepetitionsLeft = model.getShared().repetitions
        model.getShared().remainingRepetitionsRight = model.getShared().repetitions
    }

    override suspend fun calibrationProcessObservation() {
        isObservingCalibrationState = true
        val thresholdToBeInFrame = 200L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 0

        while (isObservingCalibrationState && !didUserSkipSF.value) {
            waitIfPaused()
            val result = HzShoulderStretchStartClassifier.check(person)
            if (result) {
                val diff = System.currentTimeMillis() - startTimeOfBeingInFrame
                if (diff >= thresholdToBeInFrame) {
                    isObservingCalibrationState = false
                    if (Move.currentMoveState!!::class == HzShoulderStretchModel.CalibrationState::class) {
                        delay(3000)
                        enter(HzShoulderStretchModel.StartState())
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
        _currentRepetitionState.value = 1
        val thresholdToBeInFrame = 100L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        delay(50)
        yield()
        while (isObservingStartProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = HzShoulderStretchInPositionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == HzShoulderStretchModel.StartState::class) {
                        if (model.getShared().firstRepetition) {
                            if (enter(HzShoulderStretchModel.InPositionState())) {
                                _displayCountersSF.value = true
                                _exerciseInstructionSF.value = _messageSF.value
                                model.getShared().firstRepetition = false

                                for (i in 3 downTo 0) {
                                    waitIfPaused()
                                    _exerciseInstructionSF.value = _messageSF.value
                                    delay(1000L) // this delay causes the coroutine to return the value to the UI
                                }
                                enter(HzShoulderStretchModel.RepetitionState())
                            }
                        } else {
                            enter(HzShoulderStretchModel.RepetitionInitialState())
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

    private fun inPositionObservation() {
        _currentRepetitionState.value = 1
    }

    override suspend fun repetitionProcessObservation() {
        _currentRepetitionState.value = 3
        if (model.getShared().rightCompleted) {
            model.getShared().getShared().remainingRepetitionsRight -= 1
            if (model.getShared().remainingRepetitionsRight == 0) {
                model.getShared().currentSide = Move.Side.left
                _gifDirectionSF.value = Commons.Direction.LEFT.ordinal
                activity.updateDrawnKeyPoints()
                model.getShared().rightCompleted = false
                enter(HzShoulderStretchModel.StartState())
                return
            }

            if (HzShoulderStretchRepetitionClassifier.check(person)) {
                enter(HzShoulderStretchModel.RepetitionInitialState())
                model.getShared().leftCompleted = false
                return
            }
        }

        if (model.getShared().leftCompleted) {
            model.getShared().remainingRepetitionsLeft -= 1
            if (model.getShared().remainingRepetitionsLeft == 0) {
                model.getShared().exerciseCompleted = true

                enter(HzShoulderStretchModel.EndState())
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
            if (HzShoulderStretchInPositionClassifier.check(person)) {
                enter(HzShoulderStretchModel.RepetitionInitialState())
                model.getShared().leftCompleted = false
                return
            }
        }
        enter(HzShoulderStretchModel.RepetitionInitialState())
    }
    var flag = false
    override suspend fun repetitionInitialProcessObservation() {
        _currentRepetitionState.value = 3
        if (model.isShowValidFrame) {
            activity.updateDrawnKeyPoints()
        }

        isObservingRepetitionInitialProcess = true
        val thresholdToBeInFrame = 100L
        val startTimeOfBeingInFrame = System.currentTimeMillis()
        _exerciseInstructionSF.value = _messageSF.value
        yield()

        while (isObservingRepetitionInitialProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = HzShoulderStretchRepetitionClassifier.check(person)
            if (result) {
                flag = true
            }
            if (flag) {
                val result2 = HzShoulderStretchRepetitionInProgressClassifier.check(person)
                if (result2) {
                    if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                        isObservingRepetitionInitialProcess = false
                        delay(1000)
                        enter(HzShoulderStretchModel.RepetitionInProgressState())
                        flag = false
                    } }
            }

            yield()
        }
        yield()
    }

    override suspend fun repetitionInProgressProcessObservation() {
        _currentRepetitionState.value = 3
        val thresholdToBeInFrame: Long = (HzShoulderStretchModel.duration * 1000).toLong()
        var startTimeOfBeingInFrame = System.currentTimeMillis()
        var remainingTime: Long
        yield()

        isObservingRepetitionInProgressProcess = true
        _exerciseInstructionSF.value = _messageSF.value

        while (isObservingRepetitionInProgressProcess && !didUserSkipSF.value) {
            val newStartTimeOfBeingInFrame = (startTimeOfBeingInFrame)
            if (newStartTimeOfBeingInFrame != null) {
                startTimeOfBeingInFrame = newStartTimeOfBeingInFrame
            }
            yield()
            remainingTime =
                thresholdToBeInFrame - (System.currentTimeMillis() - startTimeOfBeingInFrame)
            val result = HzShoulderStretchRepetitionInProgressClassifier.check(person)
            yield()

            if (remainingTime <= 0) {
                HzShoulderStretchModel.RepetitionInProgressState().updateInstructions(result, 0)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    HzShoulderStretchModel._repetitionStatusColorModelSF.value
                delay(100)
                yield()
                _exerciseInstructionSF.value = ""
                _repetitionStatusColorSF.value = 0
                delay(100)
                yield()
                isObservingRepetitionInProgressProcess = false
                if (Move.currentMoveState!!::class == HzShoulderStretchModel.RepetitionInProgressState::class) {
                    // based on side mark that side completed
                    if (model.getShared().currentSide == Move.Side.right) {
                        model.getShared().rightCompleted = true
                    } else {
                        model.getShared().leftCompleted = true
                    }

                    enter(HzShoulderStretchModel.RepetitionCompletedState())
                    _exerciseInstructionSF.value = _messageSF.value

                    // UPDATE
                    _currentRepCounterSF.value += 1
                    _goodRepCounterSF.value = Move.goodReps.toInt()
                    _badRepCounterSF.value = _currentRepCounterSF.value - Move.goodReps.toInt()
                    yield()
                }
            } else {
                yield()
                HzShoulderStretchModel.RepetitionInProgressState()
                    .updateInstructions(result, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    HzShoulderStretchModel._repetitionStatusColorModelSF.value
            }
        }
        yield()
    }

    override suspend fun repetitionCompletedProcessObservation() {
        var flag = true
        _currentRepetitionState.value = 4
        _exerciseInstructionSF.value = _messageSF.value
        while (AudioManagerService.tts.isSpeaking) {
            delay(100) // Time for Text-To-Speech to complete w/o being interrupted by next instruction
        }
        delay(50)
        yield()

        if (model.getShared().leftCompleted && model.getShared().rightCompleted) {
            enter(HzShoulderStretchModel.RepetitionState())
            return
        }

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 4

        while (flag) {
            val resultInPosition = HzShoulderStretchInPositionClassifier.check(person)
            val resultRepetition = HzShoulderStretchRepetitionClassifier.check(person)
            if (resultInPosition && !resultRepetition) {
                enter(HzShoulderStretchModel.RepetitionState())
                flag = false
            }
        }

        yield()
        return
    }

    companion object {
        var model: Move = shared
    }
}
