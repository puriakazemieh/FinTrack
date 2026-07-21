package com.kazemieh.data.di

import com.kazemieh.data.repository.AchievementRepositoryImpl
import com.kazemieh.data.repository.AiAdvisorRepositoryImpl
import com.kazemieh.data.repository.AssetRepositoryImpl
import com.kazemieh.data.repository.BackupRepositoryImpl
import com.kazemieh.data.repository.BudgetRepositoryImpl
import com.kazemieh.data.repository.CheckRepositoryImpl
import com.kazemieh.data.repository.DebtRepositoryImpl
import com.kazemieh.data.repository.FixedExpenseRepositoryImpl
import com.kazemieh.data.repository.GoalRepositoryImpl
import com.kazemieh.data.repository.InstallmentRepositoryImpl
import com.kazemieh.data.repository.NoteRepositoryImpl
import com.kazemieh.data.repository.PreferenceRepositoryImpl
import com.kazemieh.data.repository.SmsDraftRepositoryImpl
import com.kazemieh.data.repository.ShoppingRepositoryImpl
import com.kazemieh.data.repository.SyncRepositoryImpl
import com.kazemieh.data.repository.TransactionRepositoryImpl
import com.kazemieh.data.repository.UtilitiesRepositoryImpl
import com.kazemieh.domain.repository.AchievementRepository
import com.kazemieh.domain.repository.AiAdvisorRepository
import com.kazemieh.domain.repository.AssetRepository
import com.kazemieh.domain.repository.BackupRepository
import com.kazemieh.domain.repository.BudgetRepository
import com.kazemieh.domain.repository.CheckRepository
import com.kazemieh.domain.repository.DebtRepository
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.repository.GoalRepository
import com.kazemieh.domain.repository.InstallmentRepository
import com.kazemieh.domain.repository.NoteRepository
import com.kazemieh.domain.repository.PreferenceRepository
import com.kazemieh.domain.repository.SmsDraftRepository
import com.kazemieh.domain.repository.ShoppingRepository
import com.kazemieh.domain.repository.SyncRepository
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.repository.UtilitiesRepository
import org.koin.dsl.module

val dataModule = module {
    single<TransactionRepository> { TransactionRepositoryImpl(get(),get()) }
    single<PreferenceRepository> { PreferenceRepositoryImpl(get(), get()) }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
    single<InstallmentRepository> { InstallmentRepositoryImpl(get()) }
    single<DebtRepository> { DebtRepositoryImpl(get()) }
    single<CheckRepository> { CheckRepositoryImpl(get()) }
    single<FixedExpenseRepository> { FixedExpenseRepositoryImpl(get()) }
    single<AssetRepository> { AssetRepositoryImpl(get(), get()) }
    single<AiAdvisorRepository> { AiAdvisorRepositoryImpl(get(), get()) }
    single<ShoppingRepository> { ShoppingRepositoryImpl(get()) }
    single<NoteRepository> { NoteRepositoryImpl(get()) }
    single<SmsDraftRepository> { SmsDraftRepositoryImpl(get()) }
    single<AchievementRepository> { AchievementRepositoryImpl(get()) }
    single<GoalRepository> { GoalRepositoryImpl(get()) }
    single<UtilitiesRepository> { UtilitiesRepositoryImpl() }

    single<BackupRepository> {
        BackupRepositoryImpl(
            transactionLocalDataSource = get(),
            assetLocalDataSource = get(),
            budgetLocalDataSource = get(),
            checkLocalDataSource = get(),
            debtLocalDataSource = get(),
            fixedExpenseLocalDataSource = get(),
            installmentLocalDataSource = get(),
            noteLocalDataSource = get(),
            shoppingLocalDataSource = get(),
            syncHistoryLocalDataSource = get()
        )
    }

    single<SyncRepository> { SyncRepositoryImpl(get(), get(), get()) }
}
