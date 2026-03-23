package ru.netology.nework.presentation.newjob

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.domain.repository.JobsRepository
import ru.netology.nework.presentation.newjobs.NewJobUiState
import javax.inject.Inject

@HiltViewModel
class NewJobViewModel @Inject constructor(
    private val jobsRepository: JobsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewJobUiState>(NewJobUiState.Ready)
    val uiState: StateFlow<NewJobUiState> = _uiState.asStateFlow()

    fun createJob(
        name: String,
        position: String?,
        start: Long,
        finish: Long?,
        link: String?
    ) {
        viewModelScope.launch {
            _uiState.value = NewJobUiState.Loading
            jobsRepository.createJob(name, position, start, finish, link)
                .onSuccess {
                    _uiState.value = NewJobUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = NewJobUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

