package com.kazemieh.backup_export

import com.kazemieh.common.model.*
import com.kazemieh.common.util.SecurityUtils
import com.kazemieh.domain.repository.BackupData
import com.kazemieh.domain.repository.BackupRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BackupManager(
    private val backupRepository: BackupRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportToJson(encrypt: Boolean = true): String {
        val data = backupRepository.getBackupData()
        val jsonString = json.encodeToString(data)
        return if (encrypt) SecurityUtils.encrypt(jsonString) else jsonString
    }

    suspend fun importFromJson(jsonString: String, encrypted: Boolean = true) {
        val decrypted = if (encrypted) SecurityUtils.decrypt(jsonString) else jsonString
        val data = json.decodeFromString<BackupData>(decrypted)
        backupRepository.restoreBackupData(data)
    }

    suspend fun exportToCsv(): String {
        val data = backupRepository.getBackupData()
        val sb = StringBuilder()
        // Simple CSV implementation for Transactions
        sb.append("ID,Amount,Description,Date,Type\n")
        data.transactions.forEach {
            sb.append("${it.id},${it.amount},${it.description ?: ""},${it.timeStamp},${it.type}\n")
        }
        return sb.toString()
    }
}
