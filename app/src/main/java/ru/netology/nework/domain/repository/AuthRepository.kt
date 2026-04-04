package ru.netology.nework.domain.repository

import android.net.Uri
import ru.netology.nework.domain.model.AuthResult
import ru.netology.nework.domain.model.User

interface AuthRepository {
    suspend fun authenticate(login: String, password: String): Result<AuthResult>
    suspend fun register(
        login: String,
        password: String,
        name: String,
        avatarUri: Uri?
    ): Result<AuthResult>
    suspend fun getUserById(userId: String): Result<User>
    fun isLoggedIn(): Boolean
    fun getToken(): String?
    fun logout()
}