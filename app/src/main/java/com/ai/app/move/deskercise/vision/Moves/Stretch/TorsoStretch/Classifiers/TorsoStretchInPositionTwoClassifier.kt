package com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel

class TorsoStretchInPositionTwoClassifier {

    companion object {

        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints
            val model: TorsoStretchModel = TorsoStretchModel.shared
            val joints: List<BodyPart>

            if (model.getShared().currentSide == Move.Side.left) {
                joints = listOf(
                    BodyPart.LEFT_SHOULDER,
                    BodyPart.NOSE,
                )
            } else {
                joints = listOf(
                    BodyPart.RIGHT_SHOULDER,
                    BodyPart.NOSE,
                )
            }

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, joints)

            // Check if all required points are in.
            if (points.size < joints.size) {
                return false
            }

            return true
        }
    }
}
