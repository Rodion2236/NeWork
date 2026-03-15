package ru.netology.nework.data.mapper

import ru.netology.nework.data.remote.dto.EventDto
import ru.netology.nework.domain.model.Event
import ru.netology.nework.domain.model.EventType

fun Event(dto: EventDto): Event = Event(
    id = dto.id.toString(),
    authorId = dto.authorId.toString(),
    author = dto.author,
    authorJob = dto.authorJob,
    authorAvatar = dto.authorAvatar,
    content = dto.content,
    published = dto.published,
    datetime = dto.datetime,
    type = when (dto.type.lowercase()) {
        "offline" -> EventType.OFFLINE
        else -> EventType.ONLINE
    },
    coords = dto.coords?.let { Coordinates(it) },
    link = dto.link,
    likeOwnerIds = dto.likeOwnerIds.map { it.toString() },
    likedByMe = dto.likedByMe,
    likeCount = dto.likeOwnerIds.size,
    speakerIds = dto.speakerIds.map { it.toString() },
    participantsIds = dto.participantsIds.map { it.toString() },
    participatedByMe = dto.participatedByMe,
    attachment = dto.attachment?.let { Attachment(it) },
    users = dto.users.mapValues { it.value.let { previewDto -> UserPreview(previewDto) } }
)

fun EventDto(event: Event): EventDto = EventDto(
    id = event.id.toInt(),
    authorId = event.authorId.toInt(),
    author = event.author,
    authorJob = event.authorJob,
    authorAvatar = event.authorAvatar,
    content = event.content,
    published = event.published,
    datetime = event.datetime,
    type = when (event.type) {
        EventType.ONLINE -> "online"
        EventType.OFFLINE -> "offline"
    },
    coords = event.coords?.let { CoordinatesDto(it) },
    link = event.link,
    likeOwnerIds = event.likeOwnerIds.map { it.toInt() },
    likedByMe = event.likedByMe,
    speakerIds = event.speakerIds.map { it.toInt() },
    participantsIds = event.participantsIds.map { it.toInt() },
    participatedByMe = event.participatedByMe,
    attachment = event.attachment?.let { AttachmentDto(it) },
    users = event.users.mapValues { it.value.let { preview -> UserPreviewDto(preview) } }
)