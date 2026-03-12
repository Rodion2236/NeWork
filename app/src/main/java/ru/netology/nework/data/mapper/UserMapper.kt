package ru.netology.nework.data.mapper

import ru.netology.nework.data.remote.dto.TokenResponse
import ru.netology.nework.data.remote.dto.UserDto
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

fun User(dto: UserDto): User {
    return User(
        id = dto.id,
        login = dto.login,
        name = dto.name,
        avatar = dto.avatar
    )
}