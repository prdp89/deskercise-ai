package com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.keyPointsAreHorizontallySortedAscendingly

/**
 * StartClassifier checks if the essential body landmarks are visible on screen
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class ForwardStretchStartClassifier {
    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint> = person.keyPoints

            val calibrationJoints = listOf(
                BodyPart.NOSE,
                BodyPart.LEFT_ELBOW,
                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_EAR,
                BodyPart.LEFT_HIP,
            )

            // minimumJoints: when knee is not visible
            val minimumJoints = listOf(
                BodyPart.NOSE,
                BodyPart.LEFT_ELBOW,
                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_EAR,
                BodyPart.LEFT_HIP,
            )

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, calibrationJoints)

            // Check for any missing points
            if (points.count() < minimumJoints.count()) {
                return false
            }

            for (minJoint in minimumJoints) {
                if (points.firstOrNull { it.bodyPart == minJoint } == null) {
                    return false
                }
            }

            // Check for any missing essential points
            if (!points.any { it.bodyPart == BodyPart.LEFT_ELBOW } || !points.any { it.bodyPart == BodyPart.LEFT_SHOULDER } || !points.any { it.bodyPart == BodyPart.LEFT_EAR } || !points.any { it.bodyPart == BodyPart.LEFT_HIP } || !points.any { it.bodyPart == BodyPart.NOSE } || !points.any { it.bodyPart == BodyPart.LEFT_SHOULDER }) {
                return false
            }

            val shoulder_to_shoulder_dist_x_pos =
                (points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }.translated_coordinate.x.toDouble() - points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }.translated_coordinate.x.toDouble())

            // Check that the user is facing their left
            if (!keyPointsAreHorizontallySortedAscendingly(
                    points = listOf(
                        points.first { it.bodyPart == BodyPart.LEFT_EAR },
                        points.first { it.bodyPart == BodyPart.NOSE },
                    ),
                ) || shoulder_to_shoulder_dist_x_pos > 50.0
            ) {
                return false
            }

            // checks if knee is visible
//            if (points.firstOrNull { it.bodyPart == BodyPart.LEFT_KNEE } != null) {
//                if (!keyPointsAreHorizontallySortedAscendingly(
//                        points = listOf(
//                            points.first { it.bodyPart == BodyPart.LEFT_KNEE },
//                            points.first { it.bodyPart == BodyPart.LEFT_HIP },
//                        ),
//                    )
//                ) {
//                    return false
//                }
//            }

            return true
        }
    }
}
