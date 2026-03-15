package ru.netology.nework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserPreviewDto(
    @SerializedName("name") val name: String,
    @SerializedName("avatar") val avatar: String?
)