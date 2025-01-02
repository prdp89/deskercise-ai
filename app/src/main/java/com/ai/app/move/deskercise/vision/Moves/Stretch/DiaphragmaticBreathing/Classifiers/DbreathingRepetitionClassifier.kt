package com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

/**
 * RepetitionClassifier checks if the user has meet the minimum threshold to trigger that the stretch is in progress
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class DbreathingRepetitionClassifier {
    companion object {
        suspend fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints

            val dbreathingmodelModel: DbreathingModel = DbreathingModel.shared
            val joints: List<BodyPart> = listOf(
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
