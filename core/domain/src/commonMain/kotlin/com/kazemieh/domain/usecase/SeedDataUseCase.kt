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
        customSource: Source? = null,
        securityQuestion: String? = null,
        securityAnswer: String? = null,
        localizedNames: Map<String, String> = emptyMap()
    ) {
        // 1. Seed Financial Source (Custom or Default)
        val sourceToSave = customSource ?: Source(
            name = localizedNames["source_cash"] ?: "نقد",
            description = localizedNames["source_cash_desc"] ?: "کیف پول یا پول نقد",
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
            Quintuple(localizedNames["cat_salary"] ?: "حقوق", TransactionType.INCOME, null, 1, 11),
            Quintuple(localizedNames["cat_bonus"] ?: "پاداش", TransactionType.INCOME, null, 2, 12),
            Quintuple(localizedNames["cat_interest"] ?: "سود بانکی", TransactionType.INCOME, null, 5, 15),
            Quintuple(localizedNames["cat_gift"] ?: "هدیه", TransactionType.INCOME, null, 9, 19),
            Quintuple(localizedNames["cat_other_income"] ?: "سایر درآمدها", TransactionType.INCOME, null, 10, 20),

            // Expenses (Name, Type, Description, IconId, ColorId)
            Quintuple(localizedNames["cat_food"] ?: "خوراک", TransactionType.EXPENSE, null, 11, 1),
            Quintuple(localizedNames["cat_transport"] ?: "حمل‌ونقل", TransactionType.EXPENSE, null, 12, 2),
            Quintuple(localizedNames["cat_rent"] ?: "اجاره / وام", TransactionType.EXPENSE, null, 13, 3),
            Quintuple(localizedNames["cat_bills"] ?: "قبوض", TransactionType.EXPENSE, null, 14, 4),
            Quintuple(localizedNames["cat_shopping"] ?: "خرید", TransactionType.EXPENSE, null, 15, 5),
            Quintuple(localizedNames["cat_health"] ?: "سلامت", TransactionType.EXPENSE, null, 16, 6),
            Quintuple(localizedNames["cat_education"] ?: "آموزش", TransactionType.EXPENSE, null, 17, 7),
            Quintuple(localizedNames["cat_entertainment"] ?: "سرگرمی", TransactionType.EXPENSE, null, 18, 8),
            Quintuple(localizedNames["cat_other_expense"] ?: "سایر هزینه‌ها", TransactionType.EXPENSE, null, 10, 10),

            Quintuple(localizedNames["cat_transfer"] ?: "انتقال", TransactionType.TRANSFER, null, 19, 33)
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
            Quartuple(localizedNames["tag_work"] ?: "کار", localizedNames["tag_work_desc"] ?: "تراکنش‌های مرتبط با محل کار یا پروژه‌ها", 21, 31),
            Quartuple(localizedNames["tag_fun"] ?: "تفریح", localizedNames["tag_fun_desc"] ?: "هزینه‌های مربوط به سرگرمی و تفریح", 18, 32),
            Quartuple(localizedNames["tag_shopping"] ?: "خرید", localizedNames["tag_shopping_desc"] ?: "خرید کالا و خدمات عمومی", 4, 33),
            Quartuple(localizedNames["tag_food"] ?: "خوراک", localizedNames["tag_food_desc"] ?: "هزینه‌های غذا و رستوران", 11, 34),
            Quartuple(localizedNames["tag_travel"] ?: "مسافرت", localizedNames["tag_travel_desc"] ?: "هزینه‌های سفر و جابجایی", 20, 35),
            Quartuple(localizedNames["tag_health"] ?: "سلامت", localizedNames["tag_health_desc"] ?: "هزینه‌های پزشکی و سلامت", 16, 36),
            Quartuple(localizedNames["tag_education"] ?: "آموزش", localizedNames["tag_education_desc"] ?: "هزینه‌های دوره‌ها، کتاب و آموزش", 17, 37),
            Quartuple(localizedNames["tag_investment"] ?: "سرمایه‌گذاری", localizedNames["tag_investment_desc"] ?: "سرمایه‌گذاری‌ها و پس‌اندازها", 7, 38),
            Quartuple(localizedNames["tag_transport"] ?: "حمل و نقل", localizedNames["tag_transport_desc"] ?: "سوخت، تاکسی، حمل و نقل عمومی", 12, 39),
            Quartuple(localizedNames["tag_gift"] ?: "هدیه", localizedNames["tag_gift_desc"] ?: "هدیه‌ها و کمک‌های مالی به دیگران", 9, 40)
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

        // 5. Seed Security Details
        if (!securityQuestion.isNullOrBlank() && !securityAnswer.isNullOrBlank()) {
            preferenceRepository.putString("pref_security_question", securityQuestion)
            preferenceRepository.putString("pref_security_answer", hashPin(securityAnswer))
        }

        // 6. Mark as not first run
        preferenceRepository.putBoolean(PREF_IS_FIRST_RUN, false)
    }

    private fun hashPin(pin: String): String {
        val salt = "FinTrack_2026_Secure_Salt"
        var hash = 7L
        val combined = pin + salt
        for (char in combined) {
            hash = 31 * hash + char.code.toLong()
        }
        return hash.toString(16)
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
