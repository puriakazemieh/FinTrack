package com.tosantechno.filter.di

import com.tosantechno.filter.ReportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val reportViewModel = module {
    viewModel {
        ReportViewModel()
    }
}