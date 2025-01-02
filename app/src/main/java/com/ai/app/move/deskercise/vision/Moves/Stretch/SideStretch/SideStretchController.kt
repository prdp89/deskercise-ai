package com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch

import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.GlobalSettings
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivityBridge
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveController
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.Classifiers.SideStretchInPositionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.Classifiers.SideStretchRepetitionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.Classifiers.SideStretchRepetitionInProgressClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.Classifiers.SideStretchStartClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.SideStretchModel.Companion._messageSF
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.SideStretchModel.Companion.shared
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.SideStretchModel.Companion.states
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * The Controller manages when the different states are transitioned based on the model's flag and classifier's results
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

open class SideStretchController(private val activity: MainActivityBridge) : MoveController() {

    init {
        var initial = true
        resetState()
        _exercisesStartButtonStateSF.value = true

        viewModelScope.launch {
            SideStretchModel.goodRepCounterModelSF.collect { value ->
                _goodRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            SideStretchModel.badRepCounterModelSF.collect { value ->
                _badRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            SideStretchModel.currentRepCounterModelSF.collect { value ->
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
                SideStretchModel.InitialState::class -> initialProcessObservation()
                SideStretchModel.CalibrationState::class -> calibrationProcessObservation()
                SideStretchModel.StartState::class -> startProcessObservation()
                SideStretchModel.RepetitionState::class -> repetitionProcessObservation()
                SideStretchModel.RepetitionInitialState::class -> repetitionInitialProcessObservation()
                SideStretchModel.RepetitionInProgressState::class -> repetitionInProgressProcessObservation()
                SideStretchModel.RepetitionCompletedState::class -> repetitionCompletedProcessObservation()
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
            val newModel = SideStretchModel()
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
        _currentRepetitionState.value = 1
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

        SideStretchModel.repetitions = model.getShared().remainingRepetitions
        // reset
        model.getShared().updateToRightSide()
        _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal

        enter(SideStretchModel.CalibrationState())
        SideStretchModel._goodRepCounterModelSF.value = 0
        SideStretchModel._badRepCounterModelSF.value = 0
        SideStretchModel._currentRepCounterModelSF.value = 0
        model.getShared().remainingRepetitionsLeft = model.getShared().repetitions
        model.getShared().remainingRepetitionsRight = model.getShared().repetitions
        _exerciseInstructionSF.value = _messageSF.value
    }

    override suspend fun calibrationProcessObservation() {
        isObservingCalibrationState = true
        val thresholdToBeInFrame = 200L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 0

        while (isObservingCalibrationState && !didUserSkipSF.value) {
            waitIfPaused()
            val result = SideStretchStartClassifier.check(person)
            if (result) {
                val diff = System.currentTimeMillis() - startTimeOfBeingInFrame
                if (diff >= thresholdToBeInFrame) {
                    isObservingCalibrationState = false
                    if (Move.currentMoveState!!::class == SideStretchModel.CalibrationState::class) {
                        enter(SideStretchModel.StartState())
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
            delay(2000)
            val result = SideStretchInPositionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == SideStretchModel.StartState::class) {
                        if (model.getShared().firstRepetition) {
                            if (enter(SideStretchModel.InPositionState())) {
                                _displayCountersSF.value = true
                                _exerciseInstructionSF.value = _messageSF.value
                                model.getShared().firstRepetition = false

                                for (i in SideStretchModel.positionCountdownDuration downTo 0) {
                                    waitIfPaused()
                                    _exerciseInstructionSF.value = _messageSF.value + "\n" + i
                                    delay(1000L) // this delay causes the coroutine to return the value to the UI
                                }
                                enter(SideStretchModel.RepetitionState())
                            }
                        } else {
                            enter(SideStretchModel.RepetitionInitialState())
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
            model.getShared().getShared().remainingRepetitionsRight -= 1
            if (model.getShared().remainingRepetitionsRight == 0) {
                model.getShared().currentSide = Move.Side.left
                _gifDirectionSF.value = Commons.Direction.LEFT.ordinal
                activity.updateDrawnKeyPoints()
                model.getShared().rightCompleted = false
                enter(SideStretchModel.StartState())
                return
            }
            delay(300)
            enter(SideStretchModel.RepetitionInitialState())
            model.getShared().rightCompleted = false
            return
        }

        if (model.getShared().leftCompleted) {
            model.getShared().remainingRepetitionsLeft -= 1
            if (model.getShared().remainingRepetitionsLeft == 0) {
                model.getShared().exerciseCompleted = true

                enter(SideStretchModel.EndState())
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
            enter(SideStretchModel.RepetitionInitialState())
            model.getShared().leftCompleted = false
            return
        }
        enter(SideStretchModel.RepetitionInitialState())
    }

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
            val result = SideStretchRepetitionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingRepetitionInitialProcess = false
                    enter(SideStretchModel.RepetitionInProgressState())
                }
            }

            yield()
        }
        yield()
    }

    override suspend fun repetitionInProgressProcessObservation() {
        _currentRepetitionState.value = 3
        val thresholdToBeInFrame: Long = (SideStretchModel.duration * 1000).toLong()
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
            val result = SideStretchRepetitionInProgressClassifier.check(person)
            yield()

            if (remainingTime <= 0) {
                SideStretchModel.RepetitionInProgressState().updateInstructions(result, 0)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    SideStretchModel._repetitionStatusColorModelSF.value
                delay(100)
                yield()
                _exerciseInstructionSF.value = ""
                _repetitionStatusColorSF.value = 0
                delay(100)
                yield()
                isObservingRepetitionInProgressProcess = false
                if (Move.currentMoveState!!::class == SideStretchModel.RepetitionInProgressState::class) {
                    // based on side mark that side completed
                    if (model.getShared().currentSide == Move.Side.right) {
                        model.getShared().rightCompleted = true
                    } else {
                        model.getShared().leftCompleted = true
                    }

                    enter(SideStretchModel.RepetitionCompletedState())
                    _exerciseInstructionSF.value = _messageSF.value

                    // UPDATE
                    _currentRepCounterSF.value += 1
                    _goodRepCounterSF.value = Move.goodReps.toInt()
                    _badRepCounterSF.value = _currentRepCounterSF.value - Move.goodReps.toInt()
                    yield()
                }
            } else {
                yield()
                SideStretchModel.RepetitionInProgressState()
                    .updateInstructions(result, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    SideStretchModel._repetitionStatusColorModelSF.value
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
            enter(SideStretchModel.RepetitionState())
            return
        }

        while (flag) {
            waitIfPaused()
            yield()
            val resultInPosition = SideStretchInPositionClassifier.check(person)
            val resultRepetition = SideStretchRepetitionClassifier.check(person)
            if (resultInPosition && !resultRepetition) {
                enter(SideStretchModel.RepetitionState())
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
