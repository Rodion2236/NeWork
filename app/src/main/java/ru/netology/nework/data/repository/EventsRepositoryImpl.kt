package ru.netology.nework.data.repository

import android.util.Log
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.netology.nework.data.mapper.Event
import ru.netology.nework.data.mapper.EventDto
import ru.netology.nework.data.remote.api.EventsApi
import ru.netology.nework.domain.model.Event
import ru.netology.nework.domain.repository.EventsRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import javax.inject.Inject

class EventsRepositoryImpl @Inject constructor(
    private val eventsApi: EventsApi
) : EventsRepository {

    override fun getEvents(): Flow<PagingData<Event>> = flow {
        try {
            val response = eventsApi.getEvents()

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "events_error")
            }

            val eventsDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            val events = eventsDto.map { Event(it) }
            emit(PagingData.from(events))

        } catch (e: Exception) {
            Log.e("EventsRepository", "getEvents failed", e)
            emit(PagingData.from(emptyList<Event>()))
        }
    }

    override suspend fun getEvent(eventId: String): Result<Event> {
        return try {
            val response = eventsApi.getEvent(eventId.toInt())

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "event_not_found")
            }
            val eventDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            Result.success(Event(eventDto))

        } catch (e: Exception) {
            Log.e("EventsRepository", "getEvent failed", e)
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val response = eventsApi.createEvent(EventDto(event))

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "create_event_error")
            }
            val createdDto = response.body() ?: throw ApiError(response.code(), "empty_response")
            Result.success(Event(createdDto))

        } catch (e: Exception) {
            Log.e("EventsRepository", "createEvent failed", e)
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun updateEvent(eventId: String, event: Event): Result<Event> {
        return Result.failure(UnsupportedOperationException("updateEvent not implemented"))
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            val response = eventsApi.deleteEvent(eventId.toInt())

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "delete_event_error")
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("EventsRepository", "deleteEvent failed", e)
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun likeEvent(eventId: String, liked: Boolean): Result<Unit> {
        return try {
            val response = if (liked) {
                eventsApi.likeEvent(eventId.toInt())
            } else {
                eventsApi.unlikeEvent(eventId.toInt())
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "like_event_error")
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("EventsRepository", "likeEvent failed", e)
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun joinEvent(eventId: String): Result<Unit> {
        return try {
            val response = eventsApi.joinEvent(eventId.toInt())
            if (!response.isSuccessful) {
                throw ApiError(response.code(), "join_error")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventsRepository", "joinEvent failed", e)
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun leaveEvent(eventId: String): Result<Unit> {
        return try {
            val response = eventsApi.leaveEvent(eventId.toInt())
            if (!response.isSuccessful) {
                throw ApiError(response.code(), "leave_error")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventsRepository", "leaveEvent failed", e)
            Result.failure(AppError.from(e))
        }
    }
}