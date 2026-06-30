package com.kazemieh.utilities.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.repository.UtilitiesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventsViewModel(
    private val utilitiesRepository: UtilitiesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EventsState())
    val state = _state.asStateFlow()

    init {
        onIntent(EventsIntent.LoadEvents)
    }

    fun onIntent(intent: EventsIntent) {
        when (intent) {
            EventsIntent.LoadEvents -> loadEvents()
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            utilitiesRepository.observeEvents().collect { events ->
                _state.update { it.copy(events = events, isLoading = false) }
            }
        }
    }
}
