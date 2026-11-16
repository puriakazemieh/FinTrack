package com.kazemieh.shopping.ui

import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.common.model.ShoppingItem
import com.kazemieh.common.model.Tag
import com.kazemieh.domain.notification.NotificationScheduler
import com.kazemieh.domain.repository.ShoppingRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.*
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDateTime
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ShoppingReminderTest {
    private val dispatcher = StandardTestDispatcher()
    private val cancelled = mutableListOf<String>()
    private val repository = FakeShoppingRepository()
    private lateinit var viewModel: ShoppingViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        // These scenarios never open the category picker or post a new reminder.
        val transactions = Proxy.newProxyInstance(
            TransactionRepository::class.java.classLoader,
            arrayOf(TransactionRepository::class.java)
        ) { _, method, _ ->
            check(method.name == "observeMostUsedCategories") { "Unexpected call: ${method.name}" }
            flowOf(emptyList<com.kazemieh.common.model.Category>())
        } as TransactionRepository
        val scheduler = object : NotificationScheduler {
            override fun cancelReminder(id: String) { cancelled += id }
            override fun scheduleReminder(
                id: String, title: String, message: String, scheduledTime: LocalDateTime,
                channelId: String, extraId: Long?
            ) = error("An obsolete reminder must be cancelled, not scheduled")
        }
        viewModel = ShoppingViewModel(
            NoOpAnalyticsService(), ObserveShoppingItemsUseCase(repository),
            AddShoppingItemUseCase(repository), UpdateShoppingItemUseCase(repository),
            DeleteShoppingItemUseCase(repository), UpdateShoppingPositionsUseCase(repository),
            GetCategoryUseCase(transactions), ObserveMostUsedCategoriesUseCase(transactions),
            ObserveMostUsedShoppingTagsUseCase(repository), scheduler
        )
    }

    @AfterTest
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun deletingItemCancelsItsPendingReminder() = runTest(dispatcher) {
        viewModel.onIntent(ShoppingIntent.OnDeleteItem(7))
        runCurrent()
        assertEquals(7L, repository.deletedId)
        assertEquals(listOf("shopping_7"), cancelled)
    }

    @Test
    fun removingReminderWhileEditingCancelsThePreviousJob() = runTest(dispatcher) {
        val item = ShoppingItem(id = 7, name = "Milk", reminderTime = null)
        viewModel.onIntent(ShoppingIntent.OnUpdateItem(item))
        runCurrent()
        assertEquals(item, repository.updatedItem)
        assertEquals(listOf("shopping_7"), cancelled)
    }

    @Test
    fun purchasingItemCancelsReminderWithoutChangingItsSavedDueTime() = runTest(dispatcher) {
        val item = ShoppingItem(id = 7, name = "Milk", reminderTime = 1_900_000_000_000)
        viewModel.onIntent(ShoppingIntent.OnToggleItem(item))
        runCurrent()
        assertEquals(item.copy(isChecked = true), repository.updatedItem)
        assertEquals(listOf("shopping_7"), cancelled)
    }

    private class FakeShoppingRepository : ShoppingRepository {
        var updatedItem: ShoppingItem? = null
        var deletedId: Long? = null
        override fun observeShoppingItems(categoryIds: List<Long>, tagIds: List<Long>) = flowOf(emptyList<ShoppingItem>())
        override fun observeMostUsedTags(limit: Long) = flowOf(emptyList<Tag>())
        override suspend fun getShoppingItemById(id: Long): ShoppingItem? = null
        override suspend fun addShoppingItem(item: ShoppingItem): Long = error("Unexpected insert")
        override suspend fun updateShoppingItem(item: ShoppingItem) { updatedItem = item }
        override suspend fun deleteShoppingItem(id: Long) { deletedId = id }
        override suspend fun updatePositions(items: List<ShoppingItem>) = error("Unexpected reorder")
    }
}
