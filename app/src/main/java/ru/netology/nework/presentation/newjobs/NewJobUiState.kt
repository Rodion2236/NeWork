package ru.netology.nework.presentation.newjobs

sealed class NewJobUiState {
    object Loading : NewJobUiState()
    object Ready : NewJobUiState()
    object Success : NewJobUiState()
    data class Error(val message: String) : NewJobUiState()
}