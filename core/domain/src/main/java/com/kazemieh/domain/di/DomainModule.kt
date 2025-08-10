package com.kazemieh.domain.di

import com.kazemieh.domain.usecase.AddTransaction
import com.kazemieh.domain.usecase.DeleteTransaction
import com.kazemieh.domain.usecase.GetAllCategory
import com.kazemieh.domain.usecase.GetAllFinancialSource
import com.kazemieh.domain.usecase.GetAllTag
import com.kazemieh.domain.usecase.GetAllTransactions
import com.kazemieh.domain.usecase.TransactionUseCases
import org.koin.dsl.module

val domainModule = module {
    factory { AddTransaction(get()) }
    factory { DeleteTransaction(get()) }
    factory { GetAllTransactions(get()) }
    factory { GetAllTag(get()) }
    factory { GetAllFinancialSource(get()) }
    factory { GetAllCategory(get()) }

    single {
        TransactionUseCases(
            addTransaction = get(),
            deleteTransaction = get(),
            getAllTag = get(),
            getAllFinancialSource = get(),
            getAllCategory = get(),
            getAllTransactions = get()
        )
    }

}