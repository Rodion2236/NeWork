package ru.netology.nework.data.mapper

import ru.netology.nework.data.remote.dto.PostDto
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
    likeCount = dto.likeCount,
    attachment = dto.attachment?.let { Attachment(it) },
    users = dto.users.mapValues { it.value.let { userDto -> User(userDto) } }
)