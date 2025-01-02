package com.ai.app.move.deskercise.network.services

import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.responses.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TeamService {

    @GET("v1/teams/{id}/users")
    suspend fun getTeamUsers(@Path("id") teamId: Int): Response<List<User>>
}
