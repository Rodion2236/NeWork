package ru.netology.nework.presentation.users.jobs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.domain.model.Job
import ru.netology.nework.domain.repository.UsersRepository
import ru.netology.nework.util.BundleKeys
import javax.inject.Inject

@HiltViewModel
class UserJobsViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = savedStateHandle[BundleKeys.USER_ID] ?: ""

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    init {
        loadJobs()
    }

    private fun loadJobs() {
        viewModelScope.launch {
            usersRepository.getUserJobs(userId)
                .onSuccess { jobs ->
                    _jobs.value = jobs
                }
                .onFailure {}
        }
    }
}