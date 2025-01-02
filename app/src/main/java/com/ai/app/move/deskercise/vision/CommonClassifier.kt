package com.ai.app.move.deskercise.vision

import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.GlobalSettings.Companion.getDefaultCommonClassifierConfidence

/**
 * The CommonClassifier is used to check if a minimum threshold of landmarks are visible on screen before initiating the exercise
 */

class CommonClassifier {

    fun check(person: Person): Boolean {
        val observation: List<KeyPoint>
        observation = person.keyPoints

        val points: MutableList<KeyPoint> = mutableListOf()

        for (point in observation) {
            if (point.score > getDefaultCommonClassifierConfidence()) {
                points.add(point)
            }
        }

        // ensure minimum at least 5 points visible
        if (points.count() < 5) {
            return false
        }

        return true
    }
}
