package ru.netology.nework.data.repository

import android.util.Log
import ru.netology.nework.data.mapper.Job
import ru.netology.nework.data.mapper.JobDto
import ru.netology.nework.data.remote.api.JobsApi
import ru.netology.nework.domain.model.Job
import ru.netology.nework.domain.repository.JobsRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import javax.inject.Inject

class JobsRepositoryImpl @Inject constructor(
    private val jobsApi: JobsApi
) : JobsRepository {

    override suspend fun getUserJobs(userId: String): Result<List<Job>> {
        return try {
            val response = jobsApi.getUserJobs(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), "jobs_error")
            }
            val jobsDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            Result.success(jobsDto.map { Job(it) })
        } catch (e: Exception) {
            Log.e("JobsRepository", "getUserJobs failed", e)
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun createJob(job: Job): Result<Job> {
        return try {
            val response = jobsApi.createJob(JobDto(job))
            if (!response.isSuccessful) {
                throw ApiError(response.code(), "create_job_error")
            }
            val createdDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            Result.success(Job(createdDto))
        } catch (e: Exception) {
            Log.e("JobsRepository", "createJob failed", e)
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun deleteJob(jobId: String): Result<Unit> {
        return try {
            val response = jobsApi.deleteJob(jobId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), "delete_job_error")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("JobsRepository", "deleteJob failed", e)
            Result.failure(AppError.from(e))
        }
    }
}