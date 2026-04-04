package ru.netology.nework.presentation.auth

import androidx.annotation.StringRes
import ru.netology.nework.util.ValidationError as FieldError

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()

    data class Error(@StringRes val messageRes: Int) : LoginUiState()

    data class ValidationError(
        val loginError: FieldError? = null,
        val passwordError: FieldError? = null
    ) : LoginUiState()
}