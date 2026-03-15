package ru.netology.nework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EventDto(
    @SerializedName("id") val id: Int,
    @SerializedName("authorId") val authorId: Int,
    @SerializedName("author") val author: String,
    @SerializedName("authorJob") val authorJob: String?,
    @SerializedName("authorAvatar") val authorAvatar: String?,
    @SerializedName("content") val content: String,
    @SerializedName("published") val published: String,
    @SerializedName("datetime") val datetime: String,
    @SerializedName("type") val type: String,
    @SerializedName("coords") val coords: CoordinatesDto?,
    @SerializedName("link") val link: String?,
    @SerializedName("likeOwnerIds") val likeOwnerIds: List<Int>,
    @SerializedName("likedByMe") val likedByMe: Boolean,
    @SerializedName("speakerIds") val speakerIds: List<Int>,
    @SerializedName("participantsIds") val participantsIds: List<Int>,
    @SerializedName("participatedByMe") val participatedByMe: Boolean,
    @SerializedName("attachment") val attachment: AttachmentDto?,
    @SerializedName("users") val users: Map<String, UserPreviewDto>
)