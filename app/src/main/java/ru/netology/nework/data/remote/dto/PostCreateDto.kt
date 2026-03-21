package ru.netology.nework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PostCreateDto(
    @SerializedName("content") val content: String,
    @SerializedName("link") val link: String? = null,
    @SerializedName("coords") val coords: CoordsDto? = null,
    @SerializedName("mentionIds") val mentionIds: List<Int>? = null,
    @SerializedName("attachment") val attachment: AttachmentDto? = null
)

data class CoordsDto(
    @SerializedName("lat") val lat: Double,
    @SerializedName("long") val long: Double
)