package ru.netology.nework.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.netology.nework.domain.model.Post
import ru.netology.nework.data.remote.api.PostsApi
import ru.netology.nework.data.mapper.Post as PostMapper
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError

class PostPagingSource(
    private val api: PostsApi,
    private val pageSize: Int = 20,
    private val currentUserId: String?
) : PagingSource<Int, Post>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        return try {
            val offset = maxOf(0, params.key ?: 0)

            val response = api.getPostsLatest(
                count = params.loadSize,
                offset = offset
            )

            if (!response.isSuccessful) {
                return LoadResult.Error(ApiError(response.code(), "load_failed"))
            }

            val postDtos = response.body() ?: emptyList()
            val posts = postDtos.map { PostMapper(it, currentUserId) }

            LoadResult.Page(
                data = posts,
                prevKey = if (offset <= 0) null else offset - params.loadSize,
                nextKey = if (posts.isEmpty()) null else offset + params.loadSize
            )
        } catch (e: Exception) {
            LoadResult.Error(AppError.from(e))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Post>): Int {
        return 0
    }
}