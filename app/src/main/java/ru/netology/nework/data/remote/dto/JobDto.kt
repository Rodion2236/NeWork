package ru.netology.nework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class JobDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("position") val position: String,
    @SerializedName("start") val start: Long,
    @SerializedName("finish") val finish: Long?,
    @SerializedName("link") val link: String?
)