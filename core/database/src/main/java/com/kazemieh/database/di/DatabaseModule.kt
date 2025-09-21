package com.kazemieh.database.di

import androidx.room.Room
import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.database.DatabaseModule
import com.kazemieh.database.PrepopulateCallback
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            DatabaseModule::class.java,
            "fin_track.db"
        )
            .addCallback(PrepopulateCallback(koin = getKoin()))
            .build()
    }

    // Provide each DAO
    single { get<DatabaseModule>().transactionDao() }
    single { get<DatabaseModule>().categoryDao() }
    single { get<DatabaseModule>().financialSourceDao() }
    single { get<DatabaseModule>().tagDao() }
    single { get<DatabaseModule>().transactionTagCrossRefDao() }


    single<TransactionLocalDataSource> {
        TransactionLocalDataSourceImpl(get(), get(), get(), get())
    }
}
