package com.ai.app.move.deskercise.services

import com.ai.app.move.deskercise.vision.Move

/**
 * vasScaleImage is a helper class to map the final summary result to the corresponding smiley image and feedback message
 */

internal fun vasScaleImage(): feedback_result {
    val percentageGood: Float = 0F
    val category: Int = if (percentageGood > 0.8) {
        1
    } else if (percentageGood > 0.6) {
        2
    } else if (percentageGood > 0.4) {
        3
    } else if (percentageGood > 0.2) {
        4
    } else {
        5
    }

    val feedback: String
    val comments: String
    val totalReps: String = (Move.totalReps.toInt()).toString()
    when ((category)) {
        1 -> {
            comments = "Great job!"
            feedback = "You have over 80% good repetitions!"
        }
        2 -> {
            comments = "Great job!"
            feedback = "Good job! You have over 60% good repetitions!"
        }
        3 -> {
            comments = "Try harder"
            feedback = "You have over 40% good repetitions!"
        }
        4 -> {
            comments = "Try harder"
            feedback = "You have less than 40% good repetitions"
        }
        5 -> {
            comments = "Try harder"
            feedback = "You have less than 20% good repetitions"
        }
        else -> {
            feedback = ""
            comments = ""
        }
    }
    return feedback_result(feedback, comments, totalReps, percentageGood)
}

data class feedback_result(val feedback: String, val comments: String, val totalReps: String, var percentageGood: Float)
