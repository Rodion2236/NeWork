package ru.netology.nework.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.data.remote.dto.JobDto
import ru.netology.nework.data.remote.dto.PostDto
import ru.netology.nework.data.remote.dto.UserDto

interface UsersApi {
    @GET("api/users")
    suspend fun getUsers(): Response<List<UserDto>>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<UserDto>

    @GET("api/{authorId}/wall")
    suspend fun getUserWall(@Path("authorId") authorId: String): Response<List<PostDto>>

    @GET("api/{userId}/jobs")
    suspend fun getUserJobs(@Path("userId") userId: String): Response<List<JobDto>>
}