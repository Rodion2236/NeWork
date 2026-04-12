package ru.netology.nework.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.netology.nework.domain.model.Event
import ru.netology.nework.data.remote.api.EventsApi
import ru.netology.nework.data.mapper.Event as EventMapper

class EventPagingSource(
    private val api: EventsApi,
    private val pageSize: Int = 20
) : PagingSource<Int, Event>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Event> {
        return try {
            val offset = maxOf(0, params.key ?: 0)

            val response = api.getEventsLatest(count = pageSize, offset = offset)
            if (!response.isSuccessful) {
                return LoadResult.Error(Exception("API error: ${response.code()}"))
            }

            val eventsDto = response.body() ?: emptyList()
            val events = eventsDto.map { EventMapper(it) }

            LoadResult.Page(
                data = events,
                prevKey = if (offset <= 0) null else offset - pageSize,
                nextKey = if (events.isEmpty()) null else offset + pageSize
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Event>): Int? {
        return 0
    }
}