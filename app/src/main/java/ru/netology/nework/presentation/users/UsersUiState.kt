package ru.netology.nework.presentation.users

sealed class UsersUiState {
    object Idle : UsersUiState()
    object Loading : UsersUiState()
    data class Error(val message: String) : UsersUiState()
    data class Success(val message: String) : UsersUiState()
}