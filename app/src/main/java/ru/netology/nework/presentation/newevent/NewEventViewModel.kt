package ru.netology.nework.presentation.newevent

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.domain.model.EventType
import ru.netology.nework.domain.repository.EventsRepository
import javax.inject.Inject

@HiltViewModel
class NewEventViewModel @Inject constructor(
    private val eventsRepository: EventsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewEventUiState>(NewEventUiState.Ready)
    val uiState: StateFlow<NewEventUiState> = _uiState.asStateFlow()

    private var selectedImage: Uri? = null
    private var selectedFile: Uri? = null
    private var selectedFileName: String? = null

    private var selectedType: EventType = EventType.ONLINE
    private var selectedDate: Long? = null
    private var locationCoords: Pair<Double, Double>? = null
    private var speakerIds: List<String> = emptyList()

    private var editEventId: String? = null
    private var eventLink: String? = null


    fun onLinkEntered(link: String) {
        eventLink = link.takeIf { it.isNotBlank() }
    }

    fun initEditMode(eventId: String) {
        editEventId = eventId
    }

    fun onImageSelected(uri: Uri) {
        selectedImage = uri
        selectedFile = null
        selectedFileName = null
        _uiState.value = NewEventUiState.ImageSelected(uri)
    }

    fun onImageRemoved() {
        selectedImage = null
        _uiState.value = NewEventUiState.ImageRemoved
    }

    fun onFileSelected(uri: Uri, fileName: String? = null) {
        selectedFile = uri
        selectedFileName = fileName ?: uri.lastPathSegment?.substringAfterLast('/') ?: "file"
        selectedImage = null
        _uiState.value = NewEventUiState.FileSelected(uri, selectedFileName!!)
    }

    fun onFileRemoved() {
        selectedFile = null
        selectedFileName = null
        _uiState.value = NewEventUiState.FileRemoved
    }

    fun onTypeSelected(type: EventType) {
        selectedType = type
        _uiState.value = NewEventUiState.TypeSelected(type)
    }

    fun onDateSelected(timestamp: Long) {
        selectedDate = timestamp
        _uiState.value = NewEventUiState.DateSelected(timestamp)
    }

    fun onLocationSelected(lat: Double, long: Double) {
        locationCoords = Pair(lat, long)
        _uiState.value = NewEventUiState.LocationSelected
    }

    fun onLocationRemoved() {
        locationCoords = null
        _uiState.value = NewEventUiState.LocationRemoved
    }

    fun onSpeakersSelected(ids: List<String>) {
        speakerIds = ids
    }

    fun createEvent(content: String) {
        viewModelScope.launch {
            _uiState.value = NewEventUiState.Loading
            eventsRepository.createEvent(
                content = content,
                type = selectedType,
                datetime = selectedDate,
                coords = locationCoords,
                speakerIds = speakerIds,
                link = eventLink,
                imageUri = selectedImage,
                fileUri = selectedFile
            )
                .onSuccess { _uiState.value = NewEventUiState.Success }
                .onFailure { _uiState.value = NewEventUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun updateEvent(content: String) {
        val eventId = editEventId ?: return
        viewModelScope.launch {
            _uiState.value = NewEventUiState.Loading
            eventsRepository.deleteEvent(eventId)
                .onSuccess {
                    eventsRepository.createEvent(
                        content = content,
                        type = selectedType,
                        datetime = selectedDate,
                        coords = locationCoords,
                        speakerIds = speakerIds,
                        link = eventLink,
                        imageUri = selectedImage,
                        fileUri = selectedFile
                    )
                        .onSuccess { _uiState.value = NewEventUiState.Success }
                        .onFailure { _uiState.value = NewEventUiState.Error(it.message ?: "Unknown error") }
                }
                .onFailure { _uiState.value = NewEventUiState.Error(it.message ?: "Unknown error") }
        }
    }
}