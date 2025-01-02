package com.ai.app.move.deskercise.network.repositories

import android.net.Uri
import com.ai.app.move.deskercise.network.responses.UploadFileResponse
import com.ai.app.move.deskercise.network.services.FileService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

interface FileRepository {
    suspend fun uploadAvatar(uri: Uri): UploadFileResponse
}

class FileRepositoryImpl(
    private val fileService: FileService,
) : FileRepository {
    override suspend fun uploadAvatar(uri: Uri): UploadFileResponse {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
        requestBody.addPart(prepareFilePart("file", uri))
        return fileService.uploadFile("avatar", requestBody.build().parts).data
            ?: throw Exception("Unknown error")
    }

    private fun prepareFilePart(
        partName: String,
        fileUri: Uri,
    ): MultipartBody.Part {
        val file = File(fileUri.path.orEmpty())
        val requestFile = file.asRequestBody(("image/png").toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
}
