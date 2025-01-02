package com.ai.app.move.deskercise.usecases.reward

import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.data.Reward
import com.ai.app.move.deskercise.network.repositories.RewardRepository
import com.ai.app.move.deskercise.network.repositories.UserRepository

class RedeemRewardUseCase(
    private val rewardRepository: RewardRepository,
    private val userRepository: UserRepository,
    private val userManager: UserManager,
) {

    suspend operator fun invoke(reward: Reward): Any {
        rewardRepository.redeem(reward.id) // redeem
        val user = userRepository.getProfile()
        userManager.storeUserInfo(user) // update user point again
        return true
    }
}
