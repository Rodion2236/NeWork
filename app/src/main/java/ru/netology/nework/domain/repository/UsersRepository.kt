package ru.netology.nework.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.domain.model.User

interface UsersRepository {
    fun getUsers(): Flow<PagingData<User>>
}