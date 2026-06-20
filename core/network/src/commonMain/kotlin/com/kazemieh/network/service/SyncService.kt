package com.kazemieh.network.service

import com.kazemieh.domain.repository.BackupData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SyncService(private val client: HttpClient) {
    private val BASE_URL = "http://10.0.2.2:8080/sync" // Android Emulator localhost
    private val API_KEY = "fintrack_secret_token_2026"

    suspend fun uploadBackup(userId: String, data: BackupData) {
        client.post("$BASE_URL/upload") {
            parameter("userId", userId)
            header("X-API-Key", API_KEY)
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }

    suspend fun downloadBackup(userId: String, since: Long? = null): BackupData {
        return client.get("$BASE_URL/download") {
            parameter("userId", userId)
            if (since != null) parameter("since", since)
            header("X-API-Key", API_KEY)
        }.body()
    }
}
