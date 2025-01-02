package com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Classifier
import com.ai.app.move.deskercise.services.Commons

/**
 * StartClassifier checks if the essential body landmarks are visible on screen
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class NeckStretchStartClassifier : Classifier() {

    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints

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
            return true
        }
    }
}
