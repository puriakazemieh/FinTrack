package com.kazemieh.database.di

import app.cash.sqldelight.db.SqlDriver
import com.kazemieh.data_contract.datasource.BudgetLocalDataSource
import com.kazemieh.data_contract.datasource.CheckLocalDataSource
import com.kazemieh.data_contract.datasource.DebtLocalDataSource
import com.kazemieh.data_contract.datasource.FixedExpenseLocalDataSource
import com.kazemieh.data_contract.datasource.InstallmentLocalDataSource
import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.database.DatabaseInitializer
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.createDriver
import com.kazemieh.database.datasource.BudgetLocalDataSourceImpl
import com.kazemieh.database.datasource.CheckLocalDataSourceImpl
import com.kazemieh.database.datasource.DebtLocalDataSourceImpl
import com.kazemieh.database.datasource.FixedExpenseLocalDataSourceImpl
import com.kazemieh.database.datasource.InstallmentLocalDataSourceImpl
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import org.koin.dsl.module

val databaseModule = module {

    single<SqlDriver> { createDriver() }


    single {
        FinTrackDatabase(driver = get())
    }

    single { DatabaseInitializer(database = get(), get()) }

    single<TransactionLocalDataSource> {
        TransactionLocalDataSourceImpl(db = get())
    }

    single<BudgetLocalDataSource> {
        BudgetLocalDataSourceImpl(db = get())
    }

    single<InstallmentLocalDataSource> {
        InstallmentLocalDataSourceImpl(db = get())
    }

    single<DebtLocalDataSource> {
        DebtLocalDataSourceImpl(db = get())
    }

    single<CheckLocalDataSource> {
        CheckLocalDataSourceImpl(database = get())
    }

    single<FixedExpenseLocalDataSource> {
        FixedExpenseLocalDataSourceImpl(database = get())
    }
}
