package com.kazemieh.category.di

import com.kazemieh.category.ui.CategoryViewModel
import com.kazemieh.category.ui.add.AddCategoryViewModel
import com.kazemieh.category.ui.delete.DeleteCategoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionCategoryModule = module {
    viewModel {
        CategoryViewModel(
            getAllCategoryByType = get()
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
            editCategoryUseCase = get()
        )
    }
}