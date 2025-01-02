package com.ai.app.move.deskercise.usecases

import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.network.repositories.AuthRepository
import com.ai.app.move.deskercise.network.repositories.UserRepository
import com.ai.app.move.deskercise.network.responses.LoginResponse
import com.ai.app.move.deskercise.network.responses.SignUpResponse
import com.ai.app.move.deskercise.network.responses.VerifyResponse

class LoginUseCase(
    private val authRepository: AuthRepository,
    private val userManager: UserManager,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(email: String, password: String): LoginResponse {
        val response = authRepository.login(email, password).data ?: throw Exception("")
        userManager.storeAccessToken(response.accessToken)
        userManager.storeRefreshToken(response.refreshToken)
        val user = userRepository.getProfile()
        userManager.storeUserInfo(user)
        return response
    }
}

class SignUpUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, name: String, signUpCode: String?): SignUpResponse {
        return authRepository.signup(email, password, name, signUpCode).data ?: throw Exception("")
    }
}

class VerifyUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, confirmation_code: String): VerifyResponse {
        return authRepository.verify(email, confirmation_code).data ?: throw Exception("")
    }
}
