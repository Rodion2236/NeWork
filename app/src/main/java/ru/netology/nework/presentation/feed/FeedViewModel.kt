package ru.netology.nework.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.domain.model.Post
import ru.netology.nework.domain.repository.PostsRepository
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postsRepository: PostsRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<PagingData<Post>>(PagingData.empty())
    val posts: StateFlow<PagingData<Post>> = _posts.asStateFlow()

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Idle)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            postsRepository.getPosts()
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _posts.value = pagingData
                }
        }
    }

    fun toggleLike(postId: String, liked: Boolean) {
        viewModelScope.launch {
            val updatedPagingData = _posts.value.map { post ->
                if (post.id == postId) {
                    post.copy(
                        likedByMe = !liked,
                        likeCount = if (!liked) post.likeCount + 1 else post.likeCount - 1
                    )
                } else post
            }
            _posts.value = updatedPagingData

            postsRepository.likePost(postId, !liked)
                .onSuccess {
                    loadPosts()
                }
                .onFailure {
                    loadPosts()
                }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postsRepository.deletePost(postId)
                .onSuccess {
                    loadPosts()
                    _uiState.value = FeedUiState.Success("post_deleted")
                }
                .onFailure { _ ->
                    _uiState.value = FeedUiState.Error("delete_failed")
                }
        }
    }

    fun refresh() {
        loadPosts()
    }
}

sealed class FeedUiState {
    object Idle : FeedUiState()
    object Loading : FeedUiState()
    data class Error(val message: String) : FeedUiState()
    data class Success(val message: String) : FeedUiState()
}