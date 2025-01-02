package com.ai.app.move.deskercise.network.repositories

import com.ai.app.move.deskercise.data.Exercise
import com.ai.app.move.deskercise.network.responses.ScoreConditionResponse
import com.ai.app.move.deskercise.network.responses.StartProgramResponse
import com.ai.app.move.deskercise.network.services.ExerciseService

interface ExerciseRepository {
    suspend fun startProgram(programId: Int, exerciseCount: Int, isRandom: Boolean): StartProgramResponse

    suspend fun sendResult(programId: Int, sessionId: Int, exercises: List<Exercise>): Int

    suspend fun getScoreCondition(): ScoreConditionResponse
}

class ExerciseRepositoryImpl(private val exerciseService: ExerciseService) : ExerciseRepository {
    override suspend fun startProgram(programId: Int, exerciseCount: Int, isRandom: Boolean): StartProgramResponse {
        val res = exerciseService.startProgram(
            mapOf(
                "program_id" to programId,
                "count_exercise" to exerciseCount,
                "type" to if (isRandom) "RANDOM" else "DEFAULT",
            ),
        )
        /**
         * {
         *   "data": {
         *     "count_completed_exercise": 0,
         *     "count_exercise": 4,
         *     "id": 10307,
         *     "is_added_point": true,
         *     "program_id": 1,
         *     "score": 0,
         *     "user_id": 393
         *   },
         *   "message": "Ok",
         *   "status": true
         * }
         */
        return res.data ?: throw Exception("Unknown error")
    }

    override suspend fun sendResult(
        programId: Int,
        sessionId: Int,
        exercises: List<Exercise>,
    ): Int {
        return exerciseService.sendResult(
            mapOf(
                "program_id" to programId,
                "session_id" to sessionId,
                "exercises" to exercises.map {
                    mapOf(
                        "exercise_id" to it.exerciseId,
                        "count_good" to it.countGood,
                        "count_bad" to it.countBad,
                        "completed_reps" to it.completedReps,
                        "total_reps" to it.totalReps,
                    )
                },
            ),
        ).data ?: throw Exception("Unknown error")
    }

    override suspend fun getScoreCondition(): ScoreConditionResponse {
        return exerciseService.getScoreCondition().data ?: throw Exception("Unknown error")
    }
}
