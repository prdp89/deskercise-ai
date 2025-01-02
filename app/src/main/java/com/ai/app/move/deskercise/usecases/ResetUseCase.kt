package com.ai.app.move.deskercise.usecases

import com.ai.app.move.deskercise.network.repositories.AuthRepository
import com.ai.app.move.deskercise.network.responses.ResetResponse

class ResetUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, confirmation_code: String, new_password: String): ResetResponse {
        return authRepository.reset(email, confirmation_code, new_password).data ?: throw Exception("")
    }
}
