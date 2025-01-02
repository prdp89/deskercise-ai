package com.ai.app.move.deskercise.vision.Moves.Stretch

import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.MoveState

open class Stretch(
    thisStates: List<MoveState>,
    thisId: Int,
    thisName: String,
    thisType: Int,
    thisDuration: Float,
    thisImagesFolderPath: String,
    thisIsShowValidFrame: Boolean = false
) : Move(thisStates, thisId, thisName, thisType, thisDuration, thisImagesFolderPath, thisIsShowValidFrame) {
    /**
     * An intermediate base class,
     * assuming that in the future there may be a different exercise base class that inherits from Move
     */

    companion object
}
