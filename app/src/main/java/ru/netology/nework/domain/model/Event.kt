package ru.netology.nework.domain.model

data class Event(
    val id: String,
    val authorId: String,
    val author: String,
    val authorJob: String?,
    val authorAvatar: String?,
    val content: String,
    val published: Long,
    val eventDate: Long,
    val type: EventType,
    val coords: Coordinates?,
    val link: String?,
    val mentionIds: List<String>,
    val mentionedMe: Boolean,
    val likeOwnerIds: List<String>,
    val likedByMe: Boolean,
    val likeCount: Int,
    val attachment: Attachment?,
    val participants: List<User>,
    val speakers: List<User>,
    val users: Map<String, User>
)

enum class EventType { ONLINE, OFFLINE }