package com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.Classifiers

import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.KeyPoint
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.services.AudioManagerService
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.services.TextToSpeechText
import com.ai.app.move.deskercise.services.keyPointsAreHorizontallySortedAscendingly
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel
import kotlin.math.abs

/**
 * RepetitionInProgressClassifier checks if the user is currently meeting
 * the minimum threshold to count the ongoing exercise as a valid rep
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class TorsoStretchRepetitionInProgressClassifier {

    companion object {
        fun check(person: Person): Boolean {
            val observation: List<KeyPoint>
            observation = person.keyPoints
            val model: TorsoStretchModel = TorsoStretchModel.shared
            val joints: List<BodyPart>

            if (model.getShared().currentSide == Move.Side.left) {
                joints = listOf(
                    BodyPart.LEFT_SHOULDER,
                    BodyPart.LEFT_ELBOW,
                    BodyPart.RIGHT_SHOULDER,
                    BodyPart.NOSE,
                )
            } else {
                joints = listOf(
                    BodyPart.RIGHT_ELBOW,
                    BodyPart.RIGHT_SHOULDER,
                    BodyPart.LEFT_SHOULDER,
                    BodyPart.NOSE,
                )
            }

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, joints)

            // Check if all required points are in.
            if (points.size < joints.size) {
                if (points.size < 3) {
                    AudioManagerService.speakText(
                        text = TextToSpeechText.moveBackwards,
                        speakTillComplete = true,
                    )
                }
                model.badFrames += 1
                model.repetitionIsGood = false
                return false
            }

            val nose: KeyPoint? = points.firstOrNull { it.bodyPart == BodyPart.NOSE }

            // Check if essential points are missing
            if (model.getShared().currentSide == Move.Side.left) {
                if (points.firstOrNull { it.bodyPart == BodyPart.LEFT_SHOULDER } == null ||
                    points.firstOrNull { it.bodyPart == BodyPart.LEFT_ELBOW } == null ||
                    nose == null ||
                    points.firstOrNull { it.bodyPart == BodyPart.RIGHT_SHOULDER } == null
                ) {
                    model.badFrames += 1
                    model.repetitionIsGood = false
                    return false
                }
            } else {
                if (points.firstOrNull { it.bodyPart == BodyPart.RIGHT_SHOULDER } == null ||
                    points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ELBOW } == null ||
                    nose == null ||
                    points.firstOrNull { it.bodyPart == BodyPart.LEFT_SHOULDER } == null
                ) {
                    model.badFrames += 1
                    model.repetitionIsGood = false
                    return false
                }
            }

            if (model.getShared().currentSide == Move.Side.left) {
                if (!keyPointsAreHorizontallySortedAscendingly(
                        listOf(
                            points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER },
                            nose,
                            points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }
                        )
                    )
                ) {
                    model.badFrames += 1
                    model.repetitionIsGood = false
                    return false
                }
            } else {
                if (!keyPointsAreHorizontallySortedAscendingly(
                        listOf(
                            points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER },
                            nose,
                            points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }
                        )
                    )
                ) {
                    model.badFrames += 1
                    model.repetitionIsGood = false
                    return false
                }
            }

            val noseX = points.first { it.bodyPart == BodyPart.NOSE }.translated_coordinate.x
            val leftShoulderX = points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }.translated_coordinate.x
            val rightShoulderX = points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }.translated_coordinate.x
            val leftShoulderNoseXDistance = abs(leftShoulderX - noseX)
            val rightShoulderNoseXDistance = abs(rightShoulderX - noseX)

            if (model.getShared().currentSide == Move.Side.left) {
                if (leftShoulderNoseXDistance > rightShoulderNoseXDistance) {
                    model.badFrames += 1
                    model.repetitionIsGood = false
                    return false
                }
            } else {
                if (rightShoulderNoseXDistance > leftShoulderNoseXDistance) {
                    model.badFrames += 1
                    model.repetitionIsGood = false
                    return false
                }
            }

            model.goodFrames += 1
            model.repetitionIsGood = true
            return true
        }
    }
}
