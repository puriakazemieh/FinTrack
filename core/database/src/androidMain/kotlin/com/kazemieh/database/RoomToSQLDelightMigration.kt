//package com.kazemieh.database
//
//import android.content.Context
//import androidx.room.Room
//import app.cash.sqldelight.db.SqlDriver
//import com.kazemieh.database.FinTrackDatabase
//import com.kazemieh.database.room.AppDatabase // دیتابیس قدیمی Room
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//class RoomToSQLDelightMigration(
//    private val context: Context,
//    private val sqlDelightDriver: SqlDriver
//) {
//
//    private val roomDb by lazy {
//        Room.databaseBuilder(
//            context,
//            AppDatabase::class.java,
//            "fin_track_room.db" // اسم دیتابیس قدیمی Room
//        ).build()
//    }
//
//    private val sqlDelightDb by lazy {
//        FinTrackDatabase(sqlDelightDriver)
//    }
//
//    suspend fun migrate() = withContext(Dispatchers.Default) {
//        // بررسی اینکه آیا قبلاً مایگریت شده
//        if (isMigrationCompleted()) {
//            return@withContext
//        }
//
//        // بررسی وجود دیتابیس Room
//        if (!roomDatabaseExists()) {
//            markMigrationCompleted()
//            return@withContext
//        }
//
//        try {
//            // 1. مایگریشن Categories
//            migrateCategories()
//
//            // 2. مایگریشن Sources
//            migrateSources()
//
//            // 3. مایگریشن Tags
//            migrateTags()
//
//            // 4. مایگریشن Persons
//            migratePersons()
//
//            // 5. مایگریشن Transactions
//            migrateTransactions()
//
//            // 6. مایگریشن TransactionTags
//            migrateTransactionTags()
//
//            // 7. مایگریشن TransactionPersons
//            migrateTransactionPersons()
//
//            // علامت‌گذاری مایگریشن به عنوان کامل
//            markMigrationCompleted()
//
//            // حذف دیتابیس قدیمی (اختیاری)
//            // deleteRoomDatabase()
//
//        } catch (e: Exception) {
//            // لاگ خطا
//            throw MigrationException("Migration failed: ${e.message}", e)
//        }
//    }
//
//    private fun roomDatabaseExists(): Boolean {
//        return context.getDatabasePath("fin_track_room.db").exists()
//    }
//
//    private fun isMigrationCompleted(): Boolean {
//        val prefs = context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)
//        return prefs.getBoolean("room_to_sqldelight_completed", false)
//    }
//
//    private fun markMigrationCompleted() {
//        val prefs = context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)
//        prefs.edit().putBoolean("room_to_sqldelight_completed", true).apply()
//    }
//
//    private suspend fun migrateCategories() {
//        val categories = roomDb.categoryDao().getAllCategoriesSync()
//
//        sqlDelightDb.transaction {
//            categories.forEach { category ->
//                sqlDelightDb.categoryQueries.insertCategory(
//                    id = category.id,
//                    name = category.name,
//                    icon = category.icon,
//                    color = category.color.toLong(),
//                    type = category.type.toLong(),
//                    isDefault = if (category.isDefault) 1L else 0L,
//                    createdAt = category.createdAt,
//                    updatedAt = category.updatedAt
//                )
//            }
//        }
//    }
//
//    private suspend fun migrateSources() {
//        val sources = roomDb.sourceDao().getAllSourcesSync()
//
//        sqlDelightDb.transaction {
//            sources.forEach { source ->
//                sqlDelightDb.sourceQueries.insertSource(
//                    id = source.id,
//                    name = source.name,
//                    icon = source.icon,
//                    color = source.color.toLong(),
//                    initialBalance = source.initialBalance,
//                    currentBalance = source.currentBalance,
//                    isDefault = if (source.isDefault) 1L else 0L,
//                    createdAt = source.createdAt,
//                    updatedAt = source.updatedAt
//                )
//            }
//        }
//    }
//
//    private suspend fun migrateTags() {
//        val tags = roomDb.tagDao().getAllTagsSync()
//
//        sqlDelightDb.transaction {
//            tags.forEach { tag ->
//                sqlDelightDb.tagQueries.insertTag(
//                    id = tag.id,
//                    name = tag.name,
//                    color = tag.color.toLong(),
//                    createdAt = tag.createdAt,
//                    updatedAt = tag.updatedAt
//                )
//            }
//        }
//    }
//
//    private suspend fun migratePersons() {
//        val persons = roomDb.personDao().getAllPersonsSync()
//
//        sqlDelightDb.transaction {
//            persons.forEach { person ->
//                sqlDelightDb.personQueries.insertPerson(
//                    id = person.id,
//                    name = person.name,
//                    phoneNumber = person.phoneNumber,
//                    createdAt = person.createdAt,
//                    updatedAt = person.updatedAt
//                )
//            }
//        }
//    }
//
//    private suspend fun migrateTransactions() {
//        val transactions = roomDb.transactionDao().getAllTransactionsSync()
//
//        sqlDelightDb.transaction {
//            transactions.forEach { transaction ->
//                sqlDelightDb.transactionQueries.insertTransaction(
//                    id = transaction.id,
//                    amount = transaction.amount,
//                    type = transaction.type.toLong(),
//                    categoryId = transaction.categoryId,
//                    sourceId = transaction.sourceId,
//                    description = transaction.description,
//                    date = transaction.date,
//                    createdAt = transaction.createdAt,
//                    updatedAt = transaction.updatedAt,
//                    transferId = transaction.transferId,
//                    destinationSourceId = transaction.destinationSourceId
//                )
//            }
//        }
//    }
//
//    private suspend fun migrateTransactionTags() {
//        val transactionTags = roomDb.transactionTagDao().getAllTransactionTagsSync()
//
//        sqlDelightDb.transaction {
//            transactionTags.forEach { transactionTag ->
//                sqlDelightDb.transactionTagQueries.insertTransactionTag(
//                    transactionId = transactionTag.transactionId,
//                    tagId = transactionTag.tagId
//                )
//            }
//        }
//    }
//
//    private suspend fun migrateTransactionPersons() {
//        val transactionPersons = roomDb.transactionPersonDao().getAllTransactionPersonsSync()
//
//        sqlDelightDb.transaction {
//            transactionPersons.forEach { transactionPerson ->
//                sqlDelightDb.transactionPersonQueries.insertTransactionPerson(
//                    transactionId = transactionPerson.transactionId,
//                    personId = transactionPerson.personId
//                )
//            }
//        }
//    }
//
//    private fun deleteRoomDatabase() {
//        roomDb.close()
//        context.deleteDatabase("fin_track_room.db")
//    }
//}
//
//class MigrationException(message: String, cause: Throwable) : Exception(message, cause)
