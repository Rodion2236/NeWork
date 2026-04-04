package ru.netology.nework.presentation.auth

import android.net.Uri
import androidx.annotation.StringRes
import ru.netology.nework.util.ValidationError as FieldError

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()

    data class Error(@StringRes val messageRes: Int) : RegisterUiState()

    data class ValidationError(
        val loginError: FieldError? = null,
        val nameError: FieldError? = null,
        val passwordError: FieldError? = null,
        val repeatPasswordError: FieldError? = null,
        val avatarError: FieldError? = null
    ) : RegisterUiState()

    data class AvatarSelected(val uri: Uri) : RegisterUiState()
}