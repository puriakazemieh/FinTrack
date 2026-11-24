package com.kazemieh.person.ui.add

import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.common.model.Person
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.AddPersonUseCase
import com.kazemieh.domain.usecase.UpdatePersonUseCase
import java.lang.reflect.Proxy
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PersonSaveIdentityTest {
    private val dispatcher = StandardTestDispatcher()
    private val saved = mutableListOf<Person>()
    private var updateResult = 1
    private var insertResult = 91L
    private lateinit var vm: AddPersonViewModel

    @BeforeTest fun setup() {
        Dispatchers.setMain(dispatcher)
        val repository = Proxy.newProxyInstance(TransactionRepository::class.java.classLoader,
            arrayOf(TransactionRepository::class.java)) { _, method, args ->
            when (method.name) {
                "updatePerson" -> { saved += args[0] as Person; updateResult }
                "addPerson" -> { saved += args[0] as Person; insertResult }
                else -> error("Unexpected repository call: $method")
            }
        } as TransactionRepository
        vm = AddPersonViewModel(NoOpAnalyticsService(), AddPersonUseCase(repository), UpdatePersonUseCase(repository))
    }

    @AfterTest fun cleanup() {
        vm.viewModelScope.cancel()
        Dispatchers.resetMain()
    }


    @Test fun editReportsOriginalIdentityInsteadOfAffectedRowCount() = runTest(dispatcher) {
        val result = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onIntent(AddPersonIntent.StartEdit(Person(id = 17, name = "Edited", position = 7)))
        vm.onIntent(AddPersonIntent.Save)
        advanceUntilIdle()
        assertEquals(17L, (result.await() as AddPersonEffect.SavedPerson).person.id)
        assertEquals(7, saved.single().position)
    }

    @Test fun zeroUpdatedRowsKeepDraftAndDoNotReportSuccess() = runTest(dispatcher) {
        updateResult = 0
        val result = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onIntent(AddPersonIntent.StartEdit(Person(id = 17, name = "Keep draft")))
        vm.onIntent(AddPersonIntent.Save)
        advanceUntilIdle()
        assertFalse(result.isCompleted)
        assertEquals(AddPersonMode.Edit(17), vm.state.value.mode)
        assertEquals("Keep draft", vm.state.value.draft.name)
    }

    @Test fun insertReportsGeneratedIdentity() = runTest(dispatcher) {
        val result = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onIntent(AddPersonIntent.StartAdd)
        vm.onIntent(AddPersonIntent.UpdateName("New"))
        vm.onIntent(AddPersonIntent.Save)
        advanceUntilIdle()
        assertEquals(91L, (result.await() as AddPersonEffect.SavedPerson).person.id)
    }

    @Test fun zeroInsertIdentityKeepsDraftAndDoesNotReportSuccess() = runTest(dispatcher) {
        insertResult = 0
        val result = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onIntent(AddPersonIntent.StartAdd)
        vm.onIntent(AddPersonIntent.UpdateName("Keep draft"))
        vm.onIntent(AddPersonIntent.Save)
        advanceUntilIdle()
        assertFalse(result.isCompleted)
        assertEquals("Keep draft", vm.state.value.draft.name)
    }
}
