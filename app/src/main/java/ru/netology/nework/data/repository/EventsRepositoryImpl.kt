package ru.netology.nework.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.BuildConfig
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.data.mapper.Event
import ru.netology.nework.data.paging.EventPagingSource
import ru.netology.nework.data.remote.api.EventsApi
import ru.netology.nework.data.remote.dto.CoordsDto
import ru.netology.nework.data.remote.dto.EventCreateDto
import ru.netology.nework.domain.model.Event
import ru.netology.nework.domain.model.EventType
import ru.netology.nework.domain.repository.EventsRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import javax.inject.Inject

class EventsRepositoryImpl @Inject constructor(
    private val eventsApi: EventsApi,
    private val tokenStorage: TokenStorage
) : EventsRepository {

    override fun getEvents(): Flow<PagingData<Event>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = {
                EventPagingSource(eventsApi, pageSize = 20)
            }
        ).flow
    }

    override suspend fun getEvent(eventId: String): Result<Event> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")

            val response = eventsApi.getEvent(
                token = token,
                apiKey = BuildConfig.NETOLOGY_API_KEY,
                id = eventId.toInt()
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "event_not_found")
            }
            val eventDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            Result.success(Event(eventDto))

        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun createEvent(
        content: String,
        type: EventType,
        datetime: Long?,
        coords: Pair<Double, Double>?,
        speakerIds: List<String>
    ): Result<Unit> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")

            val eventDto = EventCreateDto(
                content = content,
                datetime = (datetime ?: System.currentTimeMillis()) / 1000,
                type = type.name,
                coords = coords?.let { CoordsDto(it.first, it.second) },
                speakerIds = if (speakerIds.isNotEmpty()) speakerIds.mapNotNull { it.toIntOrNull() } else null
            )

            val response = eventsApi.createEvent(
                token = token,
                apiKey = BuildConfig.NETOLOGY_API_KEY,
                event = eventDto
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun joinEvent(eventId: String): Result<Unit> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")

            val response = eventsApi.joinEvent(
                token = token,
                apiKey = BuildConfig.NETOLOGY_API_KEY,
                id = eventId.toInt()
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "join_error")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun leaveEvent(eventId: String): Result<Unit> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")

            val response = eventsApi.leaveEvent(
                token = token,
                apiKey = BuildConfig.NETOLOGY_API_KEY,
                id = eventId.toInt()
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "leave_error")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun likeEvent(eventId: String, liked: Boolean): Result<Unit> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")

            val response = if (liked) {
                eventsApi.likeEvent(
                    token = token,
                    apiKey = BuildConfig.NETOLOGY_API_KEY,
                    id = eventId.toInt()
                )
            } else {
                eventsApi.unlikeEvent(
                    token = token,
                    apiKey = BuildConfig.NETOLOGY_API_KEY,
                    id = eventId.toInt()
                )
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "like_event_error")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")

            val response = eventsApi.deleteEvent(
                token = token,
                apiKey = BuildConfig.NETOLOGY_API_KEY,
                id = eventId.toInt()
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "delete_event_error")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }
}