package ru.netology.nework.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.data.remote.dto.JobDto

interface JobsApi {

    @GET("api/{userId}/jobs")
    suspend fun getUserJobs(@Path("userId") userId: String): Response<List<JobDto>>

    @POST("api/my/jobs")
    suspend fun createJob(@Body job: JobDto): Response<JobDto>

    @DELETE("api/my/jobs/{id}")
    suspend fun deleteJob(@Path("id") jobId: String): Response<Unit>
}