package ru.netology.nework.presentation.auth

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
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun validateLogin(login: String): String? = when {
        login.isBlank() -> "empty_login"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isBlank() -> "empty_password"
        else -> null
    }

    fun onLoginClicked(login: String, password: String) {
        val loginError = validateLogin(login)
        val passwordError = validatePassword(password)

        if (loginError != null || passwordError != null) {
            _state.value = LoginUiState.ValidationError(
                loginError = loginError,
                passwordError = passwordError
            )
            return
        }

        viewModelScope.launch {
            _state.value = LoginUiState.Loading

            authRepository.authenticate(login, password)
                .onSuccess {
                    _state.value = LoginUiState.Success
                }
                .onFailure { error ->
                    val messageRes = when (val appError = AppError.from(error)) {
                        is ApiError -> when (appError.status) {
                            400 -> R.string.incorrect_password
                            401 -> R.string.user_unregistered
                            else -> R.string.connection_error
                        }
                        is NetworkError -> R.string.connection_error
                        else -> R.string.unknown_error
                    }
                    _state.value = LoginUiState.Error(messageRes)
                }
        }
    }

    fun resetState() {
        _state.value = LoginUiState.Idle
    }
}