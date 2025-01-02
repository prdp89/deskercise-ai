package com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingModel
import kotlinx.coroutines.yield

/**
 * RepetitionInProgressClassifier checks if the user is currently meeting
 * the minimum threshold to count the ongoing exercise as a valid rep
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class DbreathingRepetitionInProgressClassifier {

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
                dbreathingmodelModel.badFrames += 1
                dbreathingmodelModel.repetitionIsGood = false

                return false
            }

            yield()

            dbreathingmodelModel.repetitionIsGood = true
            dbreathingmodelModel.goodFrames += 1

            return true
        }
    }
}
