package ru.netology.nework.presentation.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nework.domain.model.User
import ru.netology.nework.domain.repository.UsersRepository
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val usersRepository: UsersRepository
) : ViewModel() {

    val users: Flow<PagingData<User>> = usersRepository.getUsers()
        .cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow<UsersUiState>(UsersUiState.Idle)
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    fun refresh() {}
}