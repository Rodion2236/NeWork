package ru.netology.nework.data.mapper

import ru.netology.nework.data.remote.dto.EventDto
import ru.netology.nework.domain.model.Event
import ru.netology.nework.domain.model.EventType

fun Event(dto: EventDto): Event = Event(
    id = dto.id,
    authorId = dto.authorId,
    author = dto.author,
    authorJob = dto.authorJob,
    authorAvatar = dto.authorAvatar,
    content = dto.content,
    published = dto.published,
    eventDate = dto.eventDate,
    type = when (dto.type.lowercase()) {
        "offline" -> EventType.OFFLINE
        else -> EventType.ONLINE
    },
    coords = dto.coords?.let { Coordinates(it) },
    link = dto.link,
    mentionIds = dto.mentionIds,
    mentionedMe = dto.mentionedMe,
    likeOwnerIds = dto.likeOwnerIds,
    likedByMe = dto.likedByMe,
    likeCount = dto.likeCount,
    attachment = dto.attachment?.let { Attachment(it) },
    participants = dto.participants.map { User(it) },
    speakers = dto.speakers.map { User(it) },
    users = dto.users.mapValues { it.value.let { userDto -> User(userDto) } }
)

fun EventDto(event: Event): EventDto = EventDto(
    id = event.id,
    authorId = event.authorId,
    author = event.author,
    authorJob = event.authorJob,
    authorAvatar = event.authorAvatar,
    content = event.content,
    published = event.published,
    eventDate = event.eventDate,
    type = when (event.type) {
        EventType.ONLINE -> "online"
        EventType.OFFLINE -> "offline"
    },
    coords = event.coords?.let { CoordinatesDto(it) },
    link = event.link,
    mentionIds = event.mentionIds,
    mentionedMe = event.mentionedMe,
    likeOwnerIds = event.likeOwnerIds,
    likedByMe = event.likedByMe,
    likeCount = event.likeCount,
    attachment = event.attachment?.let { AttachmentDto(it) },
    participants = event.participants.map { UserDto(it) },
    speakers = event.speakers.map { UserDto(it) },
    users = event.users.mapValues { it.value.let { user -> UserDto(user) } }
)