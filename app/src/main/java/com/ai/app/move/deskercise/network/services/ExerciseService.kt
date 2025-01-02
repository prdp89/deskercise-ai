package com.ai.app.move.deskercise.network.services

import com.ai.app.move.deskercise.network.responses.Response
import com.ai.app.move.deskercise.network.responses.ScoreConditionResponse
import com.ai.app.move.deskercise.network.responses.StartProgramResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ExerciseService {
    @POST("v1/program-results/start")
    suspend fun startProgram(@Body body: Any): Response<StartProgramResponse>

    @POST("v1/program-results/completed")
    suspend fun sendResult(@Body body: Any): Response<Int>

    @GET("v1/program-results/score-condition")
    suspend fun getScoreCondition(): Response<ScoreConditionResponse>
}
