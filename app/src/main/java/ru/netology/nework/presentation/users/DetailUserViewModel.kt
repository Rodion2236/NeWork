package ru.netology.nework.presentation.users

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.domain.model.Job
import ru.netology.nework.domain.model.User
import ru.netology.nework.domain.repository.JobsRepository
import ru.netology.nework.domain.repository.UsersRepository
import javax.inject.Inject

@HiltViewModel
class DetailUserViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val jobsRepository: JobsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val userId: String = savedStateHandle["userId"] ?: ""

    private val _uiState = MutableStateFlow<DetailUserUiState>(DetailUserUiState.Loading)
    val uiState: StateFlow<DetailUserUiState> = _uiState.asStateFlow()

    init {
        if (userId.isNotBlank()) {
            loadUser()
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            usersRepository.getUser(userId)
                .onSuccess { user ->
                    _uiState.value = DetailUserUiState.Success(user)
                    loadJobs(userId)
                }
                .onFailure {
                    _uiState.value = DetailUserUiState.Error("user_not_found")
                }
        }
    }

    private fun loadJobs(userId: String) {
        viewModelScope.launch {
            val userIdInt = userId.toIntOrNull() ?: return@launch
            jobsRepository.getUserJobs(userIdInt)
                .onSuccess { jobs ->
                    val currentState = _uiState.value
                    if (currentState is DetailUserUiState.Success) {
                        _uiState.value = currentState.copy(jobs = jobs)
                    }
                }
                .onFailure {}
        }
    }
}

sealed class DetailUserUiState {
    object Loading : DetailUserUiState()
    data class Success(val user: User, val jobs: List<Job> = emptyList()) : DetailUserUiState()
    data class Error(val message: String) : DetailUserUiState()
}