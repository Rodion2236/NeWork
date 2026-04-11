package ru.netology.nework.domain.repository

import ru.netology.nework.domain.model.Job

interface JobsRepository {
    suspend fun getMyJobs(): Result<List<Job>>
    suspend fun getUserJobs(userId: Int): Result<List<Job>>
    suspend fun createJob(
        name: String,
        position: String?,
        start: Long,
        finish: Long?,
        link: String?,
        userId: String? = null
    ): Result<Unit>
    suspend fun deleteJob(jobId: Int): Result<Unit>
}