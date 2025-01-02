package com.ai.app.move.deskercise.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Team(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("total_score") val totalScore: Float,
    @SerializedName("rank") val rank: Int,
    @SerializedName("count_user") val countUser: Int = 0,
): Serializable
