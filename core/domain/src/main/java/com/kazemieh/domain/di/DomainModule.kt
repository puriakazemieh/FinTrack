package com.kazemieh.domain.di

import com.kazemieh.domain.usecase.AddTransaction
import com.kazemieh.domain.usecase.DeleteTransaction
import com.kazemieh.domain.usecase.GetAllTransactions
import com.kazemieh.domain.usecase.TransactionUseCases
import org.koin.dsl.module

val domainModule = module {

    single {
        TransactionUseCases(
            addTransaction = AddTransaction(get()),
            deleteTransaction = DeleteTransaction(get()),
            getAllTransactions = GetAllTransactions(get())
        )
    }
}