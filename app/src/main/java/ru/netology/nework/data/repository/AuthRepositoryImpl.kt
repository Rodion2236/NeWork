package ru.netology.nework.data.repository

import android.util.Log
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.data.mapper.AuthResult
import ru.netology.nework.data.mapper.User
import ru.netology.nework.data.remote.api.AuthApi
import ru.netology.nework.data.remote.dto.AuthenticationRequest
import ru.netology.nework.data.remote.dto.RegistrationRequest
import ru.netology.nework.domain.model.AuthResult
import ru.netology.nework.domain.model.User
import ru.netology.nework.domain.repository.AuthRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import ru.netology.nework.error.NetworkError
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun authenticate(
        login: String,
        password: String
    ): Result<AuthResult> {
        return try {
            val response = authApi.authenticate(AuthenticationRequest(login, password))

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "auth_error")
            }

            val tokenResponse = response.body()
                ?: throw ApiError(response.code(), "empty_response")

            tokenStorage.saveToken(tokenResponse.token, tokenResponse.id)
            Result.success(AuthResult(tokenResponse))

        } catch (_: IOException) {
            Result.failure(NetworkError)
        } catch (e: ApiError) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun register(
        login: String,
        password: String,
        name: String
    ): Result<AuthResult> {
        return try {
            val response = authApi.register(RegistrationRequest(login, password, name))

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "register_error")
            }

            val tokenResponse = response.body()
                ?: throw ApiError(response.code(), "empty_response")

            tokenStorage.saveToken(tokenResponse.token, tokenResponse.id)
            Result.success(AuthResult(tokenResponse))

        } catch (e: IOException) {
            Log.e("AuthRepository", "register: network error", e)
            Result.failure(NetworkError)
        } catch (e: ApiError) {
            Log.e("AuthRepository", "register: api error", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthRepository", "register: unknown error", e)
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun getUserById(userId: String): Result<User> {
        return try {
            val response = authApi.getUser(userId)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "user_not_found")
            }

            val userDto = response.body()
                ?: throw ApiError(response.code(), "empty_response")

            Result.success(User(userDto))

        } catch (e: IOException) {
            Log.e("AuthRepository", "getUserById: network error", e)
            Result.failure(NetworkError)
        } catch (e: ApiError) {
            Log.e("AuthRepository", "getUserById: api error", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthRepository", "getUserById: unknown error", e)
            Result.failure(AppError.from(e))
        }
    }

    override fun isLoggedIn(): Boolean = tokenStorage.isLoggedIn()
    override fun getToken(): String? = tokenStorage.getToken()
    override fun logout() = tokenStorage.clear()
}