package com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.keyPointsAreHorizontallySortedAscendingly
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.PiriformisStretchModel

/**
 * InPositionClassifier checks if user is in position to begin the exercise.
 * When this check returns true, it triggers a countdown for the first rep.
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class PiriformisStretchInPositionClassifier {

    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint> = person.keyPoints
            val model: PiriformisStretchModel = PiriformisStretchModel.shared

            val calibrationJoints: List<BodyPart>
            var ankleJoint: BodyPart

            if (model.getShared().currentSide == Move.Side.right) {
                calibrationJoints = listOf(
                    BodyPart.LEFT_KNEE,
                    BodyPart.LEFT_SHOULDER,
                    BodyPart.LEFT_HIP,
                    BodyPart.LEFT_ELBOW,
                    BodyPart.LEFT_WRIST,
                    BodyPart.LEFT_ANKLE,
                    BodyPart.NOSE,
                    BodyPart.LEFT_EAR,
                )
            } else {
                calibrationJoints = listOf(

                    BodyPart.RIGHT_ELBOW,
                    BodyPart.RIGHT_WRIST,
                    BodyPart.RIGHT_ANKLE,
                    BodyPart.LEFT_KNEE,
                    BodyPart.LEFT_ANKLE,
                    BodyPart.LEFT_HIP,
                    BodyPart.NOSE,
                    BodyPart.RIGHT_EAR,
                )
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
                if (points.firstOrNull { it.bodyPart == BodyPart.LEFT_KNEE } == null ||
                    points.firstOrNull { it.bodyPart == BodyPart.LEFT_SHOULDER } == null ||
                    points.firstOrNull { it.bodyPart == BodyPart.LEFT_ELBOW } == null ||
                    points.firstOrNull { it.bodyPart == BodyPart.LEFT_WRIST } == null ||
                    points.firstOrNull { it.bodyPart == BodyPart.LEFT_ANKLE } == null ||
                    points.firstOrNull { it.bodyPart == BodyPart.LEFT_HIP } == null
                ) {
                    return false
                }
            } else {
                if (points.firstOrNull { it.bodyPart == BodyPart.RIGHT_KNEE } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_SHOULDER } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ELBOW } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_WRIST } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ANKLE } == null || points.firstOrNull { it.bodyPart == BodyPart.RIGHT_HIP } == null) {
                    return false
                }
            }

            // CHECK DIRECTION
            // USING ANKLE + SHOULDER AS THERE ARE USUALLY HAVE HIGHER SCORES OF CONFIDENCE
            if (model.getShared().currentSide == Move.Side.right) {
                if (!keyPointsAreHorizontallySortedAscendingly(
                        listOf(
                            points.first { it.bodyPart == BodyPart.LEFT_ANKLE },
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
                            points.first { it.bodyPart == BodyPart.RIGHT_ANKLE },
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
            }

            return true
        }
    }
}
