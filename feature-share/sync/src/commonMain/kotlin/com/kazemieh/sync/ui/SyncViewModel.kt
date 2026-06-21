package com.kazemieh.sync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.*
import com.kazemieh.common.util.DateUtils
import com.kazemieh.domain.repository.BackupStats
import com.kazemieh.domain.repository.BackupRepository
import com.kazemieh.sync.GoogleDriveSyncManager
import com.kazemieh.sync.ServerSyncManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class SyncState(
    val lastBackup: String = "—",
    val lastSyncTimestamp: Long = 0L,
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
    data class RestoreBackup(val timestamp: Long) : SyncIntent
}

class SyncViewModel(
    private val backupRepository: BackupRepository,
    private val googleDriveSyncManager: GoogleDriveSyncManager,
    private val serverSyncManager: ServerSyncManager
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
            val now = Clock.System.now().toEpochMilliseconds()
            
            _state.update { it.copy(
                stats = stats, 
                history = history,
                lastSyncTimestamp = latestSuccess?.timestamp ?: 0L,
                lastBackup = latestSuccess?.let { DateUtils.getRelativeTime(it.timestamp, now) } ?: "—"
            ) }
        }
    }

    fun onIntent(intent: SyncIntent) {
        when (intent) {
            SyncIntent.BackupNow -> backupNow()
            is SyncIntent.ToggleGoogleDrive -> {
                _state.update { it.copy(isGoogleDriveEnabled = intent.enabled) }
            }
            is SyncIntent.RestoreBackup -> restoreBackup(intent.timestamp)
        }
    }

    private fun backupNow() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                var totalInserted = 0
                var totalUpdated = 0

                if (_state.value.isGoogleDriveEnabled) {
                    googleDriveSyncManager.syncWithDrive()
                }
                
                if (_state.value.isServerSyncEnabled) {
                    val (inserted, updated) = serverSyncManager.syncWithServer(_state.value.lastSyncTimestamp)
                    totalInserted += inserted
                    totalUpdated += updated
                }
                
                backupRepository.addSyncHistory(SyncHistory(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = SyncType.MANUAL,
                    status = SyncResultStatus.SUCCESS,
                    recordCount = totalInserted + totalUpdated,
                    insertedCount = totalInserted,
                    updatedCount = totalUpdated
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

    private fun restoreBackup(time: Long) {
        viewModelScope.launch {
            _effect.send(SyncEffect.ShowMessage("Restoring backup..."))
        }
    }
}
