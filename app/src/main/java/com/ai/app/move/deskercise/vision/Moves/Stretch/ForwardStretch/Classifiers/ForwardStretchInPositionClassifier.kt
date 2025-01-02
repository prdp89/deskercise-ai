package com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.getAngleFromThreeKeyPoints
import com.ai.app.move.deskercise.services.getGradientOfLine
import com.ai.app.move.deskercise.services.getNormalizedHeightOfTriangle
import com.ai.app.move.deskercise.services.keyPointsAreHorizontallySortedAscendingly
import kotlin.math.abs

/**
 * InPositionClassifier checks if user is in position to begin the exercise.
 * When this check returns true, it triggers a countdown for the first rep.
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class ForwardStretchInPositionClassifier {

    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints

            val bodyAngle: Double

            val joints = listOf(
                BodyPart.LEFT_ELBOW,
                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_EAR,
                BodyPart.LEFT_HIP,
            )

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, joints)

            // Check if all points are in the frame
            if (points.size < joints.size) {
                return false
            }

            bodyAngle =
                getAngleFromThreeKeyPoints(
                    a = points.first { it.bodyPart == BodyPart.LEFT_ELBOW },
                    b = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER },
                    c = points.first { it.bodyPart == BodyPart.LEFT_HIP },
                )
            if (bodyAngle > 115 || bodyAngle < 60) {
                return false
            }
            if (!keyPointsAreHorizontallySortedAscendingly(
                    points = listOf(
                        points.first { it.bodyPart == BodyPart.LEFT_SHOULDER },
                        points.first { it.bodyPart == BodyPart.LEFT_ELBOW },
                    ),
                )
            ) {
                return false
            }

            val elbow = points.first { it.bodyPart == BodyPart.LEFT_ELBOW }
            val shoulder = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }
            val ear = points.first { it.bodyPart == BodyPart.LEFT_EAR }

            // check if arm is parallel or almost parallel to the ground
            // Step1: Get Gradient of Line
            val gradient_of_linear_line = abs(getGradientOfLine(elbow, shoulder))
            // Step 2: Check if gradient is within threshold
            if (gradient_of_linear_line > 0.65) {
                return false
            }

            // Check if the perpendicular distance from the ear to the line of elbow to wrist is within reasonable threshold
            // Step 1: get elbow, shoulder, ear points & then distance of each side
            // Step 2: get normalized height of triangle using heron's formula
            val triangle_height_normalized = getNormalizedHeightOfTriangle(elbow, shoulder, ear)

            // Step 3: Check if the normalized height is within threshold
            // Ensure user brings their head back up
            if (triangle_height_normalized < 0.8) {
                return false
            }

            return true
        }
    }
}
