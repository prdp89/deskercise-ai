package com.ai.app.move.deskercise.data

import com.google.gson.annotations.SerializedName

data class AvailableRewardResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("results") val availableRewards: List<AvailableReward>,
    @SerializedName("total_page") val totalPage: Int
)