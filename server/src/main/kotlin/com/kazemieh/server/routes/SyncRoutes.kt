package com.kazemieh.server.routes

import com.kazemieh.common.model.*
import com.kazemieh.domain.repository.BackupData
import com.kazemieh.server.storage.FileStorage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun Route.configureSyncRoutes() {
    val logger = LoggerFactory.getLogger("SyncRoutes")
    val API_KEY = "fintrack_secret_token_2026"

    route("/sync") {
        intercept(ApplicationCallPipeline.Plugins) {
            val key = call.request.headers["X-API-Key"]
            if (key != API_KEY) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing API Key"))
                finish()
            }
        }

        post("/upload") {
            try {
                val incomingData = call.receive<BackupData>()
                val userId = call.request.queryParameters["userId"] ?: "default_user"
                
                // Smart Merge on Server: Load existing, merge with incoming
                val existingData = FileStorage.loadData(userId)
                val mergedData = if (existingData == null) {
                    incomingData
                } else {
                    mergeBackupData(existingData, incomingData)
                }

                FileStorage.saveData(userId, mergedData)
                call.respond(HttpStatusCode.OK, mapOf("status" to "success", "timestamp" to System.currentTimeMillis()))
            } catch (e: Exception) {
                logger.error("Upload failed: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Unknown error")))
            }
        }
        
        get("/download") {
            val userId = call.request.queryParameters["userId"] ?: "default_user"
            val since = call.request.queryParameters["since"]?.toLongOrNull() ?: 0L
            
            val data = FileStorage.loadData(userId)
            if (data != null) {
                // Filter data to only return items updated after 'since'
                val filteredData = data.copy(
                    transactions = data.transactions.filter { it.updatedAt > since },
                    categories = data.categories.filter { it.updatedAt > since },
                    sources = data.sources.filter { it.updatedAt > since },
                    tags = data.tags.filter { it.updatedAt > since },
                    persons = data.persons.filter { it.updatedAt > since },
                    assets = data.assets.filter { it.updatedAt > since },
                    notes = data.notes.filter { it.updatedAt > since },
                    shoppingItems = data.shoppingItems.filter { it.updatedAt > since }
                    // ... other lists
                )
                call.respond(filteredData)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "No data found for user $userId"))
            }
        }
    }
}

private fun mergeBackupData(local: BackupData, incoming: BackupData): BackupData {
    return BackupData(
        transactions = mergeLists(local.transactions, incoming.transactions) { it.id },
        categories = mergeLists(local.categories, incoming.categories) { it.id ?: 0 },
        sources = mergeLists(local.sources, incoming.sources) { it.id ?: 0 },
        tags = mergeLists(local.tags, incoming.tags) { it.id ?: 0 },
        persons = mergeLists(local.persons, incoming.persons) { it.id ?: 0 },
        assets = mergeLists(local.assets, incoming.assets) { it.id ?: 0 },
        budgets = mergeLists(local.budgets, incoming.budgets) { it.id ?: 0 },
        checks = mergeLists(local.checks, incoming.checks) { it.id },
        debts = mergeLists(local.debts, incoming.debts) { it.id },
        fixedExpenses = mergeLists(local.fixedExpenses, incoming.fixedExpenses) { it.id },
        installments = mergeLists(local.installments, incoming.installments) { it.id },
        notes = mergeLists(local.notes, incoming.notes) { it.id },
        shoppingItems = mergeLists(local.shoppingItems, incoming.shoppingItems) { it.id },
        backupTimestamp = maxOf(local.backupTimestamp, incoming.backupTimestamp)
    )
}

private fun <T : SyncableEntity> mergeLists(local: List<T>, incoming: List<T>, idSelector: (T) -> Long): List<T> {
    val resultMap = local.associateBy(idSelector).toMutableMap()
    incoming.forEach { incomingItem ->
        val id = idSelector(incomingItem)
        val existing = resultMap[id]
        if (existing == null || incomingItem.updatedAt >= existing.updatedAt) {
            resultMap[id] = incomingItem
        }
    }
    return resultMap.values.toList()
}
