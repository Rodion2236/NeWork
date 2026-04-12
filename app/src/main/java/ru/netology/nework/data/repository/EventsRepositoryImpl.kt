package ru.netology.nework.data.repository

import android.content.Context
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.BuildConfig
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.data.mapper.Event
import ru.netology.nework.data.paging.EventPagingSource
import ru.netology.nework.data.remote.api.EventsApi
import ru.netology.nework.data.remote.api.MediaApi
import ru.netology.nework.data.remote.dto.AttachmentDto
import ru.netology.nework.data.remote.dto.CoordsDto
import ru.netology.nework.data.remote.dto.EventCreateDto
import ru.netology.nework.domain.model.Event
import ru.netology.nework.domain.model.EventType
import ru.netology.nework.domain.repository.EventsRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class EventsRepositoryImpl @Inject constructor(
    private val eventsApi: EventsApi,
    private val mediaApi: MediaApi,
    private val tokenStorage: TokenStorage,
    @ApplicationContext private val context: Context
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

    private suspend fun uploadAttachment(uri: Uri): Result<String> {
        return try {
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return Result.failure(Exception("Cannot read file"))

            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val multipart = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

            val token = tokenStorage.getToken() ?: return Result.failure(Exception("No token"))

            val response = mediaApi.uploadMedia(
                token = token,
                apiKey = BuildConfig.NETOLOGY_API_KEY,
                file = multipart
            )

            tempFile.delete()

            if (!response.isSuccessful) {
                return Result.failure(ApiError(response.code(), "upload_failed"))
            }

            val uploadResponse = response.body()
                ?: return Result.failure(ApiError(response.code(), "empty_response"))

            Result.success(uploadResponse.url)

        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun createEvent(
        content: String,
        type: EventType,
        datetime: Long?,
        coords: Pair<Double, Double>?,
        speakerIds: List<String>,
        link: String?,
        imageUri: Uri?,
        fileUri: Uri?
    ): Result<Unit> {
        return try {
            val token = tokenStorage.getToken() ?: throw IllegalStateException("No auth token")

            val attachmentDto = when {
                imageUri != null && imageUri.scheme == "content" -> {
                    val uploadResult = uploadAttachment(imageUri)
                    uploadResult.getOrNull()?.let { url ->
                        AttachmentDto(url = url, type = "IMAGE")
                    }
                }
                imageUri != null && imageUri.scheme == "https" -> {
                    AttachmentDto(url = imageUri.toString(), type = "IMAGE")
                }
                fileUri != null && fileUri.scheme == "content" -> {
                    val uploadResult = uploadAttachment(fileUri)
                    uploadResult.getOrNull()?.let { url ->
                        val fileType = when (fileUri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()) {
                            "mp4", "avi", "mov", "mkv" -> "VIDEO"
                            "mp3", "wav", "ogg", "m4a" -> "AUDIO"
                            else -> "VIDEO"
                        }
                        AttachmentDto(url = url, type = fileType)
                    }
                }
                fileUri != null && fileUri.scheme == "https" -> {
                    val fileType = when (fileUri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()) {
                        "mp4", "avi", "mov", "mkv" -> "VIDEO"
                        "mp3", "wav", "ogg", "m4a" -> "AUDIO"
                        else -> "VIDEO"
                    }
                    AttachmentDto(url = fileUri.toString(), type = fileType)
                }
                else -> null
            }

            val eventDto = EventCreateDto(
                content = content,
                datetime = (datetime ?: System.currentTimeMillis()) / 1000,
                type = type.name,
                coords = coords?.let { CoordsDto(it.first, it.second) },
                speakerIds = if (speakerIds.isNotEmpty()) speakerIds.mapNotNull { it.toIntOrNull() } else null,
                attachment = attachmentDto,
                link = link
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