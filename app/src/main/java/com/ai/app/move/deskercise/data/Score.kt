package com.ai.app.move.deskercise.data

import android.icu.util.Calendar
import com.google.gson.annotations.SerializedName
import java.util.Date

data class Score(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("created_at") val createdAt: Date = Calendar.getInstance().time,
    @SerializedName("updated_at") val updatedAt: String = "",
    @SerializedName("score") val score: Int,
    @SerializedName("session_id") val sessionId: Int,
    @SerializedName("count_completed_exercise") val countCompletedExercise: Int,
    @SerializedName("count_exercise") val countExercise: Int
)
