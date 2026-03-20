package ru.netology.nework.presentation.detailevent

import ru.netology.nework.domain.model.Event

sealed class DetailEventUiState {
    object Loading : DetailEventUiState()
    data class Success(val event: Event) : DetailEventUiState()
    data class Error(val message: String) : DetailEventUiState()
}