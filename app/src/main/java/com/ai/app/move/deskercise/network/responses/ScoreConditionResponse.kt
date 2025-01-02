package com.ai.app.move.deskercise.network.responses

import com.google.gson.annotations.SerializedName
import com.ai.app.move.deskercise.data.Streak

data class ScoreConditionResponse(
    @SerializedName("SCORE_GOOD_REP") val goodReps: Int,
    @SerializedName("SCORE_BAD_REP") val badReps: Int,
    @SerializedName("SCORE_COMPLETE_STREAK") val streaks: List<Streak> = listOf(),
)
