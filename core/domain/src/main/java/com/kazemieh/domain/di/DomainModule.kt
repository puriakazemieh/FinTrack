package com.kazemieh.domain.di

//import com.kazemieh.domain.usecase.ExportTransactionsUseCase
import com.kazemieh.domain.usecase.AddCategory
import com.kazemieh.domain.usecase.AddFinancialSource
import com.kazemieh.domain.usecase.AddPerson
import com.kazemieh.domain.usecase.AddSourceUseCases
import com.kazemieh.domain.usecase.AddTag
import com.kazemieh.domain.usecase.AddTransaction
import com.kazemieh.domain.usecase.DeleteCategory
import com.kazemieh.domain.usecase.DeleteSource
import com.kazemieh.domain.usecase.DeleteTag
import com.kazemieh.domain.usecase.DeleteTransaction
import com.kazemieh.domain.usecase.EditCategory
import com.kazemieh.domain.usecase.EditSource
import com.kazemieh.domain.usecase.EditTag
import com.kazemieh.domain.usecase.GetAllCategoryByType
import com.kazemieh.domain.usecase.GetAllPerson
import com.kazemieh.domain.usecase.GetAllSource
import com.kazemieh.domain.usecase.GetAllTag
import com.kazemieh.domain.usecase.GetAllTransactionsFiltered
import com.kazemieh.domain.usecase.GetCategorySum
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultFinancialSourceUseCase
import com.kazemieh.domain.usecase.GetSource
import com.kazemieh.domain.usecase.GetTransferCategoryUseCase
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.domain.usecase.UpdateTransaction
import org.koin.dsl.module

val domainModule = module {
    factory { AddTransaction(get()) }
    factory { DeleteTransaction(get()) }
    factory { UpdateTransaction(get()) }
    factory { GetAllTransactionsFiltered(get()) }
    factory { GetCategorySum(get()) }
    factory { GetAllTag(get()) }
    factory { GetAllSource(get()) }
    factory { GetAllCategoryByType(get()) }
    factory { AddCategory(get()) }
    factory { EditCategory(get()) }
    factory { EditTag(get()) }
    factory { DeleteCategory(get()) }
    factory { DeleteTag(get()) }
    factory { AddFinancialSource(get()) }
    factory { AddTag(get()) }
    factory { GetDefaultCategoryUseCase(get()) }
    factory { GetTransferCategoryUseCase(get()) }
    factory { GetDefaultFinancialSourceUseCase(get()) }
    factory { AddPerson(get()) }
    factory { GetAllPerson(get()) }
    factory { DeleteSource(get()) }
    factory { EditSource(get()) }
    factory { GetSource(get()) }


    single {
        AddSourceUseCases(
            editSource = get(),
            addSource = get(),
            getSource = get()
        )
    }
    single {
        TransactionUseCases(
            addTransaction = get(),
            deleteTransaction = get(),
            getDefaultCategoryUseCase = get(),
            getTransferCategoryUseCase = get(),
            getDefaultFinancialSourceUseCase = get(),
            getAllTransactionsFiltered = get(),
            updateTransaction = get(),
            getCategorySum = get(),
        )
    }

}