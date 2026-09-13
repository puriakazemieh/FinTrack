package com.kazemieh.category.di

import com.kazemieh.category.ui.list.CategoryViewModel
import com.kazemieh.category.ui.add.AddCategoryViewModel
import com.kazemieh.category.ui.delete.DeleteCategoryViewModel
import com.kazemieh.category.ui.detail.CategoryDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionCategoryModule = module {
    viewModel {
        CategoryViewModel(
            analytics = get(),
            observeCategoriesUseCase = get(),
            observeCategoriesFlatUseCase = get(),
            updateCategoryPositionsUseCase = get(),
            updateCategoryUseCase = get()
        )
    }
    viewModel { (categoryId: Long) ->
        CategoryDetailViewModel(
            analytics = get(),
            categoryId = categoryId,
            observeTransactionsUseCase = get(),
            getCategoryUseCase = get(),
            debtRepository = get(),
            installmentRepository = get(),
            checkRepository = get(),
            fixedExpenseRepository = get(),
            shoppingRepository = get(),
            budgetRepository = get()
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
