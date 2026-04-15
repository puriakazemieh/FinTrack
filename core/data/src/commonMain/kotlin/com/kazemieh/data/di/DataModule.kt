package com.kazemieh.data.di

import com.kazemieh.data.repository.TransactionRepositoryImpl
import com.kazemieh.domain.repository.TransactionRepository
import org.koin.dsl.module


val dataModule = module {
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
}
