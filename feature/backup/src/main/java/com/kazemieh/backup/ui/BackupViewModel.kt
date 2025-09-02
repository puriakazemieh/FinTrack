package com.kazemieh.backup.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.ExportTransactionsUseCase
import com.kazemieh.domain.usecase.ImportTransactionsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class BackupViewModel(
    private val exportTransactions: ExportTransactionsUseCase,
    private val importTransactions: ImportTransactionsUseCase
) : ViewModel() {

    private val _effect = MutableSharedFlow<BackupEffect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: BackupEvent) {
        when (event) {
            is BackupEvent.Export -> export()
            is BackupEvent.Import -> import(event.json)
        }
    }

    private fun export() {
        viewModelScope.launch {
            try {
                val json = exportTransactions()
                _effect.emit(BackupEffect.ShareJson(json))
            } catch (e: Exception) {
                Log.d("949494", "export: ${e.message}")
                _effect.emit(BackupEffect.ShowMessage("Export failed: ${e.message}"))
            }
        }
    }

    private fun import(json: String) {
        viewModelScope.launch {
            try {
                importTransactions(json)
                _effect.emit(BackupEffect.ShowMessage("Import successful!"))
            } catch (e: Exception) {
                _effect.emit(BackupEffect.ShowMessage("Import failed: ${e.message}"))
            }
        }
    }
}

data class BackupUiState(
    val isLoading: Boolean = false,
    val jsonString: String? = null,
    val error: String? = null
)
sealed class BackupEvent {
    object Export : BackupEvent()
    data class Import(val json: String) : BackupEvent()
}

sealed class BackupEffect {
    data class ShowMessage(val message: String) : BackupEffect()
    data class ShareJson(val json: String) : BackupEffect()
}
