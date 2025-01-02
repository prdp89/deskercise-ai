package com.ai.app.move.deskercise.usecases

import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.network.repositories.AuthRepository
import com.ai.app.move.deskercise.network.responses.LogoutResponse

class LogoutUseCase(private val authRepository: AuthRepository, private val userManager: UserManager) {
    suspend operator fun invoke(): LogoutResponse? {
        val accessToken: String = userManager.getAccessToken()
        userManager.clear()
        return authRepository.logout(accessToken)
    }
}
