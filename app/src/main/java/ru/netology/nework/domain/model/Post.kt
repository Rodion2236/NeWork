package ru.netology.nework.domain.model

data class Post(
    val id: String,
    val authorId: String,
    val author: String,
    val authorJob: String?,
    val authorAvatar: String?,
    val content: String,
    val published: Long,
    val coords: Coordinates?,
    val link: String?,
    val mentionIds: List<String>,
    val mentionedMe: Boolean,
    val likeOwnerIds: List<String>,
    val likedByMe: Boolean,
    val attachment: Attachment?,
    val users: Map<String, User>
)

data class Attachment(
    val url: String,
    val type: AttachmentType
)

enum class AttachmentType { IMAGE, VIDEO, AUDIO, NONE }

data class Coordinates(
    val lat: Double,
    val long: Double
)