package com.ai.app.move.deskercise.network.services

import com.ai.app.move.deskercise.network.responses.LogoutResponse
import com.ai.app.move.deskercise.network.responses.RefreshTokenResponse
import com.ai.app.move.deskercise.network.responses.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthService {
    @POST("v1/auth/login")
    suspend fun login(@Body body: Any): Map<String, Any>

    @POST("v1/auth/logout")
    suspend fun logout(@Body body: Any): Response<LogoutResponse>

    @POST("v1/auth/forgot-password")
    suspend fun forgot(@Body body: Any): Map<String, Any>

    @PATCH("v1/auth/reset-password")
    suspend fun reset(@Body body: Any): Map<String, Any>

    @POST("v1/auth/sign-up")
    suspend fun signup(@Body body: Any): Map<String, Any>

    @POST("v1/auth/verify")
    suspend fun verify(@Body body: Any): Map<String, Any>

    @POST("v1/auth/resent")
    suspend fun resent(@Body body: Any): Response<Any>

    @POST("v1/auth/refresh-token")
    suspend fun refreshToken(@Body body: Any): Response<RefreshTokenResponse>
}
