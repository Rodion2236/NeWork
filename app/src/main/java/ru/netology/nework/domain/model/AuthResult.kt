package ru.netology.nework.domain.model

data class AuthResult(
    val token: String,
    val user: User
)