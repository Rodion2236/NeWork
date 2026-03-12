package ru.netology.nework.presentation.auth

import androidx.annotation.StringRes

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()

    data class Error(@StringRes val messageRes: Int) : LoginUiState()

    data class ValidationError(
        val loginError: String? = null,
        val passwordError: String? = null
    ) : LoginUiState()
}