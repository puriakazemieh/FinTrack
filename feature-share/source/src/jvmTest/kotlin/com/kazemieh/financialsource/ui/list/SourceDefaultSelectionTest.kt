package com.kazemieh.financialsource.ui.list

import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.common.model.Source
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.*
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SourceDefaultSelectionTest {
    private val dispatcher = StandardTestDispatcher()
    private val previous = Source(id = 1, name = "Former", balance = 1000, isDefault = true, colorId = 0, iconId = 0)
    private val selected = Source(id = 2, name = "Selected", balance = 2000, colorId = 0, iconId = 0)
    private val live = linkedMapOf(1L to previous, 2L to selected)
    private val writes = mutableListOf<Pair<String, Long?>>()
    private lateinit var vm: SourceViewModel

    @BeforeTest fun setup() {
        Dispatchers.setMain(dispatcher)
        val repository = Proxy.newProxyInstance(TransactionRepository::class.java.classLoader,
            arrayOf(TransactionRepository::class.java)) { _, method, args ->
            when (method.name) {
                "observeSources" -> flowOf(listOf(previous, selected))
                "updateSource" -> {
                    val source = args[0] as Source
                    writes += method.name to source.id
                    source.id?.let { live[it] = source }
                    1
                }
                "setDefaultSource" -> {
                    val id = args[0] as Long
                    writes += method.name to id
                    if (id in live) {
                        live.keys.toList().forEach { live[it] = live.getValue(it).copy(isDefault = it == id) }
                        true
                    } else false
                }
                else -> error("Unexpected repository call: $method")
            }
        } as TransactionRepository
        vm = SourceViewModel(NoOpAnalyticsService(), ObserveSourcesUseCase(repository),
            UpdateSourcePositionsUseCase(repository), SetDefaultSourceUseCase(repository))
    }
    @AfterTest fun cleanup() { vm.viewModelScope.cancel(); Dispatchers.resetMain() }

    @Test fun staleListSelectionCannotOverwriteEitherBalance() = runTest(dispatcher) {
        vm.onIntent(SourceIntent.LoadAllSource)
        advanceUntilIdle()
        // Independent transactions committed after the list's snapshots were rendered.
        live[1] = previous.copy(balance = 700)
        live[2] = selected.copy(balance = 1500)
        vm.onIntent(SourceIntent.SetDefaultSource(selected))
        advanceUntilIdle()
        assertEquals(700L, live.getValue(1).balance)
        assertEquals(1500L, live.getValue(2).balance)
        assertEquals(listOf<Pair<String, Long?>>("setDefaultSource" to 2L), writes)
        assertEquals(listOf(2L), live.filterValues { it.isDefault }.keys.toList())
    }
    @Test fun transientSourceWithoutIdentityLeavesCurrentDefaultUntouched() = runTest(dispatcher) {
        vm.onIntent(SourceIntent.LoadAllSource)
        advanceUntilIdle()
        vm.onIntent(SourceIntent.SetDefaultSource(selected.copy(id = null)))
        advanceUntilIdle()
        assertTrue(writes.isEmpty())
        assertTrue(live.getValue(1).isDefault)
    }
}
