package com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.keyPointsAreHorizontallySortedAscendingly
import com.ai.app.move.deskercise.services.keyPointsAreVerticallySortedAscendingly
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.SideStretchModel

/**
 * RepetitionClassifier checks if the user has meet the minimum threshold to trigger that the stretch is in progress
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class SideStretchRepetitionClassifier {
    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint> = person.keyPoints
            val model: SideStretchModel = SideStretchModel.shared

            val armJoints = if (model.getShared().currentSide == Move.Side.left) {
                listOf(
                    BodyPart.RIGHT_SHOULDER,
                    BodyPart.LEFT_SHOULDER,
                    BodyPart.RIGHT_ELBOW
                )
            } else {
                listOf(
                    BodyPart.LEFT_SHOULDER,
                    BodyPart.RIGHT_SHOULDER,
                    BodyPart.LEFT_ELBOW
                )
            }

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, armJoints)

            if (points.size < armJoints.size) {
                return false
            }

            val shoulderCurrentSideJoint: KeyPoint
            val shoulderOtherSideJoint: KeyPoint
            val elbowJoint: KeyPoint

            if (model.getShared().currentSide == Move.Side.left) {
                shoulderCurrentSideJoint = points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }
                shoulderOtherSideJoint = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }
                elbowJoint = points.first { it.bodyPart == BodyPart.RIGHT_ELBOW }
            } else {
                shoulderCurrentSideJoint = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }
                shoulderOtherSideJoint = points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }
                elbowJoint = points.first { it.bodyPart == BodyPart.LEFT_ELBOW }
            }

            //Make sure arm is bending to the other side
            if (model.getShared().currentSide == Move.Side.left) {
                if (!keyPointsAreHorizontallySortedAscendingly(
                        listOf(
                            shoulderCurrentSideJoint,
                            elbowJoint
                        )
                    )
                ) {
                    return false
                }
            } else {
                if (!keyPointsAreHorizontallySortedAscendingly(
                        listOf(
                            elbowJoint,
                            shoulderCurrentSideJoint
                        )
                    )
                ) {
                    return false
                }
            }

            //Make sure body is tilting to the other side
            val shoulderCurrentSideYRatio =
                shoulderCurrentSideJoint.translated_coordinate.y / 640
            val shoulderOtherSideYRatio =
                shoulderOtherSideJoint.translated_coordinate.y / 640
            if (shoulderCurrentSideYRatio - shoulderOtherSideYRatio < 0.1) {
                return false
            }

            //Make sure hand is up
            if (!keyPointsAreVerticallySortedAscendingly(
                    listOf(
                        shoulderCurrentSideJoint,
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
