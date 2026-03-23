package ru.netology.nework.presentation.jobs

sealed class JobsUiState {
    object Loading : JobsUiState()
    object Success : JobsUiState()
    object Empty : JobsUiState()
    data class Error(val message: String) : JobsUiState()
}