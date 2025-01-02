package com.ai.app.move.deskercise.usecases

import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.network.repositories.AuthRepository

class RenewAccessTokenUseCase(
    private val authRepository: AuthRepository,
    private val userManager: UserManager,
) {
    suspend operator fun invoke(): String {
        val refreshToken = userManager.getRefreshToken()
        val newAccessToken = authRepository.refreshToken(refreshToken).accessToken
        userManager.storeAccessToken(newAccessToken)
        return authRepository.refreshToken(refreshToken).accessToken
    }
}
