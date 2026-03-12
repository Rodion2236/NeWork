package ru.netology.nework.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nework.data.remote.dto.AuthenticationRequest
import ru.netology.nework.data.remote.dto.RegistrationRequest
import ru.netology.nework.data.remote.dto.TokenResponse
import ru.netology.nework.data.remote.dto.UserDto

interface AuthApi {
    @POST("api/users/authentication")
    suspend fun authenticate(@Body request: AuthenticationRequest): Response<TokenResponse>

    @POST("api/users/registration")
    suspend fun register(@Body request: RegistrationRequest): Response<TokenResponse>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<UserDto>

    @GET("api/users")
    suspend fun getUsers(): Response<List<UserDto>>
}