package ru.netology.nework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PostDto(
    @SerializedName("id") val id: String,
    @SerializedName("authorId") val authorId: String,
    @SerializedName("author") val author: String,
    @SerializedName("authorJob") val authorJob: String?,
    @SerializedName("authorAvatar") val authorAvatar: String?,
    @SerializedName("content") val content: String,
    @SerializedName("published") val published: Long,
    @SerializedName("coords") val coords: CoordinatesDto?,
    @SerializedName("link") val link: String?,
    @SerializedName("mentionIds") val mentionIds: List<String>,
    @SerializedName("mentionedMe") val mentionedMe: Boolean,
    @SerializedName("likeOwnerIds") val likeOwnerIds: List<String>,
    @SerializedName("likedByMe") val likedByMe: Boolean,
    @SerializedName("attachment") val attachment: AttachmentDto?,
    @SerializedName("users") val users: Map<String, UserDto>
)

data class AttachmentDto(
    @SerializedName("url") val url: String,
    @SerializedName("type") val type: String
)

data class CoordinatesDto(
    @SerializedName("lat") val lat: Double,
    @SerializedName("long") val long: Double
)