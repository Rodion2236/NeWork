package ru.netology.nework.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.domain.model.Post

interface PostsRepository {
    fun getPosts(): Flow<PagingData<Post>>
    suspend fun createPost(post: Post): Result<Post>

    suspend fun likePost(postId: String, liked: Boolean): Result<Unit>

    suspend fun deletePost(postId: String): Result<Unit>

    suspend fun getPost(postId: String): Result<Post>
}