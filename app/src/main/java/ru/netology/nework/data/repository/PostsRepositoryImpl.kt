package ru.netology.nework.data.repository

import android.util.Log
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.netology.nework.data.mapper.Post
import ru.netology.nework.data.remote.api.PostsApi
import ru.netology.nework.domain.model.Post
import ru.netology.nework.domain.repository.PostsRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import javax.inject.Inject

class PostsRepositoryImpl @Inject constructor(
    private val postsApi: PostsApi
) : PostsRepository {

    override fun getFeed(): Flow<PagingData<Post>> = flow {
        try {
            val response = postsApi.getFeed()
            if (response.isSuccessful && response.body() != null) {
                val posts = response.body()!!.map { Post(it) }
                emit(PagingData.from(posts))
            } else {
                throw ApiError(response.code(), "feed_error")
            }
        } catch (e: Exception) {
            emit(PagingData.from(emptyList<Post>()))
        }
    }

    override suspend fun getPost(postId: String): Result<Post> {
        return try {
            val response = postsApi.getPost(postId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(Post(response.body()!!))
            } else {
                Result.failure(ApiError(response.code(), "post_not_found"))
            }
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun createPost(post: Post): Result<Post> =
        Result.failure(UnsupportedOperationException("createPost not implemented yet"))

    override suspend fun likePost(postId: String, liked: Boolean): Result<Unit> {
        return try {
            val response = if (liked) {
                postsApi.likePost(postId)
            } else {
                postsApi.unlikePost(postId)
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
            val response = postsApi.deletePost(postId)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "delete_error")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }
}