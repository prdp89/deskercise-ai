package com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing

import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.GlobalSettings
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivityBridge
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveController
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.Classifiers.DbreathingInPositionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.Classifiers.DbreathingRepetitionClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.Classifiers.DbreathingRepetitionInProgressClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.Classifiers.DbreathingStartClassifier
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingModel.Companion._messageSF
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingModel.Companion.shared
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingModel.Companion.states
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import timber.log.Timber

/**
 * The Controller manages when the different states are transitioned based on the model's flag and classifier's results
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

open class DbreathingController(private val activity: MainActivityBridge) : MoveController() {

    init {
        var initial = true
        resetState()
        _exercisesStartButtonStateSF.value = true

        viewModelScope.launch {
            DbreathingModel.goodRepCounterModelSF.collect { value ->
                Timber.d(">> goodRepCounterModelSF")
                _goodRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            DbreathingModel.badRepCounterModelSF.collect { value ->
                Timber.d(">> badRepCounterModelSF")
                _badRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            DbreathingModel.currentRepCounterModelSF.collect { value ->
                _currentRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            _totalRepCounterSF.value = shared.remainingRepetitions
        }
    }

    override fun drawnJoints(): List<BodyPart> {
        return listOf(
            BodyPart.LEFT_SHOULDER,
            BodyPart.RIGHT_SHOULDER,

        )
    }

    override fun drawnJointPairs(): List<Pair<BodyPart, BodyPart>> {
        return listOf(
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
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
                DbreathingModel.InitialState::class -> initialProcessObservation()
                DbreathingModel.CalibrationState::class -> calibrationProcessObservation()
                DbreathingModel.StartState::class -> startProcessObservation()
                DbreathingModel.RepetitionState::class -> repetitionProcessObservation()
                DbreathingModel.RepetitionInitialState::class -> repetitionInitialProcessObservation()
                DbreathingModel.RepetitionInProgressState::class -> repetitionInProgressProcessObservation()
                DbreathingModel.RepetitionCompletedState::class -> repetitionCompletedProcessObservation()
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
            val newModel = DbreathingModel()
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

        DbreathingModel.repetitions = model.getShared().repetitions

        enter(DbreathingModel.CalibrationState())
        // reset
        model.getShared().updateToRightSide()

        // update drawn keypoints
        drawnJoints()
        drawnJointPairs()
        _updateDrawnKeyPointsSF.value = true

        DbreathingModel._goodRepCounterModelSF.value = 0
        DbreathingModel._badRepCounterModelSF.value = 0
        DbreathingModel._currentRepCounterModelSF.value = 0
        model.getShared().remainingRepetitionsLeft = model.getShared().repetitions
        model.getShared().remainingRepetitionsRight = model.getShared().repetitions
        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 1
    }

    override suspend fun calibrationProcessObservation() {
        isObservingCalibrationState = true
        val thresholdToBeInFrame = 400L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value

        while (isObservingCalibrationState && !didUserSkipSF.value) {
            waitIfPaused()
            val result = DbreathingStartClassifier.check(person)
            if (result) {
                val diff = System.currentTimeMillis() - startTimeOfBeingInFrame
                if (diff >= thresholdToBeInFrame) {
                    isObservingCalibrationState = false
                    if (Move.currentMoveState!!::class == DbreathingModel.CalibrationState::class) {
                        enter(DbreathingModel.StartState())
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

        delay(50)
        yield()
        while (isObservingStartProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = DbreathingInPositionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == DbreathingModel.StartState::class) {
                        if (enter(DbreathingModel.InPositionState())) {
                            if (model.getShared().firstRepetition) {
                                _displayCountersSF.value = true
                                _exerciseInstructionSF.value = _messageSF.value

                                for (i in DbreathingModel.positionCountdownDuration downTo 0) {
                                    waitIfPaused()
                                    _exerciseInstructionSF.value = _messageSF.value + "\n" + i
                                    delay(1000L) // this delay causes the coroutine to return the value to the UI
                                }
                                enter(DbreathingModel.RepetitionState())
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
        if (model.getShared().rightCompleted && model.getShared().leftCompleted) {
            model.getShared().leftCompleted = false
            model.getShared().rightCompleted = false
            DbreathingModel.repetitions -= 1
            model.getShared().firstRepetition = false
        }
        if (DbreathingModel.repetitions <= 0) {
            model.getShared().exerciseCompleted = true
            enter(DbreathingModel.EndState())
            model.getShared().updateToRightSide()
            _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal

            // update drawn keypoints
            drawnJoints()
            drawnJointPairs()
            _updateDrawnKeyPointsSF.value = true
            _exerciseInstructionSF.value = ""
            _displayCountersSF.value = false
            delay(100)
            yield()
            exitState()
            return
        }
        // To avoid risking going back into the rest of the states
        if (!model.getShared().rightCompleted) {
            model.getShared().updateToRightSide()
            _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal

            // update drawn keypoints
            drawnJoints()
            drawnJointPairs()
            _updateDrawnKeyPointsSF.value = true
            enter(DbreathingModel.RepetitionInitialState())
            return
        }
        if (!model.getShared().leftCompleted) {
            model.getShared().updateToLeftSide()
            _gifDirectionSF.value = Commons.Direction.LEFT.ordinal

            // update drawn keypoints
            drawnJoints()
            drawnJointPairs()
            _updateDrawnKeyPointsSF.value = true
            enter(DbreathingModel.RepetitionInitialState())
            return
        }
    }

    override suspend fun repetitionInitialProcessObservation() {
        isObservingRepetitionInitialProcess = true
        val thresholdToBeInFrame = 300L
        val startTimeOfBeingInFrame = System.currentTimeMillis()
        _exerciseInstructionSF.value = _messageSF.value
        yield()

        while (isObservingRepetitionInitialProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = DbreathingRepetitionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingRepetitionInitialProcess = false
                    enter(DbreathingModel.RepetitionInProgressState())
                }
            }

            yield()
        }
        yield()
    }

    override suspend fun repetitionInProgressProcessObservation() {
        val thresholdToBeInFrame: Long = (DbreathingModel.duration * 1000).toLong()
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
            var result = DbreathingRepetitionInProgressClassifier.check(person)
            model.waitingFrame += 1
            yield()

            if (remainingTime <= 0) {
                DbreathingModel.RepetitionInProgressState().updateInstructions(result, 0)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value = 0
                delay(100)
                yield()
                _exerciseInstructionSF.value = ""
                _repetitionStatusColorSF.value = 0
                delay(100)
                yield()
                isObservingRepetitionInProgressProcess = false
                if (Move.currentMoveState!!::class == DbreathingModel.RepetitionInProgressState::class) {
//                            if(enter(DbreathingModel.RepetitionCompletedState())){
//                                _exerciseInstructionSF.value = DbreathingModel._messageSF.value
//                                yield()
//                                delay(3000)

                    // based on side mark that side completed
                    Timber.d(">> model.getShared().currentSide=${model.getShared().currentSide}")
                    if (model.getShared().currentSide == Move.Side.right) {
                        _repetitionStatusColorSF.value = 5
                        DbreathingModel.RepetitionInitialState().updateInstruction()
                        _exerciseInstructionSF.value = "Hold breath"
                        for (i in 2 downTo 0) {
                            _repetitionStatusColorSF.value = 5
                            waitIfPaused()
                            _exerciseInstructionSF.value = "Hold breath\n\n$i"
                            delay(1000L) // this delay causes the coroutine to return the value to the UI
                        }
                        model.getShared().rightCompleted = true
                    } else {
                        model.getShared().leftCompleted = true
                        _currentRepCounterSF.value += 1
                    }

                    remainingTime -= 1
                    if (Move.currentMoveState!!::class == DbreathingModel.RepetitionInProgressState::class) {
                        enter(DbreathingModel.RepetitionCompletedState())
                    }
                    yield()

                    // UPDATE
//
//                    _goodRepCounterSF.value = Move.goodReps.div(2).toInt()
//                    _badRepCounterSF.value =
//                        _currentRepCounterSF.value - Move.goodReps.times(2).toInt()
//                    yield()
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

                DbreathingModel.RepetitionInProgressState()
                    .updateInstructions(lastResult, remainingTime)
                _exerciseInstructionSF.value = _messageSF.value
                _repetitionStatusColorSF.value =
                    0
            }
        }
        yield()
    }

    override suspend fun repetitionCompletedProcessObservation() {
        _exerciseInstructionSF.value = _messageSF.value
        while (AudioManagerService.tts.isSpeaking) {
            delay(100) // Time for Text-To-Speech to complete w/o being interrupted by next instruction
        }
        delay(50)

        if (model.getShared().leftCompleted && model.getShared().rightCompleted) {
            enter(DbreathingModel.RepetitionState())
            return
        }
        enter(DbreathingModel.RepetitionState())
        yield()
        return
    }

    companion object {
        var model: Move = shared
    }
}
