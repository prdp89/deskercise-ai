package com.ai.app.move.deskercise.usecases

import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.fcm.FcmHelper
import com.ai.app.move.deskercise.network.repositories.UserRepository

class UpdateFcmTokenToServerUseCase(
    private val userRepository: UserRepository,
    private val fcmHelper: FcmHelper,
    private val userManager: UserManager,
) {
    suspend operator fun invoke(token: String? = null): Any? {
        val fcmToken = token ?: fcmHelper.getToken()
        val user = userManager.getUserInfo() ?: throw Exception("No User")
        return userRepository.updateFcmToken(fcmToken, user.id)
    }
}
