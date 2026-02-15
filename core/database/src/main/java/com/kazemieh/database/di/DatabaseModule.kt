package com.kazemieh.database.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.PrepopulateCallback
import com.kazemieh.database.datasource.TransactionLocalDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            FinTrackDatabase::class.java,
            "fin_track.db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .addCallback(PrepopulateCallback(koin = getKoin()))
            .build()
    }

    // Provide each DAO
    single { get<FinTrackDatabase>().transactionDao() }
    single { get<FinTrackDatabase>().categoryDao() }
    single { get<FinTrackDatabase>().financialSourceDao() }
    single { get<FinTrackDatabase>().tagDao() }
    single { get<FinTrackDatabase>().personDao() }


    single<TransactionLocalDataSource> {
        TransactionLocalDataSourceImpl(
            db = get(),
            transactionDao = get(),
            tagDao = get(),
            sourceDao = get(),
            categoryDao = get(),
            personDao = get()
        )
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // ایجاد جدول person
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS person (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT
            )
        """.trimIndent()
        )

        // ایجاد جدول transaction_person
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS transaction_person (
                transactionId INTEGER NOT NULL,
                personId INTEGER NOT NULL,
                PRIMARY KEY(transactionId, personId),
                FOREIGN KEY(transactionId) REFERENCES transactions(id) ON DELETE CASCADE,
                FOREIGN KEY(personId) REFERENCES person(id) ON DELETE CASCADE
            )
        """.trimIndent()
        )

        // ایندکس‌ها
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_person_transactionId ON transaction_person(transactionId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_person_personId ON transaction_person(personId)")


        db.execSQL(
            """
            INSERT INTO category (name, description, type)
            VALUES ('انتقال', NULL , 3)
            """
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE transactions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                amount INTEGER NOT NULL,
                amountTransfer INTEGER,
                categoryId INTEGER NOT NULL,
                sourceId INTEGER NOT NULL,
                sourceEndId INTEGER,
                description TEXT,
                timeStamp INTEGER NOT NULL,
                type INTEGER NOT NULL,
                FOREIGN KEY(categoryId) REFERENCES category(id) ON DELETE CASCADE,
                FOREIGN KEY(sourceId) REFERENCES financial_source(id) ON DELETE CASCADE
            )
        """
        )

        db.execSQL(
            """
            INSERT INTO transactions_new (
                id, amount, amountTransfer, categoryId,
                sourceId, sourceEndId, description, timeStamp, type
            )
            SELECT
                id,
                amount,
                NULL,
                categoryId,
                financialSourceId,
                NULL,
                description,
                timeStamp,
                type
            FROM transactions
        """
        )

        db.execSQL("DROP TABLE transactions")
        db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // 1. create source
        db.execSQL(
            """
            CREATE TABLE source (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                balance INTEGER NOT NULL,
                cardNumber TEXT,
                description TEXT,
                type INTEGER NOT NULL
            )
        """
        )

        // 2. copy data
        db.execSQL(
            """
            INSERT INTO source (id, name, balance, cardNumber, description, type)
            SELECT id, name, balance, cardNumber, description, type
            FROM financial_source
        """
        )

        // 3. recreate transactions
        db.execSQL(
            """
            CREATE TABLE transactions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                amount INTEGER NOT NULL,
                amountTransfer INTEGER,
                categoryId INTEGER NOT NULL,
                sourceId INTEGER NOT NULL,
                sourceEndId INTEGER,
                description TEXT,
                timeStamp INTEGER NOT NULL,
                type INTEGER NOT NULL,
                FOREIGN KEY(categoryId) REFERENCES category(id) ON DELETE CASCADE,
                FOREIGN KEY(sourceId) REFERENCES source(id) ON DELETE CASCADE
            )
        """
        )

        db.execSQL(
            """
            INSERT INTO transactions_new (
                id, amount, amountTransfer, categoryId,
                sourceId, sourceEndId, description, timeStamp, type
            )
            SELECT
                id, amount, amountTransfer, categoryId,
                sourceId, sourceEndId,
                description, timeStamp, type
            FROM transactions
        """
        )

        // 4. drop old tables
        db.execSQL("DROP TABLE transactions")
        db.execSQL("DROP TABLE financial_source")

        // 5. rename
        db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
    }
}

private const val COLOR_COUNT = 12
private const val ICON_COUNT = 100

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // --- category ---
        db.execSQL("ALTER TABLE category ADD COLUMN colorId INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE category ADD COLUMN iconId INTEGER NOT NULL DEFAULT 1")

        // برای کاربران آپدیتی: مقدارهای متفاوت بر اساس id
        db.execSQL(
            """
            UPDATE category
            SET colorId = ((id + 3) % $COLOR_COUNT) + 1,
                iconId  = ((id + 17) % $ICON_COUNT) + 1
        """.trimIndent()
        )

        // --- tag ---
        db.execSQL("ALTER TABLE tag ADD COLUMN colorId INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE tag ADD COLUMN iconId INTEGER NOT NULL DEFAULT 1")

        db.execSQL(
            """
            UPDATE tag
            SET colorId = ((id + 5) % $COLOR_COUNT) + 1,
                iconId  = ((id + 29) % $ICON_COUNT) + 1
        """.trimIndent()
        )

        // --- source ---
        db.execSQL("ALTER TABLE source ADD COLUMN colorId INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE source ADD COLUMN iconId INTEGER NOT NULL DEFAULT 1")

        db.execSQL(
            """
            UPDATE source
            SET colorId = ((id + 7) % $COLOR_COUNT) + 1,
                iconId  = ((id + 41) % $ICON_COUNT) + 1
        """.trimIndent()
        )
    }
}
