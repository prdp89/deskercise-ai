package com.ai.app.move.deskercise.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("cognito_uid") val cognitoUid: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") var name: String,
    @SerializedName("gender") var gender: Int,
    @SerializedName("DOB") var dob: String? = null,
    @SerializedName("total_score") val totalScore: Int,
    @SerializedName("special_code") val signUpCode: String?,
    @SerializedName("point") val point: Int,
    @SerializedName("streak") val streak: Int,
    @SerializedName("team") var team: Team? = null,
    @SerializedName("company") var company: Company? = null,
    @SerializedName("rank") val rank: Int,
    @SerializedName("count_session_completed") val countSessionCompleted: Int,
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("avatar") var avatar: String? = "",
    @SerializedName("current_rank") val currentRank: Int = 0,
    @SerializedName("before_rank") val beforeRank: Int = 0,
    @SerializedName("designation") var designation: String? = null,
    @SerializedName("phone_number") var phoneNumber: String? = "",
    @SerializedName("country") var country: String? = "",
    @SerializedName("timezone") var timezone: String? = "",
    @SerializedName("score") var score: Int
): Serializable
