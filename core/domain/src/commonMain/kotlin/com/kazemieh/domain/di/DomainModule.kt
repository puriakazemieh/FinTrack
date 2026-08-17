package com.kazemieh.domain.di

import com.kazemieh.domain.usecase.*
import org.koin.dsl.module

val domainModule = module {
    factory { AddTransactionUseCase(get(), get(), get(), get(),get(),get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { UpdateTransactionUseCase(get()) }
    factory { ObserveTransactionsUseCase(get()) }
    factory { ObserveCategorySumsUseCase(get()) }
    factory { ObserveSpendingPatternUseCase(get()) }
    factory { DetectSubscriptionsUseCase(get()) }
    factory { GetMonthlyTrendUseCase(get()) }
    factory { GetMonthlyCashflowUseCase(get()) }
    factory { GetFinancialSummaryUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { GenerateAiInsightUseCase(get()) }
    factory { AiConfigUseCase(get()) }
    factory { ObserveTagsUseCase(get()) }
    factory { ObserveSourcesUseCase(get()) }
    factory { ObserveCategoriesUseCase(get()) }
    factory { ObserveCategoriesFlatUseCase(get()) }
    factory { GetCategoryUseCase(get()) }
    factory { AddCategoryUseCase(get()) }
    factory { UpdateCategoryUseCase(get()) }
    factory { UpdateTagUseCase(get()) }
    factory { DeleteCategoryUseCase(get()) }
    factory { DeletePersonUseCase(get()) }
    factory { DeleteTagUseCase(get()) }
    factory { AddSourceUseCase(get()) }
    factory { AddTagUseCase(get()) }
    factory { GetDefaultCategoryUseCase(get()) }
    factory { GetTransferCategoryUseCase(get()) }
    factory { GetDefaultFinancialSourceUseCase(get()) }
    factory { AddPersonUseCase(get()) }
    factory { ObservePersonsUseCase(get()) }
    factory { DeleteSourceUseCase(get()) }
    factory { UpdateSourceUseCase(get()) }
    factory { ObserveSourceUseCase(get()) }
    factory { UpdatePersonUseCase(get()) }
    factory { ObserveMostUsedCategoriesUseCase(get()) }
    factory { ObserveMostUsedSourcesUseCase(get()) }
    factory { ObserveMostUsedTagsUseCase(get()) }
    factory { ObserveMostUsedPersonsUseCase(get()) }
    factory { UpdateCategoryPositionsUseCase(get()) }
    factory { UpdateSourcePositionsUseCase(get()) }
    factory { UpdateTagPositionsUseCase(get()) }
    factory { UpdatePersonPositionsUseCase(get()) }
    factory { GetTransactionAmountRangeUseCase(get()) }
    factory { GetSourceByIdentifierUseCase(get()) }
    factory { SeedDataUseCase(get(), get()) }

    factory { ObserveStreakUseCase(get()) }
    factory { ObserveAchievementsUseCase(get()) }
    factory { UpdateStreakUseCase(get()) }
    factory { UpdateXPUseCase(get()) }
    factory { CheckAchievementsUseCase(get(), get(), get(), get()) }

    factory { ObserveBudgetsWithProgressUseCase(get()) }
    factory { AddBudgetUseCase(get()) }
    factory { UpdateBudgetUseCase(get()) }
    factory { DeleteBudgetUseCase(get()) }

    factory { ObserveGoalsUseCase(get()) }
    factory { AddGoalUseCase(get()) }
    factory { UpdateGoalUseCase(get()) }
    factory { DeleteGoalUseCase(get()) }
    factory { GetGoalByIdUseCase(get()) }
    factory { CalculateFreedomStageUseCase(get(), get(), get()) }

    factory { ObserveGoalTemplatesUseCase(get()) }
    factory { AddGoalTemplateUseCase(get()) }
    factory { UpdateGoalTemplateUseCase(get()) }
    factory { DeleteGoalTemplateUseCase(get()) }

    factory { ObserveGoalBasketsUseCase(get()) }
    factory { AddGoalBasketUseCase(get()) }
    factory { UpdateGoalBasketUseCase(get()) }
    factory { DeleteGoalBasketUseCase(get()) }

    factory { AddInstallmentUseCase(get(), get()) }
    factory { ObserveInstallmentsUseCase(get()) }
    factory { DeleteInstallmentUseCase(get(), get()) }
    factory { MarkInstallmentAsPaidUseCase(get(), get(), get()) }
    factory { GetInstallmentUseCase(get()) }
    factory { UpdateInstallmentUseCase(get(), get()) }
    factory { GetBudgetByCategoryIdUseCase(get()) }
    factory { GetBudgetSpentAmountUseCase(get()) }
    factory { HasAnyBudgetsUseCase(get()) }

    factory { AddDebtUseCase(get(), get()) }
    factory { UpdateDebtUseCase(get(), get()) }
    factory { DeleteDebtUseCase(get()) }
    factory { ObserveDebtsUseCase(get()) }
    factory { ObserveDebtsByPersonUseCase(get()) }
    factory { SettleDebtUseCase(get(), get()) }
    factory { GetDebtWithRelationsUseCase(get()) }

    factory { AddCheckUseCase(get(), get()) }
    factory { UpdateCheckUseCase(get()) }
    factory { DeleteCheckUseCase(get(), get()) }
    factory { GetCheckByIdUseCase(get()) }
    factory { ObserveAllChecksUseCase(get()) }
    factory { ObserveChecksByStatusUseCase(get()) }

    factory { AddFixedExpenseUseCase(get(), get()) }
    factory { UpdateFixedExpenseUseCase(get()) }
    factory { DeleteFixedExpenseUseCase(get(), get()) }
    factory { GetFixedExpenseByIdUseCase(get()) }
    factory { GetDebtByIdUseCase(get()) }
    factory { GetPersonByIdUseCase(get()) }
    factory { GetSourceByIdUseCase(get()) }
    factory { ObserveAllFixedExpensesUseCase(get()) }
    factory { ObserveFixedExpensesFilteredUseCase(get()) }
    factory { UpdateNextDueDateUseCase(get()) }
    factory { PostFixedExpenseAsTransactionUseCase(get(), get(), get(), get()) }
    factory { GetTagByIdUseCase(get()) }

    factory { GetBooleanPreferenceUseCase(get()) }
    factory { SetBooleanPreferenceUseCase(get()) }
    factory { GetStringPreferenceUseCase(get()) }
    factory { SetStringPreferenceUseCase(get()) }
    factory { GetStringFlowUseCase(get()) }
    factory { ClearPreferencesUseCase(get()) }

    factory { SearchCategoriesUseCase(get()) }
    factory { SearchSourcesUseCase(get()) }
    factory { SearchPersonsUseCase(get()) }
    factory { SearchTagsUseCase(get()) }
    factory { GetRecentSearchesUseCase(get()) }
    factory { SaveRecentSearchUseCase(get()) }
    factory { DeleteRecentSearchUseCase(get()) }

    factory { AddAssetUseCase(get()) }
    factory { DeleteAssetUseCase(get()) }
    factory { ObserveAssetHistoryUseCase(get()) }
    factory { ObserveAssetsUseCase(get()) }
    factory { SyncAssetRatesUseCase(get()) }
    factory { UpdateAssetUseCase(get()) }

    single {
        AssetUseCases(
            observeAssets = get(),
            addAsset = get(),
            updateAsset = get(),
            deleteAsset = get(),
            syncAssetRates = get(),
            observeAssetHistory = get()
        )
    }


    single {
        SourceUseCases(
            updateSourceUseCase = get(),
            addSource = get(),
            observeSourceUseCase = get()
        )
    }
    single {
        PreferenceUseCases(
            getBooleanPreference = get(),
            setBooleanPreference = get(),
            getStringPreference = get(),
            setStringPreference = get(),
            getStringFlow = get(),
            clearPreferences = get()
        )
    }
    single {
        InstallmentUseCaseGroup(
            addInstallmentUseCase = get(),
            observeInstallmentsUseCase = get(),
            markInstallmentAsPaidUseCase = get(),
            deleteInstallmentUseCase = get(),
            getInstallmentUseCase = get(),
            updateInstallmentUseCase = get()
        )
    }
    single {
        DebtUseCaseGroup(
            addDebtUseCase = get(),
            updateDebtUseCase = get(),
            deleteDebtUseCase = get(),
            observeDebtsUseCase = get(),
            observeDebtsByPersonUseCase = get(),
            settleDebtUseCase = get(),
            getDebtByIdUseCase = get(),
            getDebtWithRelationsUseCase = get(),
            getPersonByIdUseCase = get(),
            getSourceByIdUseCase = get()
        )
    }
    single {
        CheckUseCaseGroup(
            addCheckUseCase = get(),
            updateCheckUseCase = get(),
            deleteCheckUseCase = get(),
            getCheckByIdUseCase = get(),
            observeAllChecksUseCase = get(),
            observeChecksByStatusUseCase = get()
        )
    }
    single {
        FixedExpenseUseCaseGroup(
            addFixedExpenseUseCase = get(),
            updateFixedExpenseUseCase = get(),
            deleteFixedExpenseUseCase = get(),
            getFixedExpenseByIdUseCase = get(),
            observeAllFixedExpensesUseCase = get(),
            observeFixedExpensesFilteredUseCase = get(),
            updateNextDueDateUseCase = get(),
            postFixedExpenseAsTransactionUseCase = get()
        )
    }
    single {
        com.kazemieh.domain.usecase.GoalUseCases(
            observeGoals = get(),
            addGoal = get(),
            updateGoal = get(),
            deleteGoal = get(),
            getGoalById = get(),
            calculateFreedomStage = get(),
            observeGoalTemplates = get(),
            addGoalTemplate = get(),
            updateGoalTemplate = get(),
            deleteGoalTemplate = get(),
            observeGoalBaskets = get(),
            addGoalBasket = get(),
            updateGoalBasket = get(),
            deleteGoalBasket = get()
        )
    }
    single {
        TransactionUseCaseGroup(
            addTransactionUseCase = get(),
            deleteTransactionUseCase = get(),
            getDefaultCategoryUseCase = get(),
            getTransferCategoryUseCase = get(),
            getDefaultFinancialSourceUseCase = get(),
            observeTransactionsUseCase = get(),
            updateTransactionUseCase = get(),
            observeCategorySumsUseCase = get(),
            observeSourcesUseCase = get(),
            observeMostUsedCategoriesUseCase = get(),
            observeMostUsedSourcesUseCase = get(),
            observeMostUsedTagsUseCase = get(),
            observeMostUsedPersonsUseCase = get(),
            getCategoryUseCase = get(),
            observeTagsUseCase = get(),
            observePersonsUseCase = get(),
            observeCategoriesUseCase = get(),
            getTransactionAmountRangeUseCase = get(),
            getSourceByIdentifierUseCase = get(),
            observeSourceUseCase = get()
        )
    }

}
