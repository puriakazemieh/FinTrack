package com.kazemieh.network.service

import com.kazemieh.domain.repository.BackupData
import com.kazemieh.network.SyncConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SyncService(private val client: HttpClient) {

    suspend fun uploadBackup(userId: String, data: BackupData) {
        client.post("${SyncConfig.baseUrl}/upload") {
            parameter("userId", userId)
            header("X-API-Key", SyncConfig.apiKey)
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }

    suspend fun downloadBackup(userId: String, since: Long? = null): BackupData {
        return client.get("${SyncConfig.baseUrl}/download") {
            parameter("userId", userId)
            if (since != null) parameter("since", since)
            header("X-API-Key", SyncConfig.apiKey)
        }.body()
    }
}
