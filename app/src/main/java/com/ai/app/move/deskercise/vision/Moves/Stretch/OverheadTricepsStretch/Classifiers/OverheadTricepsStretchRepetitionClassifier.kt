package com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.keyPointsAreHorizontallySortedAscendingly
import com.ai.app.move.deskercise.services.keyPointsAreVerticallySortedAscendingly
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.OverheadTricepsStretchModel

class OverheadTricepsStretchRepetitionClassifier {

    companion object {

        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints
            val model: OverheadTricepsStretchModel =
                OverheadTricepsStretchModel.shared

            val calibrationJoints: List<BodyPart> = if (model.currentSide == Move.Side.right) {
                listOf(
                    BodyPart.RIGHT_SHOULDER,
                    BodyPart.RIGHT_ELBOW,
                    BodyPart.RIGHT_WRIST
                )
            } else {
                listOf(
                    BodyPart.LEFT_SHOULDER,
                    BodyPart.LEFT_ELBOW,
                    BodyPart.LEFT_WRIST
                )
            }
            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, calibrationJoints)
            if (points.size < calibrationJoints.size) {
                return false
            }

            val elbowOtherSideJoint: KeyPoint
            val shoulderOtherSide: KeyPoint
            val wristOtherSide: KeyPoint

            if (model.currentSide == Move.Side.right) {
                elbowOtherSideJoint = points.first { it.bodyPart == BodyPart.RIGHT_ELBOW }
                shoulderOtherSide = points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }
                wristOtherSide = points.first { it.bodyPart == BodyPart.RIGHT_WRIST }
            } else {
                elbowOtherSideJoint = points.first { it.bodyPart == BodyPart.LEFT_ELBOW }
                shoulderOtherSide = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }
                wristOtherSide = points.first { it.bodyPart == BodyPart.LEFT_WRIST }
            }
            //Confirm that elbow is above shoulder
            if (!keyPointsAreVerticallySortedAscendingly(
                    listOf(shoulderOtherSide, elbowOtherSideJoint)
                )
            ) {
                return false
            }

            if (model.currentSide == Move.Side.right) {
                if (!keyPointsAreHorizontallySortedAscendingly(
                        listOf(
                            elbowOtherSideJoint,
                            shoulderOtherSide,
                            wristOtherSide
                        )
                    )
                ) {
                    return false
                }
            } else {
                if (!keyPointsAreHorizontallySortedAscendingly(
                        listOf(
                            wristOtherSide,
                            shoulderOtherSide,
                            elbowOtherSideJoint
                        )
                    )
                ) {
                    return false
                }
            }

            val elbowOtherSideXRatio = elbowOtherSideJoint.translated_coordinate.x / 480
            if (model.currentSide == Move.Side.right) {
                if (elbowOtherSideXRatio > 0.3) {
                    return false
                }
            } else {
                if (elbowOtherSideXRatio < 0.7) {
                    return false
                }
            }

            return true
        }
    }
}
