package com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.getAngleFromThreeKeyPoints
import com.ai.app.move.deskercise.services.keyPointsAreHorizontallySortedAscendingly
import com.ai.app.move.deskercise.services.keyPointsAreVerticallySortedAscendingly
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.PiriformisStretchModel

/**
 * InPositionClassifier checks if user is in position to begin the exercise.
 * When this check returns true, it triggers a countdown for the first rep.
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class PiriformisStretchInPositionTwoClassifier {

    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint> = person.keyPoints
            val model: PiriformisStretchModel = PiriformisStretchModel.shared

            val calibrationJoints: List<BodyPart>
            val shoulderJoint: BodyPart
            var hipJoint: BodyPart
            var kneeJoint: BodyPart
            var ankleJoint: BodyPart

            if (model.getShared().currentSide == Move.Side.right) {
                calibrationJoints = listOf(
                    BodyPart.LEFT_KNEE,
                    BodyPart.LEFT_SHOULDER,
                    BodyPart.LEFT_HIP,
                    BodyPart.LEFT_ELBOW,
                    BodyPart.LEFT_WRIST,
                    BodyPart.LEFT_ANKLE,
                    BodyPart.RIGHT_KNEE,
                    BodyPart.RIGHT_ANKLE,
                    BodyPart.RIGHT_HIP,
                    BodyPart.NOSE,
                    BodyPart.LEFT_EAR,
                )
                shoulderJoint = BodyPart.LEFT_SHOULDER
                hipJoint = BodyPart.LEFT_HIP
                kneeJoint = BodyPart.LEFT_KNEE
                ankleJoint = BodyPart.LEFT_ANKLE
            } else {
                calibrationJoints = listOf(
                    BodyPart.RIGHT_KNEE,
                    BodyPart.RIGHT_SHOULDER,
                    BodyPart.RIGHT_HIP,
                    BodyPart.RIGHT_ELBOW,
                    BodyPart.RIGHT_WRIST,
                    BodyPart.RIGHT_ANKLE,
                    BodyPart.LEFT_KNEE,
                    BodyPart.LEFT_ANKLE,
                    BodyPart.LEFT_HIP,
                    BodyPart.NOSE,
                    BodyPart.RIGHT_EAR,
                )
                shoulderJoint = BodyPart.RIGHT_SHOULDER
                hipJoint = BodyPart.RIGHT_HIP
                kneeJoint = BodyPart.RIGHT_KNEE
                ankleJoint = BodyPart.RIGHT_ANKLE
            }

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, calibrationJoints)

            // CAN AFFORD TO NOT HAVE THE OPPOSITE ANKLE & KNEE & HIP
            if (points.size < calibrationJoints.size - 3) {
                model.getShared().repetitionIsGood = false
                return false
            }

            // Check for missing essential points
            if (model.getShared().currentSide == Move.Side.right) {
                if (points.firstOrNull { it.bodyPart == BodyPart.LEFT_KNEE } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_SHOULDER } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_ELBOW } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_WRIST } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_ANKLE } == null || points.firstOrNull { it.bodyPart == BodyPart.LEFT_HIP } == null) {
                    return false
                }
            } else {
                if (points.firstOrNull { it.bodyPart == BodyPart.RIGHT_KNEE } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_SHOULDER } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ELBOW } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_WRIST } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ANKLE } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_HIP } == null) {
                    return false
                }
            }

            // get correct leg (or the one that is the straightest)
            val rightKnee = points.firstOrNull { it.bodyPart == BodyPart.RIGHT_KNEE }
            val rightAnkle = points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ANKLE }
            val rightHip = points.firstOrNull { it.bodyPart == BodyPart.RIGHT_HIP }

            val leftKnee = points.firstOrNull { it.bodyPart == BodyPart.LEFT_KNEE }
            val leftAnkle = points.firstOrNull { it.bodyPart == BodyPart.LEFT_ANKLE }
            val leftHip = points.firstOrNull { it.bodyPart == BodyPart.LEFT_HIP }

            if (model.getShared().currentSide == Move.Side.right) {
                if (rightKnee != null && rightAnkle != null && rightHip != null) {
                    // get gradient of opp_knee_opp_ankle
                    kneeJoint = BodyPart.LEFT_KNEE
                    ankleJoint = BodyPart.LEFT_ANKLE
                    hipJoint = BodyPart.LEFT_HIP
                }
            } else {
                if (leftKnee != null && leftAnkle != null && leftHip != null) {
                    // get gradient of opp_knee_opp_ankle
                    kneeJoint = BodyPart.RIGHT_KNEE
                    ankleJoint = BodyPart.RIGHT_ANKLE
                    hipJoint = BodyPart.RIGHT_HIP
                }
            }

            val shoulderKneeAngle: Double = getAngleFromThreeKeyPoints(
                a = points.first { it.bodyPart == kneeJoint },
                b = points.first { it.bodyPart == shoulderJoint },
                c = points.first { it.bodyPart == hipJoint },
            )

            if (shoulderKneeAngle > 80 || shoulderKneeAngle < 10) {
                PiriformisStretchModel.waitingFrame = 0
                return false
            }

            // CHECK DIRECTION
            // USING ANKLE + SHOULDER AS THERE ARE USUALLY HAVE HIGHER SCORES OF CONFIDENCE
            if (model.getShared().currentSide == Move.Side.right) {
                if (!keyPointsAreHorizontallySortedAscendingly(
                        listOf(
                            points.first { it.bodyPart == ankleJoint },
                            points.first { it.bodyPart == BodyPart.LEFT_SHOULDER },
                        ),
                    )
                ) {
                    return false
                }
            } else {
                if (!keyPointsAreHorizontallySortedAscendingly(
                        listOf(
                            points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER },
                            points.first { it.bodyPart == ankleJoint },
                        ),
                    )
                ) {
                    return false
                }
            }

            if (model.getShared().currentSide == Move.Side.right) {
                if (!keyPointsAreHorizontallySortedAscendingly(
                        points = listOf(
                            points.first { it.bodyPart == BodyPart.LEFT_KNEE },
                            points.first { it.bodyPart == BodyPart.NOSE },
                        ),
                    )
                ) {
                    return false
                }
                if (rightKnee != null && leftAnkle != null && rightAnkle != null && rightHip != null) {
                    if (!keyPointsAreVerticallySortedAscendingly(listOf(rightAnkle, leftAnkle))) {
                        return false
                    }
                }
            } else {
                if (!keyPointsAreHorizontallySortedAscendingly(
                        points = listOf(
                            points.first { it.bodyPart == BodyPart.NOSE },
                            points.first { it.bodyPart == BodyPart.RIGHT_KNEE },
                        ),
                    )
                ) {
                    return false
                }
                if (leftKnee != null && rightAnkle != null && leftAnkle != null && leftHip != null) {
                    if (!keyPointsAreVerticallySortedAscendingly(listOf(leftAnkle, rightAnkle))) {
                        return false
                    }
                }
            }

            return true
        }
    }
}
