package ru.netology.nework.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.domain.model.Job
import ru.netology.nework.domain.model.Post
import ru.netology.nework.domain.model.User

interface UsersRepository {
    fun getUsers(): Flow<List<User>>
    suspend fun getUser(userId: String): Result<User>
    fun getUserWall(userId: String): Flow<PagingData<Post>>
    suspend fun getUserJobs(userId: String): Result<List<Job>>
}