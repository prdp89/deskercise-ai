package com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch

import android.annotation.SuppressLint
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.GlobalSettings
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivityBridge
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveController
import com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.Classifiers.*
import com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.PiriformisStretchModel.Companion._messageSF
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

@OptIn(ExperimentalCoroutinesApi::class)
class PiriformisStretchController(private val activity: MainActivityBridge) : MoveController() {

    init {
        resetState()
        _exercisesStartButtonStateSF.value = true

        viewModelScope.launch {
            PiriformisStretchModel.goodRepCounterModelSF.collect { value ->
                _goodRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            PiriformisStretchModel.badRepCounterModelSF.collect { value ->
                _badRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            PiriformisStretchModel.currentRepCounterModelSF.collect { value ->
                _currentRepCounterSF.value = value
            }
        }

        viewModelScope.launch {
            _totalRepCounterSF.value = PiriformisStretchModel.shared.remainingRepetitions * 2
        }
    }

    override fun drawnJoints(): List<BodyPart> {
        return if (model.getShared().currentSide == Move.Side.right) {
            listOf(
                BodyPart.LEFT_SHOULDER,
                BodyPart.RIGHT_HIP,
                BodyPart.RIGHT_KNEE,
                BodyPart.RIGHT_ANKLE,
            )
        } else {
            listOf(
                BodyPart.RIGHT_SHOULDER,
                BodyPart.LEFT_HIP,
                BodyPart.LEFT_KNEE,
                BodyPart.LEFT_ANKLE,
            )
        }
    }

    override fun drawnJointPairs(): List<Pair<BodyPart, BodyPart>> {
        return if (model.getShared().currentSide == Move.Side.right) {
            listOf(
                Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
                Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
                Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
                Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
            )
        } else {
            listOf(
                Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
                Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
                Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
                Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE),
            )
        }
    }

    override fun refreshRateOfKeyPointsInMs(): Long = GlobalSettings.getRefreshRateOfKeyPointsInMs()
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
            // get index of controller state
            val currentStateIndex = getStateIndexOfMatchingCurrentState()
            if (currentStateIndex < 0 || currentStateIndex >= PiriformisStretchModel.states.lastIndex) {
                Move.currentMoveState = PiriformisStretchModel.states.first()
            } else {
                Move.currentMoveState = PiriformisStretchModel.states[currentStateIndex + 1]
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
                PiriformisStretchModel.InitialState::class -> initialProcessObservation()
                PiriformisStretchModel.CalibrationState::class -> calibrationProcessObservation()
                PiriformisStretchModel.StartState::class -> startProcessObservation()
                PiriformisStretchModel.StartStateTwo::class -> startProcessObservationTwo()
                PiriformisStretchModel.RepetitionState::class -> repetitionProcessObservation()
                PiriformisStretchModel.RepetitionInitialState::class -> repetitionInitialProcessObservation()
                PiriformisStretchModel.RepetitionInProgressState::class -> repetitionInProgressProcessObservation()
                PiriformisStretchModel.RepetitionCompletedState::class -> repetitionCompletedProcessObservation()
            }
        }
    }

    override fun resetState() {
        viewModelScope.launch {
            cleanState()
            Move.currentMoveState = PiriformisStretchModel.states[0]
            _exerciseInstructionSF.value = ""

            // setRepetitions
            model = PiriformisStretchModel.shared

            Move.goodReps = 0F
            Move.totalReps = 0F

            // consider adding a reset function within model
            val newModel = PiriformisStretchModel()
            model.getShared().isSet = newModel.isSet
            model.getShared().currentSide = newModel.currentSide
            model.getShared().lastWristAnkleDistance = newModel.lastWristAnkleDistance
            model.getShared().leftCompleted = newModel.leftCompleted
            model.getShared().rightCompleted = newModel.rightCompleted
            model.getShared().repetitionResults = newModel.repetitionResults
            model.getShared().leftRepetitionResults = newModel.leftRepetitionResults
            model.getShared().rightRepetitionResults = newModel.rightRepetitionResults
            model.getShared().firstRepetition = newModel.firstRepetition
            model.getShared().duration = newModel.duration
            model.getShared().remainingDuration = newModel.remainingDuration
            model.remainingDuration = newModel.remainingDuration
            model.startTimeLeft = newModel.startTimeLeft
            model.repetitionDurationLeft = newModel.repetitionDurationLeft

            model.instructed = newModel.instructed
            model.firstExercise = newModel.firstExercise
            model.exerciseCompleted = newModel.exerciseCompleted
            model.currentGoodCount = newModel.currentGoodCount
            model.currentBadCount = newModel.currentBadCount
            model.goodFrames = newModel.goodFrames
            model.badFrames = newModel.badFrames

            activity.updateDrawnKeyPoints()
        }
    }

    override fun cleanState() {
        _exerciseStateDisplaySF.value = ""
        _exerciseInstructionSF.value = ""
        _currentRepetitionState.value = 1
        isObservingCalibrationState = false
        isObservingStartProcess = false
        isObservingRepetitionInProgressProcess = false
        isObservingRepetitionProcess = false

        if (PiriformisStretchModel.shared.currentSide == Move.Side.left) {
            _gifDirectionSF.value = Commons.Direction.LEFT.ordinal
        } else {
            _gifDirectionSF.value = Commons.Direction.RIGHT.ordinal
        }
    }

    private fun getStateIndexOfMatchingCurrentState(): Int {
        for (idx in 0 until PiriformisStretchModel.states.count()) {
            if (PiriformisStretchModel.states[idx]::class == Move.currentMoveState!!::class) {
                return idx
            }
        }
        return -1
    }

    override suspend fun initialProcessObservation() {
        yield()
        _currentRepetitionState.value = 0
        PiriformisStretchModel.repetitions = model.getShared().remainingRepetitions
        // Starting Stretch Logic
        _exerciseInstructionSF.value = _messageSF.value
        // reset
        PiriformisStretchModel._goodRepCounterModelSF.value = 0
        PiriformisStretchModel._badRepCounterModelSF.value = 0
        PiriformisStretchModel._currentRepCounterModelSF.value = 0
        model.getShared().remainingRepetitionsLeft = model.getShared().remainingRepetitions
        model.getShared().remainingRepetitionsRight = model.getShared().remainingRepetitions

        enter(PiriformisStretchModel.CalibrationState())
    }

    // person is sitting on the chair - first position
    override suspend fun calibrationProcessObservation() {
        isObservingCalibrationState = true
        val thresholdToBeInFrame = 50L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 0
        delay(50)
        yield()
        while (isObservingCalibrationState && !didUserSkipSF.value) {
            waitIfPaused()
            val result = PiriformisStretchStartClassifier.check(person)
            if (result) {
                val diff = System.currentTimeMillis() - startTimeOfBeingInFrame
                if (diff >= thresholdToBeInFrame) {
                    isObservingCalibrationState = false
                    if (Move.currentMoveState!!::class == PiriformisStretchModel.CalibrationState::class) {
                        enter(PiriformisStretchModel.StartState())
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
        _exerciseInstructionSF.value = _messageSF.value
        _currentRepetitionState.value = 1
        delay(4000)

        val thresholdToBeInFrame = 100L
        var startTimeOfBeingInFrame = System.currentTimeMillis()
        yield()

        while (isObservingStartProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = PiriformisStretchInPositionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == PiriformisStretchModel.StartState::class) {
                        enter(PiriformisStretchModel.StartStateTwo())
                    }
                    yield()
                }
            } else {
                startTimeOfBeingInFrame = System.currentTimeMillis()
            }
        }
        yield()
    }

    suspend fun startProcessObservationTwo() {
        _currentRepetitionState.value = 2

        isObservingStartProcess = true
        val thresholdToBeInFrame = 400L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        _exerciseInstructionSF.value = _messageSF.value

        delay(200)
        yield()
        while (isObservingStartProcess && !didUserSkipSF.value) {
            waitIfPaused()
            yield()
            val result = PiriformisStretchInPositionTwoClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingStartProcess = false
                    if (Move.currentMoveState!!::class == PiriformisStretchModel.StartStateTwo::class) {
                        if (model.getShared().firstRepetition) {
                            if (enter(TorsoStretchModel.InPositionState())) {
                                _displayCountersSF.value = true
                                _exerciseInstructionSF.value =
                                    TorsoStretchModel._messageSF.value

                                for (i in TorsoStretchModel.countdownDuration.toInt() downTo 0) {
                                    waitIfPaused()
                                    _exerciseInstructionSF.value =
                                        TorsoStretchModel._messageSF.value + "\n" + i
                                    delay(1000L) // this delay causes the coroutine to return the value to the UI
                                }
                                enter(TorsoStretchModel.RepetitionState())
                            }
//                                enter(TorsoStretchModel.RepetitionState())
                        } else {
                            if (enter(TorsoStretchModel.InPositionState())) {
//                                    if(TorsoStretchModel.firstLeftRepetition) {
//                                        TorsoStretchModel.firstLeftRepetition = false
//                                    }
                                enter(TorsoStretchModel.RepetitionState())
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
        _currentRepetitionState.value = 3
        if (model.getShared().rightCompleted) {
            model.getShared().getShared().remainingRepetitionsRight -= 1
            if (model.getShared().remainingRepetitionsRight <= 0) {
                model.getShared().currentSide = Move.Side.left
                _gifDirectionSF.value = Commons.Direction.LEFT.ordinal
                activity.updateDrawnKeyPoints()
                model.getShared().rightCompleted = false
                enter(PiriformisStretchModel.StartState())
                return
            }
            delay(300)
            enter(PiriformisStretchModel.RepetitionInitialState())
            model.getShared().rightCompleted = false
            return
        }

        if (model.getShared().leftCompleted) {
            model.getShared().remainingRepetitionsLeft -= 1
            if (model.getShared().remainingRepetitionsLeft <= 0) {
                model.getShared().exerciseCompleted = true

                enter(PiriformisStretchModel.ExerciseEndState())
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
            enter(PiriformisStretchModel.RepetitionInitialState())
            model.getShared().leftCompleted = false
            return
        }
        enter(PiriformisStretchModel.RepetitionInitialState())
    }

    override suspend fun repetitionInitialProcessObservation() {
        _currentRepetitionState.value = 3
        isObservingRepetitionInitialProcess = true
        _exerciseInstructionSF.value = _messageSF.value
        delay(3000)
        yield()

        val thresholdToBeInFrame = 100L
        var startTimeOfBeingInFrame = System.currentTimeMillis()

        while (isObservingRepetitionInitialProcess && !didUserSkipSF.value) {
            waitIfPaused()
            val result = PiriformisStretchRepetitionClassifier.check(person)
            if (result) {
                if (System.currentTimeMillis() - startTimeOfBeingInFrame >= thresholdToBeInFrame) {
                    isObservingRepetitionInitialProcess = false
                    enter(PiriformisStretchModel.RepetitionInProgressState())
                }
            } else {
                startTimeOfBeingInFrame = System.currentTimeMillis()
            }
            yield()
        }
        yield()
    }

    @SuppressLint("SuspiciousIndentation")
    override suspend fun repetitionInProgressProcessObservation() {
        _currentRepetitionState.value = 3
        val thresholdToBeInFrame: Long = (PiriformisStretchModel.duration * 1000).toLong()
        var startTimeOfBeingInFrame = System.currentTimeMillis()
        var remainingTime: Long
        yield()

        isObservingRepetitionInProgressProcess = true
        _exerciseInstructionSF.value = _messageSF.value

        model.waitingFrame = 0
        var lastFewFramesResults: MutableList<Boolean> = mutableListOf()
        var lastResult = true

        model.getShared().goodFrames = 0
        model.getShared().badFrames = 0
        yield()

        while (isObservingRepetitionInProgressProcess && !didUserSkipSF.value) {
            val newStartTimeOfBeingInFrame = waitIfPaused(startTimeOfBeingInFrame)
            if (newStartTimeOfBeingInFrame != null) {
                startTimeOfBeingInFrame = newStartTimeOfBeingInFrame
            }
            yield()
            remainingTime =
                thresholdToBeInFrame - (System.currentTimeMillis() - startTimeOfBeingInFrame)
            var result = PiriformisStretchRepetitionInProgressClassifier.check(person)
            delay(50)
            yield()
            model.waitingFrame += 1

            // trigger timer
            if (remainingTime <= 0) {
                yield()
                PiriformisStretchModel.RepetitionInProgressState()
                    .updateInstructions(result, remainingTime)
                _exerciseInstructionSF.value =
                    _messageSF.value + "\n" + (remainingTime) / 1000
                _repetitionStatusColorSF.value =
                    PiriformisStretchModel._repetitionStatusColorModelSF.value
                yield()
                _repetitionStatusColorSF.value = 0
                isObservingRepetitionInProgressProcess = false
                if (Move.currentMoveState!!::class == PiriformisStretchModel.RepetitionInProgressState::class) {
                    if (enter(PiriformisStretchModel.RepetitionCompletedState())) {
                        PiriformisStretchModel.shared.isSet = false
                        yield()
                        if (model.getShared().currentSide == Move.Side.right) {
                            model.getShared().rightCompleted = true
                        } else {
                            model.getShared().leftCompleted = true
                            model.waitingFrame = 0
                        }
                    }
                }
            } else {
                yield()
                lastFewFramesResults.add(result)
                if (model.waitingFrame >= 15) {
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

                PiriformisStretchModel.RepetitionInProgressState()
                    .updateInstructions(lastResult, remainingTime)

                _exerciseInstructionSF.value =
                    _messageSF.value + "\n" + (remainingTime) / 1000
                _repetitionStatusColorSF.value =
                    PiriformisStretchModel._repetitionStatusColorModelSF.value
                yield()
            }
        }
        model.getShared().goodFrames = 0
        model.getShared().badFrames = 0
        yield()
    }

    override suspend fun repetitionCompletedProcessObservation() {
        _currentRepetitionState.value = 3
        if (model.getShared().firstRepetition) {
            model.getShared().firstRepetition = false
        }

        isObservingRepetitionProcess = true

        _exerciseInstructionSF.value = _messageSF.value

        delay(3000)
        yield()

        while (isObservingRepetitionProcess && !didUserSkipSF.value) {
            waitIfPaused()
            PiriformisStretchModel.RepetitionCompletedState().updateInstructions()
            _repetitionStatusColorSF.value =
                PiriformisStretchModel._repetitionStatusColorModelSF.value
            yield()
            isObservingRepetitionProcess = false
            enter(PiriformisStretchModel.RepetitionState())

            delay(50)
            yield()
        }
        yield()
    }

    companion object {
        var model: Move = PiriformisStretchModel.shared
    }
}
