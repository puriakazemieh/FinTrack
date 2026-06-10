package com.kazemieh.domain.di

import com.kazemieh.domain.usecase.AddCategoryUseCase
import com.kazemieh.domain.usecase.AddPersonUseCase
import com.kazemieh.domain.usecase.AddSourceUseCase
import com.kazemieh.domain.usecase.AddTagUseCase
import com.kazemieh.domain.usecase.AddTransactionUseCase
import com.kazemieh.domain.usecase.DeleteCategoryUseCase
import com.kazemieh.domain.usecase.DeletePersonUseCase
import com.kazemieh.domain.usecase.DeleteRecentSearchUseCase
import com.kazemieh.domain.usecase.DeleteSourceUseCase
import com.kazemieh.domain.usecase.DeleteTagUseCase
import com.kazemieh.domain.usecase.DeleteTransactionUseCase
import com.kazemieh.domain.usecase.GetBooleanPreferenceUseCase
import com.kazemieh.domain.usecase.GetStringFlowUseCase
import com.kazemieh.domain.usecase.GetStringPreferenceUseCase
import com.kazemieh.domain.usecase.GetCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultFinancialSourceUseCase
import com.kazemieh.domain.usecase.GetRecentSearchesUseCase
import com.kazemieh.domain.usecase.GetTransferCategoryUseCase
import com.kazemieh.domain.usecase.GetTransactionAmountRangeUseCase
import com.kazemieh.domain.usecase.ObserveCategoriesFlatUseCase
import com.kazemieh.domain.usecase.ObserveCategoriesUseCase
import com.kazemieh.domain.usecase.ObserveCategorySumsUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedCategoriesUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedPersonsUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedSourcesUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedTagsUseCase
import com.kazemieh.domain.usecase.ObservePersonsUseCase
import com.kazemieh.domain.usecase.ObserveSourceUseCase
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
import com.kazemieh.domain.usecase.SetBooleanPreferenceUseCase
import com.kazemieh.domain.usecase.SetStringPreferenceUseCase
import com.kazemieh.domain.usecase.SourceUseCases
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import com.kazemieh.domain.usecase.UpdateCategoryPositionsUseCase
import com.kazemieh.domain.usecase.UpdateCategoryUseCase
import com.kazemieh.domain.usecase.UpdatePersonPositionsUseCase
import com.kazemieh.domain.usecase.UpdatePersonUseCase
import com.kazemieh.domain.usecase.UpdateSourcePositionsUseCase
import com.kazemieh.domain.usecase.UpdateSourceUseCase
import com.kazemieh.domain.usecase.UpdateTagPositionsUseCase
import com.kazemieh.domain.usecase.UpdateTagUseCase
import com.kazemieh.domain.usecase.UpdateTransactionUseCase
import com.kazemieh.domain.usecase.ObserveBudgetsWithProgressUseCase
import com.kazemieh.domain.usecase.AddBudgetUseCase
import com.kazemieh.domain.usecase.UpdateBudgetUseCase
import com.kazemieh.domain.usecase.DeleteBudgetUseCase
import com.kazemieh.domain.usecase.GetBudgetByCategoryIdUseCase
import com.kazemieh.domain.usecase.GetBudgetSpentAmountUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { AddTransactionUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { UpdateTransactionUseCase(get()) }
    factory { ObserveTransactionsUseCase(get()) }
    factory { ObserveCategorySumsUseCase(get()) }
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
    factory { SeedDataUseCase(get(), get()) }

    factory { ObserveBudgetsWithProgressUseCase(get()) }
    factory { AddBudgetUseCase(get()) }
    factory { UpdateBudgetUseCase(get()) }
    factory { DeleteBudgetUseCase(get()) }
    factory { GetBudgetByCategoryIdUseCase(get()) }
    factory { GetBudgetSpentAmountUseCase(get()) }

    factory { GetBooleanPreferenceUseCase(get()) }
    factory { SetBooleanPreferenceUseCase(get()) }
    factory { GetStringPreferenceUseCase(get()) }
    factory { SetStringPreferenceUseCase(get()) }
    factory { GetStringFlowUseCase(get()) }

    factory { SearchCategoriesUseCase(get()) }
    factory { SearchSourcesUseCase(get()) }
    factory { SearchPersonsUseCase(get()) }
    factory { SearchTagsUseCase(get()) }
    factory { GetRecentSearchesUseCase(get()) }
    factory { SaveRecentSearchUseCase(get()) }
    factory { DeleteRecentSearchUseCase(get()) }


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
            getStringFlow = get()
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
            getTransactionAmountRangeUseCase = get()
        )
    }

}
