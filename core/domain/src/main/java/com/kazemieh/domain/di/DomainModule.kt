package com.kazemieh.domain.di

import com.kazemieh.domain.usecase.AddCategoryUseCase
import com.kazemieh.domain.usecase.AddSourceUseCase
import com.kazemieh.domain.usecase.AddPersonUseCase
import com.kazemieh.domain.usecase.SourceUseCases
import com.kazemieh.domain.usecase.AddTagUseCase
import com.kazemieh.domain.usecase.AddTransactionUseCase
import com.kazemieh.domain.usecase.DeleteCategoryUseCase
import com.kazemieh.domain.usecase.DeletePersonUseCase
import com.kazemieh.domain.usecase.DeleteSourceUseCase
import com.kazemieh.domain.usecase.DeleteTagUseCase
import com.kazemieh.domain.usecase.DeleteTransactionUseCase
import com.kazemieh.domain.usecase.UpdateCategory
import com.kazemieh.domain.usecase.UpdatePerson
import com.kazemieh.domain.usecase.UpdateSourceUseCase
import com.kazemieh.domain.usecase.UpdateTag
import com.kazemieh.domain.usecase.ObserveCategoriesUseCase
import com.kazemieh.domain.usecase.ObservePersonsUseCase
import com.kazemieh.domain.usecase.GetAllSource
import com.kazemieh.domain.usecase.ObserveTagsUseCase
import com.kazemieh.domain.usecase.ObserveTransactionsUseCase
import com.kazemieh.domain.usecase.ObserveCategorySumsUseCase
import com.kazemieh.domain.usecase.GetDefaultCategoryUseCase
import com.kazemieh.domain.usecase.GetDefaultFinancialSourceUseCase
import com.kazemieh.domain.usecase.ObserveSourceUseCase
import com.kazemieh.domain.usecase.GetTransferCategoryUseCase
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import com.kazemieh.domain.usecase.UpdateTransactionUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { AddTransactionUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { UpdateTransactionUseCase(get()) }
    factory { ObserveTransactionsUseCase(get()) }
    factory { ObserveCategorySumsUseCase(get()) }
    factory { ObserveTagsUseCase(get()) }
    factory { GetAllSource(get()) }
    factory { ObserveCategoriesUseCase(get()) }
    factory { AddCategoryUseCase(get()) }
    factory { UpdateCategory(get()) }
    factory { UpdateTag(get()) }
    factory { DeleteCategoryUseCase(get()) }
    factory { DeletePersonUseCase(get()) }
    factory { DeleteTagUseCase(get()) }
    factory { AddSourceUseCase(get()) }
    factory { AddTagUseCase(get()) }
    factory { GetDefaultCategoryUseCase(get()) }
    factory { GetTransferCategoryUseCase(get()) }
    factory { GetDefaultFinancialSourceUseCase(get()) }
    factory { AddPersonUseCase(get()) }
    factory { ObservePersonsUseCase(get()) }
    factory { DeleteSourceUseCase(get()) }
    factory { UpdateSourceUseCase(get()) }
    factory { ObserveSourceUseCase(get()) }
    factory { UpdatePerson(get()) }


    single {
        SourceUseCases(
            updateSourceUseCase = get(),
            addSource = get(),
            observeSourceUseCase = get()
        )
    }
    single {
        TransactionUseCaseGroup(
            addTransactionUseCase = get(),
            deleteTransactionUseCase = get(),
            getDefaultCategoryUseCase = get(),
            getTransferCategoryUseCase = get(),
            getDefaultFinancialSourceUseCase = get(),
            observeTransactionsUseCase = get(),
            updateTransactionUseCase = get(),
            observeCategorySumsUseCase = get(),
        )
    }

}