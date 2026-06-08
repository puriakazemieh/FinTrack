package com.kazemieh.category.di

import com.kazemieh.category.ui.list.CategoryViewModel
import com.kazemieh.category.ui.add.AddCategoryViewModel
import com.kazemieh.category.ui.delete.DeleteCategoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionCategoryModule = module {
    viewModel {
        CategoryViewModel(
            observeCategoriesUseCase = get(),
            observeCategoriesFlatUseCase = get(),
            updateCategoryPositionsUseCase = get()
        )
    }
}
val transactionDeleteCategoryModule = module {
    viewModel {
        DeleteCategoryViewModel(
            deleteCategoryUseCase = get()
        )
    }
}
val transactionAddCategoryModule = module {
    viewModel {
        AddCategoryViewModel(
            addCategoryUseCase = get(),
            updateCategoryUseCase = get(),
            observeCategoriesUseCase = get()
        )
    }
}
