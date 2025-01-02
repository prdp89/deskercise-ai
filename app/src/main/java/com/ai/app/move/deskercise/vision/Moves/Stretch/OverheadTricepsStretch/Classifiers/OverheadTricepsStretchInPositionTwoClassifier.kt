package com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.getPointsAbsoluteDistance
import com.ai.app.move.deskercise.services.keyPointsAreVerticallySortedAscendingly
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.OverheadTricepsStretchModel
import kotlin.math.abs


class OverheadTricepsStretchInPositionTwoClassifier {
    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints
            val model: OverheadTricepsStretchModel =
                OverheadTricepsStretchModel.shared

            val calibrationJoints: List<BodyPart> = if (model.currentSide == Move.Side.right) {
                listOf(
                    BodyPart.LEFT_ELBOW,
                    BodyPart.LEFT_SHOULDER,
                    BodyPart.RIGHT_ELBOW,
                    BodyPart.RIGHT_WRIST
                )
            } else {
                listOf(
                    BodyPart.RIGHT_ELBOW,
                    BodyPart.RIGHT_SHOULDER,
                    BodyPart.LEFT_ELBOW,
                    BodyPart.LEFT_WRIST
                )
            }
            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, calibrationJoints)
            if (points.size < calibrationJoints.size) {
                return false
            }

            val elbowJoint: KeyPoint
            val elbowOtherSizeJoint: KeyPoint
            val shoulderJoint: KeyPoint
            val wristOtherSideJoint: KeyPoint
            if (model.currentSide == Move.Side.left) {
                elbowJoint = points.first { it.bodyPart == BodyPart.RIGHT_ELBOW }
                elbowOtherSizeJoint = points.first { it.bodyPart == BodyPart.LEFT_ELBOW }
                shoulderJoint = points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }
                wristOtherSideJoint = points.first { it.bodyPart == BodyPart.LEFT_WRIST }
            } else {
                elbowJoint = points.first { it.bodyPart == BodyPart.LEFT_ELBOW }
                elbowOtherSizeJoint = points.first { it.bodyPart == BodyPart.RIGHT_ELBOW }
                shoulderJoint = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }
                wristOtherSideJoint = points.first { it.bodyPart == BodyPart.RIGHT_WRIST }
            }

            //Confirm that elbow is in valid frame
            val elbowYRatio = elbowJoint.translated_coordinate.y / 640
            if (elbowYRatio < 0.4) {
                return false
            }
            val elbowXRatio = elbowJoint.translated_coordinate.x / 480
            val elbowOtherSideXRatio = elbowOtherSizeJoint.translated_coordinate.x / 480
            if (model.currentSide == Move.Side.right) {
                if (elbowXRatio < 0.3 || elbowOtherSideXRatio < 0.25) {
                    return false
                }
            } else {
                if (elbowXRatio > 0.7 || elbowOtherSideXRatio > 0.75) {
                    return false
                }
            }
            //Check elbow is above shoulder
            if (!keyPointsAreVerticallySortedAscendingly(
                    listOf(
                        shoulderJoint,
                        elbowJoint
                    )
                ) || !keyPointsAreVerticallySortedAscendingly(
                    listOf(
                        shoulderJoint,
                        elbowOtherSizeJoint
                    )
                )
            ) {
                return false
            }

            //Confirm that the other hand is not raised straight up
            val elbowOtherSideY = elbowOtherSizeJoint.translated_coordinate.y
            val wristOtherSideY = wristOtherSideJoint.translated_coordinate.y
            if (abs(wristOtherSideY - elbowOtherSideY) > 25) {
                return false
            }
            val otherWristElbowDistance = getPointsAbsoluteDistance(
                a = wristOtherSideJoint,
                b = elbowJoint
            )

            if (otherWristElbowDistance > 60) {
                return false
            }

            return true
        }
    }
}