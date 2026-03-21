package ru.netology.nework.presentation.newpost

import android.net.Uri

sealed class NewPostUiState {
    object Loading : NewPostUiState()
    object Ready : NewPostUiState()
    object Success : NewPostUiState()
    data class Error(val message: String) : NewPostUiState()
    data class ImageSelected(val uri: Uri) : NewPostUiState()
    object ImageRemoved : NewPostUiState()
    object LocationSelected : NewPostUiState()
    object LocationRemoved : NewPostUiState()
}