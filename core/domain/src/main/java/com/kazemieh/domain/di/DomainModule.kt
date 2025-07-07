package com.kazemieh.domain.di

import com.kazemieh.domain.usecase.AddTransaction
import com.kazemieh.domain.usecase.DeleteTransaction
import com.kazemieh.domain.usecase.GetAllTransactions
import com.kazemieh.domain.usecase.TransactionUseCases
import org.koin.dsl.module

val domainModule = module {
    factory { AddTransaction(get()) }
    factory { DeleteTransaction(get()) }
    factory { GetAllTransactions(get()) }

    single {
        TransactionUseCases(
            addTransaction = get(),
            deleteTransaction = get(),
            getAllTransactions = get()
        )
    }

}