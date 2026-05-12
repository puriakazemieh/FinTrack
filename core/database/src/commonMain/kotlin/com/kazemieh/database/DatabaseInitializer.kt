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

private const val COLOR_COUNT = 12
private const val ICON_COUNT = 100

private fun colorForIndex(i: Int) = (i % COLOR_COUNT) + 1
private fun iconForIndex(i: Int) = (i % ICON_COUNT) + 1

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
            colorId = colorForIndex(0).toLong(),
            iconId = iconForIndex(0).toLong(),
            cardNumber = null
        )

        // دسته‌بندی‌های پیش‌فرض
        val defaultCategories = listOf(
            // Incomes
            Triple("حقوق و دستمزد", TransactionType.INCOME.count, null),
            Triple("پاداش و مزایا", TransactionType.INCOME.count, null),
            Triple("پروژه‌های جانبی", TransactionType.INCOME.count, null),
            Triple("فروش کالا/خدمات", TransactionType.INCOME.count, null),
            Triple("سود بانکی", TransactionType.INCOME.count, null),
            Triple("بازگشت وجه", TransactionType.INCOME.count, null),
            Triple("سرمایه‌گذاری", TransactionType.INCOME.count, null),
            Triple("اجاره دریافتی", TransactionType.INCOME.count, null),
            Triple("هدیه یا کمک مالی", TransactionType.INCOME.count, null),
            Triple("سایر دریافتی‌ها", TransactionType.INCOME.count, null),

            // Expenses
            Triple("خوراک و رستوران", TransactionType.EXPENSE.count, null),
            Triple("حمل‌ونقل و سوخت", TransactionType.EXPENSE.count, null),
            Triple("اجاره یا وام مسکن", TransactionType.EXPENSE.count, null),
            Triple("قبوض و خدمات", TransactionType.EXPENSE.count, null),
            Triple("خرید پوشاک", TransactionType.EXPENSE.count, null),
            Triple("بهداشت و درمان", TransactionType.EXPENSE.count, null),
            Triple("آموزش و تحصیل", TransactionType.EXPENSE.count, null),
            Triple("تفریح و سرگرمی", TransactionType.EXPENSE.count, null),
            Triple("کمک مالی و هدیه", TransactionType.EXPENSE.count, null),
            Triple("سایر هزینه‌ها", TransactionType.EXPENSE.count, null),

            Triple("انتقال", TransactionType.TRANSFER.count, null)
        )

        defaultCategories.forEachIndexed { index, (name, type, description) ->
            database.categoryQueries.addCategory(
                name = name,
                description = description,
                type = type.toLong(),
                colorId = colorForIndex(index).toLong(),
                iconId = iconForIndex(index).toLong()
            )
        }

        // تگ‌های پیش‌فرض
        val defaultTags = listOf(
            Pair("کار", "تراکنش‌های مرتبط با محل کار یا پروژه‌ها"),
            Pair("تفریح", "هزینه‌های مربوط به سرگرمی و تفریح"),
            Pair("خرید", "خرید کالا و خدمات عمومی"),
            Pair("خوراک", "هزینه‌های غذا و رستوران"),
            Pair("مسافرت", "هزینه‌های سفر و جابجایی"),
            Pair("سلامت", "هزینه‌های پزشکی و سلامت"),
            Pair("آموزش", "هزینه‌های دوره‌ها، کتاب و آموزش"),
            Pair("سرمایه‌گذاری", "سرمایه‌گذاری‌ها و پس‌اندازها"),
            Pair("حمل و نقل", "سوخت، تاکسی، حمل و نقل عمومی"),
            Pair("هدیه", "هدیه‌ها و کمک‌های مالی به دیگران")
        )

        defaultTags.forEachIndexed { index, (name, description) ->
            database.tagQueries.addTag(
                name = name,
                description = description,
                colorId = colorForIndex(index + 50).toLong(),
                iconId = iconForIndex(index + 50).toLong()
            )
        }
    }
}
