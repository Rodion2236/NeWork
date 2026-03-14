package ru.netology.nework.presentation.events

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
import ru.netology.nework.domain.model.Event
import ru.netology.nework.domain.repository.EventsRepository
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventsRepository: EventsRepository
) : ViewModel() {

    val events: Flow<PagingData<Event>> = eventsRepository.getEvents()
        .cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Idle)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    fun toggleLike(eventId: String, currentLiked: Boolean) {
        viewModelScope.launch {
            eventsRepository.likeEvent(eventId, !currentLiked)
                .onFailure { error ->
                    _uiState.value = EventsUiState.Error("like_failed")
                }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            eventsRepository.deleteEvent(eventId)
                .onSuccess {
                    _uiState.value = EventsUiState.Success("event_deleted")
                }
                .onFailure { error ->
                    _uiState.value = EventsUiState.Error("delete_failed")
                }
        }
    }

    fun createEvent(event: Event) {
        viewModelScope.launch {
            eventsRepository.createEvent(event)
                .onSuccess {
                    _uiState.value = EventsUiState.Success("event_created")
                }
                .onFailure { error ->
                    _uiState.value = EventsUiState.Error("create_failed")
                }
        }
    }

    fun updateEvent(eventId: String, event: Event) {
        viewModelScope.launch {
            eventsRepository.updateEvent(eventId, event)
                .onSuccess {
                    _uiState.value = EventsUiState.Success("event_updated")
                }
                .onFailure { error ->
                    _uiState.value = EventsUiState.Error("update_failed")
                }
        }
    }

    fun refresh() {}
}