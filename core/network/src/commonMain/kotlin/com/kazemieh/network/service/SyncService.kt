package com.kazemieh.network.service

import com.kazemieh.domain.repository.BackupData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SyncService(private val client: HttpClient) {
    private val BASE_URL = "https://api.fintrack.app/v1/sync" // Placeholder

    suspend fun uploadBackup(data: BackupData) {
        client.post(BASE_URL) {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }

    suspend fun downloadBackup(): BackupData {
        return client.get(BASE_URL).body()
    }
}
