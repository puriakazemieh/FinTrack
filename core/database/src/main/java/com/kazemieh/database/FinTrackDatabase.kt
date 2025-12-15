package com.kazemieh.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kazemieh.database.dao.CategoryDao
import com.kazemieh.database.dao.FinancialSourceDao
import com.kazemieh.database.dao.PersonDao
import com.kazemieh.database.dao.TagDao
import com.kazemieh.database.dao.TransactionDao
import com.kazemieh.database.entity.CategoryEntity
import com.kazemieh.database.entity.FinancialSourceEntity
import com.kazemieh.database.entity.PersonEntity
import com.kazemieh.database.entity.TagEntity
import com.kazemieh.database.entity.TransactionEntity
import com.kazemieh.database.entity.TransactionPersonCrossRef
import com.kazemieh.database.entity.TransactionTagCrossRef
import com.kazemieh.common.model.TransactionType
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
        PersonEntity::class,
        TransactionPersonCrossRef::class,
    ],
    version = 2,
    exportSchema = false
)
abstract class DatabaseModule : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun financialSourceDao(): FinancialSourceDao
    abstract fun tagDao(): TagDao
    abstract fun personDao(): PersonDao

}

class PrepopulateCallback(
    private val koin: Koin
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            val prefsDao: FinancialSourceDao by koin.inject()
            prefsDao.insertFinancialSource(
                FinancialSourceEntity(name = "منبع مالی پیش فرض", type = 1)
            )
            val categoryDao: CategoryDao by koin.inject()
            val defaultCategory = listOf(
                // Incomes
                CategoryEntity(name = "حقوق و دستمزد", type = TransactionType.INCOME),
                CategoryEntity(name = "پاداش و مزایا", type = TransactionType.INCOME),
                CategoryEntity(name = "پروژه‌های جانبی", type = TransactionType.INCOME),
                CategoryEntity(name = "فروش کالا/خدمات", type = TransactionType.INCOME),
                CategoryEntity(name = "سود بانکی", type = TransactionType.INCOME),
                CategoryEntity(name = "بازگشت وجه", type = TransactionType.INCOME),
                CategoryEntity(name = "سرمایه‌گذاری", type = TransactionType.INCOME),
                CategoryEntity(name = "اجاره دریافتی", type = TransactionType.INCOME),
                CategoryEntity(name = "هدیه یا کمک مالی", type = TransactionType.INCOME),
                CategoryEntity(name = "سایر دریافتی‌ها", type = TransactionType.INCOME),

                // Expenses
                CategoryEntity(name = "خوراک و رستوران", type = TransactionType.EXPENSE),
                CategoryEntity(name = "حمل‌ونقل و سوخت", type = TransactionType.EXPENSE),
                CategoryEntity(name = "اجاره یا وام مسکن", type = TransactionType.EXPENSE),
                CategoryEntity(name = "قبوض و خدمات", type = TransactionType.EXPENSE),
                CategoryEntity(name = "خرید پوشاک", type = TransactionType.EXPENSE),
                CategoryEntity(name = "بهداشت و درمان", type = TransactionType.EXPENSE),
                CategoryEntity(name = "آموزش و تحصیل", type = TransactionType.EXPENSE),
                CategoryEntity(name = "تفریح و سرگرمی", type = TransactionType.EXPENSE),
                CategoryEntity(name = "کمک مالی و هدیه", type = TransactionType.EXPENSE),
                CategoryEntity(name = "سایر هزینه‌ها", type = TransactionType.EXPENSE),


                CategoryEntity(name = "انتقال", type = TransactionType.TRANSFER)
            )
            categoryDao.insertAllCategory(defaultCategory)

            val tagDao: TagDao by koin.inject()
            val defaultTags = listOf(
                TagEntity(name = "کار", description = "تراکنش‌های مرتبط با محل کار یا پروژه‌ها"),
                TagEntity(name = "تفریح", description = "هزینه‌های مربوط به سرگرمی و تفریح"),
                TagEntity(name = "خرید", description = "خرید کالا و خدمات عمومی"),
                TagEntity(name = "خوراک", description = "هزینه‌های غذا و رستوران"),
                TagEntity(name = "مسافرت", description = "هزینه‌های سفر و جابجایی"),
                TagEntity(name = "سلامت", description = "هزینه‌های پزشکی و سلامت"),
                TagEntity(name = "آموزش", description = "هزینه‌های دوره‌ها، کتاب و آموزش"),
                TagEntity(name = "سرمایه‌گذاری", description = "سرمایه‌گذاری‌ها و پس‌اندازها"),
                TagEntity(name = "حمل و نقل", description = "سوخت، تاکسی، حمل و نقل عمومی"),
                TagEntity(name = "هدیه", description = "هدیه‌ها و کمک‌های مالی به دیگران")
            )
            tagDao.insertAllTag(defaultTags)
        }

    }
}