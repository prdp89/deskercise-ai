package com.ai.app.move.deskercise.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Company(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
) : Serializable
