package ru.netology.nework.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthenticationRequest(
    val login: String,
    val password: String
)

data class RegistrationRequest(
    val login: String,
    val password: String,
    val name: String
)

data class TokenResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("avatar")
    val avatar: String?
)