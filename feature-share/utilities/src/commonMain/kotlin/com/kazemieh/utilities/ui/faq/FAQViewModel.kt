package com.kazemieh.utilities.ui.faq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.repository.UtilitiesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FAQViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val utilitiesRepository: UtilitiesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FAQState())
    val state = _state.asStateFlow()

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("faq"))
        onIntent(FAQIntent.LoadFAQs)
    }

    fun onIntent(intent: FAQIntent) {
        when (intent) {
            FAQIntent.LoadFAQs -> loadFAQs()
            is FAQIntent.ToggleExpand -> toggleExpand(intent.id)
        }
    }

    private fun loadFAQs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            utilitiesRepository.observeFAQs().collect { faqs ->
                _state.update { it.copy(faqs = faqs, isLoading = false) }
            }
        }
    }

    private fun toggleExpand(id: String) {
        _state.update { 
            val newExpanded = it.expandedIds.toMutableSet()
            if (newExpanded.contains(id)) newExpanded.remove(id) else newExpanded.add(id)
            it.copy(expandedIds = newExpanded)
        }
    }
}
