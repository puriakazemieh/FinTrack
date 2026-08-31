package com.kazemieh.category.di

import com.kazemieh.category.ui.list.CategoryViewModel
import com.kazemieh.category.ui.add.AddCategoryViewModel
import com.kazemieh.category.ui.delete.DeleteCategoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionCategoryModule = module {
    viewModel {
        CategoryViewModel(
            analytics = get(),
            observeCategoriesUseCase = get(),
            observeCategoriesFlatUseCase = get(),
            updateCategoryPositionsUseCase = get()
        )
    }
}
val transactionDeleteCategoryModule = module {
    viewModel {
        DeleteCategoryViewModel(
            analytics = get(),
            deleteCategoryUseCase = get()
        )
    }
}
val transactionAddCategoryModule = module {
    viewModel {
        AddCategoryViewModel(
            analytics = get(),
            addCategoryUseCase = get(),
            updateCategoryUseCase = get(),
            observeCategoriesUseCase = get()
        )
    }
}
