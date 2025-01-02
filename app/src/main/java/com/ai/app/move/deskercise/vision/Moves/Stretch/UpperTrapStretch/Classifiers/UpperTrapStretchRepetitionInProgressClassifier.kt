package com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.getPointsAbsoluteDistance
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.UpperTrapStretchModel

/**
 * RepetitionInProgressClassifier checks if the user is currently meeting
 * the minimum threshold to count the ongoing exercise as a valid rep
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class UpperTrapStretchRepetitionInProgressClassifier {

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
                model.badFrames += 1
                model.repetitionIsGood = false
                return false
            }

            val eyeNearHand: KeyPoint
            val shoulderNearHand: KeyPoint

            if (model.getShared().currentSide == Move.Side.right) {
                eyeNearHand = points.first { it.bodyPart == BodyPart.LEFT_EYE }
                shoulderNearHand = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }
            } else {
                eyeNearHand = points.first { it.bodyPart == BodyPart.RIGHT_EYE }
                shoulderNearHand = points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }
            }

            // Check eye-shoulder distance
            val eyeShoulderDistance = getPointsAbsoluteDistance(eyeNearHand, shoulderNearHand)
            if (eyeShoulderDistance > UpperTrapStretchModel.lastEyeShoulderDistance - 20) {
                model.badFrames += 1
                model.repetitionIsGood = false
                return false
            }

            model.goodFrames += 1
            model.repetitionIsGood = true
            return true
        }
    }
}
