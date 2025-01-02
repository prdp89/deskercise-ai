package com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.Classifiers

import android.graphics.PointF
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.getAngleFromThreeKeyPoints
import com.ai.app.move.deskercise.services.getPointsAbsoluteDistance
import com.ai.app.move.deskercise.services.keyPointsAreVerticallySortedAscendingly

/**
 * InPositionClassifier checks if user is in position to begin the exercise.
 * When this check returns true, it triggers a countdown for the first rep.
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class ShoulderShrugStretchInPositionClassifier {
    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints

            val earNoseAngle: Double
            val earWidth: Double

            val armJoints = listOf(
                BodyPart.LEFT_ELBOW,
                BodyPart.LEFT_SHOULDER,
                BodyPart.NECK,
                BodyPart.RIGHT_ELBOW,
                BodyPart.RIGHT_SHOULDER,
                BodyPart.NOSE,
                BodyPart.LEFT_EAR,
                BodyPart.RIGHT_EAR,
            )

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, armJoints)

            if (points.size < armJoints.size) {
                return false
            }

            if (keyPointsAreVerticallySortedAscendingly(
                    points = listOf(
                        points.first { it.bodyPart == BodyPart.LEFT_SHOULDER },
                        points.first { it.bodyPart == BodyPart.LEFT_ELBOW },
                    ),
                ) ||
                keyPointsAreVerticallySortedAscendingly(
                    points = listOf(
                        points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER },
                        points.first { it.bodyPart == BodyPart.RIGHT_ELBOW },
                    ),
                )
            ) {
                return false
            }

            // check ALIGNMENT OF NOSE AND EARS
            earNoseAngle =
                getAngleFromThreeKeyPoints(
                    a = points.first { it.bodyPart == BodyPart.LEFT_EAR },
                    b = points.first { it.bodyPart == BodyPart.NOSE },
                    c = points.first { it.bodyPart == BodyPart.RIGHT_EAR },
                )
            earWidth = getPointsAbsoluteDistance(
                points.first { it.bodyPart == BodyPart.LEFT_EAR },
                points.first { it.bodyPart == BodyPart.RIGHT_EAR },
            )

            // Check if neck to mid point of ear is close enough
            val midEarPoint =
                PointF(
                    (points.first { it.bodyPart == BodyPart.LEFT_EAR }.translated_coordinate.x + points.first { it.bodyPart == BodyPart.RIGHT_EAR }.translated_coordinate.x) / 2,
                    (points.first { it.bodyPart == BodyPart.LEFT_EAR }.translated_coordinate.y + points.first { it.bodyPart == BodyPart.RIGHT_EAR }.translated_coordinate.y) / 2,
                )
            val neckYValue = points.first { it.bodyPart == BodyPart.NECK }.translated_coordinate.y
            val midEarNeckYValueDiff = midEarPoint.y - neckYValue
            var midEarNeckYValueDiffNormalized = midEarNeckYValueDiff / earWidth
            if (midEarNeckYValueDiff > 80) {
                midEarNeckYValueDiffNormalized -= ((35 / 100) * midEarNeckYValueDiffNormalized)
            } else if (midEarNeckYValueDiff > 70) {
                midEarNeckYValueDiffNormalized -= ((25 / 100) * midEarNeckYValueDiffNormalized)
            } else if (midEarNeckYValueDiff > 60) {
                midEarNeckYValueDiffNormalized -= ((15 / 100) * midEarNeckYValueDiffNormalized)
            } else if (midEarNeckYValueDiff > 50) {
                midEarNeckYValueDiffNormalized -= ((10 / 100) * midEarNeckYValueDiffNormalized)
            } else if (midEarNeckYValueDiff > 45) {
            } else if (midEarNeckYValueDiff > 40) {
                midEarNeckYValueDiffNormalized += ((15 / 100) * midEarNeckYValueDiffNormalized)
            } else if (midEarNeckYValueDiff > 30) {
                midEarNeckYValueDiffNormalized += ((30 / 100) * midEarNeckYValueDiffNormalized)
            } else if (midEarNeckYValueDiff > 20) {
                midEarNeckYValueDiffNormalized += ((50 / 100) * midEarNeckYValueDiffNormalized)
            } else if (midEarNeckYValueDiff > 10) {
                midEarNeckYValueDiffNormalized += ((70 / 100) * midEarNeckYValueDiffNormalized)
            } else {
                midEarNeckYValueDiffNormalized += ((30 / 100) * midEarNeckYValueDiffNormalized)
            }

            if (midEarNeckYValueDiffNormalized < 1.0) {
                return false
            }
            return true
        }
    }
}
