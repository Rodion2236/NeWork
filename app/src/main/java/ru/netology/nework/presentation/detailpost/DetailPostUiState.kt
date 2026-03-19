package ru.netology.nework.presentation.detailpost

import ru.netology.nework.domain.model.Post

sealed class DetailPostUiState {
    object Loading : DetailPostUiState()
    data class Success(val post: Post) : DetailPostUiState()
    data class Error(val message: String) : DetailPostUiState()
}