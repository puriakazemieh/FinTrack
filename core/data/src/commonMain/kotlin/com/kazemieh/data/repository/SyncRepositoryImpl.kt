package com.kazemieh.data.repository

import com.kazemieh.domain.repository.BackupRepository
import com.kazemieh.domain.repository.SyncRepository
import com.kazemieh.data_contract.datasource.SyncLocalDataSource
import com.kazemieh.network.SyncConfig
import com.kazemieh.network.service.SyncService

class SyncRepositoryImpl(
    private val syncLocalDataSource: SyncLocalDataSource,
    private val backupRepository: BackupRepository,
    private val syncService: SyncService
) : SyncRepository {

    override suspend fun uploadModifiedEntities() {
        // Push everything modified since the beginning; the server upserts by id.
        val delta = backupRepository.getDeltaBackupData(0L)
        if (delta.transactions.isNotEmpty() ||
            delta.categories.isNotEmpty() ||
            delta.sources.isNotEmpty()
        ) {
            syncService.uploadBackup(SyncConfig.userId, delta)
        }
    }

    override suspend fun downloadUpdates() {
        val remote = syncService.downloadBackup(SyncConfig.userId, since = null)
        backupRepository.restoreBackupData(remote)
    }

    override suspend fun syncAll() {
        uploadModifiedEntities()
        downloadUpdates()
    }
}
