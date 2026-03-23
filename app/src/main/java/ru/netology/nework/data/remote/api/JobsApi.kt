package ru.netology.nework.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.data.remote.dto.JobDto
import ru.netology.nework.data.remote.dto.JobCreateDto

interface JobsApi {

    @GET("api/my/jobs")
    suspend fun getMyJobs(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String
    ): Response<List<JobDto>>

    @GET("api/{userId}/jobs")
    suspend fun getUserJobs(
        @Header("Api-Key") apiKey: String,
        @Path("userId") userId: Int
    ): Response<List<JobDto>>

    @POST("api/my/jobs")
    suspend fun createJob(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Body job: JobCreateDto
    ): Response<JobDto>

    @DELETE("api/my/jobs/{id}")
    suspend fun deleteJob(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Path("id") id: Int
    ): Response<Unit>
}