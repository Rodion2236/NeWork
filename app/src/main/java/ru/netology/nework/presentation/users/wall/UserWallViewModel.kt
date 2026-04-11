package ru.netology.nework.presentation.users.wall

import androidx.lifecycle.SavedStateHandle
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
import ru.netology.nework.domain.repository.UsersRepository
import javax.inject.Inject

@HiltViewModel
class UserWallViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val postsRepository: PostsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = savedStateHandle["userId"] ?: ""

    private val _posts = MutableStateFlow<PagingData<Post>>(PagingData.empty())
    val posts: StateFlow<PagingData<Post>> = _posts.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            usersRepository.getUserWall(userId)
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _posts.value = pagingData
                }
        }
    }

    fun refresh() {
        loadPosts()
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
                    refresh()
                }
        }
    }
}