package com.kazemieh.tag.ui.add

import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.common.model.Tag
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.AddTagUseCase
import com.kazemieh.domain.usecase.UpdateTagUseCase
import java.lang.reflect.Proxy
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TagSaveIdentityTest {
    private val dispatcher = StandardTestDispatcher()
    private val saved = mutableListOf<Tag>()
    private var updateResult = 1
    private var insertResult = 91L
    private lateinit var vm: AddTagViewModel

    @BeforeTest fun setup() {
        Dispatchers.setMain(dispatcher)
        val repository = Proxy.newProxyInstance(TransactionRepository::class.java.classLoader,
            arrayOf(TransactionRepository::class.java)) { _, method, args ->
            when (method.name) {
                "updateTag" -> { saved += args[0] as Tag; updateResult }
                "addTag" -> { saved += args[0] as Tag; insertResult }
                else -> error("Unexpected repository call: $method")
            }
        } as TransactionRepository
        vm = AddTagViewModel(NoOpAnalyticsService(), AddTagUseCase(repository), UpdateTagUseCase(repository))
    }

    @AfterTest fun cleanup() {
        vm.viewModelScope.cancel()
        Dispatchers.resetMain()
    }


    @Test fun editReportsOriginalIdentityInsteadOfAffectedRowCount() = runTest(dispatcher) {
        val result = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onIntent(AddTagIntent.StartEdit(Tag(id = 17, name = "Edited", position = 7, colorId = 2, iconId = 3)))
        vm.onIntent(AddTagIntent.Save)
        advanceUntilIdle()
        assertEquals(17L, (result.await() as AddTagEffect.SavedTag).tag.id)
        assertEquals(7, saved.single().position)
    }

    @Test fun zeroUpdatedRowsKeepDraftAndDoNotReportSuccess() = runTest(dispatcher) {
        updateResult = 0
        val result = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onIntent(AddTagIntent.StartEdit(Tag(id = 17, name = "Keep draft", colorId = 2, iconId = 3)))
        vm.onIntent(AddTagIntent.Save)
        advanceUntilIdle()
        assertFalse(result.isCompleted)
        assertEquals(AddTagMode.Edit(17), vm.state.value.mode)
        assertEquals("Keep draft", vm.state.value.draft.name)
    }

    @Test fun insertReportsGeneratedIdentity() = runTest(dispatcher) {
        val result = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onIntent(AddTagIntent.StartAdd)
        vm.onIntent(AddTagIntent.UpdateName("New"))
        vm.onIntent(AddTagIntent.Save)
        advanceUntilIdle()
        assertEquals(91L, (result.await() as AddTagEffect.SavedTag).tag.id)
    }

    @Test fun zeroInsertIdentityKeepsDraftAndDoesNotReportSuccess() = runTest(dispatcher) {
        insertResult = 0
        val result = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) { vm.effect.first() }
        vm.onIntent(AddTagIntent.StartAdd)
        vm.onIntent(AddTagIntent.UpdateName("Keep draft"))
        vm.onIntent(AddTagIntent.Save)
        advanceUntilIdle()
        assertFalse(result.isCompleted)
        assertEquals("Keep draft", vm.state.value.draft.name)
    }
}
