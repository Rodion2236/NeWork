package ru.netology.nework.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.data.remote.dto.PostDto

interface PostsApi {

    @GET("api/posts")
    suspend fun getPosts(): Response<List<PostDto>>

    @POST("api/posts")
    suspend fun createPost(@Body post: PostDto): Response<PostDto>

    @POST("api/posts/{id}/likes")
    suspend fun likePost(@Path("id") postId: String): Response<Unit>

    @DELETE("api/posts/{id}/likes")
    suspend fun unlikePost(@Path("id") postId: String): Response<Unit>

    @GET("api/posts/{id}")
    suspend fun getPost(@Path("id") postId: String): Response<PostDto>

    @DELETE("api/posts/{id}")
    suspend fun deletePost(@Path("id") postId: String): Response<Unit>
}