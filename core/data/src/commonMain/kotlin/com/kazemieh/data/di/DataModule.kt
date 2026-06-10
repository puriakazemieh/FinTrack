package com.kazemieh.data.di

import com.kazemieh.data.repository.BudgetRepositoryImpl
import com.kazemieh.data.repository.InstallmentRepositoryImpl
import com.kazemieh.data.repository.PreferenceRepositoryImpl
import com.kazemieh.data.repository.TransactionRepositoryImpl
import com.kazemieh.domain.repository.BudgetRepository
import com.kazemieh.domain.repository.InstallmentRepository
import com.kazemieh.domain.repository.PreferenceRepository
import com.kazemieh.domain.repository.TransactionRepository
import org.koin.dsl.module

val dataModule = module {
    single<TransactionRepository> { TransactionRepositoryImpl(get(),get()) }
    single<PreferenceRepository> { PreferenceRepositoryImpl(get(), get()) }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
    single<InstallmentRepository> { InstallmentRepositoryImpl(get()) }
}
