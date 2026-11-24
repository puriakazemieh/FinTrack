package com.kazemieh.tag.ui.add

import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.common.model.Tag
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.AddTagUseCase
import com.kazemieh.domain.usecase.UpdateTagUseCase
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TagEditPositionTest {
    private val dispatcher = StandardTestDispatcher()
    private val saved = mutableListOf<Tag>()
    private lateinit var vm: AddTagViewModel

    @BeforeTest fun setup() {
        Dispatchers.setMain(dispatcher)
        val repository = Proxy.newProxyInstance(TransactionRepository::class.java.classLoader,
            arrayOf(TransactionRepository::class.java)) { _, method, args ->
            when (method.name) {
                "updateTag" -> { saved += args[0] as Tag; 1 }
                "addTag" -> { saved += args[0] as Tag; 91L }
                else -> error("Unexpected repository call: $method")
            }
        } as TransactionRepository
        vm = AddTagViewModel(NoOpAnalyticsService(), AddTagUseCase(repository), UpdateTagUseCase(repository))
    }

    @AfterTest fun cleanup() {
        vm.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test fun editingReorderedRecordKeepsItsPosition() = runTest(dispatcher) {
        vm.onIntent(AddTagIntent.StartEdit(Tag(id = 17, name = "Before", description = "Original", position = 7, colorId = 2, iconId = 3)))
        vm.onIntent(AddTagIntent.UpdateName("After"))
        vm.onIntent(AddTagIntent.UpdateDescription("Changed"))
        vm.onIntent(AddTagIntent.Save)
        advanceUntilIdle()
        val actual = saved.single()
        assertEquals(17L, actual.id)
        assertEquals("After", actual.name)
        assertEquals("Changed", actual.description)
        assertEquals(7, actual.position)
        assertEquals(2, actual.colorId)
        assertEquals(3, actual.iconId)
    }

    @Test fun newRecordDoesNotInheritLastEditedPosition() = runTest(dispatcher) {
        vm.onIntent(AddTagIntent.StartEdit(Tag(id = 17, name = "Before", position = 7, colorId = 2, iconId = 3)))
        vm.onIntent(AddTagIntent.StartAdd)
        vm.onIntent(AddTagIntent.UpdateName("New"))
        vm.onIntent(AddTagIntent.Save)
        advanceUntilIdle()
        assertNull(saved.single().id)
        assertEquals(0, saved.single().position)
    }
}
