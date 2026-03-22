package ru.netology.nework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EventCreateDto(
    @SerializedName("content") val content: String,
    @SerializedName("datetime") val datetime: Long,
    @SerializedName("type") val type: String,
    @SerializedName("coords") val coords: CoordsDto? = null,
    @SerializedName("link") val link: String? = null,
    @SerializedName("speakerIds") val speakerIds: List<Int>? = null,
    @SerializedName("attachment") val attachment: AttachmentDto? = null
)