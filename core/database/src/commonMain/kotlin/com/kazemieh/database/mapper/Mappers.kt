package com.kazemieh.database.mapper

import com.kazemieh.common.model.*
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.persianMonth
import com.kazemieh.common.toPersianDigits
import com.kazemieh.database.Achievement as AchievementDb
import com.kazemieh.database.Streak as StreakDb
import com.kazemieh.database.*
import com.kazemieh.database.transaction.ObserveCategorySumsByFilter
import com.kazemieh.database.Asset as AssetDb
import com.kazemieh.database.Asset_history as AssetHistoryDb
import com.kazemieh.database.Category as CategoryDb
import com.kazemieh.database.Check_table as CheckDb
import com.kazemieh.database.Debt as DebtDb
import com.kazemieh.database.Fixed_expense as FixedExpenseDb
import com.kazemieh.database.Installment as InstallmentDb
import com.kazemieh.database.Note_table as NoteDb
import com.kazemieh.database.Person as PersonDb
import com.kazemieh.database.Rate_cache as RateCacheDb
import com.kazemieh.database.Shopping_item_table as ShoppingItemDb
import com.kazemieh.database.Source as SourceDb
import com.kazemieh.database.Tag as TagDb
import com.kazemieh.database.Transactions as TransactionDb

fun GetAllTransactionsFiltered.toTransactionWithRelations(): TransactionWithRelations {
    // Parse tags
    val tags = if (tag_ids.isNullOrEmpty()) {
        emptyList()
    } else {
        val ids = tag_ids.split(",")
        val names = tag_names?.split(",") ?: emptyList()
        val descriptions = tag_descriptions?.split(",") ?: emptyList()
        val colorIds = tag_colorIds?.split(",") ?: emptyList()
        val iconIds = tag_iconIds?.split(",") ?: emptyList()

        ids.mapIndexed { index, id ->
            Tag(
                id = id.toLong(),
                name = names.getOrNull(index) ?: "",
                description = descriptions.getOrNull(index),
                colorId = colorIds.getOrNull(index)?.toIntOrNull() ?: 1,
                iconId = iconIds.getOrNull(index)?.toIntOrNull() ?: 1
            )
        }
    }

    // Parse persons
    val persons = if (person_ids.isNullOrEmpty()) {
        emptyList()
    } else {
        val ids = person_ids.split(",")
        val names = person_names?.split(",") ?: emptyList()
        val descriptions = person_descriptions?.split(",") ?: emptyList()

        ids.mapIndexed { index, id ->
            Person(
                id = id.toLong(),
                name = names.getOrNull(index) ?: "",
                description = descriptions.getOrNull(index)
            )
        }
    }

    return TransactionWithRelations(
        transaction = Transaction(
            id = id,
            amount = amount.toInt(),
            amountTransfer = amountTransfer?.toInt() ?: 0,
            categoryId = categoryId,
            sourceId = sourceId,
            sourceEndId = sourceEndId,
            relatedDebtId = relatedDebtId,
            description = description,
            photoPath = photoPath,
            timeStamp = timeStamp,
            date = PersianDateTime.parse(timeStamp)
                .let { "${it.day} ${it.persianMonth().displayName} ${it.year}" }.toPersianDigits(),
            type = TransactionType.fromInt(type.toInt()),
            updatedAt = updatedAt,
            syncStatus = SyncStatus.fromInt(syncStatus.toInt())
        ),
        category = Category(
            id = category_id ?: 0L,
            name = category_name ?: "",
            description = category_description,
            type = TransactionType.fromInt(
                category_type?.toInt() ?: TransactionType.TRANSFER.count
            ),
            colorId = category_colorId?.toInt() ?: 1,
            iconId = category_iconId?.toInt() ?: 19,
            parentId = category_parentId
        ),
        source = Source(
            id = source_id,
            name = source_name,
            balance = source_balance.toInt(),
            cardNumber = source_cardNumber,
            description = source_description,
            type = source_type.toInt(),
            colorId = source_colorId.toInt(),
            iconId = source_iconId.toInt(),
            shabaNumber = source_shabaNumber,
            accountNumber = source_accountNumber,
            cvv2 = source_cvv2,
            expirationMonth = source_expirationMonth,
            expirationYear = source_expirationYear,
            branchCode = source_branchCode,
            branchName = source_branchName
        ),
        sourceEnd = sourceEnd_id?.let {
            Source(
                id = it,
                name = sourceEnd_name!!,
                balance = sourceEnd_balance!!.toInt(),
                cardNumber = sourceEnd_cardNumber,
                description = sourceEnd_description,
                type = sourceEnd_type!!.toInt(),
                colorId = sourceEnd_colorId!!.toInt(),
                iconId = sourceEnd_iconId!!.toInt(),
                shabaNumber = sourceEnd_shabaNumber,
                accountNumber = sourceEnd_accountNumber,
                cvv2 = sourceEnd_cvv2,
                expirationMonth = sourceEnd_expirationMonth,
                expirationYear = sourceEnd_expirationYear,
                branchCode = sourceEnd_branchCode,
                branchName = sourceEnd_branchName
            )
        },
        tags = tags,
        persons = persons
    )
}

// Category Mappers
fun CategoryDb.toCategory() = Category(
    id = id,
    name = name,
    description = description,
    type = TransactionType.fromInt(type.toInt()),
    colorId = colorId.toInt(),
    iconId = iconId.toInt(),
    parentId = parentId,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun ObserveCategorySumsByFilter.toCategorySum(): CategorySum {
    return CategorySum(
        categoryId = categoryId ?: 0L,
        name = name ?: "",
        totalAmount = totalAmount ?: 0,
        type = TransactionType.fromInt(type?.toInt() ?: TransactionType.TRANSFER.count),
        colorId = colorId?.toInt() ?: 1,
        iconId = iconId?.toInt() ?: 19
    )
}

// Source Mappers
fun SourceDb.toSource() = Source(
    id = id,
    name = name,
    balance = balance.toInt(),
    cardNumber = cardNumber,
    description = description,
    type = type.toInt(),
    colorId = colorId.toInt(),
    iconId = iconId.toInt(),
    shabaNumber = shabaNumber,
    accountNumber = accountNumber,
    cvv2 = cvv2,
    expirationMonth = expirationMonth,
    expirationYear = expirationYear,
    branchCode = branchCode,
    branchName = branchName,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

// Tag Mappers
fun TagDb.toTag() = Tag(
    id = id,
    name = name,
    description = description,
    colorId = colorId.toInt(),
    iconId = iconId.toInt(),
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

// Person Mappers
fun PersonDb.toPerson() = Person(
    id = id,
    name = name,
    description = description,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

// Debt Mappers
fun DebtDb.toDebt() = Debt(
    id = id,
    personId = personId,
    amount = amount,
    categoryId = categoryId,
    date = date,
    dueDate = dueDate,
    sourceId = sourceId,
    description = description,
    type = DebtType.fromInt(type.toInt()),
    isSettled = isSettled == 1L,
    reminderEnabled = reminderEnabled == 1L,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun ObserveAllDebts.toDebt() = Debt(
    id = id,
    personId = personId,
    amount = amount,
    categoryId = categoryId,
    date = date,
    dueDate = dueDate,
    sourceId = sourceId,
    description = description,
    type = DebtType.fromInt(type.toInt()),
    isSettled = isSettled == 1L,
    reminderEnabled = reminderEnabled == 1L,
    personName = personName,
    sourceName = sourceName,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun ObserveDebtsByPerson.toDebt() = Debt(
    id = id,
    personId = personId,
    amount = amount,
    categoryId = categoryId,
    date = date,
    dueDate = dueDate,
    sourceId = sourceId,
    description = description,
    type = DebtType.fromInt(type.toInt()),
    isSettled = isSettled == 1L,
    reminderEnabled = reminderEnabled == 1L,
    personName = personName,
    sourceName = sourceName,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun ObserveAllDebts.toDebtWithRelations(): DebtWithRelations {
    val tags = if (tagIds.isNullOrEmpty()) {
        emptyList()
    } else {
        val ids = tagIds.split(",")
        val names = tagNames?.split(",") ?: emptyList()
        val colorIds = tagColorIds?.split(",") ?: emptyList()
        ids.mapIndexed { index, id ->
            Tag(
                id = id.toLong(),
                name = names.getOrNull(index) ?: "",
                colorId = colorIds.getOrNull(index)?.toIntOrNull() ?: 1,
                iconId = 1
            )
        }
    }
    return DebtWithRelations(
        debt = this.toDebt(),
        person = Person(
            id = personId,
            name = personName,
            description = personDescription,
            updatedAt = 0,
            syncStatus = SyncStatus.SYNCED
        ),
        category = categoryId?.let {
            Category(
                id = it,
                name = categoryName ?: "",
                colorId = categoryColorId?.toInt() ?: 1,
                iconId = categoryIconId?.toInt() ?: 1,
                type = TransactionType.fromInt(type.toInt()), // or map based on DebtType
                updatedAt = 0,
                syncStatus = SyncStatus.SYNCED
            )
        },
        source = sourceId?.let {
            Source(
                id = it,
                name = sourceName ?: "",
                colorId = sourceColorId?.toInt() ?: 0,
                iconId = sourceIconId?.toInt() ?: 0,
                updatedAt = 0,
                syncStatus = SyncStatus.SYNCED
            )
        },
        tags = tags
    )
}

fun ObserveDebtsByPerson.toDebtWithRelations(): DebtWithRelations {
    val tags = if (tagIds.isNullOrEmpty()) {
        emptyList()
    } else {
        val ids = tagIds.split(",")
        val names = tagNames?.split(",") ?: emptyList()
        val colorIds = tagColorIds?.split(",") ?: emptyList()
        ids.mapIndexed { index, id ->
            Tag(
                id = id.toLong(),
                name = names.getOrNull(index) ?: "",
                colorId = colorIds.getOrNull(index)?.toIntOrNull() ?: 1,
                iconId = 1
            )
        }
    }
    return DebtWithRelations(
        debt = this.toDebt(),
        person = Person(
            id = personId,
            name = personName,
            description = personDescription,
            updatedAt = 0,
            syncStatus = SyncStatus.SYNCED
        ),
        category = categoryId?.let {
            Category(
                id = it,
                name = categoryName ?: "",
                colorId = categoryColorId?.toInt() ?: 1,
                iconId = categoryIconId?.toInt() ?: 1,
                type = TransactionType.fromInt(type.toInt()),
                updatedAt = 0,
                syncStatus = SyncStatus.SYNCED
            )
        },
        source = sourceId?.let {
            Source(
                id = it,
                name = sourceName ?: "",
                colorId = sourceColorId?.toInt() ?: 0,
                iconId = sourceIconId?.toInt() ?: 0,
                updatedAt = 0,
                syncStatus = SyncStatus.SYNCED
            )
        },
        tags = tags
    )
}

fun InstallmentDb.toInstallment() = Installment(
    id = id,
    title = title,
    totalAmount = totalAmount,
    installmentAmount = installmentAmount,
    totalInstallments = totalInstallments.toInt(),
    paidInstallments = paidInstallments.toInt(),
    categoryId = categoryId,
    sourceId = sourceId,
    startDate = startDate,
    nextDueDate = nextDueDate,
    frequency = InstallmentFrequency.valueOf(frequency),
    description = description,
    isCompleted = isCompleted == 1L,
    reminderEnabled = reminderEnabled == 1L,
    postAsTransaction = postAsTransaction == 1L,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun ObserveInstallments.toInstallment() = Installment(
    id = id,
    title = title,
    totalAmount = totalAmount,
    installmentAmount = installmentAmount,
    totalInstallments = totalInstallments.toInt(),
    paidInstallments = paidInstallments.toInt(),
    categoryId = categoryId,
    sourceId = sourceId,
    startDate = startDate,
    nextDueDate = nextDueDate,
    frequency = InstallmentFrequency.valueOf(frequency),
    description = description,
    isCompleted = isCompleted == 1L,
    reminderEnabled = reminderEnabled == 1L,
    postAsTransaction = postAsTransaction == 1L,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun ObserveInstallments.toInstallmentWithRelations(): InstallmentWithRelations {
    return mapInstallmentWithRelations(
        installment = this.toInstallment(),
        category_id = category_id,
        category_name = category_name,
        category_description = category_description,
        category_type = category_type,
        category_colorId = category_colorId,
        category_iconId = category_iconId,
        category_parentId = category_parentId,
        source_id = source_id,
        source_name = source_name,
        source_balance = source_balance,
        source_cardNumber = source_cardNumber,
        source_description = source_description,
        source_type = source_type,
        source_colorId = source_colorId,
        source_iconId = source_iconId,
        source_shabaNumber = source_shabaNumber,
        source_accountNumber = source_accountNumber,
        source_cvv2 = source_cvv2,
        source_expirationMonth = source_expirationMonth,
        source_expirationYear = source_expirationYear,
        source_branchCode = source_branchCode,
        source_branchName = source_branchName,
        tag_ids = tag_ids,
        tag_names = tag_names,
        tag_colorIds = tag_colorIds,
        tag_iconIds = tag_iconIds,
        person_ids = person_ids,
        person_names = person_names
    )
}

fun GetInstallmentWithRelationsById.toInstallmentWithRelations(): InstallmentWithRelations {
    return mapInstallmentWithRelations(
        installment = this.toInstallment(),
        category_id = category_id,
        category_name = category_name,
        category_description = category_description,
        category_type = category_type,
        category_colorId = category_colorId,
        category_iconId = category_iconId,
        category_parentId = category_parentId,
        source_id = source_id,
        source_name = source_name,
        source_balance = source_balance,
        source_cardNumber = source_cardNumber,
        source_description = source_description,
        source_type = source_type,
        source_colorId = source_colorId,
        source_iconId = source_iconId,
        source_shabaNumber = source_shabaNumber,
        source_accountNumber = source_accountNumber,
        source_cvv2 = source_cvv2,
        source_expirationMonth = source_expirationMonth,
        source_expirationYear = source_expirationYear,
        source_branchCode = source_branchCode,
        source_branchName = source_branchName,
        tag_ids = tag_ids,
        tag_names = tag_names,
        tag_colorIds = tag_colorIds,
        tag_iconIds = tag_iconIds,
        person_ids = person_ids,
        person_names = person_names
    )
}

private fun mapInstallmentWithRelations(
    installment: Installment,
    category_id: Long?,
    category_name: String?,
    category_description: String?,
    category_type: Long?,
    category_colorId: Long?,
    category_iconId: Long?,
    category_parentId: Long?,
    source_id: Long?,
    source_name: String?,
    source_balance: Long?,
    source_cardNumber: String?,
    source_description: String?,
    source_type: Long?,
    source_colorId: Long?,
    source_iconId: Long?,
    source_shabaNumber: String?,
    source_accountNumber: String?,
    source_cvv2: String?,
    source_expirationMonth: String?,
    source_expirationYear: String?,
    source_branchCode: String?,
    source_branchName: String?,
    tag_ids: String?,
    tag_names: String?,
    tag_colorIds: String?,
    tag_iconIds: String?,
    person_ids: String?,
    person_names: String?
): InstallmentWithRelations {
    val tags = if (tag_ids.isNullOrEmpty()) {
        emptyList()
    } else {
        val ids = tag_ids.split(",")
        val names = tag_names?.split(",") ?: emptyList()
        val colorIds = tag_colorIds?.split(",") ?: emptyList()
        val iconIds = tag_iconIds?.split(",") ?: emptyList()

        ids.mapIndexed { index, id ->
            Tag(
                id = id.toLong(),
                name = names.getOrNull(index) ?: "",
                colorId = colorIds.getOrNull(index)?.toIntOrNull() ?: 1,
                iconId = iconIds.getOrNull(index)?.toIntOrNull() ?: 1
            )
        }
    }

    val persons = if (person_ids.isNullOrEmpty()) {
        emptyList()
    } else {
        val ids = person_ids.split(",")
        val names = person_names?.split(",") ?: emptyList()

        ids.mapIndexed { index, id ->
            Person(
                id = id.toLong(),
                name = names.getOrNull(index) ?: ""
            )
        }
    }

    return InstallmentWithRelations(
        installment = installment,
        category = Category(
            id = category_id ?: 0L,
            name = category_name ?: "",
            description = category_description,
            type = TransactionType.fromInt(category_type?.toInt() ?: TransactionType.EXPENSE.count),
            colorId = category_colorId?.toInt() ?: 1,
            iconId = category_iconId?.toInt() ?: 1,
            parentId = category_parentId,
            updatedAt = 0,
            syncStatus = SyncStatus.SYNCED
        ),
        source = Source(
            id = source_id ?: 0L,
            name = source_name ?: "",
            balance = source_balance?.toInt() ?: 0,
            cardNumber = source_cardNumber,
            description = source_description,
            type = source_type?.toInt() ?: 0,
            colorId = source_colorId?.toInt() ?: 1,
            iconId = source_iconId?.toInt() ?: 1,
            shabaNumber = source_shabaNumber,
            accountNumber = source_accountNumber,
            cvv2 = source_cvv2,
            expirationMonth = source_expirationMonth,
            expirationYear = source_expirationYear,
            branchCode = source_branchCode,
            branchName = source_branchName,
            updatedAt = 0,
            syncStatus = SyncStatus.SYNCED
        ),
        tags = tags,
        persons = persons
    )
}

fun GetInstallmentWithRelationsById.toInstallment() = Installment(
    id = id,
    title = title,
    totalAmount = totalAmount,
    installmentAmount = installmentAmount,
    totalInstallments = totalInstallments.toInt(),
    paidInstallments = paidInstallments.toInt(),
    categoryId = categoryId,
    sourceId = sourceId,
    startDate = startDate,
    nextDueDate = nextDueDate,
    frequency = InstallmentFrequency.valueOf(frequency),
    description = description,
    isCompleted = isCompleted == 1L,
    reminderEnabled = reminderEnabled == 1L,
    postAsTransaction = postAsTransaction == 1L,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

// Check Mappers
fun CheckDb.toCheck() = Check(
    id = id,
    amount = amount,
    date = date,
    dueDate = dueDate,
    status = CheckStatus.valueOf(status),
    personId = personId,
    categoryId = categoryId,
    sourceId = sourceId,
    tagIds = tagIds?.split(",")?.mapNotNull { it.toLongOrNull() },
    reminderEnabled = reminderEnabled == 1L,
    photoPath = photoPath,
    description = description,
    isIncoming = isIncoming == 1L,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun ObserveAllChecks.toCheck() = Check(
    id = id,
    amount = amount,
    date = date,
    dueDate = dueDate,
    status = CheckStatus.valueOf(status),
    personId = personId,
    categoryId = categoryId,
    sourceId = sourceId,
    tagIds = tagIds?.split(",")?.mapNotNull { it.toLongOrNull() },
    reminderEnabled = reminderEnabled == 1L,
    personName = personName,
    photoPath = photoPath,
    description = description,
    isIncoming = isIncoming == 1L,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun ObserveChecksByStatus.toCheck() = Check(
    id = id,
    amount = amount,
    date = date,
    dueDate = dueDate,
    status = CheckStatus.valueOf(status),
    personId = personId,
    categoryId = categoryId,
    sourceId = sourceId,
    tagIds = tagIds?.split(",")?.mapNotNull { it.toLongOrNull() },
    reminderEnabled = reminderEnabled == 1L,
    personName = personName,
    photoPath = photoPath,
    description = description,
    isIncoming = isIncoming == 1L,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

// Fixed Expense Mappers
fun FixedExpenseDb.toFixedExpense() = FixedExpense(
    id = id,
    title = title,
    amount = amount,
    categoryId = categoryId,
    sourceId = sourceId,
    description = description,
    recurrence = RecurrenceType.valueOf(recurrence),
    startDate = startDate,
    nextDueDate = nextDueDate,
    endDate = endDate,
    isAutoPostEnabled = isAutoPostEnabled == 1L,
    isActive = isActive == 1L,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun ObserveAllFixedExpenses.toFixedExpense() = FixedExpense(
    id = id,
    title = title,
    amount = amount,
    categoryId = categoryId,
    categoryName = categoryName,
    sourceId = sourceId,
    sourceName = sourceName,
    description = description,
    recurrence = RecurrenceType.valueOf(recurrence),
    startDate = startDate,
    nextDueDate = nextDueDate,
    endDate = endDate,
    isAutoPostEnabled = isAutoPostEnabled == 1L,
    isActive = isActive == 1L,
    tagIds = tagIds?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList(),
    tagNames = tagNames?.split(",") ?: emptyList(),
    personIds = personIds?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList(),
    personNames = personNames?.split(",") ?: emptyList(),
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun ObserveFixedExpensesFiltered.toFixedExpense() = FixedExpense(
    id = id,
    title = title,
    amount = amount,
    categoryId = categoryId,
    categoryName = categoryName,
    sourceId = sourceId,
    sourceName = sourceName,
    description = description,
    recurrence = RecurrenceType.valueOf(recurrence),
    startDate = startDate,
    nextDueDate = nextDueDate,
    endDate = endDate,
    isAutoPostEnabled = isAutoPostEnabled == 1L,
    isActive = isActive == 1L,
    tagIds = tagIds?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList(),
    tagNames = tagNames?.split(",") ?: emptyList(),
    personIds = personIds?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList(),
    personNames = personNames?.split(",") ?: emptyList(),
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

// Transaction Mappers
fun TransactionDb.toTransaction() = Transaction(
    id = id,
    amount = amount.toInt(),
    amountTransfer = amountTransfer?.toInt() ?: 0,
    categoryId = categoryId,
    sourceId = sourceId,
    sourceEndId = sourceEndId,
    relatedDebtId = relatedDebtId,
    description = description,
    photoPath = photoPath,
    timeStamp = timeStamp,
    date = PersianDateTime.parse(timeStamp).let { "${it.day} ${it.persianMonth().displayName} ${it.year}" }.toPersianDigits(),
    type = TransactionType.fromInt(type.toInt()),
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

// Asset Mappers
fun AssetDb.toAsset() = Asset(
    id = id,
    name = name,
    type = type,
    quantity = quantity,
    purchasePrice = purchasePrice,
    currentPrice = currentPrice,
    currency = currency,
    description = description,
    colorId = colorId.toInt(),
    iconId = iconId.toInt(),
    lastUpdate = lastUpdate?.let { kotlin.time.Instant.fromEpochMilliseconds(it) },
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun AssetHistoryDb.toAssetHistory() = AssetHistory(
    id = id,
    assetId = assetId,
    price = price,
    date = kotlin.time.Instant.fromEpochMilliseconds(date),
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun RateCacheDb.toAssetRate() = AssetRate(
    type = type,
    code = code,
    name = name,
    price = price,
    lastUpdate = kotlin.time.Instant.fromEpochMilliseconds(lastUpdate)
)

// Shopping Mappers
fun ShoppingItemDb.toShoppingItem() = ShoppingItem(
    id = id,
    name = name,
    isChecked = isChecked,
    priority = priority.toInt(),
    estimatedPrice = estimatedPrice,
    reminderTime = reminderTime,
    categoryId = categoryId,
    note = note,
    position = position.toInt(),
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt())
)

fun ObserveShoppingItems.toShoppingItem(): ShoppingItem {
    val tags = if (tag_ids.isNullOrEmpty()) {
        emptyList()
    } else {
        val ids = tag_ids.split(",")
        val names = tag_names?.split(",") ?: emptyList()
        ids.mapIndexed { index, id ->
            Tag(
                id = id.toLongOrNull() ?: 0L,
                name = names.getOrNull(index) ?: "",
                colorId = 1, // Default or join with tag table properly if needed
                iconId = 1
            )
        }
    }
    return ShoppingItem(
        id = id,
        name = name,
        isChecked = isChecked,
        priority = priority.toInt(),
        estimatedPrice = estimatedPrice,
        reminderTime = reminderTime,
        categoryId = categoryId,
        note = note,
        tags = tags,
        position = position.toInt(),
        updatedAt = updatedAt,
        syncStatus = SyncStatus.fromInt(syncStatus.toInt())
    )
}

fun ObserveShoppingItemsFiltered.toShoppingItem(): ShoppingItem {
    val tags = if (tag_ids.isNullOrEmpty()) {
        emptyList()
    } else {
        val ids = tag_ids.split(",")
        val names = tag_names?.split(",") ?: emptyList()
        ids.mapIndexed { index, id ->
            Tag(
                id = id.toLongOrNull() ?: 0L,
                name = names.getOrNull(index) ?: "",
                colorId = 1,
                iconId = 1
            )
        }
    }
    return ShoppingItem(
        id = id,
        name = name,
        isChecked = isChecked,
        priority = priority.toInt(),
        estimatedPrice = estimatedPrice,
        reminderTime = reminderTime,
        categoryId = categoryId,
        note = note,
        tags = tags,
        position = position.toInt(),
        updatedAt = updatedAt,
        syncStatus = SyncStatus.fromInt(syncStatus.toInt())
    )
}

fun GetShoppingItemById.toShoppingItem(): ShoppingItem {
    val tags = if (tag_ids.isNullOrEmpty()) {
        emptyList()
    } else {
        val ids = tag_ids.split(",")
        val names = tag_names?.split(",") ?: emptyList()
        ids.mapIndexed { index, id ->
            Tag(
                id = id.toLongOrNull() ?: 0L,
                name = names.getOrNull(index) ?: "",
                colorId = 1,
                iconId = 1
            )
        }
    }
    return ShoppingItem(
        id = id,
        name = name,
        isChecked = isChecked,
        priority = priority.toInt(),
        estimatedPrice = estimatedPrice,
        reminderTime = reminderTime,
        categoryId = categoryId,
        note = note,
        tags = tags,
        position = position.toInt(),
        updatedAt = updatedAt,
        syncStatus = SyncStatus.fromInt(syncStatus.toInt())
    )
}

fun Sync_history.toSyncHistory() = SyncHistory(
    id = id,
    timestamp = timestamp,
    type = SyncType.valueOf(type),
    status = SyncResultStatus.valueOf(status),
    recordCount = recordCount.toInt(),
    insertedCount = insertedCount.toInt(),
    updatedCount = updatedCount.toInt(),
    errorMessage = errorMessage
)

fun NoteDb.toNote(tags: List<Tag> = emptyList()) = Note(
    id = id,
    title = title,
    content = content,
    color = color,
    isPinned = isPinned,
    isLocked = isLocked,
    reminderTime = reminderTime,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.fromInt(syncStatus.toInt()),
    tags = tags
)

fun Sms_draft.toSmsDraft() = SmsDraft(
    id = id,
    sender = sender,
    body = body,
    amount = amount.toInt(),
    bankName = bankName,
    type = TransactionType.fromInt(type.toInt()),
    sourceId = sourceId,
    sourceIdentifier = sourceIdentifier,
    timeStamp = timeStamp,
    date = date,
    categoryId = categoryId,
    confidence = confidence.toInt(),
    isUsed = isUsed == 1L
)

fun AchievementDb.toAchievement() = Achievement(
    id = id,
    type = AchievementType.entries.find { it.key == type } ?: AchievementType.FIRST_TRANSACTION,
    isUnlocked = isUnlocked,
    unlockTime = unlockTime,
    progress = progress.toInt(),
    target = target.toInt()
)

fun StreakDb.toStreak() = Streak(
    currentStreak = currentStreak.toInt(),
    bestStreak = bestStreak.toInt(),
    lastTransactionDate = lastTransactionDate,
    xp = xp.toInt(),
    level = level.toInt()
)
