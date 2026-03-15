package ru.netology.nework.data.mapper

import ru.netology.nework.data.remote.dto.UserPreviewDto
import ru.netology.nework.domain.model.UserPreview

fun UserPreview(dto: UserPreviewDto): UserPreview = UserPreview(
    name = dto.name,
    avatar = dto.avatar
)

fun UserPreviewDto(preview: UserPreview): UserPreviewDto = UserPreviewDto(
    name = preview.name,
    avatar = preview.avatar
)