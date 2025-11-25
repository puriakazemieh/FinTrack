package com.kazemieh.database.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
            .addMigrations(MIGRATION_1_2)
            .addCallback(PrepopulateCallback(koin = getKoin()))
            .build()
    }

    // Provide each DAO
    single { get<DatabaseModule>().transactionDao() }
    single { get<DatabaseModule>().categoryDao() }
    single { get<DatabaseModule>().financialSourceDao() }
    single { get<DatabaseModule>().tagDao() }
    single { get<DatabaseModule>().personDao() }


    single<TransactionLocalDataSource> {
        TransactionLocalDataSourceImpl(get(), get(), get(), get(), get())
    }
}


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // ایجاد جدول person
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS person (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT
            )
        """.trimIndent())

        // ایجاد جدول transaction_person
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS transaction_person (
                transactionId INTEGER NOT NULL,
                personId INTEGER NOT NULL,
                PRIMARY KEY(transactionId, personId),
                FOREIGN KEY(transactionId) REFERENCES transactions(id) ON DELETE CASCADE,
                FOREIGN KEY(personId) REFERENCES person(id) ON DELETE CASCADE
            )
        """.trimIndent())

        // ایندکس‌ها
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_person_transactionId ON transaction_person(transactionId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_person_personId ON transaction_person(personId)")


        db.execSQL(
            """
            ALTER TABLE transactions 
            ADD COLUMN financialSourceEndId INTEGER
            """
        )

        db.execSQL(
            """
            ALTER TABLE transactions 
            ADD COLUMN amountTransfer INTEGER
            """
        )

        db.execSQL(
            """
            INSERT INTO category (name, description, type)
            VALUES ('انتقال', NULL , 3)
            """
        )
    }
}
