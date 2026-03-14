package ru.netology.nework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EventDto(
    @SerializedName("id") val id: String,
    @SerializedName("authorId") val authorId: String,
    @SerializedName("author") val author: String,
    @SerializedName("authorJob") val authorJob: String?,
    @SerializedName("authorAvatar") val authorAvatar: String?,
    @SerializedName("content") val content: String,
    @SerializedName("published") val published: Long,
    @SerializedName("eventDate") val eventDate: Long,
    @SerializedName("type") val type: String,
    @SerializedName("coords") val coords: CoordinatesDto?,
    @SerializedName("link") val link: String?,
    @SerializedName("mentionIds") val mentionIds: List<String>,
    @SerializedName("mentionedMe") val mentionedMe: Boolean,
    @SerializedName("likeOwnerIds") val likeOwnerIds: List<String>,
    @SerializedName("likedByMe") val likedByMe: Boolean,
    @SerializedName("likeCount") val likeCount: Int,
    @SerializedName("attachment") val attachment: AttachmentDto?,
    @SerializedName("participants") val participants: List<UserDto>,
    @SerializedName("speakers") val speakers: List<UserDto>,
    @SerializedName("users") val users: Map<String, UserDto>
)