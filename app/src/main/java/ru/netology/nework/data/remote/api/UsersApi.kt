package ru.netology.nework.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.data.remote.dto.PostDto
import ru.netology.nework.data.remote.dto.UserDto

interface UsersApi {
    @GET("api/users")
    suspend fun getUsers(): Response<List<UserDto>>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<UserDto>

    @GET("api/{authorId}/wall")
    suspend fun getUserWall(
        @Path("authorId") authorId: String,
        @Query("limit") limit: Int = 20,
        @Query("id") lastId: String? = null
    ): Response<List<PostDto>>
}