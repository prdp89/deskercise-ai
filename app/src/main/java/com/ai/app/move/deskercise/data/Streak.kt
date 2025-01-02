package com.ai.app.move.deskercise.data

import com.google.gson.annotations.SerializedName

data class Streak(
    @SerializedName("day")
    val day: Int = 0,
    @SerializedName("score")
    val score: Int = 0,
)
