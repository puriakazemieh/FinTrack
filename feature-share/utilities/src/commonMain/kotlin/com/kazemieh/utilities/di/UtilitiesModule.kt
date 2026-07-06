package com.kazemieh.utilities.di

import com.kazemieh.utilities.ui.calendar.FinancialCalendarViewModel
import com.kazemieh.utilities.ui.converter.CurrencyConverterViewModel
import com.kazemieh.utilities.ui.events.EventsViewModel
import com.kazemieh.utilities.ui.faq.FAQViewModel
import com.kazemieh.utilities.ui.fx.FxRatesViewModel
import com.kazemieh.utilities.ui.news.NewsViewModel
import com.kazemieh.utilities.ui.support.SupportViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val utilitiesModule = module {
    viewModelOf(::CurrencyConverterViewModel)
    viewModelOf(::FxRatesViewModel)
    viewModelOf(::NewsViewModel)
    viewModelOf(::FAQViewModel)
    viewModelOf(::SupportViewModel)
    viewModelOf(::EventsViewModel)
    viewModelOf(::FinancialCalendarViewModel)
}
