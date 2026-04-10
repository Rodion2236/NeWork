package ru.netology.nework.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.data.remote.dto.PostCreateDto
import ru.netology.nework.data.remote.dto.PostDto

interface PostsApi {

    @GET("api/posts")
    suspend fun getPosts(): Response<List<PostDto>>

    @GET("api/posts/latest")
    suspend fun getPostsLatest(
        @Query("count") count: Int,
        @Query("offset") offset: Int = 0
    ): Response<List<PostDto>>

    @GET("api/posts/{id}")
    suspend fun getPost(@Path("id") id: String): Response<PostDto>

    @POST("api/posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body post: PostCreateDto
    ): Response<PostDto>

    @POST("api/posts/{id}/likes")
    suspend fun likePost(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Path("id") postId: String
    ): Response<Unit>

    @DELETE("api/posts/{id}/likes")
    suspend fun unlikePost(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Path("id") postId: String
    ): Response<Unit>

    @DELETE("api/posts/{id}")
    suspend fun deletePost(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Path("id") postId: String
    ): Response<Unit>
}