package com.kazemieh.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import com.kazemieh.common.model.TransactionType
import com.kazemieh.database.FinTrackDatabase.Companion.Schema
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseInitializer(
    private val database: FinTrackDatabase,
    private val driver: SqlDriver,
) {

    private val _ready = CompletableDeferred<Unit>()
    val ready: Deferred<Unit> = _ready
    suspend fun initialize() = withContext(Dispatchers.Default) {
        Schema.awaitCreate(driver)
        _ready.complete(Unit)
        val sourceCount = database.sourceQueries.observeSources().awaitAsList().size
        println(sourceCount)
        if (sourceCount > 0L) return@withContext

        // منبع مالی پیش‌فرض
        database.sourceQueries.addSource(
            name = "منبع مالی پیش فرض",
            description = null,
            type = 1,
            balance = 0,
            colorId = 1,
            iconId = 1092, // ic_bank_unknown
            cardNumber = null,
            shabaNumber = null,
            accountNumber = null,
            cvv2 = null,
            expirationMonth = null,
            expirationYear = null,
            branchCode = null,
            branchName = null
        )

        // دسته‌بندی‌های پیش‌فرض
        val defaultCategories = listOf(
            // Incomes (Name, Type, Description, IconId, ColorId)
            Quintuple("حقوق و دستمزد", TransactionType.INCOME.count, null, 204, 1),
            Quintuple("پاداش و مزایا", TransactionType.INCOME.count, null, 205, 2),
            Quintuple("پروژه‌های جانبی", TransactionType.INCOME.count, null, 206, 3),
            Quintuple("فروش کالا/خدمات", TransactionType.INCOME.count, null, 207, 4),
            Quintuple("سود بانکی", TransactionType.INCOME.count, null, 208, 5),
            Quintuple("بازگشت وجه", TransactionType.INCOME.count, null, 209, 6),
            Quintuple("سرمایه‌گذاری", TransactionType.INCOME.count, null, 210, 7),
            Quintuple("اجاره دریافتی", TransactionType.INCOME.count, null, 211, 8),
            Quintuple("هدیه یا کمک مالی", TransactionType.INCOME.count, null, 212, 9),
            Quintuple("سایر دریافتی‌ها", TransactionType.INCOME.count, null, 213, 10),

            // Expenses
            Quintuple("خوراک و رستوران", TransactionType.EXPENSE.count, null, 214, 11),
            Quintuple("حمل‌ونقل و سوخت", TransactionType.EXPENSE.count, null, 215, 12),
            Quintuple("اجاره یا وام مسکن", TransactionType.EXPENSE.count, null, 216, 13),
            Quintuple("قبوض و خدمات", TransactionType.EXPENSE.count, null, 217, 14),
            Quintuple("خرید پوشاک", TransactionType.EXPENSE.count, null, 218, 15),
            Quintuple("بهداشت و درمان", TransactionType.EXPENSE.count, null, 219, 16),
            Quintuple("آموزش و تحصیل", TransactionType.EXPENSE.count, null, 220, 17),
            Quintuple("تفریح و سرگرمی", TransactionType.EXPENSE.count, null, 221, 18),
            Quintuple("کمک مالی و هدیه", TransactionType.EXPENSE.count, null, 212, 19),
            Quintuple("سایر هزینه‌ها", TransactionType.EXPENSE.count, null, 213, 20),

            Quintuple("انتقال", TransactionType.TRANSFER.count, null, 222, 21)
        )

        defaultCategories.forEach { (name, type, description, iconId, colorId) ->
            database.categoryQueries.addCategory(
                name = name,
                description = description,
                type = type.toLong(),
                colorId = colorId.toLong(),
                iconId = iconId.toLong()
            )
        }

        // تگ‌های پیش‌فرض
        val defaultTags = listOf(
            // Name, Description, IconId, ColorId
            Quartuple("کار", "تراکنش‌های مرتبط با محل کار یا پروژه‌ها", 224, 31),
            Quartuple("تفریح", "هزینه‌های مربوط به سرگرمی و تفریح", 221, 32),
            Quartuple("خرید", "خرید کالا و خدمات عمومی", 207, 33),
            Quartuple("خوراک", "هزینه‌های غذا و رستوران", 214, 34),
            Quartuple("مسافرت", "هزینه‌های سفر و جابجایی", 223, 35),
            Quartuple("سلامت", "هزینه‌های پزشکی و سلامت", 219, 36),
            Quartuple("آموزش", "هزینه‌های دوره‌ها، کتاب و آموزش", 220, 37),
            Quartuple("سرمایه‌گذاری", "سرمایه‌گذاری‌ها و پس‌اندازها", 210, 38),
            Quartuple("حمل و نقل", "سوخت، تاکسی، حمل و نقل عمومی", 215, 39),
            Quartuple("هدیه", "هدیه‌ها و کمک‌های مالی به دیگران", 212, 40)
        )

        defaultTags.forEach { (name, description, iconId, colorId) ->
            database.tagQueries.addTag(
                name = name,
                description = description,
                colorId = colorId.toLong(),
                iconId = iconId.toLong()
            )
        }
    }
}

private data class Quintuple<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

private data class Quartuple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
