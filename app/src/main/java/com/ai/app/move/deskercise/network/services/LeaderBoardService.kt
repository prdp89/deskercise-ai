package com.ai.app.move.deskercise.network.services

import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.responses.PersonalBoardResponse
import com.ai.app.move.deskercise.network.responses.Response
import com.ai.app.move.deskercise.network.responses.TeamBoardResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LeaderBoardService {
    @GET("v1/leaderboards/personal-v2")
    suspend fun getPersonalBoard(@Query("filter") filter: String): Response<List<PersonalBoardResponse>>

    @GET("v1/leaderboards/team-v2")
    suspend fun getTeamBoard(@Query("filter") filter: String): Response<List<TeamBoardResponse>>

    @GET("v1/leaderboards/profile-v2")
    suspend fun getProfileBoard(
        @Query("filter") filter: String,
        @Query("type") type: String
    ): Response<User>
}
