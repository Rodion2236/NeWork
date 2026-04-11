package ru.netology.nework.data.repository

import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.BuildConfig
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.data.mapper.Post
import ru.netology.nework.data.paging.PostPagingSource
import ru.netology.nework.data.remote.api.PostsApi
import ru.netology.nework.data.remote.dto.CoordsDto
import ru.netology.nework.data.remote.dto.PostCreateDto
import ru.netology.nework.domain.model.Post
import ru.netology.nework.domain.repository.PostsRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import javax.inject.Inject

class PostsRepositoryImpl @Inject constructor(
    private val postsApi: PostsApi,
    private val tokenStorage: TokenStorage
) : PostsRepository {

    override fun getPosts(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                PostPagingSource(postsApi, pageSize = 20)
            }
        ).flow
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

            val postDto = PostCreateDto(
                content = content,
                link = null,
                coords = coords?.let { CoordsDto(it.first, it.second) },
                mentionIds = if (mentionIds.isNotEmpty()) mentionIds.mapNotNull { it.toIntOrNull() } else null,
                attachment = null
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
            Result.success(Post(postDto))
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }
}