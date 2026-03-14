package ru.netology.nework.presentation.events

sealed class EventsUiState {
    object Idle : EventsUiState()
    object Loading : EventsUiState()
    data class Error(val message: String) : EventsUiState()
    data class Success(val message: String) : EventsUiState()
}