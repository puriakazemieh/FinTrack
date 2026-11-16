package com.kazemieh.transaction.ui.add

import androidx.lifecycle.viewModelScope
import com.kazemieh.common.ImageStorage
import com.kazemieh.common.analytics.NoOpAnalyticsService
import com.kazemieh.common.model.*
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.domain.repository.*
import com.kazemieh.domain.usecase.*
import java.lang.reflect.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SmsSourceSelectionTest {
    private val dispatcher = StandardTestDispatcher()
    private val a = Source(id = 1, name = "بلو شخصی", cardNumber = "6219861812341111", colorId = 0, iconId = 0)
    private val b = a.copy(id = 2, name = "بلو کاری", cardNumber = "6219861912342222")
    private var sources = listOf(a, b)
    private val draft = SmsDraft(id = 7, sender = "bank", body = "برداشت 500,000 ریال",
        amount = 500_000, bankName = "Blu", type = TransactionType.EXPENSE, timeStamp = 1, date = "1405/08/25")
    private lateinit var vm: AddTransactionViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        val transactions = proxy<TransactionRepository> { name, args -> when (name) {
            "observeMostUsedCategories", "observeMostUsedSources", "observeMostUsedTags", "observeMostUsedPersons" -> flowOf(emptyList<Any>())
            "observeSources" -> flowOf(sources)
            "getDefaultSource" -> a
            "observeSource" -> flowOf(sources.find { it.id == args[0] })
            "getSourceByIdentifier" -> sources.firstOrNull { it.cardNumber?.endsWith(args[0] as String) == true }
            "getDefaultCategory" -> Category(id = 1, name = "Food", type = TransactionType.EXPENSE, colorId = 0, iconId = 0)
            "addTransactionWithBalance" -> error("An unsafe SMS must not reach financial storage")
            else -> error("Unexpected repository call: $name")
        } }
        val achievements = proxy<AchievementRepository>()
        val budgets = proxy<BudgetRepository>()
        val xp = UpdateXPUseCase(achievements)
        val group = TransactionUseCaseGroup(
            AddTransactionUseCase(transactions, budgets, proxy<NotificationManager>(), UpdateStreakUseCase(achievements),
                CheckAchievementsUseCase(achievements, transactions, budgets, xp), xp),
            GetDefaultCategoryUseCase(transactions), GetTransferCategoryUseCase(transactions),
            GetDefaultFinancialSourceUseCase(transactions), DeleteTransactionUseCase(transactions),
            UpdateTransactionUseCase(transactions), ObserveTransactionsUseCase(transactions),
            ObserveCategorySumsUseCase(transactions), ObserveSourcesUseCase(transactions),
            ObserveMostUsedCategoriesUseCase(transactions), ObserveMostUsedSourcesUseCase(transactions),
            ObserveMostUsedTagsUseCase(transactions), ObserveMostUsedPersonsUseCase(transactions),
            GetCategoryUseCase(transactions), ObserveTagsUseCase(transactions), ObservePersonsUseCase(transactions),
            ObserveCategoriesUseCase(transactions), GetTransactionAmountRangeUseCase(transactions),
            GetSourceByIdentifierUseCase(transactions), ObserveSourceUseCase(transactions)
        )
        val preferences = proxy<PreferenceRepository> { name, _ -> when (name) {
            "getString" -> "IRT"
            "getBoolean" -> false
            else -> error("Unexpected preference call: $name")
        } }
        val preferenceCases = PreferenceUseCases(GetBooleanPreferenceUseCase(preferences), SetBooleanPreferenceUseCase(preferences),
            GetStringPreferenceUseCase(preferences), SetStringPreferenceUseCase(preferences), GetStringFlowUseCase(preferences),
            ClearPreferencesUseCase(preferences))
        val sms = proxy<SmsDraftRepository> { name, _ -> when (name) {
            "observeUnusedSmsDrafts" -> flowOf(emptyList<SmsDraft>())
            else -> error("Unexpected SMS write: $name")
        } }
        vm = AddTransactionViewModel(NoOpAnalyticsService(), group, proxy<ImageStorage>(), sms,
            proxy<GoalRepository>(), preferenceCases)
    }

    @AfterTest
    fun cleanup() {
        vm.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun ambiguousBankDoesNotFallBackToDefaultAccount() = runTest(dispatcher) {
        vm.onIntent(AddTransactionIntent.FetchDefaultData(null, draft))
        runCurrent()
        assertNull(vm.state.value.source)
        assertEquals("50000", vm.state.value.amount)
    }

    @Test
    fun cardIdentifierSelectsTheSecondAccount() = runTest(dispatcher) {
        vm.onIntent(AddTransactionIntent.FetchDefaultData(null, draft.copy(sourceIdentifier = "2222")))
        runCurrent()
        assertEquals(b, vm.state.value.source)
    }

    @Test
    fun deletedSelectedSourceDoesNotFallBackToDefault() = runTest(dispatcher) {
        vm.onIntent(AddTransactionIntent.FetchDefaultData(null, draft.copy(sourceId = 99)))
        runCurrent()
        assertNull(vm.state.value.source)
    }

    @Test
    fun selectionMadeDuringLoadingIsPreserved() = runTest(dispatcher) {
        vm.onIntent(AddTransactionIntent.FetchDefaultData(null, draft))
        vm.onIntent(AddTransactionIntent.SetSource(b))
        runCurrent()
        assertEquals(b, vm.state.value.source)
    }

    @Test
    fun foreignSourceCannotPostRialSmsAsForeignMoney() = runTest(dispatcher) {
        val foreign = a.copy(currencyCode = "USD")
        sources = listOf(foreign)
        vm.onIntent(AddTransactionIntent.FetchDefaultData(null, draft.copy(sourceId = 1)))
        runCurrent()
        vm.onIntent(AddTransactionIntent.Submit)
        runCurrent()
        assertTrue(vm.state.value.isSourceError)
    }

    private inline fun <reified T> proxy(crossinline call: (String, Array<out Any?>) -> Any? = { name, _ ->
        error("Unexpected dependency call: $name")
    }): T = Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
        call(method.name, args ?: emptyArray())
    } as T
}
