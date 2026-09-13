package com.kazemieh.tag.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Note
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.usecase.ObserveTagsUseCase
import com.kazemieh.domain.usecase.ObserveTransactionsUseCase
import com.kazemieh.domain.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TagDetailState(
    val tag: Tag? = null,
    val transactions: List<TransactionWithRelations> = emptyList(),
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false
)

class TagDetailViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val tagId: Long,
    private val observeTagsUseCase: ObserveTagsUseCase,
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TagDetailState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("tag_detail"))
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            observeTagsUseCase().collect { tags ->
                val tag = tags.find { it.id == tagId }
                _state.update { it.copy(tag = tag) }
            }
        }

        viewModelScope.launch {
            observeTransactionsUseCase(
                TransactionFilterParams(tags = setOf(Tag(id = tagId, name = "", colorId = 1, iconId = 1)), isAllTags = false),
                PageRequest(limit = Int.MAX_VALUE, offset = 0)
            ).collect { page ->
                _state.update { it.copy(transactions = page.items, isLoading = false) }
            }
        }

        viewModelScope.launch {
            noteRepository.observeNotes().collect { allNotes ->
                val related = allNotes.filter { note -> note.tags.any { it.id == tagId } }
                _state.update { it.copy(notes = related) }
            }
        }
    }
}

