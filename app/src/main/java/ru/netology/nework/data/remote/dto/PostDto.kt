package ru.netology.nework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PostDto(
    @SerializedName("id") val id: Int,
    @SerializedName("authorId") val authorId: Int,
    @SerializedName("author") val author: String,
    @SerializedName("authorJob") val authorJob: String?,
    @SerializedName("authorAvatar") val authorAvatar: String?,
    @SerializedName("content") val content: String,
    @SerializedName("published") val published: String,
    @SerializedName("coords") val coords: CoordinatesDto?,
    @SerializedName("link") val link: String?,
    @SerializedName("mentionIds") val mentionIds: List<Int>,
    @SerializedName("mentionedMe") val mentionedMe: Boolean,
    @SerializedName("likeOwnerIds") val likeOwnerIds: List<Int>,
    @SerializedName("likedByMe") val likedByMe: Boolean,
    @SerializedName("attachment") val attachment: AttachmentDto?,
    @SerializedName("users") val users: Map<String, UserPreviewDto>
)

data class AttachmentDto(
    @SerializedName("url") val url: String,
    @SerializedName("type") val type: String
)

data class CoordinatesDto(
    @SerializedName("lat") val lat: Double,
    @SerializedName("long") val long: Double
)