package com.ai.app.move.deskercise.network.responses

import com.google.gson.annotations.SerializedName

data class StartProgramResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("program_id") val programId: Int,
    @SerializedName("count_completed_exercise") val countCompletedExercise: Int,
    @SerializedName("count_exercise") val countExercise: Int,
    @SerializedName("score") val score: Int,
    @SerializedName("is_added_point") val isAddedPoint: Boolean = false

)
