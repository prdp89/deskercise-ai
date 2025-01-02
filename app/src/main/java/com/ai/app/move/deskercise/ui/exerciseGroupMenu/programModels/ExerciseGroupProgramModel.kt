package com.ai.app.move.deskercise.ui.exerciseGroupMenu.programModels

import android.os.Parcelable
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.vision.Move
import kotlinx.android.parcel.Parcelize

/**
 * The model file contains variables and functions that the controller will manipulate and utilize
 */
@Parcelize
open class ExerciseGroupProgramModel : Parcelable {

    // Program Name
    open var programName: String = "Program Name"
    open var backgroundImage: Int = R.drawable.five_min_thumbnail

    // Exercise List (including reps & durations)
    var availableMoves: ArrayList<Move> = ArrayList()

    open var isRandomized: Boolean = false
}
