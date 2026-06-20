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

    /**
     * Orchestrates the sync process:
     * 1. Downloads the latest remote data from Google Drive (AppData folder).
     * 2. Merges it with local data using per-entity Last-Writer-Wins logic.
     * 3. Persists the merged data locally.
     * 4. Uploads the updated merged data back to Google Drive.
     */
    suspend fun syncWithDrive() {
        val localData = backupRepository.getBackupData()
        
        // Step 1: Fetch remote data
        val remoteJson = fetchFromDrive(DRIVE_FILE_NAME)
        val remoteData = if (remoteJson != null) json.decodeFromString<BackupData>(remoteJson) else null

        if (remoteData == null) {
            // No remote data yet, upload local as base
            uploadToDrive(DRIVE_FILE_NAME, json.encodeToString(localData))
            return
        }

        // Step 2: Merge logic (Granular LWW)
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

        // Step 3: Standardize state
        backupRepository.restoreBackupData(mergedData)
        
        // Step 4: Upload final merged state
        uploadToDrive(DRIVE_FILE_NAME, json.encodeToString(mergedData))
    }

    private fun <T> mergeEntities(
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
                    val localUpdatedAt = getUpdatedAt(localItem)
                    val remoteUpdatedAt = getUpdatedAt(remoteItem)
                    if (localUpdatedAt >= remoteUpdatedAt) localItem else remoteItem
                }
                localItem != null -> localItem
                else -> remoteItem!!
            }
        }
    }

    private fun getUpdatedAt(item: Any): Long {
        return try {
            // Using reflection in KMP or checking specific property
            // For now, assume entities have updatedAt
            val property = item::class.members.find { it.name == "updatedAt" }
            (property?.call(item) as? Long) ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Placeholder for real Google Drive AppData download.
     * Use Ktor or platform-specific Google client here.
     */
    private suspend fun fetchFromDrive(fileName: String): String? {
        // TODO: Implementation with Google Drive REST API
        // val response = ktorClient.get("https://www.googleapis.com/drive/v3/files?spaces=appDataFolder")
        return null 
    }

    /**
     * Placeholder for real Google Drive AppData upload.
     */
    private suspend fun uploadToDrive(fileName: String, data: String) {
        // TODO: Implementation with Google Drive REST API
        // ktorClient.post("https://www.googleapis.com/upload/drive/v3/files?uploadType=media") { body = data }
    }
}
