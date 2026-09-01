package com.kazemieh.search.di

import com.kazemieh.search.ui.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
    viewModel {
        SearchViewModel(
            analytics = get(),
            observeTransactionsUseCase = get(),
            searchCategoriesUseCase = get(),
            searchSourcesUseCase = get(),
            searchPersonsUseCase = get(),
            searchTagsUseCase = get(),
            getRecentSearchesUseCase = get(),
            saveRecentSearchUseCase = get(),
            deleteRecentSearchUseCase = get(),
            observeMostUsedCategoriesUseCase = get(),
            observeMostUsedSourcesUseCase = get(),
            observeMostUsedPersonsUseCase = get(),
            observeMostUsedTagsUseCase = get()
        )
    }
}
