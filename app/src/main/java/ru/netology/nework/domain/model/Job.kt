package ru.netology.nework.domain.model

data class Job(
    val id: String,
    val name: String,
    val position: String,
    val start: Long,
    val finish: Long?,
    val link: String?
)