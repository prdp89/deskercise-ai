package com.ai.app.move.deskercise.network.services

import com.ai.app.move.deskercise.data.Score
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.responses.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserService {
    @GET("v1/users/profile")
    suspend fun profile(): Response<User>

    @PUT("v1/users/profile")
    suspend fun update(@Body body: Any): Response<User?>

    @GET("v1/users/score-histories")
    suspend fun scoreHistories(): Response<List<Score>>


    @POST("v1/users/request-delete")
    suspend fun requestDeleteAccount(): Response<String>

    @POST("v1/notifications")
    suspend fun updateFcmToken(@Body body: Any): Response<String?>

    @HTTP(method = "DELETE", path = "v1/notifications", hasBody = true)
    suspend fun deleteFcmToken(@Body body: Any): Response<String?>
}
