package com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.PiriformisStretchModel

/**
 * StartClassifier checks if the essential body landmarks are visible on screen
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class PiriformisStretchStartClassifier {

    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint> = person.keyPoints
            val model: PiriformisStretchModel = PiriformisStretchModel.shared

            val calibrationJoints: List<BodyPart>

            if (model.getShared().currentSide == Move.Side.right) {
                calibrationJoints = listOf(
                    BodyPart.LEFT_KNEE,
                    BodyPart.LEFT_SHOULDER,
                    BodyPart.LEFT_HIP,
                    BodyPart.LEFT_ELBOW,
                    BodyPart.LEFT_WRIST,
                    BodyPart.LEFT_ANKLE,
                )
            } else {
                calibrationJoints = listOf(
                    BodyPart.RIGHT_KNEE,
                    BodyPart.RIGHT_SHOULDER,
                    BodyPart.RIGHT_HIP,
                    BodyPart.RIGHT_ELBOW,
                    BodyPart.RIGHT_WRIST,
                    BodyPart.RIGHT_ANKLE,
                )
            }

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, calibrationJoints)

            if (points.count() < calibrationJoints.count()) {
                return false
            }

            // Check if ankles are in frame
            if (model.getShared().currentSide == Move.Side.right) {
                if (points.firstOrNull { it.bodyPart == BodyPart.LEFT_ANKLE } == null) {
                    return false
                }
            } else {
                if (points.firstOrNull { it.bodyPart == BodyPart.LEFT_ANKLE } == null) {
                    return false
                }
            }

            return true
        }
    }
}
