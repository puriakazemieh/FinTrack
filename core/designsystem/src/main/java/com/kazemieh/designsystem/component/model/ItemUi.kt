package com.kazemieh.designsystem.component.model

import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag


data class ItemUi(
    val id: Long,
    var title: UiText,
    val payload: ItemPayload? = null
)

// ---------- TAG ----------
fun Tag.toItemUi(): ItemUi {
    val safeId = id ?: 0L
    return ItemUi(
        id = safeId,
        title = UiText.DynamicString(name),
        payload = ItemPayload.TagPayload(safeId, name)
    )
}

fun ItemUi.toTag(): Tag {
    val p = payload as? ItemPayload.TagPayload
        ?: error("ItemUi payload is not TagPayload")
    return Tag(id = p.id, name = p.name)
}

// ---------- PERSON ----------
fun Person.toItemUi(): ItemUi {
    val safeId = id ?: 0L
    return ItemUi(
        id = safeId,
        title = UiText.DynamicString(name),
        payload = ItemPayload.PersonPayload(safeId, name)
    )
}

fun ItemUi.toPerson(): Person {
    val p = payload as? ItemPayload.PersonPayload
        ?: error("ItemUi payload is not PersonPayload")
    return Person(id = p.id, name = p.name)
}

// ---------- CATEGORY ----------
fun Category.toItemUi(): ItemUi {
    val safeId = id ?: 0L
    return ItemUi(
        id = safeId,
        title = UiText.DynamicString(name),
        payload = ItemPayload.CategoryPayload(safeId, name, type)
    )
}

fun ItemUi.toCategory(): Category {
    val p = payload as? ItemPayload.CategoryPayload
        ?: error("ItemUi payload is not CategoryPayload")
    return Category(id = p.id, name = p.name, type = p.type)
}

// ---------- SOURCE ----------
fun Source.toItemUi(): ItemUi {
    val safeId = id ?: 0L
    return ItemUi(
        id = safeId,
        title = UiText.DynamicString(name),
        payload = ItemPayload.SourcePayload(safeId, name, formattedBalance)
    )
}

fun ItemUi.toSource(): Source {
    val p = payload as? ItemPayload.SourcePayload
        ?: error("ItemUi payload is not SourcePayload")
    return Source(id = p.id, name = p.name, formattedBalance = p.formattedBalance)
}

