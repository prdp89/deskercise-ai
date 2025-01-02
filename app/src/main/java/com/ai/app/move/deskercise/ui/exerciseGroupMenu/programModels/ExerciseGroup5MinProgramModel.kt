package com.ai.app.move.deskercise.ui.exerciseGroupMenu.programModels

import com.ai.app.move.deskercise.ui.exerciseGroupMenu.ExerciseHelper
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.SideStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.UpperTrapStretchModel

/**
 * The model file contains variables and functions that the controller will manipulate and utilize
 */

open class ExerciseGroup5MinProgramModel(isRandom: Boolean = false) : ExerciseGroupProgramModel() {

    // Program Name
    override var programName = "5"

    override var isRandomized = false
    // Exercise List (including reps & durations)
    init {
        availableMoves.clear()
        isRandomized = isRandom
        if (isRandom) {
            val moves = ExerciseHelper.getRandom5Minutes()
            moves.forEachIndexed { index, item ->
                item.order = "$index"
            }
            availableMoves.addAll(moves)
        } else {
            initStaticMoves()
        }
    }

    private fun initStaticMoves() {
        val upperTrapStretchModel = UpperTrapStretchModel()
        upperTrapStretchModel.getShared().order = "1"
        upperTrapStretchModel.getShared().remainingRepetitions = 3
        upperTrapStretchModel.getShared().repetitions = 3
        upperTrapStretchModel.getShared().duration = 10.0F
        upperTrapStretchModel.getShared().repetitionDuration = 10.0F

        val dbreathingModel = DbreathingModel()
        dbreathingModel.getShared().order = "4"
        dbreathingModel.getShared().repetitions = 2
        dbreathingModel.getShared().remainingRepetitions = 2
        dbreathingModel.getShared().duration = 10.0F
        dbreathingModel.getShared().repetitionDuration = 10.0F

        val sideStretchModel = SideStretchModel()
        sideStretchModel.getShared().order = "2"
        sideStretchModel.getShared().repetitions = 3
        sideStretchModel.getShared().remainingRepetitions = 3
        sideStretchModel.getShared().duration = 10.0F
        sideStretchModel.getShared().repetitionDuration = 10.0F

        val torsoStretchModel = TorsoStretchModel()
        torsoStretchModel.getShared().order = "3"
        torsoStretchModel.getShared().repetitions = 3
        torsoStretchModel.getShared().remainingRepetitions = 3
        torsoStretchModel.getShared().duration = 10.0F
        torsoStretchModel.getShared().repetitionDuration = 10.0F

        availableMoves.add(upperTrapStretchModel)
        availableMoves.add(sideStretchModel)
        availableMoves.add(torsoStretchModel)
        availableMoves.add(dbreathingModel)
    }
}
