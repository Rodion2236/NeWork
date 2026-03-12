package ru.netology.nework.domain.model

data class User(
    val id: String,
    val login: String,
    val name: String,
    val avatar: String?
)