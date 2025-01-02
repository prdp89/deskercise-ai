package com.ai.app.move.deskercise.usecases.reward

import com.ai.app.move.deskercise.data.RedeemRewards
import com.ai.app.move.deskercise.network.repositories.RewardRepository

class FetchRedeemRewardListUseCase(
    private val rewardRepository: RewardRepository,
) {
    suspend operator fun invoke(page: Int, type: String): List<RedeemRewards> {
        return rewardRepository.getRedeemRewards(page, pageSize = 20, type)
    }
}