package com.ai.app.move.deskercise.vision

/**
 * MoveState: Contains the base logic for the individual state which are part of the state machine
 */

open class MoveState {

    open val description: String = ""
    open fun isValidNextState(state: Any): Boolean {
        return false
    }

    open fun didEnter(from: Any?) {
    }
}
