package ru.netology.nework.presentation.newevent

import android.net.Uri
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
    data class ImageSelected(val uri: Uri) : NewEventUiState()
    object ImageRemoved : NewEventUiState()
    data class FileSelected(val uri: Uri, val fileName: String) : NewEventUiState()
    object FileRemoved : NewEventUiState()
}