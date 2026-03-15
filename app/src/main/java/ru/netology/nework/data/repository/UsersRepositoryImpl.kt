package ru.netology.nework.data.repository

import android.util.Log
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.netology.nework.data.mapper.User
import ru.netology.nework.data.remote.api.UsersApi
import ru.netology.nework.domain.model.User
import ru.netology.nework.domain.repository.UsersRepository
import ru.netology.nework.error.ApiError
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi
) : UsersRepository {
    override fun getUsers(): Flow<PagingData<User>> = flow {
        try {
            val response = usersApi.getUsers()

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "users_error")
            }

            val usersDto = response.body() ?: throw ApiError(response.code(), "empty_response")

            val users = usersDto.map { User(it) }
            emit(PagingData.from(users))

        } catch (e: Exception) {
            Log.e("UsersRepository", "getUsers failed", e)
            emit(PagingData.from(emptyList<User>()))
        }
    }
}