package com.kazemieh.person.ui.add

import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.common.model.Person
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.AddPersonUseCase
import com.kazemieh.domain.usecase.UpdatePersonUseCase
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PersonEditPositionTest {
    private val dispatcher = StandardTestDispatcher()
    private val saved = mutableListOf<Person>()
    private lateinit var vm: AddPersonViewModel

    @BeforeTest fun setup() {
        Dispatchers.setMain(dispatcher)
        val repository = Proxy.newProxyInstance(TransactionRepository::class.java.classLoader,
            arrayOf(TransactionRepository::class.java)) { _, method, args ->
            when (method.name) {
                "updatePerson" -> { saved += args[0] as Person; 1 }
                "addPerson" -> { saved += args[0] as Person; 91L }
                else -> error("Unexpected repository call: $method")
            }
        } as TransactionRepository
        vm = AddPersonViewModel(NoOpAnalyticsService(), AddPersonUseCase(repository), UpdatePersonUseCase(repository))
    }

    @AfterTest fun cleanup() {
        vm.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test fun editingReorderedRecordKeepsItsPosition() = runTest(dispatcher) {
        vm.onIntent(AddPersonIntent.StartEdit(Person(id = 17, name = "Before", description = "Original", position = 7)))
        vm.onIntent(AddPersonIntent.UpdateName("After"))
        vm.onIntent(AddPersonIntent.UpdateDescription("Changed"))
        vm.onIntent(AddPersonIntent.Save)
        advanceUntilIdle()
        val actual = saved.single()
        assertEquals(17L, actual.id)
        assertEquals("After", actual.name)
        assertEquals("Changed", actual.description)
        assertEquals(7, actual.position)

    }

    @Test fun newRecordDoesNotInheritLastEditedPosition() = runTest(dispatcher) {
        vm.onIntent(AddPersonIntent.StartEdit(Person(id = 17, name = "Before", position = 7)))
        vm.onIntent(AddPersonIntent.StartAdd)
        vm.onIntent(AddPersonIntent.UpdateName("New"))
        vm.onIntent(AddPersonIntent.Save)
        advanceUntilIdle()
        assertNull(saved.single().id)
        assertEquals(0, saved.single().position)
    }
}
