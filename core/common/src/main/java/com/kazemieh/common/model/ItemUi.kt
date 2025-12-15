package com.kazemieh.common.model


data class ItemUi(
    val id: Long,
    val title: String,
    val extraData: Any? = null
)

fun List<ItemUi>.toIdSet(): Set<Long> = this.map { it.id }.toSet()

fun Set<Pair<Int, String>>.toIdSet(): Set<Int> = this.map { it.first }.toSet()

fun Set<Long>.toPairSetFrom(items: List<ItemUi>): Set<Pair<Long, String>> =
    this.mapNotNull { id ->
        items.firstOrNull { it.id == id }?.let { id to it.title }
    }.toSet()

suspend fun convertIdsToPairs(
    selectedIds: Set<Int>,
    allCategories: List<Category>
): Set<Pair<Int, String>> {
    return selectedIds.mapNotNull { id ->
        allCategories.firstOrNull { it.id?.toInt() == id }?.let { id to it.name }
    }.toSet()
}


fun ItemUi.toSource(): Source {
    return Source(id = id, name = title, formattedBalance = extraData as String)
}

fun Source.toItemUi(): ItemUi {
    return ItemUi(id = id ?: 0, title = name, extraData = formattedBalance)
}

fun ItemUi.toCategory(): Category {
    return Category(id = id, name = title, type = extraData as TransactionType)
}


fun Category.toItemUi(): ItemUi {
    return ItemUi(id = id ?: 0, title = name, extraData = type)
}


fun ItemUi.toTag(): Tag {
    return Tag(id = id, name = title)
}

fun Tag.toItemUi(): ItemUi {
    return ItemUi(id = id ?: 0, title = name)
}


fun ItemUi.toPerson(): Person {
    return Person(id = id, name = title)
}

fun Person.toItemUi(): ItemUi {
    return ItemUi(id = id ?: 0, title = name)
}

