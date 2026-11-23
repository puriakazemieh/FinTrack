package com.kazemieh.financialsource.ui.list

import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.common.model.Source
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.*
import com.kazemieh.money.Currency
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SourceCurrencyBalanceTest {
    private fun source(id: Long, amount: Long, currency: String) =
        Source(id = id, name = "Source $id", balance = amount, currencyCode = currency, colorId = 0, iconId = 0)

    @Test fun unrelatedCurrenciesNeverShareATotal() {
        val result = sourceCurrencyBalances(listOf(source(1, -100, "IRT"), source(2, 40, "IRT"),
            source(3, 900, "IRR"), source(4, 70, "USD"), source(5, -70, "USD")))
        assertEquals(mapOf("IRR" to 900L, "IRT" to -60L, "USD" to 0L), result.associate { it.currency.code to it.balance })
    }
    @Test fun legacySerializedCurrencyJoinsItsCodeButUnknownCodeStaysSeparate() {
        val result = sourceCurrencyBalances(listOf(source(1, 100, "IRT"),
            source(2, 200, Json.encodeToString(Currency.serializer(), Currency.TOMAN)), source(3, 500, "QA_UNKNOWN")))
        assertEquals(mapOf("IRT" to 300L, "QA_UNKNOWN" to 500L), result.associate { it.currency.code to it.balance })
        assertEquals("QA_UNKNOWN", result.last().currency.symbol)
    }
    @Test fun overflowNeverWrapsIntoAFalseFinancialAmount() {
        assertNull(sourceCurrencyBalances(listOf(source(1, Long.MAX_VALUE, "IRT"), source(2, 1, "IRT"))).single().balance)
        assertNull(sourceCurrencyBalances(listOf(source(1, Long.MIN_VALUE, "IRT"), source(2, -1, "IRT"))).single().balance)
        assertTrue(sourceCurrencyBalances(emptyList()).isEmpty())
    }
    @Test fun cancellationDoesNotReportAFalseOverflow() {
        assertEquals(Long.MAX_VALUE, sourceCurrencyBalances(listOf(source(1, Long.MAX_VALUE, "IRT"),
            source(2, 1, "IRT"), source(3, -1, "IRT"))).single().balance)
        assertEquals(Long.MIN_VALUE, sourceCurrencyBalances(listOf(source(1, Long.MIN_VALUE, "IRT"),
            source(2, -1, "IRT"), source(3, 1, "IRT"))).single().balance)
    }
    @Test fun signedFormattingHandlesMinimumLongAndPrivacyIncludingZero() {
        val minimum = SourceCurrencyBalance(Currency.TOMAN, Long.MIN_VALUE).formattedBalance(false)!!
        assertTrue(minimum.startsWith("\u200E- "))
        assertEquals(1, minimum.count { it == '-' })
        assertFalse(minimum.contains(",-"))
        for (value in listOf<Long?>(Long.MIN_VALUE, -1L, 0L, 1L, null))
            assertEquals("••••••", SourceCurrencyBalance(Currency.TOMAN, value).formattedBalance(true))
    }
    @Test fun searchKeepsAccountWideTotalsAndNewBalancesAreObserved() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val rows = MutableStateFlow(listOf(source(1, 100, "IRT"), source(2, 200, "USD")))
        val repository = Proxy.newProxyInstance(TransactionRepository::class.java.classLoader,
            arrayOf(TransactionRepository::class.java)) { _, method, _ ->
            when (method.name) { "observeSources" -> rows; else -> error("Unexpected call: $method") }
        } as TransactionRepository
        val vm = SourceViewModel(NoOpAnalyticsService(), ObserveSourcesUseCase(repository),
            UpdateSourcePositionsUseCase(repository), SetDefaultSourceUseCase(repository))
        try {
            vm.onIntent(SourceIntent.LoadAllSource); advanceUntilIdle()
            vm.onIntent(SourceIntent.UpdateSearchQuery("Source 1")); advanceUntilIdle()
            assertEquals(listOf(1L), vm.state.value.filteredSources.map { it.id })
            assertEquals(2, vm.state.value.balancesByCurrency.size)
            rows.value = listOf(source(1, -300, "IRT"), source(2, 200, "USD")); advanceUntilIdle()
            assertEquals(-300L, vm.state.value.balancesByCurrency.single { it.currency.code == "IRT" }.balance)
            assertEquals("Source 1", vm.state.value.searchQuery)
        } finally { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
    }
}
