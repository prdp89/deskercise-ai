package com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch

import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.GlobalSettings
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivityBridge
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveController
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.Classifiers.UpperTrapStretchInPositionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.Classifiers.UpperTrapStretchRepetitionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.Classifiers.UpperTrapStretchRepetitionInProgressClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.Classifiers.UpperTrapStretchStartClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.UpperTrapStretchModel.Companion._messageSF
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.UpperTrapStretchModel.Companion.shared
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.UpperTrapStretchModel.Companion.states
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * The Controller manages when the different states are transitioned based on the model's flag and classifier's results
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

open class UpperTrapStretchController(private val activity: MainActivityBridge) : MoveController() {

    init {
        var initial = true
        resetState()
        _exercisesStartButtonStateSF.value = true

        viewModelScope.launch {
            UpperTrapStretchModel.goodRepCounterModelSF.collect { value ->
                _goodRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            UpperTrapStretchModel.badRepCounterModelSF.collect { value ->
                _badRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            UpperTrapStretchModel.currentRepCounterModelSF.collect { value ->
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
                UpperTrapStretchModel.InitialState::class -> initialProcessObservation()
                UpperTrapStretchModel.CalibrationState::class -> calibrationProcessObservation()
                UpperTrapStretchModel.StartState::class -> startProcessObservation()
                UpperTrapStretchModel.RepetitionState::class -> repetitionProcessObservation()
                UpperTrapStretchModel.RepetitionInitialState::class -> repetitionInitialProcessObservation()
                UpperTrapStretchModel.RepetitionInProgressState::class -> repetitionInProgressProcessObservation()
                UpperTrapStretchModel.RepetitionCompletedState::class -> repetitionCompletedProcessObservation()
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
            val newModel = UpperTrapStretchModel()
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

        UpperTrapStretchModel.repetitions = model.getShared().remainingRepetitions
        _currentRepetitionState.value = 0
        // reset
        model.getShared().updateToRightSide()
        _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal

        enter(UpperTrapStretchModel.CalibrationState())
        UpperTrapStretchModel._goodRepCounterModelSF.value = 0
        UpperTrapStretchModel._badRepCounterModelSF.value = 0
        UpperTrapStretchModel._currentRepCounterModelSF.value = 0
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
//            delay(3000)

        while (isObservingCalibrationState && !didUserSkipSF.value) {
            waitIfPaused()
            val result = UpperTrapStretchStartClassifier.check(person)
            if (result) {
                val diff = System.currentTimeMillis() - startTimeOfBeingInFrame
                if (diff >= thresholdToBeInFrame) {
                    isObservingCalibrationState = false
                    if (Move.currentMoveState!!::class == UpperTrapStretchModel.CalibrationState::class) {
                        _currentRepetitionState.value = 1
                        enter(UpperTrapStretchModel.StartState())
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

        isObservingStartProcess = true
        val thresholdToBeInFrame = 100L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 2

//            delay(3000)
        yield()
        while (isObservingStartProcess && !didUserSkipSF.value) {
            _currentRepetitionState.value = 2
            waitIfPaused()
            yield()
            val result = UpperTrapStretchInPositionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == UpperTrapStretchModel.StartState::class) {
                        if (model.getShared().firstRepetition) {
                            if (enter(UpperTrapStretchModel.InPositionState())) {
                                _displayCountersSF.value = true
                                _exerciseInstructionSF.value = _messageSF.value

                                model.getShared().firstRepetition = false

                                for (i in UpperTrapStretchModel.positionCountdownDuration downTo 0) {
                                    waitIfPaused()
                                    _exerciseInstructionSF.value = _messageSF.value + "\n" + i
                                    _currentRepetitionState.value = 2
                                    delay(1000L) // this delay causes the coroutine to return the value to the U
                                    _currentRepetitionState.value = 2
                                }
                                _currentRepetitionState.value = 2
                                enter(UpperTrapStretchModel.RepetitionState())
                            }
                        } else {
                            enter(UpperTrapStretchModel.RepetitionInitialState())
                        }
                    }
                    yield()
                }
            } else {
                _currentRepetitionState.value = 2
                startTimeOfBeingInFrame = System.currentTimeMillis()
            }
        }
        yield()
    }

    override suspend fun repetitionProcessObservation() {
        _currentRepetitionState.value = 2

        if (model.getShared().rightCompleted) {
            model.getShared().getShared().remainingRepetitionsRight -= 1
            if (model.getShared().remainingRepetitionsRight == 0) {
                model.getShared().currentSide = Move.Side.left
                _gifDirectionSF.value = Commons.Direction.LEFT.ordinal
                _currentRepetitionState.value = 2
                activity.updateDrawnKeyPoints()
                model.getShared().rightCompleted = false
                enter(UpperTrapStretchModel.StartState())
                return
            }
            _currentRepetitionState.value = 2
            delay(300)
            enter(UpperTrapStretchModel.RepetitionInitialState())
            model.getShared().rightCompleted = false
            return
        }

        if (model.getShared().leftCompleted) {
            model.getShared().remainingRepetitionsLeft -= 1
            if (model.getShared().remainingRepetitionsLeft == 0) {
                model.getShared().exerciseCompleted = true

                enter(UpperTrapStretchModel.EndState())
                _exerciseInstructionSF.value = ""
                _currentRepetitionState.value = 2
                _displayCountersSF.value = false

                delay(100)
                yield()

                exitState()

                model.getShared().currentSide = Move.Side.right
                _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal
                activity.updateDrawnKeyPoints()

                return
            }
            enter(UpperTrapStretchModel.RepetitionInitialState())
            _currentRepetitionState.value = 2
            model.getShared().leftCompleted = false
            return
        }
        _currentRepetitionState.value = 2
        enter(UpperTrapStretchModel.RepetitionInitialState())
    }

    override suspend fun repetitionInitialProcessObservation() {
        _currentRepetitionState.value = 3

        isObservingRepetitionInitialProcess = true
        val thresholdToBeInFrame = 100L
        val startTimeOfBeingInFrame = System.currentTimeMillis()
        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 3
//            delay(3000)
        yield()

        while (isObservingRepetitionInitialProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            _currentRepetitionState.value = 3
            val result = UpperTrapStretchRepetitionClassifier.check(person)
            if (result) {
                _currentRepetitionState.value = 3
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingRepetitionInitialProcess = false
                    _currentRepetitionState.value = 3
                    enter(UpperTrapStretchModel.RepetitionInProgressState())
                }
            }

            yield()
        }
        yield()
    }

    override suspend fun repetitionInProgressProcessObservation() {
        _currentRepetitionState.value = 3
        val thresholdToBeInFrame: Long = (UpperTrapStretchModel.duration * 1000).toLong()
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

            remainingTime =
                thresholdToBeInFrame - (System.currentTimeMillis() - startTimeOfBeingInFrame)
            val result = UpperTrapStretchRepetitionInProgressClassifier.check(person)
            yield()

            if (remainingTime <= 0) {
                _currentRepetitionState.value = 3
                UpperTrapStretchModel.RepetitionInProgressState().updateInstructions(result, 0)
                _exerciseInstructionSF.value = _messageSF.value
                _currentRepetitionState.value = 3
                _repetitionStatusColorSF.value =
                    UpperTrapStretchModel._repetitionStatusColorModelSF.value
                delay(100)

                _exerciseInstructionSF.value = ""
                _currentRepetitionState.value = 2
                _repetitionStatusColorSF.value = 0

                delay(100)

                isObservingRepetitionInProgressProcess = false
                if (Move.currentMoveState!!::class == UpperTrapStretchModel.RepetitionInProgressState::class) {
                    // based on side mark that side completed
                    if (model.getShared().currentSide == Move.Side.right) {
                        model.getShared().rightCompleted = true
                    } else {
                        model.getShared().leftCompleted = true
                    }

                    enter(UpperTrapStretchModel.RepetitionCompletedState())
                    _exerciseInstructionSF.value = _messageSF.value
                    _currentRepetitionState.value = 3

                    // UPDATE
                    _currentRepCounterSF.value += 1
                    _goodRepCounterSF.value = Move.goodReps.toInt()
                    _badRepCounterSF.value = _currentRepCounterSF.value - Move.goodReps.toInt()
                    yield()
                }
            } else {
                yield()
                UpperTrapStretchModel.RepetitionInProgressState()
                    .updateInstructions(result, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    UpperTrapStretchModel._repetitionStatusColorModelSF.value
            }
        }
        yield()
    }

    override suspend fun repetitionCompletedProcessObservation() {
        var flag = true
        _currentRepetitionState.value = 4
        if (model.getShared().remainingRepetitionsLeft == 1) {
            _exerciseInstructionSF.value = ""
            _currentRepetitionState.value = 2
            enter(UpperTrapStretchModel.RepetitionState())
            return
        }
        while (AudioManagerService.tts.isSpeaking) {
            delay(100) // Time for Text-To-Speech to complete w/o being interrupted by next instruction
            _currentRepetitionState.value = 4
        }
        delay(50)
        yield()
        _currentRepetitionState.value = 4
        _exerciseInstructionSF.value = _messageSF.value
        while (flag) {
            waitIfPaused()
            yield()
            val resultInPosition = UpperTrapStretchInPositionClassifier.check(person)
            val resultRepetition = UpperTrapStretchRepetitionClassifier.check(person)
            if (resultInPosition && !resultRepetition) {
                enter(UpperTrapStretchModel.RepetitionState())
                yield()
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
