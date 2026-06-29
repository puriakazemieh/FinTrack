package com.kazemieh.gamification.di

import com.kazemieh.gamification.ui.GamificationViewModel
import org.koin.dsl.module
import org.koin.compose.viewmodel.dsl.viewModelOf

val gamificationModule = module {
    viewModelOf(::GamificationViewModel)
}
