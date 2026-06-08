package com.kazemieh.database.mapper

import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.persianMonth
import com.kazemieh.common.toPersianDigits
import com.kazemieh.database.GetAllTransactionsFiltered
import com.kazemieh.database.transaction.ObserveCategorySumsByFilter
import com.kazemieh.database.Category as CategoryDb
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
            name = category_name ?: "انتقال",
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
        name = name ?: "انتقال",
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
