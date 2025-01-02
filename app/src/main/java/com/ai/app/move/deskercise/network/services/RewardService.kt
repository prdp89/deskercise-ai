package com.ai.app.move.deskercise.network.services

import com.ai.app.move.deskercise.data.AvailableRewardResponse
import com.ai.app.move.deskercise.data.RedeemRewards
import com.ai.app.move.deskercise.data.Reward
import com.ai.app.move.deskercise.data.RewardDetail
import com.ai.app.move.deskercise.network.responses.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RewardService {
    @GET("v1/rewards")
    suspend fun getRewards(
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<List<Reward>>

    @GET("v1/rewards/list")
    suspend fun getAvailableRewards(
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<AvailableRewardResponse>

    @POST("v1/rewards/redeem")
    suspend fun reward(
        @Body body: Any,
    ): Response<Any>

    @POST("v1/rewards/redemptions")
    suspend fun rewardRedemptions(
        @Body body: Any,
    ): Response<Any>

    @GET("v1/rewards/redemptions")
    suspend fun getRedeemRewards(
        @Query("page") page: Int,
        @Query("page_size") size: Int,
        @Query("type") type: String
    ): Response<List<RedeemRewards>>

    @GET("v1/rewards/{reward_id}")
    suspend fun getRewardDetail(
        @Path("reward_id") id: Int
    ): Response<RewardDetail>
}
