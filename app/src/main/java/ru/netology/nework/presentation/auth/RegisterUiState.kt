package ru.netology.nework.presentation.auth

import android.net.Uri
import androidx.annotation.StringRes

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()

    data class Error(@StringRes val messageRes: Int) : RegisterUiState()

    data class ValidationError(
        val loginError: String? = null,
        val nameError: String? = null,
        val passwordError: String? = null,
        val repeatPasswordError: String? = null,
        val avatarError: String? = null
    ) : RegisterUiState()

    data class AvatarSelected(val uri: Uri) : RegisterUiState()
}