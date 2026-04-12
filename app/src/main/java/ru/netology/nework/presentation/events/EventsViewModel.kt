package ru.netology.nework.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _events = MutableStateFlow<PagingData<Event>>(PagingData.empty())
    val events: StateFlow<PagingData<Event>> = _events.asStateFlow()

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Idle)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            eventsRepository.getEvents()
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _events.value = pagingData
                }
        }
    }

    fun toggleLike(eventId: String, currentLiked: Boolean) {
        viewModelScope.launch {
            eventsRepository.likeEvent(eventId, !currentLiked)
                .onFailure { _ ->
                    _uiState.value = EventsUiState.Error("like_failed")
                }
        }
    }

    fun toggleParticipation(eventId: String) {
        viewModelScope.launch {
            eventsRepository.joinEvent(eventId)
                .onSuccess {
                    loadEvents()
                    _uiState.value = EventsUiState.Success("participation_updated")
                }
                .onFailure {
                    _uiState.value = EventsUiState.Error("join_failed")
                }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            eventsRepository.deleteEvent(eventId)
                .onSuccess {
                    loadEvents()
                    _uiState.value = EventsUiState.Success("event_deleted")
                }
                .onFailure { _ ->
                    _uiState.value = EventsUiState.Error("delete_failed")
                }
        }
    }

    fun refresh() {
        loadEvents()
    }
}