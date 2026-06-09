package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.PreferenceRepository
import com.kazemieh.domain.repository.TransactionRepository

class SeedDataUseCase(
    private val transactionRepository: TransactionRepository,
    private val preferenceRepository: PreferenceRepository
) {
    companion object {
        const val PREF_IS_FIRST_RUN = "pref_is_first_run"
    }

    suspend operator fun invoke(
        customSource: Source? = null
    ) {
        // 1. Seed Financial Source (Custom or Default)
        val sourceToSave = customSource ?: Source(
            name = "منبع مالی پیش فرض",
            description = null,
            type = 1,
            balance = 0,
            colorId = 34, // Blue
            iconId = 22, // ic_source_default
            position = 0
        )
        transactionRepository.addSource(sourceToSave)

        // 2. Seed Categories (Matching DatabaseInitializer logic)
        val defaultCategories = listOf(
            // Incomes (Name, Type, Description, IconId, ColorId)
            Quintuple("حقوق و دستمزد", TransactionType.INCOME, null, 1, 11),
            Quintuple("پاداش و مزایا", TransactionType.INCOME, null, 2, 12),
            Quintuple("پروژه‌های جانبی", TransactionType.INCOME, null, 3, 13),
            Quintuple("فروش کالا/خدمات", TransactionType.INCOME, null, 4, 14),
            Quintuple("سود بانکی", TransactionType.INCOME, null, 5, 15),
            Quintuple("بازگشت وجه", TransactionType.INCOME, null, 6, 16),
            Quintuple("سرمایه‌گذاری", TransactionType.INCOME, null, 7, 17),
            Quintuple("اجاره دریافتی", TransactionType.INCOME, null, 8, 18),
            Quintuple("هدیه یا کمک مالی", TransactionType.INCOME, null, 9, 19),
            Quintuple("سایر دریافتی‌ها", TransactionType.INCOME, null, 10, 20),

            // Expenses (Name, Type, Description, IconId, ColorId)
            Quintuple("خوراک و رستوران", TransactionType.EXPENSE, null, 11, 1),
            Quintuple("حمل‌ونقل و سوخت", TransactionType.EXPENSE, null, 12, 2),
            Quintuple("اجاره یا وام مسکن", TransactionType.EXPENSE, null, 13, 3),
            Quintuple("قبوض و خدمات", TransactionType.EXPENSE, null, 14, 4),
            Quintuple("خرید پوشاک", TransactionType.EXPENSE, null, 15, 5),
            Quintuple("بهداشت و درمان", TransactionType.EXPENSE, null, 16, 6),
            Quintuple("آموزش و تحصیل", TransactionType.EXPENSE, null, 17, 7),
            Quintuple("تفریح و سرگرمی", TransactionType.EXPENSE, null, 18, 8),
            Quintuple("کمک مالی و هدیه", TransactionType.EXPENSE, null, 9, 9),
            Quintuple("سایر هزینه‌ها", TransactionType.EXPENSE, null, 10, 10),

            Quintuple("انتقال", TransactionType.TRANSFER, null, 19, 33)
        )

        defaultCategories.forEach { (name, type, description, iconId, colorId) ->
            transactionRepository.addCategory(
                Category(
                    name = name,
                    description = description,
                    type = type,
                    colorId = colorId,
                    iconId = iconId,
                    position = 0,
                    parentId = null
                )
            )
        }

        // 3. Seed Tags
        val defaultTags = listOf(
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
            transactionRepository.addTag(
                Tag(
                    name = name,
                    description = description,
                    colorId = colorId,
                    iconId = iconId,
                    position = 0
                )
            )
        }

        // 4. Seed Theme and Currency
        preferenceRepository.putString("pref_theme", "GLASS_DARK")
        preferenceRepository.putString("pref_currency", "TOMAN")

        // 5. Mark as not first run
        preferenceRepository.putBoolean(PREF_IS_FIRST_RUN, false)
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
