package com.kazemieh.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kazemieh.database.dao.CategoryDao
import com.kazemieh.database.dao.FinancialSourceDao
import com.kazemieh.database.dao.TagDao
import com.kazemieh.database.dao.TransactionDao
import com.kazemieh.database.entity.CategoryEntity
import com.kazemieh.database.entity.FinancialSourceEntity
import com.kazemieh.database.entity.TagEntity
import com.kazemieh.database.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        FinancialSourceEntity::class,
        TagEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun financialSourceDao(): FinancialSourceDao
    abstract fun tagDao(): TagDao
}
