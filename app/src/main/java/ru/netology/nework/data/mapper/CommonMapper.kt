package ru.netology.nework.data.mapper

import ru.netology.nework.data.remote.dto.AttachmentDto
import ru.netology.nework.data.remote.dto.CoordinatesDto
import ru.netology.nework.data.remote.dto.UserDto
import ru.netology.nework.domain.model.Attachment
import ru.netology.nework.domain.model.AttachmentType
import ru.netology.nework.domain.model.Coordinates
import ru.netology.nework.domain.model.User

fun User(dto: UserDto): User = User(
    id = dto.id,
    login = dto.login,
    name = dto.name,
    avatar = dto.avatar
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

fun UserDto(user: User): UserDto = UserDto(
    id = user.id,
    login = user.login,
    name = user.name,
    avatar = user.avatar
)

fun AttachmentDto(attachment: Attachment): AttachmentDto = AttachmentDto(
    url = attachment.url,
    type = when (attachment.type) {
        AttachmentType.IMAGE -> "image"
        AttachmentType.VIDEO -> "video"
        AttachmentType.AUDIO -> "audio"
        AttachmentType.NONE -> "none"
    }
)

fun CoordinatesDto(coords: Coordinates): CoordinatesDto = CoordinatesDto(
    lat = coords.lat,
    long = coords.long
)