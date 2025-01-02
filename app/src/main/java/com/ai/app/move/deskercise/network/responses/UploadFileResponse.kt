package com.ai.app.move.deskercise.network.responses

import com.google.gson.annotations.SerializedName

data class UploadFileResponse(
    @SerializedName("url") val url: String,
)
