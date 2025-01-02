package com.ai.app.move.deskercise.usecases.reward

import com.ai.app.move.deskercise.data.AvailableRewardResponse
import com.ai.app.move.deskercise.network.repositories.RewardRepository

class FetchAvailableRewardListUseCase (
    private val rewardRepository: RewardRepository,
) {
    suspend operator fun invoke(page: Int): AvailableRewardResponse {
        return rewardRepository.getAvailableReward(page, pageSize = 20)
    }
}