package com.ai.app.move.deskercise.network.repositories

import com.ai.app.move.deskercise.data.AvailableRewardResponse
import com.ai.app.move.deskercise.data.RedeemRewards
import com.ai.app.move.deskercise.data.Reward
import com.ai.app.move.deskercise.data.RewardDetail
import com.ai.app.move.deskercise.network.services.RewardService

interface RewardRepository {
    suspend fun getRewardList(page: Int, pageSize: Int): List<Reward>

    suspend fun getAvailableReward(page: Int, pageSize: Int): AvailableRewardResponse

    suspend fun redeem(rewardId: Int): Any?

    suspend fun rewardsRedemptions(rewardId: Int, voucherStatus: Int): Any?

    suspend fun getRedeemRewards(page: Int, pageSize: Int, type: String): List<RedeemRewards>

    suspend fun getRewardDetail(rewardId: Int): RewardDetail
}

class RewardRepositoryImpl(
    private val rewardService: RewardService
) : RewardRepository {

    override suspend fun getRewardList(page: Int, pageSize: Int): List<Reward> {
        return rewardService.getRewards(page, pageSize).data ?: throw Exception("Unknown error")
    }

    override suspend fun getAvailableReward(page: Int, pageSize: Int): AvailableRewardResponse {
        val res = rewardService.getAvailableRewards(page, pageSize)
        /**
         * {"data":{"results":[],"count":0,"total_page":0},"message":"Ok","status":true}
         */
        return res.data
            ?: throw Exception("Unknown error")
    }

    override suspend fun redeem(rewardId: Int): Any? {
        return rewardService.reward(
            mapOf(
                "reward_id" to rewardId,
            ),
        ).data
    }

    override suspend fun rewardsRedemptions(rewardId: Int, voucherStatus: Int): Any? {
        return rewardService.rewardRedemptions(
            mapOf(
                "reward_id" to rewardId,
                "voucher_status" to voucherStatus
            )
        ).data
    }

    override suspend fun getRedeemRewards(
        page: Int,
        pageSize: Int,
        type: String,
    ): List<RedeemRewards> {
        return rewardService.getRedeemRewards(page, pageSize, type).data
            ?: throw Exception("Unknown error")
    }

    override suspend fun getRewardDetail(rewardId: Int): RewardDetail {
        return rewardService.getRewardDetail(rewardId).data ?: throw Exception("Unknown error")
    }
}
