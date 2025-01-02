package com.ai.app.move.deskercise.vision

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * The Class that represents all Moves (i.e. Stretches, Exercises.. etc)
 *
 *  Type has to be concrete as it is used as a generic wrapper class. (e.g used in a list as Type)
 */
@Parcelize
open class Move() : java.io.Serializable, Parcelable {
    internal var id: Int = 0
    var order: String = "0"

    // / Name of the Move
    lateinit var name: String
    lateinit var move_states: List<MoveState>

    internal var type: Int = 0
    internal var duration: Float = 0.0f
    lateinit var imagesFolderPath: String
    internal var isShowValidFrame: Boolean = false

    open var repetitionResults: MutableList<Boolean> = mutableListOf()
    open var rightRepetitionResults: MutableList<Boolean> = mutableListOf()
    open var leftRepetitionResults: MutableList<Boolean> = mutableListOf()
    open var remainingRepetitions: Int = 0
    open var remainingDuration: Float = duration
    open var startTimeLeft: Float = 3F
    open var repetitionDurationLeft: Float = duration
    open var firstRepetition: Boolean = true
    open var lastRepetition: Boolean = false
    open var leftCompleted: Boolean = false
    open var rightCompleted: Boolean = false
    open var waitingFrame: Int = 0
    open var lastWristAnkleDistance: Double = 0.0
    open var isSet: Boolean = true
    open var liveAngle: Double = 0.0
    var repetitions: Int = 2
    var repetitionDuration: Float = 10.0F

    // for 2 sided exercises
    enum class Side {
        right,
        left,
    }

    open var goodMoreThanBad: Boolean = false
    open var audioLeftKnee: Boolean = true
    open var currentSide: Side = Side.right
    open var updateCount: Int = 0
    open var lastKneeShoulderDistance: Double = 0.0
    open var remainingRepetitionsRight: Int = repetitions
    open var remainingRepetitionsLeft: Int = repetitions
    open var rightRepetitionDone: Boolean = false
    open var leftRepetitionDone: Boolean = false
    open var repetitionDurationRemaining: Float = repetitionDuration
    open var currentLeftAngle: Double = 0.0
    open var currentRightAngle: Double = 0.0
    open var liveLeftAngle: Double = 0.0
    open var liveRightAngle: Double = 0.0
    open var repetitionIsGood = false
    open var currentRepetitionGood: Boolean = false

    internal fun updateToLeftSide() {
        this.getShared().currentSide = Side.left
        this.currentSide = Side.left
    }

    internal fun updateToRightSide() {
        this.getShared().currentSide = Side.right
        this.currentSide = Side.right
    }

    companion object {
        var goodReps: Float = 0F
        internal var totalReps: Float = 0F
        var currentMoveState: MoveState? = null
    }

    internal var instructed: Boolean = false
    internal var firstExercise: Boolean = true
    internal var exerciseCompleted: Boolean = false
    internal var currentGoodCount: Int = 0
    internal var currentBadCount: Int = 0
    internal var goodFrames: Int = 0
    internal var badFrames: Int = 0
    internal var set: Int = 0

    // / Resets the internal State Machine.
    internal open fun resetStateMachine() {}

    // / To get the shared variable
    internal open fun getShared(): Move = this

    fun setInstructed(firstExercise: Boolean) {
        getShared().instructed = true
        getShared().firstExercise = firstExercise
    }

    internal fun setNotInstructed(firstExercise: Boolean) {
        getShared().instructed = false
        getShared().firstExercise = firstExercise
    }

    constructor(
        thisStates: List<MoveState>,
        thisId: Int,
        thisName: String,
        thisType: Int,
        thisDuration: Float,
        thisImagesFolderPath: String,
        thisIsShowValidFrame: Boolean = false
    ) : this() {
        this.move_states = thisStates
        this.id = thisId
        this.name = thisName
        this.type = thisType
        this.duration = thisDuration
        this.imagesFolderPath = thisImagesFolderPath
        this.isShowValidFrame = thisIsShowValidFrame
    }

    open fun welcomeMessage() {
    }

    // / Gets the name of Move.
    // /
    // / - Returns: Name of Move
    internal fun description(): String =
        name
}
