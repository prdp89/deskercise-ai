package com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.keyPointsAreVerticallySortedAscendingly
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.OverheadTricepsStretchModel

class OverheadTricepsStretchInPositionClassifier {

    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints
            val model: OverheadTricepsStretchModel =
                OverheadTricepsStretchModel.shared

            val calibrationJoints: List<BodyPart> = if (model.currentSide == Move.Side.right) {
                listOf(
                    BodyPart.LEFT_ELBOW,
                    BodyPart.LEFT_SHOULDER
                )
            } else {
                listOf(
                    BodyPart.RIGHT_ELBOW,
                    BodyPart.RIGHT_SHOULDER
                )
            }
            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, calibrationJoints)
            if (points.size < calibrationJoints.size) {
                return false
            }

            val elbowJoint: KeyPoint
            val shoulderJoint: KeyPoint
            if (model.currentSide == Move.Side.left) {
                elbowJoint = points.first { it.bodyPart == BodyPart.RIGHT_ELBOW }
                shoulderJoint = points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }
            } else {
                elbowJoint = points.first { it.bodyPart == BodyPart.LEFT_ELBOW }
                shoulderJoint = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }
            }

            val elbowYRatio = elbowJoint.translated_coordinate.y / 640
            if (elbowYRatio < 0.4) {
                return false
            }
            val elbowXRatio = elbowJoint.translated_coordinate.x / 480
            if (model.currentSide == Move.Side.right) {
                if (elbowXRatio < 0.3) {
                    return false
                }
            } else {
                if (elbowXRatio > 0.7) {
                    return false
                }
            }
            //Check elbow is above shoulder
            if (!keyPointsAreVerticallySortedAscendingly(
                    listOf(
                        shoulderJoint,
                        elbowJoint
                    )
                )
            ) {
                return false
            }

            return true
        }
    }
}
