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

/**
 * InPositionClassifier checks if user is in position to begin the exercise.
 * When this check returns true, it triggers a countdown for the first rep.
 *
 * NOTE: All LEFT & RIGHT landmarks are swapped as the images are mirrored.
 * i.e. "BodyPart.LEFT_ELBOW" maps to the physical RIGHT elbow of the actual person
 */

class TorsoStretchInPositionClassifier {
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
                    BodyPart.RIGHT_SHOULDER,
                    BodyPart.RIGHT_ELBOW,
                    BodyPart.LEFT_SHOULDER,
                    BodyPart.NOSE,
                )
            }

            val points: MutableList<KeyPoint> =
                Commons.getKeyPointsAboveConfidenceThreshold(observation, joints)

            // Check if all required points are in.
            if (points.size < joints.size) {
                if (points.size < 2) {
                    AudioManagerService.speakText(
                        text = TextToSpeechText.moveBackwards,
                        speakTillComplete = true,
                    )
                }
                return false
            }

            val nose: KeyPoint? = points.firstOrNull { it.bodyPart == BodyPart.NOSE }

            // Check if essential points are missing
            if (model.getShared().currentSide == Move.Side.left) {
                if (points.firstOrNull { it.bodyPart == BodyPart.LEFT_SHOULDER } == null ||
                    points.firstOrNull { it.bodyPart == BodyPart.LEFT_ELBOW } == null ||
                    nose == null
                ) {
                    return false
                }
            } else {
                if (points.firstOrNull { it.bodyPart == BodyPart.RIGHT_SHOULDER } == null ||
                    points.firstOrNull { it.bodyPart == BodyPart.RIGHT_ELBOW } == null ||
                    nose == null
                ) {
                    return false
                }
            }

            if (model.getShared().currentSide == Move.Side.left) {
                if (points.firstOrNull { it.bodyPart == BodyPart.RIGHT_SHOULDER } != null) {
                    if (!keyPointsAreHorizontallySortedAscendingly(
                            listOf(
                                nose,
                                points.first { it.bodyPart == BodyPart.LEFT_SHOULDER }
                            )
                        ) || !keyPointsAreHorizontallySortedAscendingly(
                            listOf(
                                nose,
                                points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER }
                            )
                        )
                    ) {
                        return false
                    }
                }
            } else {
                if (points.firstOrNull { it.bodyPart == BodyPart.LEFT_SHOULDER } != null) {
                    if (!keyPointsAreHorizontallySortedAscendingly(
                            listOf(
                                points.first { it.bodyPart == BodyPart.RIGHT_SHOULDER },
                                nose
                            )
                        ) || !keyPointsAreHorizontallySortedAscendingly(
                            listOf(
                                points.first { it.bodyPart == BodyPart.LEFT_SHOULDER },
                                nose
                            )
                        )
                    ) {
                        return false
                    }
                }
            }

            return true
        }
    }
}
