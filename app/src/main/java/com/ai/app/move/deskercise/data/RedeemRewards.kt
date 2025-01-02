package com.ai.app.move.deskercise.data

import com.google.gson.annotations.SerializedName

data class RedeemRewards(
    @SerializedName("id") val id: Int,
    @SerializedName("point") val point: Int,
    @SerializedName("reward_id") val rewardId: Int,
    @SerializedName("redemption_id") val redemptionId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("status") val status: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("redemption_detail") val redemptionDetail: RedemptionDetail,
) {
    data class RedemptionDetail(
        @SerializedName("reward_validity") val rewardValidity: String? = null,
        @SerializedName("code") val code: String,
        @SerializedName("description") val description: String,
        @SerializedName("reward_name") val rewardName: String,
        @SerializedName("reward_thumbnail_img_url") val rewardThumbnailImgUrl: String = "",
        @SerializedName("reward_display_img_url") val rewardDisplayImgUrl: String = "",
    )
}