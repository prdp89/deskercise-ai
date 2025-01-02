package com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.getPointsAbsoluteDistance
import com.ai.app.move.deskercise.services.getPointsDistance
import com.ai.app.move.deskercise.services.keyPointsAreVerticallySortedAscendingly
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.OverheadShoulderStretchModel

/**
 * RepetitionInProgressClassifier checks if the user is currently meeting
 * the minimum threshold to count the ongoing exercise as a valid rep
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class OverheadShoulderStretchRepetitionInProgressClassifier {

    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints
            val model = OverheadShoulderStretchModel.shared

            val armJoints = listOf(
                BodyPart.LEFT_EYE,
                BodyPart.LEFT_WRIST,
                BodyPart.LEFT_SHOULDER,
                BodyPart.RIGHT_EYE,
                BodyPart.RIGHT_WRIST,
                BodyPart.RIGHT_SHOULDER
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
            val leftShoulderJoint = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }
            val rightShoulderJoint = points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }

            //Confirm that hands art above eyes
            if (!keyPointsAreVerticallySortedAscendingly(
                    listOf(leftEyeJoint, leftWristJoint)
                ) || !keyPointsAreVerticallySortedAscendingly(
                    listOf(rightEyeJoint, rightWristJoint)
                )
            ) {
                model.badFrames += 1
                model.currentRepetitionGood = false
                return false
            }

            //Confirm that hands are away from eyes
            val leftEyeWristDistance = getPointsAbsoluteDistance(
                a = leftEyeJoint,
                b = leftWristJoint
            )
            val rightEyeWristDistance = getPointsAbsoluteDistance(
                a = rightEyeJoint,
                b = rightWristJoint
            )

            if (leftEyeWristDistance < 120 || rightEyeWristDistance < 120) {
                model.badFrames += 1
                model.currentRepetitionGood = false
                return false
            }


            //Confirm that wrist are close to each other
            val wristsDistance = getPointsDistance(leftWristJoint, rightWristJoint)
            if (wristsDistance > 125) {
                model.badFrames += 11
                model.currentRepetitionGood = false
                return false
            }

            model.goodFrames += 1
            model.currentRepetitionGood = true
            return true
        }
    }
}
