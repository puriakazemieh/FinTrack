package com.kazemieh.database.mapper

import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetHistory
import com.kazemieh.common.model.AssetType
import com.kazemieh.common.model.Budget
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.model.Debt
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.Installment
import com.kazemieh.common.model.InstallmentFrequency
import com.kazemieh.common.model.InstallmentWithRelations
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.persianMonth
import com.kazemieh.common.toPersianDigits
import com.kazemieh.database.GetAllTransactionsFiltered
import com.kazemieh.database.ObserveAllChecks
import com.kazemieh.database.ObserveAllDebts
import com.kazemieh.database.ObserveAllFixedExpenses
import com.kazemieh.database.ObserveChecksByStatus
import com.kazemieh.database.ObserveInstallments
import com.kazemieh.database.transaction.ObserveCategorySumsByFilter
import com.kazemieh.database.Asset as AssetDb
import com.kazemieh.database.Asset_history as AssetHistoryDb
import com.kazemieh.database.Category as CategoryDb
import com.kazemieh.database.Check_table as CheckDb
import com.kazemieh.database.Debt as DebtDb
import com.kazemieh.database.Fixed_expense as FixedExpenseDb
import com.kazemieh.database.Installment as InstallmentDb
import com.kazemieh.database.Person as PersonDb
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
            amountTransfer = amountTransfer!!.toInt(),
            categoryId = categoryId,
            sourceId = sourceId,
            sourceEndId = sourceEndId,
            description = description,
            photoPath = photoPath,
            timeStamp = timeStamp,
            date = PersianDateTime.parse(timeStamp).let { "${it.day} ${it.persianMonth().displayName} ${it.year}" }.toPersianDigits(),
            type = TransactionType.fromInt(type.toInt())
        ),
        category = Category(
            id = category_id ?: 0L,
            name = category_name ?: "",
            description = category_description,
            type = TransactionType.fromInt(category_type?.toInt() ?: TransactionType.TRANSFER.count),
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
    parentId = parentId
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
    branchName = branchName
)

// Tag Mappers
fun TagDb.toTag() = Tag(
    id = id,
    name = name,
    description = description,
    colorId = colorId.toInt(),
    iconId = iconId.toInt()
)

// Person Mappers
fun PersonDb.toPerson() = Person(
    id = id,
    name = name,
    description = description
)

// Debt Mappers
fun DebtDb.toDebt() = Debt(
    id = id,
    personId = personId,
    amount = amount,
    date = date,
    dueDate = dueDate,
    sourceId = sourceId,
    description = description,
    type = DebtType.fromInt(type.toInt()),
    isSettled = isSettled == 1L
)

fun ObserveAllDebts.toDebt() = Debt(
    id = id,
    personId = personId,
    amount = amount,
    date = date,
    dueDate = dueDate,
    sourceId = sourceId,
    description = description,
    type = DebtType.fromInt(type.toInt()),
    isSettled = isSettled == 1L,
    personName = personName,
    sourceName = sourceName
)

// Check Mappers
fun CheckDb.toCheck() = Check(
    id = id,
    amount = amount,
    date = date,
    dueDate = dueDate,
    status = CheckStatus.valueOf(status),
    personId = personId,
    photoPath = photoPath,
    description = description,
    isIncoming = isIncoming == 1L
)

fun ObserveAllChecks.toCheck() = Check(
    id = id,
    amount = amount,
    date = date,
    dueDate = dueDate,
    status = CheckStatus.valueOf(status),
    personId = personId,
    personName = personName,
    photoPath = photoPath,
    description = description,
    isIncoming = isIncoming == 1L
)

fun ObserveChecksByStatus.toCheck() = Check(
    id = id,
    amount = amount,
    date = date,
    dueDate = dueDate,
    status = CheckStatus.valueOf(status),
    personId = personId,
    personName = personName,
    photoPath = photoPath,
    description = description,
    isIncoming = isIncoming == 1L
)

// Fixed Expense Mappers
fun FixedExpenseDb.toFixedExpense() = FixedExpense(
    id = id,
    amount = amount,
    categoryId = categoryId,
    sourceId = sourceId,
    description = description,
    recurrence = RecurrenceType.valueOf(recurrence),
    startDate = startDate,
    nextDueDate = nextDueDate,
    isAutoPostEnabled = isAutoPostEnabled == 1L,
    isActive = isActive == 1L
)

fun ObserveAllFixedExpenses.toFixedExpense() = FixedExpense(
    id = id,
    amount = amount,
    categoryId = categoryId,
    categoryName = categoryName,
    sourceId = sourceId,
    sourceName = sourceName,
    description = description,
    recurrence = RecurrenceType.valueOf(recurrence),
    startDate = startDate,
    nextDueDate = nextDueDate,
    isAutoPostEnabled = isAutoPostEnabled == 1L,
    isActive = isActive == 1L
)

// Transaction Mappers
fun TransactionDb.toTransaction() = Transaction(
    id = id,
    amount = amount.toInt(),
    amountTransfer = amountTransfer!!.toInt(),
    categoryId = categoryId,
    sourceId = sourceId,
    sourceEndId = sourceEndId,
    description = description,
    photoPath = photoPath,
    timeStamp = timeStamp,
    date = PersianDateTime.parse(timeStamp).let { "${it.day} ${it.persianMonth().displayName} ${it.year}" }.toPersianDigits(),
    type = TransactionType.fromInt(type.toInt())
)

// Installment Mappers
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
    reminderEnabled = reminderEnabled == 1L
)

fun ObserveInstallments.toInstallmentWithRelations() = InstallmentWithRelations(
    installment = Installment(
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
        reminderEnabled = reminderEnabled == 1L
    ),
    category = Category(
        id = category_id ?: 0L,
        name = category_name ?: "",
        description = category_description,
        type = TransactionType.fromInt(category_type?.toInt() ?: TransactionType.EXPENSE.count),
        colorId = category_colorId?.toInt() ?: 1,
        iconId = category_iconId?.toInt() ?: 1,
        parentId = category_parentId
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
        branchName = source_branchName
    )
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
    lastUpdate = lastUpdate?.let { kotlin.time.Instant.fromEpochMilliseconds(it) }
)

fun AssetHistoryDb.toAssetHistory() = AssetHistory(
    id = id,
    assetId = assetId,
    price = price,
    date = kotlin.time.Instant.fromEpochMilliseconds(date)
)
