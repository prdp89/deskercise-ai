package com.ai.app.move.deskercise.services

import com.ai.app.move.deskercise.data.KeyPoint
// import java.lang.Math.*
import kotlin.math.pow

/**
 * Observation helper contains all the helper functions to calculate distance based on the points
 *
 * NOTE: We use the custom KeyPoint.translated_coordinate instead of the KeyPoint.coordinate
 * as the coordinate points refer to the physical camera orientated coordinates,
 * while the translate_coordinates refers to our custom logic to account for screen orientation
 */

// / Gets the angle from 3 `KeyPoint`.
// /
// / Uses the Cosine Rule to derive the angle, with `b` being the vertex of the 3 points.
fun getPointsAbsoluteDistance(a: KeyPoint, b: KeyPoint): Double {
    val xval: Double =
        (a.translated_coordinate.x.toDouble() - b.translated_coordinate.x.toDouble()).pow(2.0)
    val yval: Double =
        (a.translated_coordinate.y.toDouble() - b.translated_coordinate.y.toDouble()).pow(2.0)
    return kotlin.math.sqrt(xval + yval)
}

fun getPointsDistance(a: KeyPoint, b: KeyPoint): Double {
    return a.translated_coordinate.x.toDouble() - b.translated_coordinate.x.toDouble()
}

// / Gets the angle from 3 `KeyPoint`.
// /
// / Uses the Cosine Rule to derive the angle, with `b` being the vertex of the 3 points.
fun getAngleFromThreeKeyPoints(a: KeyPoint, b: KeyPoint, c: KeyPoint): Double {
    val ab: Double = kotlin.math.sqrt(
        (a.translated_coordinate.x.toDouble() - b.translated_coordinate.x.toDouble()).pow(2.0) + (a.translated_coordinate.y.toDouble() - b.translated_coordinate.y.toDouble()).pow(
            2.0,
        ),
    )
    val bc: Double = kotlin.math.sqrt(
        (b.translated_coordinate.x.toDouble() - c.translated_coordinate.x.toDouble()).pow(2.0) + (b.translated_coordinate.y.toDouble() - c.translated_coordinate.y.toDouble()).pow(
            2.0,
        ),
    )
    val ac: Double = kotlin.math.sqrt(
        (a.translated_coordinate.x.toDouble() - c.translated_coordinate.x.toDouble()).pow(2.0) + (a.translated_coordinate.y.toDouble() - c.translated_coordinate.y.toDouble()).pow(
            2.0,
        ),
    )
    val angle: Double = (ab.pow(2.0) + bc.pow(2.0) - ac.pow(2.0)) / (2 * ab * bc)
    return kotlin.math.acos(angle) * 180 / kotlin.math.PI
}

// / Checks if the list of `KeyPoint` are sorted in a vertically ascending manner.
fun keyPointsAreVerticallySortedAscendingly(points: List<KeyPoint>): Boolean {
    if (points.isEmpty()) {
        return false
    }
    var currentY = points[0].translated_coordinate.y.toDouble()
    for (i in 1 until points.size) {
        val point = points[i]
        if (currentY > point.translated_coordinate.y.toDouble()) {
            return false
        }
        currentY = point.translated_coordinate.y.toDouble()
    }
    return true
}

// / Checks if the list of `KeyPoint` are sorted in a horizontally ascending manner.
fun keyPointsAreHorizontallySortedAscendingly(points: List<KeyPoint>): Boolean {
    if (points.isEmpty()) {
        return false
    }
    var currentX = points[0].translated_coordinate.x.toDouble()
    for (i in 1 until points.size) {
        val point = points[i]
        if (currentX > point.translated_coordinate.x.toDouble()) {
            return false
        }
        currentX = point.translated_coordinate.x.toDouble()
    }
    return true
}

fun getNormalizedHeightOfTriangle(pt1_base: KeyPoint, pt2_base: KeyPoint, pt3: KeyPoint): Double {
    val triangle_side_1_base = kotlin.math.hypot(
        pt1_base.translated_coordinate.x - pt2_base.translated_coordinate.x,
        pt1_base.translated_coordinate.y - pt2_base.translated_coordinate.y,
    )

    val triangle_side_2 = kotlin.math.hypot(
        pt1_base.translated_coordinate.x - pt3.translated_coordinate.x,
        pt1_base.translated_coordinate.y - pt3.translated_coordinate.y,
    )

    val triangle_side_3 = kotlin.math.hypot(
        pt3.translated_coordinate.x - pt2_base.translated_coordinate.x,
        pt3.translated_coordinate.y - pt2_base.translated_coordinate.y,
    )

    // Step 3: get height based on triangle's area using heron's formula
    val semi_perimeter = (triangle_side_1_base + triangle_side_2 + triangle_side_3) / 2
    val triangle_area =
        kotlin.math.sqrt((semi_perimeter * (semi_perimeter - triangle_side_1_base) * (semi_perimeter - triangle_side_2) * (semi_perimeter - triangle_side_3)))
    val triangle_base = 0.5 * triangle_side_1_base
    val triangle_height = (triangle_area * 2) / triangle_base
    return (triangle_height / triangle_side_1_base)
}

fun getGradientOfLine(pt1: KeyPoint, pt2: KeyPoint): Float {
    if (pt2.translated_coordinate.x - pt1.translated_coordinate.x != 0F) {
        return (pt2.translated_coordinate.y - pt1.translated_coordinate.y) / (pt2.translated_coordinate.x - pt1.translated_coordinate.x)
    }
    return Float.MAX_VALUE
}
