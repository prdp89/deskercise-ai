package com.ai.app.move.deskercise.ui.exerciseGroupMenu

import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.ForwardStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.HzShoulderStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.NeckStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.OverheadShoulderStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.OverheadTricepsStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.ShoulderShrugStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.SideStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.Stretch
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.UpperTrapStretchModel

object ExerciseHelper {
    val exercises = listOf(
        UpperTrapStretchModel().apply {
            getShared().order = "1"
            getShared().remainingRepetitions = 3
            getShared().repetitions = 3
            getShared().duration = 10.0F
            getShared().repetitionDuration = 10.0F
        },
        SideStretchModel().apply {
            getShared().order = "2"
            getShared().repetitions = 3
            getShared().remainingRepetitions = 3
            getShared().duration = 10.0F
            getShared().repetitionDuration = 10.0F
        },
        TorsoStretchModel().apply {
            getShared().order = "3"
            getShared().repetitions = 3
            getShared().remainingRepetitions = 3
            getShared().duration = 10.0F
            getShared().repetitionDuration = 10.0F
        },
        ShoulderShrugStretchModel().apply {
            getShared().order = "2"
            getShared().repetitions = 3
            getShared().remainingRepetitions = 3
            getShared().duration = 10.0F
            getShared().repetitionDuration = 10.0F
        },
        NeckStretchModel().apply {
            getShared().order = "3"
            getShared().repetitions = 3
            getShared().remainingRepetitions = 3
            getShared().duration = 10.0F
            getShared().repetitionDuration = 10.0F
        },
        OverheadShoulderStretchModel().apply {
            getShared().order = "5"
            getShared().repetitions = 3
            getShared().remainingRepetitions = 3
            getShared().duration = 10.0F
            getShared().repetitionDuration = 10.0F
        },
        ForwardStretchModel().apply {
            getShared().order = "7"
            getShared().repetitions = 3
            getShared().remainingRepetitions = 3
            getShared().duration = 10.0F
            getShared().repetitionDuration = 10.0F
        },
        OverheadTricepsStretchModel().apply {
            getShared().order = "8"
            getShared().repetitions = 3
            getShared().remainingRepetitions = 3
            getShared().duration = 10.0F
            getShared().repetitionDuration = 10.0F
        },
        HzShoulderStretchModel().apply {
            getShared().order = "9"
            getShared().repetitions = 3
            getShared().remainingRepetitions = 3
            getShared().duration = 10.0F
            getShared().repetitionDuration = 10.0F
        },
    )
    val breathing = DbreathingModel().apply {
        getShared().order = "4"
        getShared().repetitions = 2
        getShared().remainingRepetitions = 2
        getShared().duration = 10.0F
        getShared().repetitionDuration = 10.0F
    }

    fun getRandom5Minutes(): List<Stretch> {
        var iterator = 1
        var listed = exercises.shuffled().take(3)
        val arrayl = listed.toMutableList()
        arrayl.add(breathing)
        listed = arrayl.toList()
        for (i in listed) {
            i.getShared().order = iterator.toString()
            iterator++
        }
        return listed
    }
    fun getRandom10Minutes(): List<Stretch> {
        var iterator = 1
        var listed = exercises.shuffled().take(6)
        val arrayl = listed.toMutableList()
        arrayl.add(breathing)
        listed = arrayl.toList()
        for (i in listed) {
            i.getShared().order = iterator.toString()
            iterator++
        }
        return listed
    }
    fun getRandom15Minutes(): List<Stretch> {
        var iterator = 1
        var listed = exercises.shuffled().take(9)
        val arrayl = listed.toMutableList()
        arrayl.add(breathing)
        listed = arrayl.toList()
        for (i in listed) {
            i.getShared().order = iterator.toString()
            iterator++
        }
        return listed
    }
}
