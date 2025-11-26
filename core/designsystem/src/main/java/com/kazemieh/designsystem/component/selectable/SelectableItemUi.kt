package com.kazemieh.designsystem.component.selectable

import com.kazemieh.common.model.Category


data class SelectableItemUi(
    val id: Int,
    val title: String
)

fun List<SelectableItemUi>.toIdSet(): Set<Int> = this.map { it.id }.toSet()

fun Set<Pair<Int, String>>.toIdSet(): Set<Int> = this.map { it.first }.toSet()

fun Set<Int>.toPairSetFrom(items: List<SelectableItemUi>): Set<Pair<Int, String>> =
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