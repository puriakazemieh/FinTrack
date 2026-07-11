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
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.sync_msg_drive_unavailable
import fintrack.core.designsystem.generated.resources.sync_msg_failed
import fintrack.core.designsystem.generated.resources.sync_msg_no_provider
import fintrack.core.designsystem.generated.resources.sync_msg_restore_failed
import fintrack.core.designsystem.generated.resources.sync_msg_restored
import fintrack.core.designsystem.generated.resources.sync_msg_restoring
import fintrack.core.designsystem.generated.resources.sync_msg_success
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock

data class SyncState(
    val lastBackup: String = "—",
    val lastSyncTimestamp: Long = 0L,
    val stats: BackupStats? = null,
    val history: List<SyncHistory> = emptyList(),
    // Server sync is the real, in-repo path (Ktor client <-> server module). Google
    // Drive stays off until OAuth credentials are wired up, so we never report a
    // fake success for it.
    val isGoogleDriveEnabled: Boolean = false,
    // No sync server is configured in this build, so server sync stays off by default —
    // otherwise "backup now" would fail against the dev URL. The user can enable it once
    // a real server/URL is set.
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
                // Google Drive needs an OAuth sign-in that isn't wired up in this build, so we
                // refuse to enable it (and say so) rather than let it fail on the next sync.
                if (intent.enabled) {
                    viewModelScope.launch {
                        _effect.send(SyncEffect.ShowMessage(getString(Res.string.sync_msg_drive_unavailable)))
                    }
                } else {
                    _state.update { it.copy(isGoogleDriveEnabled = false) }
                }
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
                var ranAnyProvider = false

                if (_state.value.isGoogleDriveEnabled) {
                    googleDriveSyncManager.syncWithDrive()
                    ranAnyProvider = true
                }

                if (_state.value.isServerSyncEnabled) {
                    val (inserted, updated) = serverSyncManager.syncWithServer(_state.value.lastSyncTimestamp)
                    totalInserted += inserted
                    totalUpdated += updated
                    ranAnyProvider = true
                }

                if (!ranAnyProvider) {
                    // Nothing actually happened — don't record a fake successful backup.
                    _effect.send(SyncEffect.ShowMessage(getString(Res.string.sync_msg_no_provider)))
                    _state.update { it.copy(isLoading = false) }
                    return@launch
                }

                backupRepository.addSyncHistory(SyncHistory(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = SyncType.MANUAL,
                    status = SyncResultStatus.SUCCESS,
                    recordCount = totalInserted + totalUpdated,
                    insertedCount = totalInserted,
                    updatedCount = totalUpdated
                ))
                
                _effect.send(SyncEffect.ShowMessage(getString(Res.string.sync_msg_success)))
                loadData()
            } catch (e: Exception) {
                backupRepository.addSyncHistory(SyncHistory(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = SyncType.MANUAL,
                    status = SyncResultStatus.FAILED,
                    recordCount = 0,
                    errorMessage = e.message
                ))
                _effect.send(SyncEffect.ShowMessage(getString(Res.string.sync_msg_failed, e.message ?: "")))
                loadData()
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun restoreBackup(time: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            _effect.send(SyncEffect.ShowMessage(getString(Res.string.sync_msg_restoring)))
            try {
                val (inserted, updated) = serverSyncManager.restoreFromServer()
                _effect.send(SyncEffect.ShowMessage(getString(Res.string.sync_msg_restored, inserted.toString(), updated.toString())))
                loadData()
            } catch (e: Exception) {
                _effect.send(SyncEffect.ShowMessage(getString(Res.string.sync_msg_restore_failed, e.message ?: "")))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
