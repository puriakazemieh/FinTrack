package com.kazemieh.database.di

import app.cash.sqldelight.db.SqlDriver
import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.database.DatabaseInitializer
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.createDriver
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import org.koin.dsl.module

val databaseModule = module {

    single<SqlDriver> { createDriver() }


    single {
        FinTrackDatabase(driver = get())
    }

    single {
        DatabaseInitializer(database = get())
    }



    single<TransactionLocalDataSource> {
        TransactionLocalDataSourceImpl(
            db = get(),
            initializer = get()
        )
    }
}
