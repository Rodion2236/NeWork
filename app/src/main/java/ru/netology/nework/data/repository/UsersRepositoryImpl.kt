package ru.netology.nework.data.repository

import android.util.Log
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.netology.nework.data.mapper.Job
import ru.netology.nework.data.mapper.Post
import ru.netology.nework.data.mapper.User
import ru.netology.nework.data.remote.api.UsersApi
import ru.netology.nework.domain.model.Job
import ru.netology.nework.domain.model.Post
import ru.netology.nework.domain.model.User
import ru.netology.nework.domain.repository.UsersRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi
) : UsersRepository {

    override fun getUsers(): Flow<PagingData<User>> = flow {
        try {
            val response = usersApi.getUsers()
            if (!response.isSuccessful) throw ApiError(response.code(), "users_error")
            val usersDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            emit(PagingData.from(usersDto.map { User(it) }))
        } catch (e: Exception) {
            Log.e("UsersRepository", "getUsers failed", e)
            emit(PagingData.from(emptyList()))
        }
    }

    override suspend fun getUser(userId: String): Result<User> {
        return try {
            val response = usersApi.getUser(userId)
            if (!response.isSuccessful) throw ApiError(response.code(), "user_not_found")
            val userDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            Result.success(User(userDto))
        } catch (e: Exception) {
            Log.e("UsersRepository", "getUser failed", e)
            Result.failure(AppError.from(e))
        }
    }

    override fun getUserWall(userId: String): Flow<PagingData<Post>> = flow {
        try {
            val response = usersApi.getUserWall(userId)
            if (!response.isSuccessful) throw ApiError(response.code(), "wall_error")
            val postsDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            emit(PagingData.from(postsDto.map { Post(it) }))
        } catch (e: Exception) {
            Log.e("UsersRepository", "getUserWall failed", e)
            emit(PagingData.from(emptyList()))
        }
    }

    override suspend fun getUserJobs(userId: String): Result<List<Job>> {
        return try {
            val response = usersApi.getUserJobs(userId)
            if (!response.isSuccessful) throw ApiError(response.code(), "jobs_error")
            val jobsDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            Result.success(jobsDto.map { Job(it) })
        } catch (e: Exception) {
            Log.e("UsersRepository", "getUserJobs failed", e)
            Result.failure(AppError.from(e))
        }
    }
}