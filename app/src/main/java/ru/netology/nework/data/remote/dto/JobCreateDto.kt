package ru.netology.nework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class JobCreateDto(
    @SerializedName("name") val name: String,
    @SerializedName("position") val position: String?,
    @SerializedName("start") val start: String,
    @SerializedName("finish") val finish: String?,
    @SerializedName("link") val link: String?
)