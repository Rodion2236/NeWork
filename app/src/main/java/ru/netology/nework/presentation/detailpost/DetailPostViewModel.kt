package ru.netology.nework.presentation.detailpost

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.domain.repository.PostsRepository
import ru.netology.nework.util.BundleKeys
import javax.inject.Inject

@HiltViewModel
class DetailPostViewModel @Inject constructor(
    private val postsRepository: PostsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = savedStateHandle[BundleKeys.POST_ID] ?: ""

    private val _uiState = MutableStateFlow<DetailPostUiState>(DetailPostUiState.Loading)
    val uiState: StateFlow<DetailPostUiState> = _uiState.asStateFlow()

    init {
        if (postId.isNotBlank()) {
            loadPost()
        }
    }

    private fun loadPost() {
        viewModelScope.launch {
            postsRepository.getPost(postId)
                .onSuccess { post ->
                    _uiState.value = DetailPostUiState.Success(post)
                }
                .onFailure {
                    _uiState.value = DetailPostUiState.Error("post_not_found")
                }
        }
    }

    fun toggleLike() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DetailPostUiState.Success) {
                val newLiked = !currentState.post.isLikedByMe
                postsRepository.likePost(currentState.post.id, newLiked)
                    .onSuccess {
                        val updatedPost = currentState.post.copy(
                            likedByMe = newLiked,
                            likeCount = if (newLiked) currentState.post.likeCount + 1
                            else currentState.post.likeCount - 1
                        )
                        _uiState.value = DetailPostUiState.Success(updatedPost)
                    }
                    .onFailure {
                        loadPost()
                    }
            }
        }
    }
}