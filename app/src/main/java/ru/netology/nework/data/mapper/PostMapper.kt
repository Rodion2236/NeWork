package ru.netology.nework.data.mapper

import ru.netology.nework.data.remote.dto.PostDto
import ru.netology.nework.domain.model.Post

fun Post(dto: PostDto): Post = Post(
    id = dto.id.toString(),
    authorId = dto.authorId.toString(),
    author = dto.author,
    authorJob = dto.authorJob,
    authorAvatar = dto.authorAvatar,
    content = dto.content,
    published = dto.published,
    coords = dto.coords?.let { Coordinates(it) },
    link = dto.link,
    mentionIds = dto.mentionIds.map { it.toString() },
    mentionedMe = dto.mentionedMe,
    likeOwnerIds = dto.likeOwnerIds.map { it.toString() },
    likedByMe = dto.likedByMe,
    likeCount = dto.likeOwnerIds.size,
    attachment = dto.attachment?.let { Attachment(it) },
    users = dto.users.mapValues { it.value.let { previewDto -> UserPreview(previewDto) } }
)

fun PostDto(post: Post): PostDto = PostDto(
    id = post.id.toInt(),
    authorId = post.authorId.toInt(),
    author = post.author,
    authorJob = post.authorJob,
    authorAvatar = post.authorAvatar,
    content = post.content,
    published = post.published,
    coords = post.coords?.let { CoordinatesDto(it) },
    link = post.link,
    mentionIds = post.mentionIds.map { it.toInt() },
    mentionedMe = post.mentionedMe,
    likeOwnerIds = post.likeOwnerIds.map { it.toInt() },
    likedByMe = post.likedByMe,
    attachment = post.attachment?.let { AttachmentDto(it) },
    users = post.users.mapValues { it.value.let { preview -> UserPreviewDto(preview) } }
)