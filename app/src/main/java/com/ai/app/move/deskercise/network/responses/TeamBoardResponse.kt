package com.ai.app.move.deskercise.network.responses

import com.google.gson.annotations.SerializedName

data class TeamBoardResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") var name: String,
    @SerializedName("score") val score: Int,
    @SerializedName("current_rank") val currentRank: Int = 0,
    @SerializedName("before_rank") val beforeRank: Int = 0,

    )
