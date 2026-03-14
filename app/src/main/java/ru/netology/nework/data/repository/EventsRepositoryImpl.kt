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
            if (response.isSuccessful && response.body() != null) {
                val events = response.body()!!.map { Event(it) }
                emit(PagingData.from(events))
            } else {
                throw ApiError(response.code(), "events_error")
            }
        } catch (e: Exception) {
            Log.e("EventsRepository", "getEvents failed", e)
            emit(PagingData.from(emptyList<Event>()))
        }
    }

    override suspend fun getEvent(eventId: String): Result<Event> {
        return try {
            val response = eventsApi.getEvent(eventId)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "event_not_found")
            }

            val eventDto = response.body()
                ?: throw ApiError(response.code(), "empty_response")

            Result.success(Event(eventDto))

        } catch (e: Exception) {
            Log.e("EventsRepository", "getEvent failed", e)
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val eventDto = EventDto(event)

            val response = eventsApi.createEvent(eventDto)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "create_event_error")
            }

            val createdDto = response.body()
                ?: throw ApiError(response.code(), "empty_response")

            Result.success(Event(createdDto))

        } catch (e: Exception) {
            Log.e("EventsRepository", "createEvent failed", e)
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun updateEvent(eventId: String, event: Event): Result<Event> {
        return try {
            val eventDto = EventDto(event)

            val response = eventsApi.updateEvent(eventId, eventDto)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), "update_event_error")
            }

            val updatedDto = response.body()
                ?: throw ApiError(response.code(), "empty_response")

            Result.success(Event(updatedDto))

        } catch (e: Exception) {
            Log.e("EventsRepository", "updateEvent failed", e)
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            val response = eventsApi.deleteEvent(eventId)

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
                eventsApi.likeEvent(eventId)
            } else {
                eventsApi.unlikeEvent(eventId)
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
}