package com.ai.app.move.deskercise.usecases.exercise

import com.ai.app.move.deskercise.app.AppStorage
import com.ai.app.move.deskercise.network.repositories.ExerciseRepository
import com.ai.app.move.deskercise.network.responses.ScoreConditionResponse

class FetchScoreConditionUseCase(
    private val exerciseRepository: ExerciseRepository,
    private val appStorage: AppStorage,
) {

    suspend operator fun invoke(): ScoreConditionResponse {
        val score = exerciseRepository.getScoreCondition()
        appStorage.storeBadReps(score.badReps)
        appStorage.storeGoodReps(score.goodReps)
        return score
    }
}
