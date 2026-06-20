package com.kazemieh.sync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.*
import com.kazemieh.domain.repository.BackupStats
import com.kazemieh.domain.repository.BackupRepository
import com.kazemieh.sync.GoogleDriveSyncManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class SyncState(
    val lastBackup: String = "—",
    val stats: BackupStats? = null,
    val history: List<SyncHistory> = emptyList(),
    val isGoogleDriveEnabled: Boolean = true,
    val isServerSyncEnabled: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface SyncEffect {
    data class ShowMessage(val message: String) : SyncEffect
}

sealed interface SyncIntent {
    data object BackupNow : SyncIntent
    data class ToggleGoogleDrive(val enabled: Boolean) : SyncIntent
    data class RestoreBackup(val time: String) : SyncIntent
}

class SyncViewModel(
    private val backupRepository: BackupRepository,
    private val googleDriveSyncManager: GoogleDriveSyncManager
) : ViewModel() {

    private val _state = MutableStateFlow(SyncState())
    val state = _state.asStateFlow()

    private val _effect = Channel<SyncEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val stats = backupRepository.getBackupStats()
            val history = backupRepository.getSyncHistory()
            val latestSuccess = history.firstOrNull { it.status == SyncResultStatus.SUCCESS }
            _state.update { it.copy(
                stats = stats, 
                history = history,
                lastBackup = latestSuccess?.timestamp?.toString() ?: "—" // TODO: Format date
            ) }
        }
    }

    fun onIntent(intent: SyncIntent) {
        when (intent) {
            SyncIntent.BackupNow -> backupNow()
            is SyncIntent.ToggleGoogleDrive -> {
                _state.update { it.copy(isGoogleDriveEnabled = intent.enabled) }
            }
            is SyncIntent.RestoreBackup -> restoreBackup(intent.time)
        }
    }

    private fun backupNow() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                googleDriveSyncManager.syncWithDrive()
                
                backupRepository.addSyncHistory(SyncHistory(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = SyncType.MANUAL,
                    status = SyncResultStatus.SUCCESS,
                    recordCount = 0 // TODO: Get merged count
                ))
                
                _effect.send(SyncEffect.ShowMessage("Backup and sync successful"))
                loadData()
            } catch (e: Exception) {
                backupRepository.addSyncHistory(SyncHistory(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = SyncType.MANUAL,
                    status = SyncResultStatus.FAILED,
                    recordCount = 0,
                    errorMessage = e.message
                ))
                _effect.send(SyncEffect.ShowMessage("Sync failed: ${e.message}"))
                loadData()
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun restoreBackup(time: String) {
        viewModelScope.launch {
            _effect.send(SyncEffect.ShowMessage("Restoring backup from $time..."))
        }
    }
}
