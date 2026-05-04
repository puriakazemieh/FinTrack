//// database/src/androidMain/kotlin/com/kazemieh/database/di/MigrationModule.kt
//package com.kazemieh.database
//
//import com.kazemieh.database.migration.RoomToSQLDelightMigration
//import org.koin.android.ext.koin.androidContext
//import org.koin.dsl.module
//
//val migrationModule = module {
//    single {
//        RoomToSQLDelightMigration(
//            context = androidContext(),
//            sqlDelightDriver = get()
//        )
//    }
//}
