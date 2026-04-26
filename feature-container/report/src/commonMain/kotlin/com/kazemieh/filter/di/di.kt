package com.kazemieh.filter.di

import com.kazemieh.filter.ReportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val reportViewModel = module {
    viewModel {
        ReportViewModel()
    }
}