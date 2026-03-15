package ru.netology.nework.domain.repository

import ru.netology.nework.domain.model.Job

interface JobsRepository {
    suspend fun getUserJobs(userId: String): Result<List<Job>>
    suspend fun createJob(job: Job): Result<Job>
    suspend fun deleteJob(jobId: String): Result<Unit>
}