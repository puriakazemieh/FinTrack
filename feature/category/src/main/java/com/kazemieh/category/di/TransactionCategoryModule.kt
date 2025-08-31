package com.kazemieh.category.di

import com.kazemieh.category.ui.CategoryViewModel
import com.kazemieh.category.ui.add.AddCategoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val transactionCategoryModule = module {
    viewModel {
        CategoryViewModel(
            getAllCategory = get()
        )
    }
}
val transactionAddCategoryModule = module {
    viewModel {
        AddCategoryViewModel(
            addCategoryUseCase = get()
        )
    }
}