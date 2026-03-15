package ru.netology.nework.presentation.users.wall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.domain.model.Post
import ru.netology.nework.domain.repository.UsersRepository
import javax.inject.Inject

@HiltViewModel
class UserWallViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = savedStateHandle["userId"] ?: ""

    val posts: Flow<PagingData<Post>> = usersRepository.getUserWall(userId)
        .cachedIn(viewModelScope)

    fun toggleLike(postId: String, liked: Boolean) {
        // TODO: Вызов репозитория для лайка
    }
}