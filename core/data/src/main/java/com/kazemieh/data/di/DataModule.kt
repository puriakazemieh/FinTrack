package com.kazemieh.data.di

import com.kazemieh.data.datasource.TransactionLocalDataSource
import com.kazemieh.data.repository.TransactionRepositoryImpl
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import com.kazemieh.domain.repository.TransactionRepository
import org.koin.dsl.module


val dataModule = module {
    single<TransactionLocalDataSource> {
        TransactionLocalDataSourceImpl(get())
    }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
}
