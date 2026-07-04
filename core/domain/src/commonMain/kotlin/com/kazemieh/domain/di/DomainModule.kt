package com.kazemieh.domain.di

import com.kazemieh.domain.usecase.AddBudgetUseCase
import com.kazemieh.domain.usecase.AddCategoryUseCase
import com.kazemieh.domain.usecase.AddCheckUseCase
import com.kazemieh.domain.usecase.AddDebtUseCase
import com.kazemieh.domain.usecase.AddFixedExpenseUseCase
import com.kazemieh.domain.usecase.AddInstallmentUseCase
import com.kazemieh.domain.usecase.AddPersonUseCase
import com.kazemieh.domain.usecase.AddSourceUseCase
import com.kazemieh.domain.usecase.AddTagUseCase
import com.kazemieh.domain.usecase.AddTransactionUseCase
import com.kazemieh.domain.usecase.CheckAchievementsUseCase
import com.kazemieh.domain.usecase.CheckUseCaseGroup
import com.kazemieh.domain.usecase.DeleteBudgetUseCase
import com.kazemieh.domain.usecase.DeleteCategoryUseCase
import com.kazemieh.domain.usecase.DeleteCheckUseCase
import com.kazemieh.domain.usecase.DeleteDebtUseCase
import com.kazemieh.domain.usecase.DeleteFixedExpenseUseCase
import com.kazemieh.domain.usecase.DeleteInstallmentUseCase
import com.kazemieh.domain.usecase.DeletePersonUseCase
import com.kazemieh.domain.usecase.DeleteRecentSearchUseCase
import com.kazemieh.domain.usecase.DeleteSourceUseCase
import com.kazemieh.domain.usecase.DeleteTagUseCase
import com.kazemieh.domain.usecase.DeleteTransactionUseCase
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import com.kazemieh.domain.usecase.GetBooleanPreferenceUseCase
import com.kazemieh.domain.usecase.GetCheckByIdUseCase
import com.kazemieh.domain.usecase.GetDebtByIdUseCase
import com.kazemieh.domain.usecase.GetFixedExpenseByIdUseCase
import com.kazemieh.domain.usecase.GetPersonByIdUseCase
import com.kazemieh.domain.usecase.GetSourceByIdUseCase
import com.kazemieh.domain.usecase.GetStringFlowUseCase
import com.kazemieh.domain.usecase.GetStringPreferenceUseCase
import com.kazemieh.domain.usecase.GetCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultFinancialSourceUseCase
import com.kazemieh.domain.usecase.GetGoalByIdUseCase
import com.kazemieh.domain.usecase.GetRecentSearchesUseCase
import com.kazemieh.domain.usecase.GetSourceByIdentifierUseCase
import com.kazemieh.domain.usecase.GetTransferCategoryUseCase
import com.kazemieh.domain.usecase.GetTransactionAmountRangeUseCase
import com.kazemieh.domain.usecase.DebtUseCaseGroup
import com.kazemieh.domain.usecase.InstallmentUseCaseGroup
import com.kazemieh.domain.usecase.GetInstallmentByIdUseCase
import com.kazemieh.domain.usecase.UpdateInstallmentUseCase
import com.kazemieh.domain.usecase.MarkInstallmentAsPaidUseCase
import com.kazemieh.domain.usecase.ObserveGoalsUseCase
import com.kazemieh.domain.usecase.ObserveAllChecksUseCase
import com.kazemieh.domain.usecase.ObserveAllFixedExpensesUseCase
import com.kazemieh.domain.usecase.ObserveChecksByStatusUseCase
import com.kazemieh.domain.usecase.ObserveDebtsByPersonUseCase
import com.kazemieh.domain.usecase.ObserveDebtsUseCase
import com.kazemieh.domain.usecase.ObserveBudgetsWithProgressUseCase
import com.kazemieh.domain.usecase.ObserveCategoriesFlatUseCase
import com.kazemieh.domain.usecase.ObserveCategoriesUseCase
import com.kazemieh.domain.usecase.ObserveSpendingPatternUseCase
import com.kazemieh.domain.usecase.ObserveCategorySumsUseCase
import com.kazemieh.domain.usecase.ObserveInstallmentsUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedCategoriesUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedPersonsUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedSourcesUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedTagsUseCase
import com.kazemieh.domain.usecase.ObservePersonsUseCase
import com.kazemieh.domain.usecase.ObserveSourceUseCase
import com.kazemieh.domain.usecase.ObserveStreakUseCase
import com.kazemieh.domain.usecase.ObserveSourcesUseCase
import com.kazemieh.domain.usecase.ObserveTagsUseCase
import com.kazemieh.domain.usecase.ObserveTransactionsUseCase
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.domain.usecase.SaveRecentSearchUseCase
import com.kazemieh.domain.usecase.SearchCategoriesUseCase
import com.kazemieh.domain.usecase.SearchPersonsUseCase
import com.kazemieh.domain.usecase.SearchSourcesUseCase
import com.kazemieh.domain.usecase.SearchTagsUseCase
import com.kazemieh.domain.usecase.SeedDataUseCase
import com.kazemieh.domain.usecase.SettleDebtUseCase
import com.kazemieh.domain.usecase.SetBooleanPreferenceUseCase
import com.kazemieh.domain.usecase.SetStringPreferenceUseCase
import com.kazemieh.domain.usecase.SourceUseCases
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import com.kazemieh.domain.usecase.UpdateCategoryPositionsUseCase
import com.kazemieh.domain.usecase.UpdateCategoryUseCase
import com.kazemieh.domain.usecase.UpdateCheckUseCase
import com.kazemieh.domain.usecase.UpdateDebtUseCase
import com.kazemieh.domain.usecase.UpdateFixedExpenseUseCase
import com.kazemieh.domain.usecase.UpdateNextDueDateUseCase
import com.kazemieh.domain.usecase.UpdatePersonPositionsUseCase
import com.kazemieh.domain.usecase.UpdatePersonUseCase
import com.kazemieh.domain.usecase.UpdateSourcePositionsUseCase
import com.kazemieh.domain.usecase.UpdateSourceUseCase
import com.kazemieh.domain.usecase.UpdateXPUseCase
import com.kazemieh.domain.usecase.UpdateGoalUseCase
import com.kazemieh.domain.usecase.UpdateStreakUseCase
import com.kazemieh.domain.usecase.UpdateTagPositionsUseCase
import com.kazemieh.domain.usecase.UpdateTagUseCase
import com.kazemieh.domain.usecase.UpdateTransactionUseCase
import com.kazemieh.domain.usecase.AssetUseCases
import com.kazemieh.domain.usecase.AddBudgetUseCase as AddBudgetUseCaseAlias
import com.kazemieh.domain.usecase.AddAssetUseCase
import com.kazemieh.domain.usecase.AddGoalUseCase
import com.kazemieh.domain.usecase.ClearPreferencesUseCase
import com.kazemieh.domain.usecase.DeleteAssetUseCase
import com.kazemieh.domain.usecase.DeleteGoalUseCase
import com.kazemieh.domain.usecase.ObserveAssetHistoryUseCase
import com.kazemieh.domain.usecase.ObserveAchievementsUseCase
import com.kazemieh.domain.usecase.ObserveAssetsUseCase
import com.kazemieh.domain.usecase.SyncAssetRatesUseCase
import com.kazemieh.domain.usecase.UpdateAssetUseCase
import com.kazemieh.domain.usecase.UpdateBudgetUseCase
import com.kazemieh.domain.usecase.GetBudgetByCategoryIdUseCase
import com.kazemieh.domain.usecase.GetBudgetSpentAmountUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { AddTransactionUseCase(get(), get(), get(), get(),get(),get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { UpdateTransactionUseCase(get()) }
    factory { ObserveTransactionsUseCase(get()) }
    factory { ObserveCategorySumsUseCase(get()) }
    factory { ObserveSpendingPatternUseCase(get()) }
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

    factory { AddInstallmentUseCase(get(), get()) }
    factory { ObserveInstallmentsUseCase(get()) }
    factory { DeleteInstallmentUseCase(get(), get()) }
    factory { MarkInstallmentAsPaidUseCase(get(), get(), get()) }
    factory { GetInstallmentByIdUseCase(get()) }
    factory { UpdateInstallmentUseCase(get(), get()) }
    factory { GetBudgetByCategoryIdUseCase(get()) }
    factory { GetBudgetSpentAmountUseCase(get()) }

    factory { AddDebtUseCase(get()) }
    factory { UpdateDebtUseCase(get()) }
    factory { DeleteDebtUseCase(get()) }
    factory { ObserveDebtsUseCase(get()) }
    factory { ObserveDebtsByPersonUseCase(get()) }
    factory { SettleDebtUseCase(get(), get()) }

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
    factory { UpdateNextDueDateUseCase(get()) }

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
            getInstallmentByIdUseCase = get(),
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
            updateNextDueDateUseCase = get()
        )
    }
    single {
        com.kazemieh.domain.usecase.GoalUseCases(
            observeGoals = get(),
            addGoal = get(),
            updateGoal = get(),
            deleteGoal = get(),
            getGoalById = get()
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
