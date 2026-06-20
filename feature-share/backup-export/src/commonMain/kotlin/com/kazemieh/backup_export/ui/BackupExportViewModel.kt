package com.kazemieh.backup_export.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.backup_export.BackupManager
import com.kazemieh.backup_export.PlatformExporter
import com.kazemieh.domain.repository.BackupRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BackupExportState(
    val selectedRange: String = "همه",
    val isLoading: Boolean = false,
    val transactionCount: Int = 0,
    val dateRange: Pair<String, String> = "" to ""
)

sealed interface BackupExportEffect {
    data class ShowMessage(val message: String) : BackupExportEffect
}

sealed interface BackupExportIntent {
    data class ChangeRange(val range: String) : BackupExportIntent
    data object ExportJson : BackupExportIntent
    data object ExportCsv : BackupExportIntent
    data object ExportExcel : BackupExportIntent
    data object ExportPdf : BackupExportIntent
}

class BackupExportViewModel(
    private val backupManager: BackupManager,
    private val backupRepository: BackupRepository,
    private val platformExporter: PlatformExporter
) : ViewModel() {

    private val _state = MutableStateFlow(BackupExportState())
    val state = _state.asStateFlow()

    private val _effect = Channel<BackupExportEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val stats = backupRepository.getBackupStats()
            _state.update { it.copy(transactionCount = stats.transactionCount) }
        }
    }

    fun onIntent(intent: BackupExportIntent) {
        when (intent) {
            is BackupExportIntent.ChangeRange -> {
                _state.update { it.copy(selectedRange = intent.range) }
            }
            BackupExportIntent.ExportJson -> exportJson()
            BackupExportIntent.ExportCsv -> exportCsv()
            BackupExportIntent.ExportExcel -> exportExcel()
            BackupExportIntent.ExportPdf -> exportPdf()
        }
    }

    private fun exportJson() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val json = backupManager.exportToJson()
            platformExporter.shareText(json, "FinTrack Backup JSON")
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun exportCsv() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val csv = backupManager.exportToCsv()
            platformExporter.shareText(csv, "FinTrack Export CSV")
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun exportExcel() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val timestamps = getTimestampsFromRange(_state.value.selectedRange)
            val transactions = backupRepository.getBackupData(timestamps.first, timestamps.second).transactions
            val path = platformExporter.exportToExcel(transactions)
            if (path != null) {
                platformExporter.shareFile(path)
            } else {
                _effect.send(BackupExportEffect.ShowMessage("Excel export failed"))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun exportPdf() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val timestamps = getTimestampsFromRange(_state.value.selectedRange)
            val transactions = backupRepository.getBackupData(timestamps.first, timestamps.second).transactions
            val path = platformExporter.exportToPdf(transactions)
            if (path != null) {
                platformExporter.shareFile(path)
            } else {
                _effect.send(BackupExportEffect.ShowMessage("PDF export failed"))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun getTimestampsFromRange(range: String): Pair<Long?, Long?> {
        // Basic implementation for range logic
        return null to null // TODO: Implement real range calculation
    }
}
