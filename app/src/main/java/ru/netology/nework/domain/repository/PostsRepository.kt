package ru.netology.nework.domain.repository

import android.net.Uri
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.domain.model.Post

interface PostsRepository {
    fun getPosts(): Flow<PagingData<Post>>
    suspend fun createPost(
        content: String,
        imageUri: Uri?,
        fileUri: Uri?,
        coords: Pair<Double, Double>?,
        mentionIds: List<String>
    ): Result<Unit>

    suspend fun likePost(postId: String, liked: Boolean): Result<Unit>

    suspend fun deletePost(postId: String): Result<Unit>

    suspend fun getPost(postId: String): Result<Post>
}