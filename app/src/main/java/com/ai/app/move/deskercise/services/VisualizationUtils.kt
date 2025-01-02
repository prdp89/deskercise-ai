/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package com.ai.app.move.deskercise.services

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.ai.app.move.deskercise.data.BodyPart
import com.ai.app.move.deskercise.data.Person
import com.ai.app.move.deskercise.data.ValidFramePosition

/**
 * VisualizationUtils contains the configurations that is used to visualize the key points & lines drawn over the camera feed
 */

object VisualizationUtils {
    /** Radius of circle used to draw keypoints.  */
    private const val CIRCLE_RADIUS = 7f

    /** Width of line used to connected two keypoints.  */
    private const val LINE_WIDTH = 5f

    /** The text size of the person id that will be displayed when the tracker is available.  */
    private const val PERSON_ID_TEXT_SIZE = 30f

    //    var originalSizeCanvas: Canvas? = null
    private var previousPersons: List<Person>? = null
    private var lastRecordedTimeInMs: Long? = null

    // Pass Body Joints here
    // Draw line and point indicate body pose
    fun drawBodyKeypoints(
        input: Bitmap,
        persons: List<Person>,
        isTrackerEnabled: Boolean = false,
        displayable_bodyJoints: List<Pair<BodyPart, BodyPart>> =
            listOf(
                Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
                Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
                Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
                Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),
                Pair(BodyPart.NECK, BodyPart.LEFT_SHOULDER),
                Pair(BodyPart.NECK, BodyPart.RIGHT_SHOULDER),
                Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
                Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
                Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
                Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
                Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
                Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
                Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
                Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
                Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
                Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
                Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
                Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE),
            ),
        displayable_bodyPoints: List<BodyPart> =
            listOf(
                BodyPart.NOSE,
                BodyPart.NECK,
                BodyPart.LEFT_EYE,
                BodyPart.LEFT_EAR,
                BodyPart.LEFT_SHOULDER,
                BodyPart.LEFT_ELBOW,
                BodyPart.LEFT_HIP,
                BodyPart.LEFT_WRIST,
                BodyPart.LEFT_KNEE,
                BodyPart.RIGHT_EYE,
                BodyPart.RIGHT_EAR,
                BodyPart.RIGHT_SHOULDER,
                BodyPart.RIGHT_ELBOW,
                BodyPart.RIGHT_HIP,
                BodyPart.RIGHT_WRIST,
                BodyPart.RIGHT_KNEE,
            ),
        refreshFrequencyInMs: Long? = null,
        confidenceLevel: Float = GlobalSettings.getDefaultConfidence(),
        isDrawValidFrame: Boolean = false,
        validFramePosition: ValidFramePosition? = null,
    ): Bitmap {
        val paintCircleFill = Paint().apply {
            strokeWidth = CIRCLE_RADIUS
            color = Color.rgb(255, 255, 255) // White
            style = Paint.Style.FILL
        }
        val paintCircleOutline = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.rgb(78, 195, 194) // Green
            maskFilter = BlurMaskFilter(2f, BlurMaskFilter.Blur.NORMAL)
        }
        val paintLine = Paint().apply {
            color = Color.WHITE
            strokeWidth = LINE_WIDTH
        }

        val paintText = Paint().apply {
            textSize = PERSON_ID_TEXT_SIZE
            color = Color.BLUE
            textAlign = Paint.Align.LEFT
        }

        val paintValidFrameFill = Paint().apply {
            color = Color.argb(80, 152, 255, 152)
            style = Paint.Style.FILL
        }

        val output = input.copy(Bitmap.Config.ARGB_8888, true)
        val originalSizeCanvas = Canvas(output)
        var myPersons: List<Person>? = persons

        if (refreshFrequencyInMs != null) {
            // update based on refreshFrequencyInMs
            val currentTimeInMs: Long = System.currentTimeMillis()
            if (lastRecordedTimeInMs == null || previousPersons == null || currentTimeInMs - lastRecordedTimeInMs!! > refreshFrequencyInMs) {
                previousPersons = persons
                lastRecordedTimeInMs = currentTimeInMs
            } else {
                myPersons = previousPersons
            }
        }

        myPersons!!.forEach { person ->

            displayable_bodyJoints.forEach {
                if (person.keyPoints[it.first.position].score >= confidenceLevel && person.keyPoints[it.second.position].score >= confidenceLevel) {
                    val pointA = person.keyPoints[it.first.position].coordinate
                    val pointB = person.keyPoints[it.second.position].coordinate
                    originalSizeCanvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLine)
                }
            }

            person.keyPoints.forEach { point ->
                if (point.score > confidenceLevel && point.bodyPart in displayable_bodyPoints) {
                    originalSizeCanvas.drawCircle(
                        point.coordinate.x,
                        point.coordinate.y,
                        CIRCLE_RADIUS,
                        paintCircleFill,
                    )

                    originalSizeCanvas.drawCircle(
                        point.coordinate.x,
                        point.coordinate.y,
                        CIRCLE_RADIUS,
                        paintCircleOutline,
                    )
                }
            }
        }

        if (isDrawValidFrame && validFramePosition != null) {
            originalSizeCanvas.drawRect(
                validFramePosition.left,
                validFramePosition.top,
                validFramePosition.right,
                validFramePosition.bottom,
                paintValidFrameFill
            )
        }

        return output
    }
}
