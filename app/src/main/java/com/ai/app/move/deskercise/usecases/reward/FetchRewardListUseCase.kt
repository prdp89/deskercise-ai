package com.ai.app.move.deskercise.usecases.reward

import com.ai.app.move.deskercise.data.Reward
import com.ai.app.move.deskercise.network.repositories.RewardRepository

class FetchRewardListUseCase(
    private val rewardRepository: RewardRepository,
) {

    suspend operator fun invoke(page: Int): List<Reward> {
        return rewardRepository.getRewardList(page, pageSize = 20)
    }
}
