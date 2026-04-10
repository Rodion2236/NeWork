package ru.netology.nework.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.netology.nework.domain.model.Post
import ru.netology.nework.data.remote.api.PostsApi
import ru.netology.nework.data.mapper.Post as PostMapper

class PostPagingSource(
    private val api: PostsApi,
    private val pageSize: Int = 20
) : PagingSource<Int, Post>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        return try {
            val currentPage = params.key ?: 0
            val offset = currentPage * pageSize

            val response = api.getPostsLatest(
                count = pageSize,
                offset = offset
            )

            if (!response.isSuccessful) {
                return LoadResult.Error(Exception("API error: ${response.code()}"))
            }

            val postsDto = response.body() ?: emptyList()

            val posts = postsDto.map { PostMapper(it) }

            LoadResult.Page(
                data = posts,
                prevKey = if (currentPage == 0) null else currentPage - 1,
                nextKey = if (posts.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}