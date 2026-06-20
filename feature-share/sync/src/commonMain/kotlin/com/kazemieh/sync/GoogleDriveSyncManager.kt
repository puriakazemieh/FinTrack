package com.kazemieh.sync

import com.kazemieh.common.model.*
import com.kazemieh.domain.repository.BackupData
import com.kazemieh.domain.repository.BackupRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GoogleDriveSyncManager(
    private val backupRepository: BackupRepository
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val DRIVE_FILE_NAME = "fintrack_sync_data.json"

    suspend fun syncWithDrive() {
        val localData = backupRepository.getBackupData()
        val remoteJson = fetchFromDrive(DRIVE_FILE_NAME)
        val remoteData = if (remoteJson != null) json.decodeFromString<BackupData>(remoteJson) else null

        if (remoteData == null) {
            uploadToDrive(DRIVE_FILE_NAME, json.encodeToString(localData))
            return
        }

        val mergedData = BackupData(
            transactions = mergeEntities(localData.transactions, remoteData.transactions) { it.id },
            categories = mergeEntities(localData.categories, remoteData.categories) { it.id ?: 0 },
            sources = mergeEntities(localData.sources, remoteData.sources) { it.id ?: 0 },
            tags = mergeEntities(localData.tags, remoteData.tags) { it.id ?: 0 },
            persons = mergeEntities(localData.persons, remoteData.persons) { it.id ?: 0 },
            assets = mergeEntities(localData.assets, remoteData.assets) { it.id ?: 0 },
            budgets = mergeEntities(localData.budgets, remoteData.budgets) { it.id ?: 0 },
            checks = mergeEntities(localData.checks, remoteData.checks) { it.id },
            debts = mergeEntities(localData.debts, remoteData.debts) { it.id },
            fixedExpenses = mergeEntities(localData.fixedExpenses, remoteData.fixedExpenses) { it.id },
            installments = mergeEntities(localData.installments, remoteData.installments) { it.id },
            notes = mergeEntities(localData.notes, remoteData.notes) { it.id },
            shoppingItems = mergeEntities(localData.shoppingItems, remoteData.shoppingItems) { it.id },
            backupTimestamp = maxOf(localData.backupTimestamp, remoteData.backupTimestamp)
        )

        backupRepository.restoreBackupData(mergedData)
        uploadToDrive(DRIVE_FILE_NAME, json.encodeToString(mergedData))
    }

    private fun <T : SyncableEntity> mergeEntities(
        local: List<T>,
        remote: List<T>,
        idSelector: (T) -> Long
    ): List<T> {
        val localMap = local.associateBy(idSelector)
        val remoteMap = remote.associateBy(idSelector)
        val allIds = localMap.keys + remoteMap.keys

        return allIds.map { id ->
            val localItem = localMap[id]
            val remoteItem = remoteMap[id]

            when {
                localItem != null && remoteItem != null -> {
                    if (localItem.updatedAt >= remoteItem.updatedAt) localItem else remoteItem
                }
                localItem != null -> localItem
                else -> remoteItem!!
            }
        }
    }

    private suspend fun fetchFromDrive(fileName: String): String? {
        return null 
    }

    private suspend fun uploadToDrive(fileName: String, data: String) {
    }
}
