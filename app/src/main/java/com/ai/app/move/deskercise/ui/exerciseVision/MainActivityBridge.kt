package com.ai.app.move.deskercise.ui.exerciseVision

interface MainActivityBridge {
    suspend fun isStartingExerciseFragmentVisible(): Boolean
    fun updateDrawnKeyPoints()
}
