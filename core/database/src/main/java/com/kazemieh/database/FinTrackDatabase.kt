package com.kazemieh.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kazemieh.database.dao.CategoryDao
import com.kazemieh.database.dao.FinancialSourceDao
import com.kazemieh.database.dao.TagDao
import com.kazemieh.database.dao.TransactionDao
import com.kazemieh.database.dao.TransactionTagCrossRefDao
import com.kazemieh.database.entity.CategoryEntity
import com.kazemieh.database.entity.FinancialSourceEntity
import com.kazemieh.database.entity.TagEntity
import com.kazemieh.database.entity.TransactionEntity
import com.kazemieh.database.entity.TransactionTagCrossRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.Koin

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        FinancialSourceEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class DatabaseModule : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun financialSourceDao(): FinancialSourceDao
    abstract fun tagDao(): TagDao
    abstract fun transactionTagCrossRefDao(): TransactionTagCrossRefDao

}

class PrepopulateCallback(
    private val koin: Koin
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            val prefsDao: FinancialSourceDao by koin.inject()
            prefsDao.insertFinancialSource(
                FinancialSourceEntity(name = "test", type = 1)
            )
        }

    }
}