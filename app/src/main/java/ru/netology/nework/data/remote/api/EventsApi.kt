package ru.netology.nework.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.data.remote.dto.EventCreateDto
import ru.netology.nework.data.remote.dto.EventDto

interface EventsApi {
    @GET("api/events")
    suspend fun getEvents(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String
    ): Response<List<EventDto>>

    @GET("api/events/{id}")
    suspend fun getEvent(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Path("id") id: Int
    ): Response<EventDto>

    @POST("api/events")
    suspend fun createEvent(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Body event: EventCreateDto
    ): Response<EventDto>

    @POST("api/events/{id}/participants")
    suspend fun joinEvent(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Path("id") id: Int
    ): Response<EventDto>

    @DELETE("api/events/{id}/participants")
    suspend fun leaveEvent(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Path("id") id: Int
    ): Response<EventDto>

    @POST("api/events/{id}/likes")
    suspend fun likeEvent(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Path("id") id: Int
    ): Response<Unit>

    @DELETE("api/events/{id}/likes")
    suspend fun unlikeEvent(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Path("id") id: Int
    ): Response<Unit>

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(
        @Header("Authorization") token: String,
        @Header("Api-Key") apiKey: String,
        @Path("id") id: Int
    ): Response<Unit>
}