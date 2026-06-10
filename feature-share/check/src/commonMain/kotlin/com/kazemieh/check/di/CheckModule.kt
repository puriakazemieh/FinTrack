package com.kazemieh.check.di

import com.kazemieh.check.ui.add.AddCheckViewModel
import com.kazemieh.check.ui.list.CheckListViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val checkModule = module {
    viewModelOf(::CheckListViewModel)
    viewModelOf(::AddCheckViewModel)
}
