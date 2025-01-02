package com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.getAngleFromThreeKeyPoints
import com.ai.app.move.deskercise.services.keyPointsAreVerticallySortedAscendingly
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.PiriformisStretchModel

/**
 * RepetitionClassifier checks if the user has meet the minimum threshold to trigger that the stretch is in progress
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class PiriformisStretchRepetitionClassifier {

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
                    BodyPart.RIGHT_KNEE,
                    BodyPart.RIGHT_ANKLE,
                    BodyPart.RIGHT_HIP,
                )
            } else {
                calibrationJoints = listOf(
                    BodyPart.RIGHT_KNEE,
                    BodyPart.RIGHT_SHOULDER,
                    BodyPart.RIGHT_HIP,
                    BodyPart.RIGHT_ELBOW,
                    BodyPart.RIGHT_WRIST,
                    BodyPart.RIGHT_ANKLE,
                    BodyPart.LEFT_KNEE,
                    BodyPart.LEFT_ANKLE,
                    BodyPart.LEFT_HIP,
                )
            }

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, calibrationJoints)

            // CAN AFFORD TO NOT HAVE THE OPPOSITE ANKLE & KNEE & HIP
            if (points.size < calibrationJoints.size - 3) {
                model.getShared().repetitionIsGood = false
                return false
            }

            // Check for missing essential points
            if (model.getShared().currentSide == Move.Side.right) {
                if (points.firstOrNull { it.bodyPart == BodyPart.LEFT_KNEE } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_SHOULDER } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_ELBOW } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_WRIST } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_ANKLE } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_HIP } == null) {
                    return false
                }
            } else {
                if (points.firstOrNull { it.bodyPart == BodyPart.RIGHT_KNEE } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_SHOULDER } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ELBOW } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_WRIST } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ANKLE } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_HIP } == null) {
                    return false
                }
            }

            // get correct leg (or the one that is the straightest)
            val rightKnee = points.firstOrNull { it.bodyPart == BodyPart.RIGHT_KNEE }
            val rightHip = points.firstOrNull { it.bodyPart == BodyPart.RIGHT_HIP }
            val rightShoulder = points.firstOrNull { it.bodyPart == BodyPart.RIGHT_SHOULDER }
            val rightAnkle = points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ANKLE }

            val leftKnee = points.firstOrNull { it.bodyPart == BodyPart.LEFT_KNEE }
            val leftHip = points.firstOrNull { it.bodyPart == BodyPart.LEFT_HIP }
            val leftShoulder = points.firstOrNull { it.bodyPart == BodyPart.LEFT_SHOULDER }
            val leftAnkle = points.firstOrNull { it.bodyPart == BodyPart.LEFT_ANKLE }

            if (model.getShared().currentSide == Move.Side.right) {
                if (rightKnee != null && leftAnkle != null && rightAnkle != null && rightHip != null) {
                    if (!keyPointsAreVerticallySortedAscendingly(listOf(rightAnkle, leftAnkle))) {
                        return false
                    }
                }
                if (points.firstOrNull { it.bodyPart == BodyPart.LEFT_KNEE } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_SHOULDER } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_ELBOW } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_WRIST } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_ANKLE } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_HIP } == null) {
                    return false
                }
                if (rightShoulder != null && rightHip != null && rightKnee != null) {
                    val shoulderKneeAngle =
                        getAngleFromThreeKeyPoints(a = rightShoulder, b = rightHip, c = rightKnee)
                    if (shoulderKneeAngle > 75) return false
                }
            } else {
                if (leftKnee != null && rightAnkle != null && leftAnkle != null && leftHip != null) {
                    if (!keyPointsAreVerticallySortedAscendingly(listOf(leftAnkle, rightAnkle))) {
                        return false
                    }
                }
                if (points.firstOrNull { it.bodyPart == BodyPart.RIGHT_KNEE } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_SHOULDER } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ELBOW } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_WRIST } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ANKLE } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_HIP } == null) {
                    return false
                }
                if (leftShoulder != null && leftHip != null && leftKnee != null) {
                    val shoulderKneeAngle =
                        getAngleFromThreeKeyPoints(a = leftShoulder, b = leftHip, c = leftKnee)
                    if (shoulderKneeAngle > 75) return false
                }
            }

            return true
        }
    }
}
