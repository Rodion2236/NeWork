package ru.netology.nework.domain.repository

import android.net.Uri
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.domain.model.Event
import ru.netology.nework.domain.model.EventType

interface EventsRepository {

    fun getEvents(): Flow<PagingData<Event>>

    suspend fun getEvent(eventId: String): Result<Event>

    suspend fun createEvent(
        content: String,
        type: EventType,
        datetime: Long?,
        coords: Pair<Double, Double>?,
        speakerIds: List<String>,
        link: String?,
        imageUri: Uri? = null,
        fileUri: Uri? = null
    ): Result<Unit>

    suspend fun deleteEvent(eventId: String): Result<Unit>

    suspend fun likeEvent(eventId: String, liked: Boolean): Result<Unit>

    suspend fun joinEvent(eventId: String): Result<Unit>

    suspend fun leaveEvent(eventId: String): Result<Unit>
}