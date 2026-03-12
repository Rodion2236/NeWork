package ru.netology.nework.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.domain.model.Post
import ru.netology.nework.domain.repository.PostsRepository
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(private val postsRepository: PostsRepository) : ViewModel() {
    val posts: Flow<PagingData<Post>> = postsRepository.getFeed()
        .cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Idle)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    fun toggleLike(postId: String, currentLiked: Boolean) {
        viewModelScope.launch {
            postsRepository.likePost(postId, !currentLiked)
                .onFailure { error ->
                    _uiState.value = FeedUiState.Error("like_failed")
                }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postsRepository.deletePost(postId)
                .onSuccess {
                    _uiState.value = FeedUiState.Success("post_deleted")
                }
                .onFailure { error ->
                    _uiState.value = FeedUiState.Error("delete_failed")
                }
        }
    }

    fun refresh() {}
}

sealed class FeedUiState {
    object Idle : FeedUiState()
    object Loading : FeedUiState()
    data class Error(val message: String) : FeedUiState()
    data class Success(val message: String) : FeedUiState()
}