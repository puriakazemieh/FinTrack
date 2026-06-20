package com.kazemieh.sync

import com.kazemieh.common.model.*
import com.kazemieh.domain.repository.BackupData
import com.kazemieh.domain.repository.BackupRepository
import com.kazemieh.network.service.SyncService

class ServerSyncManager(
    private val backupRepository: BackupRepository,
    private val syncService: SyncService
) {
    private val USER_ID = "user_123"

    suspend fun syncWithServer(lastSyncTimestamp: Long): Pair<Int, Int> {
        // 1. Send local changes (Delta) to server
        val localChanges = backupRepository.getDeltaBackupData(lastSyncTimestamp)
        if (localChanges.transactions.isNotEmpty() || localChanges.categories.isNotEmpty()) {
            syncService.uploadBackup(USER_ID, localChanges)
        }
        
        // 2. Download updates from server since last sync
        val remoteUpdates = try {
            syncService.downloadBackup(USER_ID, lastSyncTimestamp)
        } catch (e: Exception) {
            null
        }

        // 3. Apply updates to local database
        return if (remoteUpdates != null) {
            backupRepository.restoreBackupData(remoteUpdates)
        } else {
            0 to 0
        }
    }
}
