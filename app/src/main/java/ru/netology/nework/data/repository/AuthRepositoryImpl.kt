package ru.netology.nework.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.data.mapper.AuthResult
import ru.netology.nework.data.mapper.User
import ru.netology.nework.data.remote.api.AuthApi
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
    private val tokenStorage: TokenStorage,
    @ApplicationContext private val context: Context
) : AuthRepository {

    override suspend fun authenticate(
        login: String,
        password: String
    ): Result<AuthResult> {
        return try {
            val response = authApi.authenticate(
                login = login,
                pass = password
            )

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
        name: String,
        avatarUri: Uri?,
    ): Result<AuthResult> {
        return try {
            val avatarPart = avatarUri?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: ByteArray(0)
                inputStream?.close()

                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val fileName = uri.lastPathSegment ?: "avatar.jpg"

                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

                MultipartBody.Part.createFormData("file", fileName, requestBody)
            }

            val response = authApi.register(
                login = login,
                pass = password,
                name = name,
                avatar = avatarPart
            )

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