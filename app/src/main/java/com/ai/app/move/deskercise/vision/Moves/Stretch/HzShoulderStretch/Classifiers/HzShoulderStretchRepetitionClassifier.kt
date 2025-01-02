package com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.HzShoulderStretchModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

/**
 * RepetitionInProgressClassifier checks if the user is currently meeting
 * the minimum threshold to count the ongoing exercise as a valid rep
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class HzShoulderStretchRepetitionClassifier {

    companion object {

        suspend fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints

            val hzShoulderStretchModel: HzShoulderStretchModel = HzShoulderStretchModel.shared
            val joints: List<BodyPart> = listOf(
                BodyPart.NECK,
                BodyPart.NOSE,
                BodyPart.LEFT_ELBOW,
                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_WRIST,
                BodyPart.RIGHT_ELBOW,
                BodyPart.RIGHT_SHOULDER,
                BodyPart.RIGHT_WRIST,
            )

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, joints)

            if (points.size < joints.size) {
                return false
            }

            val cameraWidth = 480
            val cameraHeight = 640
            val leftWristYRatio = points.first { it.bodyPart == BodyPart.RIGHT_WRIST }.translated_coordinate.y / cameraHeight
            val rightWristYRatio = points.first { it.bodyPart == BodyPart.LEFT_WRIST }.translated_coordinate.y / cameraHeight
            if (leftWristYRatio < 0.2 || leftWristYRatio > 0.6 || rightWristYRatio < 0.2 || rightWristYRatio > 0.6) {
                return false
            }

            val leftWristXRatio = points.first { it.bodyPart == BodyPart.RIGHT_WRIST }.translated_coordinate.x / cameraWidth
            val rightWristXRatio = points.first { it.bodyPart == BodyPart.LEFT_WRIST }.translated_coordinate.x / cameraWidth
            if (hzShoulderStretchModel.currentSide == Move.Side.right) {
                if (leftWristXRatio > 0.5 || rightWristXRatio > 0.5) {
                    return false
                }
            } else {
                if (leftWristXRatio < 0.5 || rightWristXRatio < 0.5) {
                    return false
                }
            }

            yield()
            delay(100)
            return true
        }
    }
}
