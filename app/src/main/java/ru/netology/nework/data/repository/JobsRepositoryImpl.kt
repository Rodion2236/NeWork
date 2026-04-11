package ru.netology.nework.data.repository

import ru.netology.nework.BuildConfig
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.data.mapper.Job
import ru.netology.nework.data.remote.api.JobsApi
import ru.netology.nework.data.remote.dto.JobCreateDto
import ru.netology.nework.domain.repository.JobsRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import ru.netology.nework.domain.model.Job as DomainJob

class JobsRepositoryImpl @Inject constructor(
    private val jobsApi: JobsApi,
    private val tokenStorage: TokenStorage
) : JobsRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun formatIsoDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    override suspend fun getMyJobs(): Result<List<DomainJob>> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")

            val response = jobsApi.getMyJobs(
                token = token,
                apiKey = BuildConfig.NETOLOGY_API_KEY
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "jobs_error")
            }

            val jobsDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            val jobs = jobsDto.map { Job(it) }
            Result.success(jobs)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun getUserJobs(userId: Int): Result<List<DomainJob>> {
        return try {
            val response = jobsApi.getUserJobs(
                apiKey = BuildConfig.NETOLOGY_API_KEY,
                userId = userId
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "jobs_error")
            }
            val jobsDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            val jobs = jobsDto.map { Job(it) }
            Result.success(jobs)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun createJob(
        name: String,
        position: String?,
        start: Long,
        finish: Long?,
        link: String?,
        userId: String?
    ): Result<Unit> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")

            val jobDto = JobCreateDto(
                name = name,
                position = position,
                start = formatIsoDate(start),
                finish = finish?.let { formatIsoDate(it) },
                link = link
            )

            val response = jobsApi.createJob(
                token = token,
                apiKey = BuildConfig.NETOLOGY_API_KEY,
                job = jobDto
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun deleteJob(jobId: Int): Result<Unit> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")

            val response = jobsApi.deleteJob(
                token = token,
                apiKey = BuildConfig.NETOLOGY_API_KEY,
                id = jobId
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "delete_error")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }
}