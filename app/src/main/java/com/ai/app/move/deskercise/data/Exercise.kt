package com.ai.app.move.deskercise.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Exercise(
    @SerializedName("exerciseId") val exerciseId: Int,
    @SerializedName("countGood") var countGood: Int,
    @SerializedName("countBad") var countBad: Int,
    @SerializedName("completedReps") var completedReps: Int,
    @SerializedName("totalReps") var totalReps: Int,
) : Serializable
