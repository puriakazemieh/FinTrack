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
            colorId = 34, // Blue
            iconId = 22, // ic_source_default
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
            // Incomes (Name, Type, Description, IconId, ColorId) - Use 11-20 for Green
            Quintuple("حقوق و دستمزد", TransactionType.INCOME.count, null, 1, 11),
            Quintuple("پاداش و مزایا", TransactionType.INCOME.count, null, 2, 12),
            Quintuple("پروژه‌های جانبی", TransactionType.INCOME.count, null, 3, 13),
            Quintuple("فروش کالا/خدمات", TransactionType.INCOME.count, null, 4, 14),
            Quintuple("سود بانکی", TransactionType.INCOME.count, null, 5, 15),
            Quintuple("بازگشت وجه", TransactionType.INCOME.count, null, 6, 16),
            Quintuple("سرمایه‌گذاری", TransactionType.INCOME.count, null, 7, 17),
            Quintuple("اجاره دریافتی", TransactionType.INCOME.count, null, 8, 18),
            Quintuple("هدیه یا کمک مالی", TransactionType.INCOME.count, null, 9, 19),
            Quintuple("سایر دریافتی‌ها", TransactionType.INCOME.count, null, 10, 20),

            // Expenses (Name, Type, Description, IconId, ColorId) - Use 1-10 for Red
            Quintuple("خوراک و رستوران", TransactionType.EXPENSE.count, null, 11, 1),
            Quintuple("حمل‌ونقل و سوخت", TransactionType.EXPENSE.count, null, 12, 2),
            Quintuple("اجاره یا وام مسکن", TransactionType.EXPENSE.count, null, 13, 3),
            Quintuple("قبوض و خدمات", TransactionType.EXPENSE.count, null, 14, 4),
            Quintuple("خرید پوشاک", TransactionType.EXPENSE.count, null, 15, 5),
            Quintuple("بهداشت و درمان", TransactionType.EXPENSE.count, null, 16, 6),
            Quintuple("آموزش و تحصیل", TransactionType.EXPENSE.count, null, 17, 7),
            Quintuple("تفریح و سرگرمی", TransactionType.EXPENSE.count, null, 18, 8),
            Quintuple("کمک مالی و هدیه", TransactionType.EXPENSE.count, null, 9, 9),
            Quintuple("سایر هزینه‌ها", TransactionType.EXPENSE.count, null, 10, 10),

            Quintuple("انتقال", TransactionType.TRANSFER.count, null, 19, 33)
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
            Quartuple("کار", "تراکنش‌های مرتبط با محل کار یا پروژه‌ها", 21, 31),
            Quartuple("تفریح", "هزینه‌های مربوط به سرگرمی و تفریح", 18, 32),
            Quartuple("خرید", "خرید کالا و خدمات عمومی", 4, 33),
            Quartuple("خوراک", "هزینه‌های غذا و رستوران", 11, 34),
            Quartuple("مسافرت", "هزینه‌های سفر و جابجایی", 20, 35),
            Quartuple("سلامت", "هزینه‌های پزشکی و سلامت", 16, 36),
            Quartuple("آموزش", "هزینه‌های دوره‌ها، کتاب و آموزش", 17, 37),
            Quartuple("سرمایه‌گذاری", "سرمایه‌گذاری‌ها و پس‌اندازها", 7, 38),
            Quartuple("حمل و نقل", "سوخت، تاکسی، حمل و نقل عمومی", 12, 39),
            Quartuple("هدیه", "هدیه‌ها و کمک‌های مالی به دیگران", 9, 40)
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
