package com.kazemieh.common.model

import android.content.Context
import com.kazemieh.common.UiText


data class ItemUi(
    val id: Long,
    var title: UiText,
    val extraData: Any? = null
)

fun ItemUi.toSource(context: Context): Source {
    return Source(id = id, name = title.asString(context), formattedBalance = extraData as String)
}

fun Source.toItemUi(): ItemUi {
    return ItemUi(id = id ?: 0, title = UiText.DynamicString(name), extraData = formattedBalance)
}

fun ItemUi.toCategory(context: Context): Category {
    return Category(id = id, name = title.asString(context), type = extraData as TransactionType)
}


fun Category.toItemUi(): ItemUi {
    return ItemUi(id = id ?: 0, title = UiText.DynamicString(name), extraData = type)
}


fun ItemUi.toTag(context: Context): Tag {
    return Tag(id = id, name = title.asString(context))
}

fun Tag.toItemUi(): ItemUi {
    return ItemUi(id = id ?: 0, title = UiText.DynamicString(name))
}


fun ItemUi.toPerson(context: Context): Person {
    return Person(id = id, name = title.asString(context))
}

fun Person.toItemUi(): ItemUi {
    return ItemUi(id = id ?: 0, title = UiText.DynamicString(name))
}

