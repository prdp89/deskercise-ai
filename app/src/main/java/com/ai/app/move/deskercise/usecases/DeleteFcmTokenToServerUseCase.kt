package com.ai.app.move.deskercise.usecases

import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.fcm.FcmHelper
import com.ai.app.move.deskercise.network.repositories.UserRepository

class DeleteFcmTokenToServerUseCase(
    private val userRepository: UserRepository,
    private val fcmHelper: FcmHelper,
    private val userManager: UserManager,
) {
    suspend operator fun invoke(): Any? {
        val fcmToken = fcmHelper.getToken()
        val user = userManager.getUserInfo() ?: throw Exception("No User")
        return userRepository.deleteFcmToken(fcmToken, user.id)
    }
}
