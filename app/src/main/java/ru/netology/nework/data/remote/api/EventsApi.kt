package ru.netology.nework.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.data.remote.dto.EventDto

interface EventsApi {
    @GET("api/events")
    suspend fun getEvents(
        @Query("limit") limit: Int = 20,
        @Query("id") lastId: String? = null
    ): Response<List<EventDto>>

    @GET("api/events/{id}")
    suspend fun getEvent(@Path("id") eventId: String): Response<EventDto>

    @POST("api/events")
    suspend fun createEvent(@Body event: EventDto): Response<EventDto>

    @PUT("api/events/{id}")
    suspend fun updateEvent(@Path("id") eventId: String, @Body event: EventDto): Response<EventDto>

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(@Path("id") eventId: String): Response<Unit>

    @POST("api/events/{id}/likes")
    suspend fun likeEvent(@Path("id") eventId: String): Response<Unit>

    @DELETE("api/events/{id}/likes")
    suspend fun unlikeEvent(@Path("id") eventId: String): Response<Unit>
}