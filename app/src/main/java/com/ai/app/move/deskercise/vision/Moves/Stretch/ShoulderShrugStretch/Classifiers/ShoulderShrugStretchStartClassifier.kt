package com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons

/**
 * StartClassifier checks if the essential body landmarks are visible on screen
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class ShoulderShrugStretchStartClassifier {

    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints

            val calibrationJoints = listOf(
                BodyPart.LEFT_WRIST,
                BodyPart.LEFT_ELBOW,
                BodyPart.LEFT_SHOULDER,
                BodyPart.RIGHT_WRIST,
                BodyPart.RIGHT_SHOULDER,
                BodyPart.RIGHT_ELBOW,
                BodyPart.NOSE,
            )

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, calibrationJoints)

            if (points.count() < calibrationJoints.count()) {
                return false
            }

            return true
        }
    }
}
