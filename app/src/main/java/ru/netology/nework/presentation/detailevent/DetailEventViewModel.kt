package ru.netology.nework.presentation.detailevent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.domain.repository.EventsRepository
import ru.netology.nework.util.BundleKeys
import javax.inject.Inject

@HiltViewModel
class DetailEventViewModel @Inject constructor(
    private val eventsRepository: EventsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = savedStateHandle[BundleKeys.EVENT_ID] ?: ""

    private val _uiState = MutableStateFlow<DetailEventUiState>(DetailEventUiState.Loading)
    val uiState: StateFlow<DetailEventUiState> = _uiState.asStateFlow()

    init {
        if (eventId.isNotBlank()) {
            loadEvent()
        }
    }

    private fun loadEvent() {
        viewModelScope.launch {
            eventsRepository.getEvent(eventId)
                .onSuccess { event ->
                    _uiState.value = DetailEventUiState.Success(event)
                }
                .onFailure {
                    _uiState.value = DetailEventUiState.Error("event_not_found")
                }
        }
    }

    fun toggleLike() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DetailEventUiState.Success) {
                val newLiked = !currentState.event.likedByMe
                eventsRepository.likeEvent(currentState.event.id, newLiked)
                    .onSuccess {
                        val updatedEvent = currentState.event.copy(
                            likedByMe = newLiked,
                            likeOwnerIds = if (newLiked) currentState.event.likeOwnerIds + ""
                            else currentState.event.likeOwnerIds.dropLast(1)
                        )
                        _uiState.value = DetailEventUiState.Success(updatedEvent)
                    }
            }
        }
    }

    fun toggleParticipation() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DetailEventUiState.Success) {
                val newParticipated = !currentState.event.participatedByMe
                val result = if (newParticipated) {
                    eventsRepository.joinEvent(currentState.event.id)
                } else {
                    eventsRepository.leaveEvent(currentState.event.id)
                }
                result.onSuccess {
                    val updatedEvent = currentState.event.copy(
                        participatedByMe = newParticipated,
                        participantsIds = if (newParticipated) currentState.event.participantsIds + ""
                        else currentState.event.participantsIds.dropLast(1)
                    )
                    _uiState.value = DetailEventUiState.Success(updatedEvent)
                }
            }
        }
    }
}