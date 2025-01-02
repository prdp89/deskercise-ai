package com.ai.app.move.deskercise.ui.exerciseGroupMenu.programModels

import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.ExerciseHelper
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.ForwardStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.HzShoulderStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.NeckStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.OverheadShoulderStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.OverheadTricepsStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.ShoulderShrugStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.SideStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.UpperTrapStretchModel

/**
 * The model file contains variables and functions that the controller will manipulate and utilize
 */

open class ExerciseGroup15MinProgramModel(isRandom: Boolean = false) : ExerciseGroupProgramModel() {

    // Program Name
    override var programName = "15"
    override var backgroundImage = R.drawable.fifteen_min_thumbnail
    override var isRandomized = false

    // Exercise List (including reps & durations)
    init {
        availableMoves.clear()
        if (isRandom) {
            isRandomized = isRandom
            val moves = ExerciseHelper.getRandom15Minutes()
            moves.forEachIndexed { index, item ->
                item.order = "$index"
            }
            availableMoves.addAll(moves)
        } else {
            initStaticMoves()
        }
    }

    private fun initStaticMoves() {
        val uppertrapstretchmodel = UpperTrapStretchModel()
        uppertrapstretchmodel.getShared().order = "1"
        uppertrapstretchmodel.getShared().remainingRepetitions = 3
        uppertrapstretchmodel.getShared().repetitions = 3
        uppertrapstretchmodel.getShared().duration = 10.0F
        uppertrapstretchmodel.getShared().repetitionDuration = 10.0F

        val shoulderShrugStretchModel = ShoulderShrugStretchModel()
        shoulderShrugStretchModel.getShared().order = "2"
        shoulderShrugStretchModel.getShared().remainingRepetitions = 3
        shoulderShrugStretchModel.getShared().repetitions = 3
        shoulderShrugStretchModel.getShared().duration = 10.0F
        shoulderShrugStretchModel.getShared().repetitionDuration = 10.0F

        val neckStretchModel = NeckStretchModel()
        neckStretchModel.getShared().order = "3"
        neckStretchModel.getShared().remainingRepetitions = 3
        neckStretchModel.getShared().repetitions = 3
        neckStretchModel.getShared().duration = 10.0F
        neckStretchModel.getShared().repetitionDuration = 10.0F

        val torsoStretchModel = TorsoStretchModel()
        torsoStretchModel.getShared().order = "4"
        torsoStretchModel.getShared().repetitions = 3
        torsoStretchModel.getShared().remainingRepetitions = 3
        torsoStretchModel.getShared().duration = 10.0F
        torsoStretchModel.getShared().repetitionDuration = 10.0F

        val sideStretchModel = SideStretchModel()
        sideStretchModel.getShared().order = "6"
        sideStretchModel.getShared().remainingRepetitions = 3
        sideStretchModel.getShared().repetitions = 3
        sideStretchModel.getShared().duration = 10.0F
        sideStretchModel.getShared().repetitionDuration = 10.0F

        val piriformisStretchModel = ForwardStretchModel()
        piriformisStretchModel.getShared().order = "7"
        piriformisStretchModel.getShared().repetitions = 3
        piriformisStretchModel.getShared().remainingRepetitions = 3
        piriformisStretchModel.getShared().duration = 10.0F
        piriformisStretchModel.getShared().repetitionDuration = 10.0F

        val overheadTricepStretchModel = OverheadTricepsStretchModel()
        overheadTricepStretchModel.getShared().order = "8"
        overheadTricepStretchModel.getShared().repetitions = 3
        overheadTricepStretchModel.getShared().remainingRepetitions = 3
        overheadTricepStretchModel.getShared().duration = 10.0F
        overheadTricepStretchModel.getShared().repetitionDuration = 10.0F

        val horizontalShoulderStretchModel = HzShoulderStretchModel()
        horizontalShoulderStretchModel.getShared().order = "9"
        horizontalShoulderStretchModel.getShared().repetitions = 3
        horizontalShoulderStretchModel.getShared().remainingRepetitions = 3
        horizontalShoulderStretchModel.getShared().duration = 10.0F
        horizontalShoulderStretchModel.getShared().repetitionDuration = 10.0F

        val overheadShoulderStretchModel = OverheadShoulderStretchModel()
        overheadShoulderStretchModel.getShared().order = "5"
        overheadShoulderStretchModel.getShared().repetitions = 3
        overheadShoulderStretchModel.getShared().remainingRepetitions = 3
        overheadShoulderStretchModel.getShared().duration = 10.0F
        overheadShoulderStretchModel.getShared().repetitionDuration = 10.0F

        val dbreathingModel = DbreathingModel()
        dbreathingModel.getShared().order = "10"
        dbreathingModel.getShared().repetitions = 2
        dbreathingModel.getShared().remainingRepetitions = 2
        dbreathingModel.getShared().duration = 10.0F
        dbreathingModel.getShared().repetitionDuration = 10.0F

        availableMoves.add(uppertrapstretchmodel)
        availableMoves.add(shoulderShrugStretchModel)
        availableMoves.add(neckStretchModel)
        availableMoves.add(torsoStretchModel)
        availableMoves.add(overheadShoulderStretchModel)
        availableMoves.add(sideStretchModel)
        availableMoves.add(piriformisStretchModel)
        availableMoves.add(overheadTricepStretchModel)
        availableMoves.add(horizontalShoulderStretchModel)
        availableMoves.add(dbreathingModel)
    }
}
