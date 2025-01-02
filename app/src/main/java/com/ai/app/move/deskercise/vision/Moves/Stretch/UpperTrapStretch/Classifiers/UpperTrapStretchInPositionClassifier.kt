package com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.getPointsAbsoluteDistance
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.UpperTrapStretchModel
import kotlin.math.abs

/**
 * InPositionClassifier checks if user is in position to begin the exercise.
 * When this check returns true, it triggers a countdown for the first rep.
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class UpperTrapStretchInPositionClassifier {
    companion object {
        suspend fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints

            val model: UpperTrapStretchModel = UpperTrapStretchModel.shared
            val joints: List<BodyPart>
            if (model.getShared().currentSide == Move.Side.right) {
                joints = listOf(
                    BodyPart.LEFT_SHOULDER,
                    BodyPart.LEFT_WRIST,
                    BodyPart.RIGHT_EYE,
                    BodyPart.LEFT_EYE
                )
            } else {
                joints = listOf(
                    BodyPart.RIGHT_SHOULDER,
                    BodyPart.RIGHT_WRIST,
                    BodyPart.RIGHT_EYE,
                    BodyPart.LEFT_EYE
                )
            }

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, joints)
            if (points.size < joints.size) {
                return false
            }

            val wristJoint: KeyPoint
            val eyeNearHand: KeyPoint
            val eyeFarHand: KeyPoint
            val shoulderNearHand: KeyPoint

            if (model.getShared().currentSide == Move.Side.right) {
                wristJoint = points.first { it.bodyPart == BodyPart.LEFT_WRIST }
                eyeNearHand = points.first { it.bodyPart == BodyPart.LEFT_EYE }
                eyeFarHand = points.first { it.bodyPart == BodyPart.RIGHT_EYE }
                shoulderNearHand = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }
            } else {
                wristJoint = points.first { it.bodyPart == BodyPart.RIGHT_WRIST }
                eyeNearHand = points.first { it.bodyPart == BodyPart.RIGHT_EYE }
                eyeFarHand = points.first { it.bodyPart == BodyPart.LEFT_EYE }
                shoulderNearHand = points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }
            }

            // Confirm that hand is higher than the eye
            val wristY = wristJoint.translated_coordinate.y
            val eyeNearHandY = eyeNearHand.translated_coordinate.y
            val eyeFarHandY = eyeFarHand.translated_coordinate.y
            if (wristY < eyeNearHandY || wristY < eyeFarHandY) {
                return false
            }

            // Confirm that the hand is near the head
            val eyeWristDistance = getPointsAbsoluteDistance(wristJoint, eyeNearHand)
            if (eyeWristDistance > 80) {
                return false
            }

            // Confirm that head is straight
            val eyeYDistance = abs(eyeNearHandY - eyeFarHandY)
            if (eyeYDistance > 5) {
                return false
            }

            // Check eye-shoulder distance
            val eyeShoulderDistance = getPointsAbsoluteDistance(eyeNearHand, shoulderNearHand)
            if (eyeShoulderDistance < 80) {
                return false
            }

            UpperTrapStretchModel.lastEyeShoulderDistance = eyeShoulderDistance
            return true
        }
    }
}
