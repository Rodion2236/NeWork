package ru.netology.nework.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.data.remote.dto.TokenResponse
import ru.netology.nework.data.remote.dto.UserDto

interface AuthApi {
    @FormUrlEncoded
    @POST("api/users/authentication")
    suspend fun authenticate(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<TokenResponse>

    @Multipart
    @POST("api/users/registration")
    suspend fun register(
        @Query("login") login: String,
        @Query("pass") pass: String,
        @Query("name") name: String,
        @Part avatar: MultipartBody.Part? = null
    ): Response<TokenResponse>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<UserDto>
}