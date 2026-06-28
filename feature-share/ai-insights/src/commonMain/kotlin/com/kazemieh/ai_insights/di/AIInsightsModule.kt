package com.kazemieh.ai_insights.di

import com.kazemieh.ai_insights.ui.AIAdvisorViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val aiInsightsModule = module {
    viewModel { AIAdvisorViewModel(get(), get()) }
}
