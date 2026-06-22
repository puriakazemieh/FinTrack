package com.kazemieh.database.di

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.kazemieh.common.model.AssetType
import com.kazemieh.data_contract.datasource.*
import com.kazemieh.database.Asset as AssetDb
import com.kazemieh.database.DatabaseInitializer
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.createDriver
import com.kazemieh.database.datasource.*
import org.koin.dsl.module

val databaseModule = module {

    single<SqlDriver> { createDriver() }

    single {
        FinTrackDatabase(
            driver = get(),
            assetAdapter = AssetDb.Adapter(
                typeAdapter = EnumColumnAdapter<AssetType>()
            )
        )
    }

    single { DatabaseInitializer(get()) }

    single<TransactionLocalDataSource> {
        TransactionLocalDataSourceImpl(db = get())
    }

    single<AssetLocalDataSource> {
        AssetLocalDataSourceImpl(db = get())
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

    single<ShoppingLocalDataSource> {
        ShoppingLocalDataSourceImpl(db = get())
    }

    single<NoteLocalDataSource> {
        NoteLocalDataSourceImpl(db = get())
    }

    single<SmsDraftLocalDataSource> {
        SmsDraftLocalDataSourceImpl(db = get())
    }

    single<SyncHistoryLocalDataSource> {
        SyncHistoryLocalDataSourceImpl(db = get())
    }

    single<SyncLocalDataSource> {
        SyncLocalDataSourceImpl(
            transactionLocalDataSource = get(),
            noteLocalDataSource = get(),
            assetLocalDataSource = get(),
            budgetLocalDataSource = get(),
            checkLocalDataSource = get(),
            debtLocalDataSource = get(),
            fixedExpenseLocalDataSource = get(),
            installmentLocalDataSource = get(),
            shoppingLocalDataSource = get()
        )
    }
}
