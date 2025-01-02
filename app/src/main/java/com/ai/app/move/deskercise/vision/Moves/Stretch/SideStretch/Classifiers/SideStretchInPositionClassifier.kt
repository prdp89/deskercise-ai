package com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import kotlin.math.abs

/**
 * InPositionClassifier checks if user is in position to begin the exercise.
 * When this check returns true, it triggers a countdown for the first rep.
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class SideStretchInPositionClassifier {
    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints
            // Calibration of the image is basically asking them to move backwards sufficiently
            val calibrationJoints: List<BodyPart> = listOf(
                BodyPart.LEFT_ELBOW,
                BodyPart.LEFT_EAR,
                BodyPart.LEFT_SHOULDER,
                BodyPart.RIGHT_ELBOW,
                BodyPart.RIGHT_SHOULDER,
                BodyPart.RIGHT_EAR,
                BodyPart.NECK,
            )
            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, calibrationJoints)
            if (points.size < calibrationJoints.size) {
                return false
            }

            val screenHeight = 640
            val screenWidth = 480
            val leftShoulderPoint = points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }
            val rightShoulderPoint = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }

            // Confirm that user is not too low or too high
            val leftShoulderYRatio =
                leftShoulderPoint.translated_coordinate.y / screenHeight
            val rightShoulderYRatio =
                rightShoulderPoint.translated_coordinate.y / screenHeight
            if (leftShoulderYRatio < 0.25 || leftShoulderYRatio > 0.65 || rightShoulderYRatio < 0.25 || rightShoulderYRatio > 0.65) {
                return false
            }

            // Confirm that user is not too far
            val leftShoulderXRatio = leftShoulderPoint.translated_coordinate.x
            val rightShoulderXRatio = rightShoulderPoint.translated_coordinate.x
            val shoulderDistanceRatio = abs(leftShoulderXRatio - rightShoulderXRatio) / screenWidth
            if (shoulderDistanceRatio < 0.25 || shoulderDistanceRatio > 0.4) {
                return false
            }

            if (abs(leftShoulderYRatio - rightShoulderYRatio) > 0.07) {
                return false
            }

            return true
        }
    }
}
