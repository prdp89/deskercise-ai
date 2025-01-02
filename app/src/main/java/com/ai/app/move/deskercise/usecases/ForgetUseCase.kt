package com.ai.app.move.deskercise.usecases

import com.ai.app.move.deskercise.network.repositories.AuthRepository
import com.ai.app.move.deskercise.network.responses.ForgotResponse

class ForgetUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String): ForgotResponse {
        return authRepository.forgot(email).data ?: throw Exception("")
    }
}
