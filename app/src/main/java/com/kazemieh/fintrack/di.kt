package com.kazemieh.fintrack

import com.kazemieh.fintrack.report.ReportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module



val reportViewModel = module {
    viewModel {
        ReportViewModel()
    }
}