package com.ai.app.move.deskercise.network.services

import com.ai.app.move.deskercise.network.responses.Response
import com.ai.app.move.deskercise.network.responses.UploadFileResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FileService {
    @Multipart
    @POST("v1/files/upload")
    suspend fun uploadFile(
        @Query("type") type: String,
        @Part file: List<MultipartBody.Part>,
    ): Response<UploadFileResponse>
}
