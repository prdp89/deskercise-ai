package com.ai.app.move.deskercise.usecases.user

import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.network.repositories.UserRepository

class RequestDeleteAccountUseCase(
    private val userRepository: UserRepository,
    private val userManager: UserManager,
) {
    suspend operator fun invoke(): String {
        val result = userRepository.requestDeleteAccount()
        userManager.clear()
        return result
    }
}