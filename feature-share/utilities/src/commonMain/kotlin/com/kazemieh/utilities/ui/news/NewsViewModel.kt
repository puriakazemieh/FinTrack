package com.kazemieh.utilities.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.repository.UtilitiesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewsViewModel(
    private val utilitiesRepository: UtilitiesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewsState())
    val state = _state.asStateFlow()

    init {
        onIntent(NewsIntent.LoadNews)
    }

    fun onIntent(intent: NewsIntent) {
        when (intent) {
            NewsIntent.LoadNews -> loadNews()
            is NewsIntent.SelectArticle -> selectArticle(intent.id)
        }
    }

    private fun loadNews() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            utilitiesRepository.observeNews().collect { news ->
                _state.update { it.copy(news = news, isLoading = false) }
            }
        }
    }

    private fun selectArticle(id: String) {
        viewModelScope.launch {
            val article = utilitiesRepository.getNewsById(id)
            _state.update { it.copy(selectedArticle = article) }
        }
    }
}
