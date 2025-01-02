package com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.getPointsAbsoluteDistance
import com.ai.app.move.deskercise.services.keyPointsAreVerticallySortedAscendingly

/**
 * InPositionClassifier checks if user is in position to begin the exercise.
 * When this check returns true, it triggers a countdown for the first rep.
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class OverheadShoulderStretchInPositionClassifier {
    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints

            val armJoints = listOf(
                BodyPart.LEFT_EYE,
                BodyPart.LEFT_WRIST,
                BodyPart.RIGHT_EYE,
                BodyPart.RIGHT_WRIST,
            )
            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, armJoints)

            if (points.size < armJoints.size) {
                return false
            }

            val leftEyeJoint = points.first { it.bodyPart == BodyPart.LEFT_EYE }
            val rightEyeJoint = points.first { it.bodyPart == BodyPart.RIGHT_EYE }
            val leftWristJoint = points.first { it.bodyPart == BodyPart.LEFT_WRIST }
            val rightWristJoint = points.first { it.bodyPart == BodyPart.RIGHT_WRIST }

            //Confirm that hands art above eyes
            if (!keyPointsAreVerticallySortedAscendingly(
                    listOf(leftEyeJoint, leftWristJoint)
                ) || !keyPointsAreVerticallySortedAscendingly(
                    listOf(rightEyeJoint, rightWristJoint)
                )
            ) {
                return false
            }

            //Confirm that hands are on the head
            val leftEyeWristDistance = getPointsAbsoluteDistance(
                a = leftEyeJoint,
                b = leftWristJoint
            )
            val rightEyeWristDistance = getPointsAbsoluteDistance(
                a = rightEyeJoint,
                b = rightWristJoint
            )

            if (leftEyeWristDistance < 30 || leftEyeWristDistance > 100 || rightEyeWristDistance < 30 || rightEyeWristDistance > 100) {
                return false
            }

            return true
        }
    }
}
