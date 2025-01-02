package com.ai.app.move.deskercise.vision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.data.ValidFramePosition
import com.ai.app.move.deskercise.services.GlobalSettings
import com.ai.app.move.deskercise.services.UIInstructionText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * MoveController: Contains all the base logic to handle & display the statemachine and it's instructions to be displayed.
 * This class is inherited by all exercise Controllers
 */

open class MoveController : ViewModel() {

    var _exerciseStateDisplaySF = MutableStateFlow("")
    var _gifDirectionSF = MutableStateFlow(0) // 0- Right, 1-Left
    val gifDirectionSF = _gifDirectionSF.asStateFlow()
    var _exerciseInstructionSF = MutableStateFlow("")
    val exerciseInstructionSF = _exerciseInstructionSF.asStateFlow()
    var _countdownSF = MutableStateFlow("")
    private var _midInstructionMessageSF = MutableStateFlow("")
    val midInstructionMessageSF = _midInstructionMessageSF.asStateFlow()
    private var _midInstructionTimerSF = MutableStateFlow("")
    val midInstructionTimerSF = _midInstructionTimerSF.asStateFlow()
    var _repetitionStatusColorSF = MutableStateFlow(0)
    val repetitionStatusColorSF = _repetitionStatusColorSF.asStateFlow()
    var _exerciseCompletedSF = MutableStateFlow(false)
    val exerciseCompletedSF = _exerciseCompletedSF.asStateFlow()
    var _exercisesStartButtonStateSF = MutableStateFlow(false)
    var _didUserExitSF = MutableStateFlow(false)
    val didUserExitSF = _didUserExitSF.asStateFlow()
    var _didUserSkipSF = MutableStateFlow(false)
    val didUserSkipSF = _didUserSkipSF.asStateFlow()
    var _displayCountersSF = MutableStateFlow(false)
    val displayCountersSF = _displayCountersSF.asStateFlow()
    var _updateDrawnKeyPointsSF = MutableStateFlow(false)
    val updateDrawnKeyPointsSF = _updateDrawnKeyPointsSF.asStateFlow()
    var _goodRepCounterSF = MutableStateFlow(0)
    val goodRepCounterSF = _goodRepCounterSF.asStateFlow()
    var _badRepCounterSF = MutableStateFlow(0)
    val badRepCounterSF = _badRepCounterSF.asStateFlow()
    var _currentRepCounterSF = MutableStateFlow(0)
    val currentRepCounterSF = _currentRepCounterSF.asStateFlow()
    var _totalRepCounterSF = MutableStateFlow(0)
    val totalRepCounterSF = _totalRepCounterSF.asStateFlow()
    var _currentRepetitionState = MutableStateFlow(0)
    val currentRepetitionState = _currentRepetitionState.asStateFlow()
    var isObservingCalibrationState = false
    var isObservingStartProcess = false
    var isObservingRepetitionInitialProcess = false
    var isObservingRepetitionInProgressProcess = false
    var isObservingRepetitionProcess = false
    var firstRepetition: Boolean = true
    var lastRepetition: Boolean = false
    open lateinit var model: Move
    open fun welcome() {
    }

    open fun drawnJoints(): List<BodyPart> {
        return listOf(
            BodyPart.NOSE,
            BodyPart.LEFT_EYE,
            BodyPart.LEFT_EAR,
            BodyPart.LEFT_SHOULDER,
            BodyPart.LEFT_ELBOW,
            BodyPart.LEFT_HIP,
            BodyPart.LEFT_WRIST,
            BodyPart.LEFT_KNEE,
            BodyPart.RIGHT_EYE,
            BodyPart.RIGHT_EAR,
            BodyPart.RIGHT_SHOULDER,
            BodyPart.RIGHT_ELBOW,
            BodyPart.RIGHT_HIP,
            BodyPart.RIGHT_WRIST,
            BodyPart.RIGHT_KNEE,
        )
    }

    open fun drawnJointPairs(): List<Pair<BodyPart, BodyPart>> {
        return listOf(
            Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
            Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
            Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
            Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),
            Pair(BodyPart.NOSE, BodyPart.LEFT_SHOULDER),
            Pair(BodyPart.NOSE, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
            Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
            Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
            Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
            Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
            Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
            Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
            Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
            Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
            Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE),
        )
    }

    open fun validFramePosition(): ValidFramePosition? {
        return null
    }

    open fun refreshRateOfKeyPointsInMs(): Long? {
        return null
    }

    open fun confidenceLevel(): Float {
        return GlobalSettings.getDefaultConfidence()
    }

    open suspend fun initialView() {
        while (_isPausedSF.value) {
            yield()
        }

        _exerciseInstructionSF.value = UIInstructionText.startingStretch

        if (!didUserSkipSF.value) {
            delay(1000)
        }

        while (!CommonClassifier().check(person) && !didUserSkipSF.value) {
            yield()
        }
    }

    open suspend fun endView() {
        _exerciseInstructionSF.value = ""

        for (i in 3 downTo 0) {
            _midInstructionMessageSF.value = UIInstructionText.stretchCompleted
            _midInstructionTimerSF.value = i.toString()
            delay(1000L)
        }

        _midInstructionMessageSF.value = ""
        _midInstructionTimerSF.value = ""
    }

    // waitIfPaused is used to check if the app has been paused (i.e. the app is not on the foreground),
    // and if the app is paused, this will ensure the logic waits and runs other background tasks until the app is resumed
    open suspend fun waitIfPaused() {
        while (_isPausedSF.value) {
            yield()
        }
    }

    open suspend fun waitIfPaused(startTimeOfBeingInFrame: Long): Long? {
        val pausedTime: Long
        if (_isPausedSF.value) {
            pausedTime = System.currentTimeMillis()
            val differenceInTime = pausedTime - startTimeOfBeingInFrame
            while (_isPausedSF.value) {
                yield()
            }
            return System.currentTimeMillis() - differenceInTime
        }
        return null
    }

    open fun resetState() {
    }

    open fun cleanState() {
    }

    open fun updateState() {
    }

    open fun executeState() {
        viewModelScope.launch {
            processObservation()
        }
    }

    open fun exitState() {
    }

    open fun userExit() {
    }

    open fun userSkip() {
    }

    open fun userSkipReset() {
    }

    fun onUpdatePerson(_person: Person) {
        person = _person
    }

    open suspend fun processObservation() {
        cleanState()
        yield()
        if (didUserSkipSF.value) {
            return
        }
        if (Move.currentMoveState == null) {
            Move.currentMoveState = model.move_states.first()
        }
    }

    open suspend fun initialProcessObservation() {
    }

    open suspend fun calibrationProcessObservation() {
    }

    open suspend fun startProcessObservation() {
    }

    open suspend fun repetitionProcessObservation() {
    }

    open suspend fun repetitionInitialProcessObservation() {
    }

    open suspend fun repetitionInProgressProcessObservation() {
    }

    open suspend fun repetitionCompletedProcessObservation() {
    }

    // todo: refactor this enter logic into the "Move.kt" to make it similar to iOS logic (was not able to figure out how to do this)
    fun enter(myState: MoveState): Boolean {
        if (Move.currentMoveState == null) {
            Move.currentMoveState = model.move_states[0]
        }

        if (Move.currentMoveState!!.isValidNextState(myState)) {
            Move.currentMoveState = myState
        } else {
            return false
        }

        executeState()

        Move.currentMoveState!!.didEnter(null)
        return true
    }

    companion object {
        lateinit var person: Person

        var _isPausedSF = MutableStateFlow(false)
    }
}
