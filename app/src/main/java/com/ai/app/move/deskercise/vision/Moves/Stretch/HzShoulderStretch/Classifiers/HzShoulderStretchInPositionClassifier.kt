package com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.HzShoulderStretchModel

/**
 * InPositionClassifier checks if user is in position to begin the exercise.
 * When this check returns true, it triggers a countdown for the first rep.
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class HzShoulderStretchInPositionClassifier {
    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints
            val hzShoulderStretchModel = HzShoulderStretchModel.shared

            // Calibration of the image is basically asking them to move backwards sufficiently
            val calibrationJoints: List<BodyPart> = listOf(
                BodyPart.LEFT_EAR,
                BodyPart.RIGHT_EAR,
                BodyPart.NECK,
                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_ELBOW,
                BodyPart.RIGHT_SHOULDER,
                BodyPart.RIGHT_ELBOW,

                )
            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, calibrationJoints)
            if (points.size < calibrationJoints.size) {
                return false
            }

            val cameraWidth = 480
            val cameraHeight = 640
            val leftShoulderYRatio = points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }.coordinate.y / cameraHeight
            val rightShoulderYRatio = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }.coordinate.y / cameraHeight
            if (leftShoulderYRatio < 0.2 || leftShoulderYRatio > 0.6 || rightShoulderYRatio < 0.2 || rightShoulderYRatio > 0.6) {
                return false
            }
            val leftShoulderXRatio = points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }.coordinate.x / cameraWidth
            val rightShoulderXRatio = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }.coordinate.x / cameraWidth
            if (leftShoulderXRatio < 0.2 || leftShoulderXRatio > 0.4 || rightShoulderXRatio < 0.6 || rightShoulderXRatio > 0.8) {
                return false
            }

            val leftElbowYRatio = 1 - points.first { it.bodyPart == BodyPart.RIGHT_ELBOW }.coordinate.y / cameraHeight
            val rightElbowYRatio = 1 - points.first { it.bodyPart == BodyPart.LEFT_ELBOW }.coordinate.y / cameraHeight

            if (leftElbowYRatio > 0.35 || rightElbowYRatio > 0.35) {
                return false
            }

            return true
        }
    }
}
