package ru.netology.nework.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.data.remote.dto.EventDto

interface EventsApi {
    @GET("api/events")
    suspend fun getEvents(): Response<List<EventDto>>

    @POST("api/events")
    suspend fun createEvent(@Body event: EventDto): Response<EventDto>

    @GET("api/events/{id}")
    suspend fun getEvent(@Path("id") eventId: Int): Response<EventDto>

    @POST("api/events/{id}/likes")
    suspend fun likeEvent(@Path("id") eventId: Int): Response<Unit>

    @DELETE("api/events/{id}/likes")
    suspend fun unlikeEvent(@Path("id") eventId: Int): Response<Unit>

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(@Path("id") eventId: Int): Response<Unit>

    @POST("api/events/{id}/participants")
    suspend fun joinEvent(@Path("id") eventId: Int): Response<Unit>

    @DELETE("api/events/{id}/participants")
    suspend fun leaveEvent(@Path("id") eventId: Int): Response<Unit>
}