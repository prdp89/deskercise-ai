package com.ai.app.move.deskercise.services

import com.ai.app.move.deskercise.data.Person

/**
 * Helper class to ensure perform checks for various states by analysing the observation given.
 */
open class Classifier {
    companion object {
        fun check(person: Person): Boolean {
            return false
        }
    }
}
