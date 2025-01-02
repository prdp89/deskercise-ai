package com.ai.app.move.deskercise.ui.exerciseGroupMenu.programModels

import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.ExerciseHelper
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.NeckStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.OverheadShoulderStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.ShoulderShrugStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.SideStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.UpperTrapStretchModel

/**
 * The model file contains variables and functions that the controller will manipulate and utilize
 */

open class ExerciseGroup10MinProgramModel(isRandom: Boolean = false) : ExerciseGroupProgramModel() {

    // Program Name
    override var programName = "10"
    override var backgroundImage = R.drawable.ten_min_thumbnail
    override var isRandomized = false

    // Exercise List (including reps & durations)
    init {
        availableMoves.clear()
        if (isRandom) {
            isRandomized = isRandom
            val moves = ExerciseHelper.getRandom10Minutes()
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
        upperTrapStretchModel.getShared().repetitions = 3
        upperTrapStretchModel.getShared().remainingRepetitions = 3
        upperTrapStretchModel.getShared().duration = 10.0F
        upperTrapStretchModel.getShared().repetitionDuration = 10.0F
        availableMoves.add(upperTrapStretchModel)

        val dbreathingModel = DbreathingModel()
        dbreathingModel.getShared().order = "7"
        dbreathingModel.getShared().repetitions = 2
        dbreathingModel.getShared().remainingRepetitions = 2
        dbreathingModel.getShared().duration = 10.0F
        dbreathingModel.getShared().repetitionDuration = 10.0F

        val shoulderShrugStretchModel = ShoulderShrugStretchModel()
        shoulderShrugStretchModel.getShared().order = "2"
        shoulderShrugStretchModel.getShared().repetitions = 3
        shoulderShrugStretchModel.getShared().remainingRepetitions = 3
        shoulderShrugStretchModel.getShared().duration = 10.0F
        shoulderShrugStretchModel.getShared().repetitionDuration = 10.0F
        availableMoves.add(shoulderShrugStretchModel)

        val neckStretchModel = NeckStretchModel()
        neckStretchModel.getShared().order = "3"
        neckStretchModel.getShared().repetitions = 3
        neckStretchModel.getShared().remainingRepetitions = 3
        neckStretchModel.getShared().duration = 10.0F
        neckStretchModel.getShared().repetitionDuration = 10.0F
        availableMoves.add(neckStretchModel)

        val torsoStretchModel = TorsoStretchModel()
        torsoStretchModel.getShared().order = "4"
        torsoStretchModel.getShared().repetitions = 3
        torsoStretchModel.getShared().remainingRepetitions = 3
        torsoStretchModel.getShared().duration = 10.0F
        torsoStretchModel.getShared().repetitionDuration = 10.0F
        availableMoves.add(torsoStretchModel)

        val overheadShoulderStretchModel = OverheadShoulderStretchModel()
        overheadShoulderStretchModel.getShared().order = "5"
        overheadShoulderStretchModel.getShared().repetitions = 3
        overheadShoulderStretchModel.getShared().remainingRepetitions = 3
        overheadShoulderStretchModel.getShared().duration = 10.0F
        overheadShoulderStretchModel.getShared().repetitionDuration = 10.0F
        availableMoves.add(overheadShoulderStretchModel)

        val sideStretchModel = SideStretchModel()
        sideStretchModel.getShared().order = "6"
        sideStretchModel.getShared().repetitions = 3
        sideStretchModel.getShared().remainingRepetitions = 3
        sideStretchModel.getShared().duration = 10.0F
        sideStretchModel.getShared().repetitionDuration = 10.0F
        availableMoves.add(sideStretchModel)
        availableMoves.add(dbreathingModel)
    }
}
