package ru.netology.nework.data.mapper

import ru.netology.nework.data.remote.dto.AttachmentDto
import ru.netology.nework.data.remote.dto.CoordinatesDto
import ru.netology.nework.data.remote.dto.PostDto
import ru.netology.nework.domain.model.Attachment
import ru.netology.nework.domain.model.AttachmentType
import ru.netology.nework.domain.model.Coordinates
import ru.netology.nework.domain.model.Post

fun Post(dto: PostDto): Post = Post(
    id = dto.id,
    authorId = dto.authorId,
    author = dto.author,
    authorJob = dto.authorJob,
    authorAvatar = dto.authorAvatar,
    content = dto.content,
    published = dto.published,
    coords = dto.coords?.let { Coordinates(it) },
    link = dto.link,
    mentionIds = dto.mentionIds,
    mentionedMe = dto.mentionedMe,
    likeOwnerIds = dto.likeOwnerIds,
    likedByMe = dto.likedByMe,
    attachment = dto.attachment?.let { Attachment(it) },
    users = dto.users.mapValues { User(it.value) }
)

fun Attachment(dto: AttachmentDto): Attachment = Attachment(
    url = dto.url,
    type = when (dto.type.lowercase()) {
        "image" -> AttachmentType.IMAGE
        "video" -> AttachmentType.VIDEO
        "audio" -> AttachmentType.AUDIO
        else -> AttachmentType.NONE
    }
)

fun Coordinates(dto: CoordinatesDto): Coordinates = Coordinates(
    lat = dto.lat,
    long = dto.long
)