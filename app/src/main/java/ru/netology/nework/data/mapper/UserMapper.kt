package ru.netology.nework.data.mapper

import ru.netology.nework.data.remote.dto.TokenResponse
import ru.netology.nework.domain.model.AuthResult
import ru.netology.nework.domain.model.User

fun AuthResult(dto: TokenResponse): AuthResult = AuthResult(
    token = dto.token,
    user = User(
        id = dto.id,
        login = "",
        name = "",
        avatar = dto.avatar
    )
)