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
    private var selectedFileName: String? = null
    private var locationCoords: Pair<Double, Double>? = null
    private var mentionIds: List<String> = emptyList()

    private var editPostId: String? = null

    private var currentAttachmentUrl: String? = null
    private var currentAttachmentType: String? = null

    fun initEditMode(postId: String, attachmentUrl: String? = null, attachmentType: String? = null) {
        editPostId = postId
        currentAttachmentUrl = attachmentUrl
        currentAttachmentType = attachmentType
    }

    fun onImageSelected(uri: Uri) {
        selectedImage = uri
        selectedFile = null
        selectedFileName = null
        currentAttachmentUrl = null
        currentAttachmentType = null
        _uiState.value = NewPostUiState.ImageSelected(uri)
    }

    fun onImageRemoved() {
        selectedImage = null
        currentAttachmentUrl = null
        currentAttachmentType = null
        _uiState.value = NewPostUiState.ImageRemoved
    }

    fun onFileSelected(uri: Uri, fileName: String? = null) {
        selectedFile = uri
        selectedFileName = fileName ?: uri.lastPathSegment?.substringAfterLast('/') ?: "file"
        selectedImage = null
        currentAttachmentUrl = null
        currentAttachmentType = null
        _uiState.value = NewPostUiState.FileSelected(uri, selectedFileName!!)
    }

    fun onFileRemoved() {
        selectedFile = null
        selectedFileName = null
        currentAttachmentUrl = null
        currentAttachmentType = null
        _uiState.value = NewPostUiState.FileRemoved
    }

    fun onLocationSelected(lat: Double, long: Double) {
        locationCoords = Pair(lat, long)
        _uiState.value = NewPostUiState.LocationSelected
    }

    fun onLocationRemoved() {
        locationCoords = null
        _uiState.value = NewPostUiState.LocationRemoved
    }

    fun onMentionsSelected(ids: List<String>) { mentionIds = ids }

    private fun getAttachmentUri(): Pair<Uri?, Uri?> {
        val finalImageUri = when {
            selectedImage != null -> selectedImage
            editPostId != null && !currentAttachmentUrl.isNullOrBlank() && currentAttachmentType == "IMAGE" -> {
                Uri.parse(currentAttachmentUrl)
            }
            else -> null
        }

        val finalFileUri = when {
            selectedFile != null -> selectedFile
            editPostId != null && !currentAttachmentUrl.isNullOrBlank() && currentAttachmentType in listOf("VIDEO", "AUDIO") -> {
                Uri.parse(currentAttachmentUrl)
            }
            else -> null
        }

        return Pair(finalImageUri, finalFileUri)
    }

    fun createPost(content: String, userId: String? = null) {
        viewModelScope.launch {
            _uiState.value = NewPostUiState.Loading
            val (imageUri, fileUri) = getAttachmentUri()

            postsRepository.createPost(
                content = content,
                imageUri = imageUri,
                fileUri = fileUri,
                coords = locationCoords,
                mentionIds = mentionIds,
                userId = userId
            )
                .onSuccess { _uiState.value = NewPostUiState.Success }
                .onFailure { _uiState.value = NewPostUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun updatePost(content: String, userId: String? = null) {
        val postId = editPostId ?: return
        viewModelScope.launch {
            _uiState.value = NewPostUiState.Loading
            postsRepository.deletePost(postId)
                .onSuccess {
                    val (imageUri, fileUri) = getAttachmentUri()

                    postsRepository.createPost(
                        content = content,
                        imageUri = imageUri,
                        fileUri = fileUri,
                        coords = locationCoords,
                        mentionIds = mentionIds,
                        userId = userId
                    )
                        .onSuccess { _uiState.value = NewPostUiState.Success }
                        .onFailure { _uiState.value = NewPostUiState.Error(it.message ?: "Unknown error") }
                }
                .onFailure { _uiState.value = NewPostUiState.Error(it.message ?: "Unknown error") }
        }
    }
}