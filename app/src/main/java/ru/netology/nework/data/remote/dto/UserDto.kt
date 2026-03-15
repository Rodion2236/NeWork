package ru.netology.nework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("login")
    val login: String = "",
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar")
    val avatar: String?
)