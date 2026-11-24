package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.PreferenceRepository
import com.kazemieh.domain.repository.TransactionRepository
import java.lang.reflect.Proxy
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SeedCategoryDefaultsTest {
    @Test fun seedsOneDefaultPerTypeWhileKeepingAllCategories() = runTest {
        val categories = mutableListOf<Category>()
        val preferences = mutableMapOf<String, Any>()
        val repository = Proxy.newProxyInstance(TransactionRepository::class.java.classLoader,
            arrayOf(TransactionRepository::class.java)) { _, method, args ->
            when (method.name) {
                "addCategory" -> { categories += args[0] as Category; categories.size.toLong() }
                "addSource", "addTag" -> 1L
                else -> error("Unexpected financial write: $method")
            }
        } as TransactionRepository
        val prefs = Proxy.newProxyInstance(PreferenceRepository::class.java.classLoader,
            arrayOf(PreferenceRepository::class.java)) { _, method, args ->
            when (method.name) {
                "putString", "putBoolean" -> { preferences[args[0] as String] = args[1]; null }
                else -> error("Unexpected preference call: $method")
            }
        } as PreferenceRepository
        SeedDataUseCase(repository, prefs)(localizedNames = mapOf("cat_salary" to "دسته نخست درآمد", "cat_food" to "دسته نخست هزینه"))
        assertEquals(15, categories.size)
        assertEquals(setOf(TransactionType.INCOME, TransactionType.EXPENSE, TransactionType.TRANSFER), categories.map { it.type }.toSet())
        categories.groupBy { it.type to it.parentId }.forEach { (_, group) ->
            assertEquals(listOf(group.first()), group.filter { it.isDefault })
        }
        assertEquals("دسته نخست درآمد", categories.first { it.type == TransactionType.INCOME }.name)
        assertEquals("دسته نخست هزینه", categories.first { it.type == TransactionType.EXPENSE }.name)
        assertEquals(false, preferences[SeedDataUseCase.PREF_IS_FIRST_RUN])
    }
}
