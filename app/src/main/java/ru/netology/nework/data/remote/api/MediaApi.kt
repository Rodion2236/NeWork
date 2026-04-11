package ru.netology.nework.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface MediaApi {
    @Multipart
    @POST("api/media")
    suspend fun uploadMedia(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Part file: MultipartBody.Part
    ): Response<MediaUploadResponse>
}

data class MediaUploadResponse(
    val url: String
)