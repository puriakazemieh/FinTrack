package com.kazemieh.domain.di

import com.kazemieh.domain.usecase.AddCategory
import com.kazemieh.domain.usecase.AddFinancialSource
import com.kazemieh.domain.usecase.AddPerson
import com.kazemieh.domain.usecase.AddTag
import com.kazemieh.domain.usecase.AddTransaction
import com.kazemieh.domain.usecase.DeleteTransaction
//import com.kazemieh.domain.usecase.ExportTransactionsUseCase
import com.kazemieh.domain.usecase.GetAllCategoryByType
import com.kazemieh.domain.usecase.GetAllFinancialSource
import com.kazemieh.domain.usecase.GetAllPerson
import com.kazemieh.domain.usecase.GetAllTag
import com.kazemieh.domain.usecase.GetAllTransactions
import com.kazemieh.domain.usecase.GetAllTransactionsByType
import com.kazemieh.domain.usecase.GetAllTransactionsFiltered
import com.kazemieh.domain.usecase.GetCategorySum
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultFinancialSourceUseCase
import com.kazemieh.domain.usecase.GetTransferCategoryUseCase
import com.kazemieh.domain.usecase.TransactionUseCases
import org.koin.dsl.module

val domainModule = module {
    factory { AddTransaction(get()) }
    factory { DeleteTransaction(get()) }
    factory { GetAllTransactions(get()) }
    factory { GetAllTransactionsByType(get()) }
    factory { GetAllTransactionsFiltered(get()) }
    factory { GetCategorySum(get()) }
    factory { GetAllTag(get()) }
    factory { GetAllFinancialSource(get()) }
    factory { GetAllCategoryByType(get()) }
    factory { AddCategory(get()) }
    factory { AddFinancialSource(get()) }
    factory { AddTag(get()) }
    factory { GetDefaultCategoryUseCase(get()) }
    factory { GetTransferCategoryUseCase(get()) }
    factory { GetDefaultFinancialSourceUseCase(get()) }
    factory { AddPerson(get()) }
    factory { GetAllPerson(get()) }


    single {
        TransactionUseCases(
            addTransaction = get(),
            deleteTransaction = get(),
            getDefaultCategoryUseCase = get(),
            getTransferCategoryUseCase = get(),
            getDefaultFinancialSourceUseCase = get(),
            getAllTransactions = get(),
            getAllTransactionsByType = get(),
            getAllTransactionsFiltered = get(),
            getCategorySum = get(),
        )
    }

}