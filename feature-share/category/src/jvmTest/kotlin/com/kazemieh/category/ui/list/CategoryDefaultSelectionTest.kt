package com.kazemieh.category.ui.list

import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.*
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CategoryDefaultSelectionTest {
    private val dispatcher = StandardTestDispatcher()
    private val selected = Category(id = 2, name = "Old name", type = TransactionType.EXPENSE, colorId = 0, iconId = 0)
    private val live = linkedMapOf(2L to selected.copy(name = "Renamed elsewhere", parentId = 10))
    private val writes = mutableListOf<Pair<String, Long?>>()
    private lateinit var vm: CategoryViewModel
    @BeforeTest fun setup() {
        Dispatchers.setMain(dispatcher)
        val repository = Proxy.newProxyInstance(TransactionRepository::class.java.classLoader,
            arrayOf(TransactionRepository::class.java)) { _, method, args ->
            when (method.name) {
                "updateCategory" -> {
                    val category = args[0] as Category
                    writes += method.name to category.id
                    category.id?.let { live[it] = category }
                    1
                }
                "setDefaultCategory" -> {
                    val id = args[0] as Long
                    writes += method.name to id
                    live[id]?.let { live[id] = it.copy(isDefault = true); true } ?: false
                }
                else -> error("Unexpected repository call: $method")
            }
        } as TransactionRepository
        vm = CategoryViewModel(NoOpAnalyticsService(), ObserveCategoriesUseCase(repository),
            ObserveCategoriesFlatUseCase(repository), UpdateCategoryPositionsUseCase(repository), SetDefaultCategoryUseCase(repository))
    }
    @AfterTest fun cleanup() { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    @Test fun staleSelectionCannotRewriteNameOrParent() = runTest(dispatcher) {
        vm.onIntent(CategoryIntent.SetDefaultCategory(selected)); advanceUntilIdle()
        assertEquals("Renamed elsewhere", live.getValue(2).name)
        assertEquals(10L, live.getValue(2).parentId)
        assertTrue(live.getValue(2).isDefault)
        assertEquals(listOf<Pair<String, Long?>>("setDefaultCategory" to 2L), writes)
    }
    @Test fun missingIdentityDoesNotWrite() = runTest(dispatcher) {
        vm.onIntent(CategoryIntent.SetDefaultCategory(selected.copy(id = null))); advanceUntilIdle()
        assertTrue(writes.isEmpty())
        assertFalse(live.getValue(2).isDefault)
    }
}
