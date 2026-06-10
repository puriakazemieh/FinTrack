package com.kazemieh.fixed_expense.di

import com.kazemieh.fixed_expense.ui.detail.AddFixedExpenseViewModel
import com.kazemieh.fixed_expense.ui.list.FixedExpenseListViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val fixedExpenseModule = module {
    viewModelOf(::FixedExpenseListViewModel)
    viewModelOf(::AddFixedExpenseViewModel)
}
