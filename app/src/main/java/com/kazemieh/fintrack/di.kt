package com.kazemieh.fintrack

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val dashboardViewModel = module {
    viewModel {
        DashboardViewModel()
    }
}