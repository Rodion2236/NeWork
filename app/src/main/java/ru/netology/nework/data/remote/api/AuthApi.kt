package ru.netology.nework.data.remote.api

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

    @FormUrlEncoded
    @POST("api/users/registration")
    suspend fun register(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String
    ): Response<TokenResponse>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<UserDto>
}