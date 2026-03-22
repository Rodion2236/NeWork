package ru.netology.nework.presentation.newevent

import ru.netology.nework.domain.model.EventType

sealed class NewEventUiState {
    object Loading : NewEventUiState()
    object Ready : NewEventUiState()
    object Success : NewEventUiState()
    data class Error(val message: String) : NewEventUiState()
    data class DateSelected(val timestamp: Long) : NewEventUiState()
    data class TypeSelected(val type: EventType) : NewEventUiState()
    object LocationSelected : NewEventUiState()
    object LocationRemoved : NewEventUiState()
}