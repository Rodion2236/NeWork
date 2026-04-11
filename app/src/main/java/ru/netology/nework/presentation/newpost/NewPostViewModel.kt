package ru.netology.nework.presentation.newpost

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.domain.repository.PostsRepository
import javax.inject.Inject

@HiltViewModel
class NewPostViewModel @Inject constructor(
    private val postsRepository: PostsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewPostUiState>(NewPostUiState.Ready)
    val uiState: StateFlow<NewPostUiState> = _uiState.asStateFlow()

    private var selectedImage: Uri? = null
    private var selectedFile: Uri? = null
    private var locationCoords: Pair<Double, Double>? = null
    private var mentionIds: List<String> = emptyList()

    private var editPostId: String? = null

    fun initEditMode(postId: String) {
        editPostId = postId
    }

    fun onImageSelected(uri: Uri) {
        selectedImage = uri
        _uiState.value = NewPostUiState.ImageSelected(uri)
    }

    fun onImageRemoved() {
        selectedImage = null
        _uiState.value = NewPostUiState.ImageRemoved
    }

    fun onFileSelected(uri: Uri) {
        selectedFile = uri
    }

    fun onLocationSelected(lat: Double, long: Double) {
        locationCoords = Pair(lat, long)
        _uiState.value = NewPostUiState.LocationSelected
    }

    fun onLocationRemoved() {
        locationCoords = null
        _uiState.value = NewPostUiState.LocationRemoved
    }

    fun onMentionsSelected(ids: List<String>) {
        mentionIds = ids
    }

    fun createPost(content: String) {
        viewModelScope.launch {
            _uiState.value = NewPostUiState.Loading
            postsRepository.createPost(
                content = content,
                imageUri = selectedImage,
                fileUri = selectedFile,
                coords = locationCoords,
                mentionIds = mentionIds
            )
                .onSuccess { _uiState.value = NewPostUiState.Success }
                .onFailure { _uiState.value = NewPostUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun updatePost(content: String) {
        val postId = editPostId ?: return
        viewModelScope.launch {
            _uiState.value = NewPostUiState.Loading
            postsRepository.deletePost(postId)
                .onSuccess {
                    postsRepository.createPost(
                        content = content,
                        imageUri = selectedImage,
                        fileUri = selectedFile,
                        coords = locationCoords,
                        mentionIds = mentionIds
                    )
                        .onSuccess { _uiState.value = NewPostUiState.Success }
                        .onFailure { _uiState.value = NewPostUiState.Error(it.message ?: "Unknown error") }
                }
                .onFailure { _uiState.value = NewPostUiState.Error(it.message ?: "Unknown error") }
        }
    }
}