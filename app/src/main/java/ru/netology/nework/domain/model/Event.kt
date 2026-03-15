package ru.netology.nework.domain.model

data class Event(
    val id: String,
    val authorId: String,
    val author: String,
    val authorJob: String?,
    val authorAvatar: String?,
    val content: String,
    val published: String,
    val datetime: String,
    val type: EventType,
    val coords: Coordinates?,
    val link: String?,
    val likeOwnerIds: List<String>,
    val likedByMe: Boolean,
    val likeCount: Int,
    val speakerIds: List<String>,
    val participantsIds: List<String>,
    val participatedByMe: Boolean,
    val attachment: Attachment?,
    val users: Map<String, UserPreview>
)

enum class EventType { ONLINE, OFFLINE }