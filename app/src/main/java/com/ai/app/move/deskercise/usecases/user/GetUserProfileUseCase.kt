package com.ai.app.move.deskercise.usecases.user

import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.repositories.UserRepository

class GetUserProfileUseCase(
    private val userRepository: UserRepository,
    private val userManager: UserManager,
) {
    suspend operator fun invoke(): User {
        val user = userRepository.getProfile()
        userManager.storeUserInfo(user)
        return user
    }
}
