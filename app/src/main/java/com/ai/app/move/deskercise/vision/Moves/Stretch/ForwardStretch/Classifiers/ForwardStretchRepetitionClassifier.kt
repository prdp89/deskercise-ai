package com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.getAngleFromThreeKeyPoints
import com.ai.app.move.deskercise.services.keyPointsAreHorizontallySortedAscendingly
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.ForwardStretchModel

/**
 * RepetitionClassifier checks if the user has meet the minimum threshold to trigger that the stretch is in progress
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class ForwardStretchRepetitionClassifier {

    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints

            val joints = listOf(
                BodyPart.LEFT_ELBOW,
                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_EAR,
            )

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, joints)

            // Check if there are sufficient points in the frame
            if (points.size < joints.size) {
                return false
            }

            // Check for missing essential points
            if (!points.any { it.bodyPart == BodyPart.LEFT_ELBOW } || !points.any { it.bodyPart == BodyPart.LEFT_SHOULDER }) {
                return false
            }

            // Check if elbow is in front of shoulder
            if (!keyPointsAreHorizontallySortedAscendingly(
                    points = listOf(
                        points.first { it.bodyPart == BodyPart.LEFT_SHOULDER },
                        points.first { it.bodyPart == BodyPart.LEFT_ELBOW },
                    ),
                )
            ) {
                return false
            }

            // Check ear is close to arm
            return (
                getAngleFromThreeKeyPoints(
                    a = points.first { it.bodyPart == BodyPart.LEFT_EAR },
                    b = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER },
                    c = points.first { it.bodyPart == BodyPart.LEFT_ELBOW },
                ) < ForwardStretchModel.repetitionAngleThreshold
                )
        }
    }
}
