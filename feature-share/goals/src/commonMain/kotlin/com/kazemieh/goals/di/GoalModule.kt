package com.kazemieh.goals.di

import com.kazemieh.goals.presentation.GoalViewModel
import com.kazemieh.goals.presentation.add.AddGoalViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val goalModule = module {
    viewModel { GoalViewModel(get(), get(), get(), get(), get()) }
    viewModel { AddGoalViewModel(get(), get()) }
}
