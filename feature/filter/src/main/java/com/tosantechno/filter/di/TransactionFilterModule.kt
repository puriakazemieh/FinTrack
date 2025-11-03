package com.tosantechno.filter.di

import com.tosantechno.filter.FilterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val transactionFilterModule = module {
    viewModel {
        FilterViewModel()
    }
}