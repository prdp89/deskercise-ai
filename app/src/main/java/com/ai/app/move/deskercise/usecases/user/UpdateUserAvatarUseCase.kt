package com.ai.app.move.deskercise.usecases.user

import android.net.Uri
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.repositories.FileRepository
import com.ai.app.move.deskercise.network.repositories.UserRepository

class UpdateUserAvatarUseCase(
    private val userRepository: UserRepository,
    private val fileRepository: FileRepository,
    private val userManager: UserManager,
) {

    suspend operator fun invoke(uri: Uri): User {
        val user = userManager.getUserInfo() ?: throw Exception("No User")
        val avatarPath = fileRepository.uploadAvatar(uri).url
        user.avatar = avatarPath
        userRepository.update(user)
        userManager.storeUserInfo(user)
        return user
    }
}
