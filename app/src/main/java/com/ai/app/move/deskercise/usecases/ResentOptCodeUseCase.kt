package com.ai.app.move.deskercise.usecases

import com.ai.app.move.deskercise.network.repositories.AuthRepository

class ResentOptCodeUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String): Any? {
        return authRepository.resent(email)
    }
}
