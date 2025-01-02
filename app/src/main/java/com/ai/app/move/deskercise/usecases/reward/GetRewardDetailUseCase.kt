package com.ai.app.move.deskercise.usecases.reward

import com.ai.app.move.deskercise.data.RewardDetail
import com.ai.app.move.deskercise.network.repositories.RewardRepository

class GetRewardDetailUseCase(
    private val rewardRepository: RewardRepository,
) {
    suspend operator fun invoke(rewardId: Int): RewardDetail {
        return rewardRepository.getRewardDetail(rewardId)
    }
}