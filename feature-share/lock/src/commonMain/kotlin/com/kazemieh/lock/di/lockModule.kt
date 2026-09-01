package com.kazemieh.lock.di

import com.kazemieh.lock.LockViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val lockModule = module {
    viewModel { LockViewModel(get(), get()) }
}
