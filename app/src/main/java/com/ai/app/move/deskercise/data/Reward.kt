package com.ai.app.move.deskercise.data

import com.google.gson.annotations.SerializedName

data class Reward(
    @SerializedName("id") val id: Int,
    @SerializedName("name") var name: String,
    @SerializedName("image") var image: String = "",
    @SerializedName("point") val point: Int,
    @SerializedName("amount") val amount: Int,
    @SerializedName("limit_amount_per_user") val limitAmountPerUser: Int,
)
