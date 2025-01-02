package com.ai.app.move.deskercise.network.services

import com.ai.app.move.deskercise.data.Company
import com.ai.app.move.deskercise.data.Team
import com.ai.app.move.deskercise.network.responses.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CompanyService {
    @GET("v1/companies")
    suspend fun getCompany(): Response<List<Company>>

    @GET("v1/teams/{id}")
    suspend fun getTeams(@Path("id") teamID: Int): Response<List<Team>>
}
