package com.ai.app.move.deskercise.usecases.user

import android.net.Uri
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.network.repositories.FileRepository
import com.ai.app.move.deskercise.network.repositories.UserRepository
import com.ai.app.move.deskercise.utils.isUrl
import timber.log.Timber

class UpdateUserProfileUseCase(
    private val userRepository: UserRepository,
    private val fileRepository: FileRepository,
    private val userManager: UserManager,
) {
    suspend operator fun invoke(user: User): User {
        user.avatar?.let { avatar ->
            if (avatar.isNotEmpty() && !avatar.isUrl()) { // upload new avatar first
                val newAvatarUrl = fileRepository.uploadAvatar(Uri.parse(user.avatar)).url
                user.avatar = newAvatarUrl
            }
        }
        val r = userRepository.update(user)
        Timber.d(">> $r")
        userManager.storeUserInfo(user)
        return user
    }
}
