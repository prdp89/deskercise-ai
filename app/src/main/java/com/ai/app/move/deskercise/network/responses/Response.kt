package com.ai.app.move.deskercise.network.responses

import com.google.gson.annotations.SerializedName

class Response<DATA>(
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: DATA? = null,
    @SerializedName("status") val status: Boolean = false,
) {
    fun requiredData(): DATA {
        return data ?: throw Exception("Unknown Error")
    }
}
