package ru.netology.nework.data.repository

import android.content.Context
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.BuildConfig
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.data.mapper.Post
import ru.netology.nework.data.paging.PostPagingSource
import ru.netology.nework.data.remote.api.MediaApi
import ru.netology.nework.data.remote.api.PostsApi
import ru.netology.nework.data.remote.dto.AttachmentDto
import ru.netology.nework.data.remote.dto.CoordsDto
import ru.netology.nework.data.remote.dto.PostCreateDto
import ru.netology.nework.domain.model.Post
import ru.netology.nework.domain.repository.PostsRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PostsRepositoryImpl @Inject constructor(
    private val postsApi: PostsApi,
    private val mediaApi: MediaApi,
    private val tokenStorage: TokenStorage,
    @ApplicationContext private val context: Context
) : PostsRepository {

    override fun getPosts(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                PostPagingSource(postsApi, pageSize = 20, tokenStorage.getUserId())
            }
        ).flow
    }

    private suspend fun uploadAttachment(uri: Uri): Result<String> {
        return try {
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return Result.failure(Exception("Cannot read file"))

            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val multipart = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

            val token = tokenStorage.getToken() ?: return Result.failure(Exception("No token"))

            val response = mediaApi.uploadMedia(
                token = token,
                apiKey = BuildConfig.NETOLOGY_API_KEY,
                file = multipart
            )

            tempFile.delete()

            if (!response.isSuccessful) {
                return Result.failure(ApiError(response.code(), "upload_failed"))
            }

            val uploadResponse = response.body()
                ?: return Result.failure(ApiError(response.code(), "empty_response"))

            Result.success(uploadResponse.url)

        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun createPost(
        content: String,
        imageUri: Uri?,
        fileUri: Uri?,
        coords: Pair<Double, Double>?,
        mentionIds: List<String>,
        userId: String?
    ): Result<Unit> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")

            val attachmentDto = when {
                imageUri != null && imageUri.scheme == "content" -> {
                    val uploadResult = uploadAttachment(imageUri)
                    uploadResult.getOrNull()?.let { url ->
                        AttachmentDto(url = url, type = "IMAGE")
                    }
                }
                imageUri != null && imageUri.scheme == "https" -> {
                    AttachmentDto(url = imageUri.toString(), type = "IMAGE")
                }
                fileUri != null && fileUri.scheme == "content" -> {
                    val uploadResult = uploadAttachment(fileUri)
                    uploadResult.getOrNull()?.let { url ->
                        val fileType = when (fileUri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()) {
                            "mp4", "avi", "mov", "mkv" -> "VIDEO"
                            "mp3", "wav", "ogg", "m4a" -> "AUDIO"
                            else -> "VIDEO"
                        }
                        AttachmentDto(url = url, type = fileType)
                    }
                }
                fileUri != null && fileUri.scheme == "https" -> {
                    val fileType = when (fileUri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()) {
                        "mp4", "avi", "mov", "mkv" -> "VIDEO"
                        "mp3", "wav", "ogg", "m4a" -> "AUDIO"
                        else -> "VIDEO"
                    }
                    AttachmentDto(url = fileUri.toString(), type = fileType)
                }
                else -> null
            }

            val postDto = PostCreateDto(
                content = content,
                link = null,
                coords = coords?.let { CoordsDto(it.first, it.second) },
                mentionIds = if (mentionIds.isNotEmpty()) mentionIds.mapNotNull { it.toIntOrNull() } else null,
                attachment = attachmentDto
            )

            val response = postsApi.createPost(
                token = token,
                post = postDto
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun likePost(postId: String, liked: Boolean): Result<Unit> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")
            val response = if (liked) {
                postsApi.likePost(
                    token = token,
                    apiKey = BuildConfig.NETOLOGY_API_KEY,
                    postId = postId
                )
            } else {
                postsApi.unlikePost(
                    token = token,
                    apiKey = BuildConfig.NETOLOGY_API_KEY,
                    postId = postId
                )
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), "like_error")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")

            val response = postsApi.deletePost(
                token = token,
                apiKey = BuildConfig.NETOLOGY_API_KEY,
                postId = postId
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "delete_error")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun getPost(postId: String): Result<Post> {
        return try {
            val response = postsApi.getPost(postId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), "post_not_found")
            }
            val postDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            Result.success(Post(postDto, tokenStorage.getUserId()))
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }
}