package com.kazemieh.installment.di

import com.kazemieh.installment.ui.InstallmentViewModel
import com.kazemieh.installment.ui.add.AddInstallmentViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

val installmentModule = module {
    viewModel { InstallmentViewModel(get()) }
    viewModel { AddInstallmentViewModel(get(), get()) }
}
