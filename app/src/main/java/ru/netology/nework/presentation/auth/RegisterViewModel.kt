package ru.netology.nework.presentation.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.domain.repository.AuthRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.util.ValidationError as FieldError
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun validateLogin(login: String): FieldError? = when {
        login.isBlank() -> FieldError.EmptyLogin
        login.length < 3 -> FieldError.LoginTooShort
        else -> null
    }

    fun validateName(name: String): FieldError? = when {
        name.isBlank() -> FieldError.EmptyName
        else -> null
    }

    fun validatePassword(password: String): FieldError? = when {
        password.isBlank() -> FieldError.EmptyPassword
        password.length < 6 -> FieldError.PasswordTooShort
        else -> null
    }

    fun validateRepeatPassword(password: String, repeat: String): FieldError? = when {
        repeat.isBlank() -> FieldError.EmptyPassword
        password != repeat -> FieldError.PasswordsDontMatch
        else -> null
    }

    fun validateAvatar(uri: Uri?): FieldError? = when {
        uri == null -> null
        else -> null
    }

    fun onAvatarSelected(uri: Uri) {
        _state.value = RegisterUiState.AvatarSelected(uri)
    }

    fun onRegisterClicked(
        login: String,
        name: String,
        password: String,
        repeatPassword: String,
        avatarUri: Uri?
    ) {
        val errors = RegisterUiState.ValidationError(
            loginError = validateLogin(login),
            nameError = validateName(name),
            passwordError = validatePassword(password),
            repeatPasswordError = validateRepeatPassword(password, repeatPassword),
            avatarError = validateAvatar(avatarUri)
        )

        if (errors.loginError != null || errors.nameError != null ||
            errors.passwordError != null || errors.repeatPasswordError != null) {
            _state.value = errors
            return
        }

        viewModelScope.launch {
            _state.value = RegisterUiState.Loading
            authRepository.register(login, password, name, avatarUri)
                .onSuccess {
                    _state.value = RegisterUiState.Success
                }
                .onFailure { error ->
                    val messageRes = when (val appError = AppError.from(error)) {
                        is ApiError -> when (appError.status) {
                            400 -> R.string.the_user_is_already_registered
                            401 -> R.string.user_unregistered
                            else -> R.string.connection_error
                        }
                        is NetworkError -> R.string.connection_error
                        else -> R.string.unknown_error
                    }
                    _state.value = RegisterUiState.Error(messageRes)
                }
        }
    }

    fun resetState() {
        _state.value = RegisterUiState.Idle
    }
}