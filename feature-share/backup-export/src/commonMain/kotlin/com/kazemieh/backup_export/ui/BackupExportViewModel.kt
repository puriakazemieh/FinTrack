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
import kotlin.time.Clock

data class BackupExportState(
    val selectedRange: DateRange = DateRange.ALL,
    val isLoading: Boolean = false,
    val transactionCount: Int = 0,
    val dateRange: Pair<String, String> = "" to ""
)

sealed interface BackupExportEffect {
    data class ShowMessage(val message: String) : BackupExportEffect
}

sealed interface BackupExportIntent {
    data class ChangeRange(val range: DateRange) : BackupExportIntent
    data object ExportJson : BackupExportIntent
    data object ExportCsv : BackupExportIntent
    data object ExportExcel : BackupExportIntent
    data object ExportPdf : BackupExportIntent
    data class ImportJson(val content: String) : BackupExportIntent
}

class BackupExportViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
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
            is BackupExportIntent.ImportJson -> importJson(intent.content)
        }
    }

    private fun importJson(content: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                backupManager.importFromJson(content)
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("backup_restored"))
                _effect.send(BackupExportEffect.ShowMessage("Restore successful"))
                loadStats()
            } catch (e: Exception) {
                _effect.send(BackupExportEffect.ShowMessage("Import failed: ${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun exportJson() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val json = backupManager.exportToJson()
            platformExporter.shareText(json, "FinTrack Backup JSON")
            analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("backup_exported_local"))
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun exportCsv() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val csv = backupManager.exportToCsv()
            platformExporter.shareText(csv, "FinTrack Export CSV")
            analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("backup_exported_local"))
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
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("backup_exported_local"))
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
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("backup_exported_local"))
            } else {
                _effect.send(BackupExportEffect.ShowMessage("PDF export failed"))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun getTimestampsFromRange(range: DateRange): Pair<Long?, Long?> {
        val now = Clock.System.now().toEpochMilliseconds()
        val oneDay = 24 * 60 * 60 * 1000L
        return when (range) {
            DateRange.THIS_MONTH -> (now - 30 * oneDay) to now
            DateRange.THREE_MONTHS -> (now - 90 * oneDay) to now
            DateRange.SIX_MONTHS -> (now - 180 * oneDay) to now
            DateRange.THIS_YEAR -> (now - 365 * oneDay) to now
            DateRange.ALL -> null to null
        }
    }
}
