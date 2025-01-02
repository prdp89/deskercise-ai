package com.ai.app.move.deskercise.network.responses

import com.google.gson.annotations.SerializedName

class LoginResponse(val accessToken: String, val refreshToken: String)

class SignUpResponse(val status: String)

class VerifyResponse(val status: String)

class ForgotResponse(val status: String)

class ResetResponse(val status: String)

class LogoutResponse(val status: String)

class RefreshTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
)
