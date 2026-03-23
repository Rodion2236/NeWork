package ru.netology.nework.presentation.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.domain.model.Job
import ru.netology.nework.domain.repository.JobsRepository
import javax.inject.Inject

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val jobsRepository: JobsRepository
) : ViewModel() {

    private var userId: String = ""
    private var currentUserId: String = ""

    private val _uiState = MutableStateFlow<JobsUiState>(JobsUiState.Loading)
    val uiState: StateFlow<JobsUiState> = _uiState.asStateFlow()

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    val isOwnProfile: Boolean
        get() = userId == currentUserId

    fun setProfileIds(userId: String, currentUserId: String) {
        this.userId = userId
        this.currentUserId = currentUserId
        if (userId.isNotEmpty()) {
            loadJobs()
        }
    }


    fun loadJobs() {
        viewModelScope.launch {
            _uiState.value = JobsUiState.Loading

            val result = if (isOwnProfile) {
                jobsRepository.getMyJobs()
            } else {
                jobsRepository.getUserJobs(userId.toIntOrNull() ?: 0)
            }

            result
                .onSuccess { jobList ->
                    _jobs.value = jobList
                    _uiState.value = if (jobList.isEmpty()) {
                        JobsUiState.Empty
                    } else {
                        JobsUiState.Success
                    }
                }
                .onFailure { error ->
                    _uiState.value = JobsUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun deleteJob(jobId: Int) {
        viewModelScope.launch {
            jobsRepository.deleteJob(jobId)
                .onSuccess { loadJobs() }
                .onFailure { error ->
                    _uiState.value = JobsUiState.Error(error.message ?: "Delete failed")
                }
        }
    }
}