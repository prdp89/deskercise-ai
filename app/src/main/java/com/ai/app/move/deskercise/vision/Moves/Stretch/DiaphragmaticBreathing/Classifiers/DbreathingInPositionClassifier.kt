package com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

/**
 * InPositionClassifier checks if user is in position to begin the exercise.
 * When this check returns true, it triggers a countdown for the first rep.
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class DbreathingInPositionClassifier {
    companion object {
        suspend fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints

            val dbreathingmodelModel: DbreathingModel = DbreathingModel.shared
            val joints: List<BodyPart> = listOf(
                BodyPart.NECK,
                BodyPart.LEFT_SHOULDER,
                BodyPart.RIGHT_SHOULDER,
            )

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, joints)

            if (points.size < joints.size) {
                return false
            }

            yield()
            delay(100)
            return true
        }
    }
}
