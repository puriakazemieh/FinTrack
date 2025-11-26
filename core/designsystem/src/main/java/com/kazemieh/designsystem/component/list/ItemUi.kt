package com.kazemieh.designsystem.component.list

import com.kazemieh.common.model.Category


data class ItemUi(
    val id: Int,
    val title: String,
    val extraData: Any? = null
)

fun List<ItemUi>.toIdSet(): Set<Int> = this.map { it.id }.toSet()

fun Set<Pair<Int, String>>.toIdSet(): Set<Int> = this.map { it.first }.toSet()

fun Set<Int>.toPairSetFrom(items: List<ItemUi>): Set<Pair<Int, String>> =
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