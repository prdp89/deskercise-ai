package com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.getAngleFromThreeKeyPoints
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.NeckStretchModel

/**
 * InPositionClassifier checks if user is in position to begin the exercise.
 * When this check returns true, it triggers a countdown for the first rep.
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class NeckStretchInPositionClassifier {
    companion object {

        fun check(person: Person): Boolean {
            val observation: List<KeyPoint> = person.keyPoints

            val neckStretchModel: NeckStretchModel = NeckStretchModel.shared
            val joints: List<BodyPart> = listOf(
                BodyPart.NOSE,
                BodyPart.NECK,
                BodyPart.LEFT_ELBOW,
                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_EAR,
                BodyPart.RIGHT_ELBOW,
                BodyPart.RIGHT_SHOULDER,
                BodyPart.RIGHT_EAR,
            )

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, joints)
            // 1. Too few points detected
            if (points.size < joints.size) {
                return false
            }

            val nose = points.firstOrNull { it.bodyPart == BodyPart.NOSE }
            val neck = points.firstOrNull { it.bodyPart == BodyPart.NECK }
            val left_elbow = points.firstOrNull { it.bodyPart == BodyPart.LEFT_ELBOW }
            val left_shoulder = points.firstOrNull { it.bodyPart == BodyPart.LEFT_SHOULDER }
            val right_elbow = points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ELBOW }
            val right_shoulder = points.firstOrNull { it.bodyPart == BodyPart.RIGHT_SHOULDER }

            // 2. Check if any essential points are missing
            if (neck == null || left_elbow == null || left_shoulder == null || right_elbow == null || right_shoulder == null || nose == null) {
                return false
            }
            // 3. Check that the angles are not passing simply because the user is tilting head forward, causing the angles from ears to shoulders to pass the check
//            if (left_ear != null && right_ear != null) {
//                val leftEarAngle: Double = getAngleFromThreeKeyPoints(a = left_ear, b = neck, c = left_shoulder)
//                val rightEarAngle: Double = getAngleFromThreeKeyPoints(a = right_ear, b = neck, c = right_shoulder)
//                if (leftEarAngle < 40 && rightEarAngle < 40) {
//                    return false
//                }
//            }
            val earJoint: BodyPart
            val shoulderJoint: BodyPart
            val oppositeEarJoint: BodyPart
            val oppositeShoulderJoint: BodyPart
            if (neckStretchModel.currentSide == Move.Side.right) {
                earJoint = BodyPart.LEFT_EAR
                shoulderJoint = BodyPart.LEFT_SHOULDER
                oppositeEarJoint = BodyPart.RIGHT_EAR
                oppositeShoulderJoint = BodyPart.RIGHT_SHOULDER
            } else {
                earJoint = BodyPart.RIGHT_EAR
                shoulderJoint = BodyPart.RIGHT_SHOULDER
                oppositeEarJoint = BodyPart.LEFT_EAR
                oppositeShoulderJoint = BodyPart.LEFT_SHOULDER
            }
            // earJoint
            val earAngle: Double =
                getAngleFromThreeKeyPoints(
                    a = points.first { it.bodyPart == earJoint },
                    b = points.first { it.bodyPart == BodyPart.NECK },
                    c = points.first { it.bodyPart == shoulderJoint },
                )

            // 4. Ear to Shoulder angle too wide
            // If both ears are seen, then use the ear closest to the shoulder. Otherwise, use the opposite ear.
            if ((points.firstOrNull { it.bodyPart == earJoint } == null || earAngle < 53)) {
                return false
            }
            return true
        }
    }
}
