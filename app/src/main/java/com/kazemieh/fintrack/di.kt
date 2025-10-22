package com.kazemieh.fintrack

import com.kazemieh.fintrack.dashboard.DashboardViewModel
import com.kazemieh.fintrack.report.ReportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val dashboardViewModel = module {
    viewModel {
        DashboardViewModel()
    }
}

val reportViewModel = module {
    viewModel {
        ReportViewModel()
    }
}