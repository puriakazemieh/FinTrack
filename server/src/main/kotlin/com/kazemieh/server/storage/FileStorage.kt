package com.kazemieh.server.storage

import com.kazemieh.domain.repository.BackupData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object FileStorage {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val storageDir = File("data").apply { if (!exists()) mkdirs() }
    
    fun saveData(userId: String, data: BackupData) {
        val file = File(storageDir, "$userId.json")
        file.writeText(json.encodeToString(data))
    }
    
    fun loadData(userId: String): BackupData? {
        val file = File(storageDir, "$userId.json")
        if (!file.exists()) return null
        return try {
            json.decodeFromString<BackupData>(file.readText())
        } catch (e: Exception) {
            null
        }
    }
}
