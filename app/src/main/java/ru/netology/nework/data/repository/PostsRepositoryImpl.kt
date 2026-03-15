package ru.netology.nework.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.netology.nework.data.remote.api.PostsApi
import ru.netology.nework.domain.model.Post
import ru.netology.nework.domain.repository.PostsRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import javax.inject.Inject
import ru.netology.nework.data.mapper.Post as PostMapper
import ru.netology.nework.data.mapper.PostDto as PostDtoMapper

class PostsRepositoryImpl @Inject constructor(
    private val postsApi: PostsApi
) : PostsRepository {

    override fun getPosts(): Flow<PagingData<Post>> = flow {
        try {
            val response = postsApi.getPosts()

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "posts_error")
            }

            val postsDto = response.body() ?: throw ApiError(response.code(), "empty_response")

            val posts = postsDto.map { PostMapper(it) }
            emit(PagingData.from(posts))

        } catch (e: Exception) {
            emit(PagingData.from(emptyList<Post>()))
        }
    }

    override suspend fun createPost(post: Post): Result<Post> {
        return try {
            val postDto = PostDtoMapper(post)

            val response = postsApi.createPost(postDto)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), "create_post_error")
            }
            val createdDto = response.body() ?: throw ApiError(response.code(), "empty_response")

            Result.success(PostMapper(createdDto))
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

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