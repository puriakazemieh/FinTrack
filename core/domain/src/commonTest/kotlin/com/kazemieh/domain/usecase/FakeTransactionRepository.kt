package com.kazemieh.domain.usecase

import com.kazemieh.common.model.*
import com.kazemieh.domain.repository.*
import kotlinx.coroutines.flow.*

open class FakeTransactionRepository : TransactionRepository {
    val balanceDeltasApplied = mutableMapOf<Long, Long>()

    override suspend fun addTransactionWithBalance(transaction: Transaction, tagIds: List<Long>, personIds: List<Long>, balanceDeltas: Map<Long, Long>): Long {
        balanceDeltas.forEach { (k, v) -> balanceDeltasApplied[k] = (balanceDeltasApplied[k] ?: 0L) + v }
        return 1L
    }
    override suspend fun updateTransactionWithBalance(transaction: Transaction, tagIds: List<Long>, personIds: List<Long>, balanceDeltas: Map<Long, Long>): Long {
        balanceDeltas.forEach { (k, v) -> balanceDeltasApplied[k] = (balanceDeltasApplied[k] ?: 0L) + v }
        return transaction.id
    }
    override suspend fun deleteTransactionWithBalance(transaction: Transaction, balanceDeltas: Map<Long, Long>) {
        balanceDeltas.forEach { (k, v) -> balanceDeltasApplied[k] = (balanceDeltasApplied[k] ?: 0L) + v }
    }
    override fun observeTransactions(transactionFilterParams: TransactionFilterParams, request: PageRequest): Flow<Page<TransactionWithRelations>> = emptyFlow()
    override suspend fun addCategory(category: Category): Long = 1L
    override suspend fun updateCategory(category: Category): Int = 1
    override suspend fun deleteCategory(category: Category, moveCategory: Category?) {}
    override fun observeCategorySums(transactionFilterParams: TransactionFilterParams): Flow<List<CategorySum>> = emptyFlow()
    override fun observeCategories(type: TransactionType?, parentId: Long?): Flow<List<Category>> = emptyFlow()
    override fun observeCategoriesFlat(type: TransactionType?): Flow<List<Category>> = emptyFlow()
    override suspend fun getCategoryById(id: Long): Category? = null
    override suspend fun getDefaultCategory(type: TransactionType): Category = Category(0, "", null, type, 0, 0, 0, null)
    override suspend fun getTransferCategory(): Category = Category(0, "", null, TransactionType.TRANSFER, 0, 0, 0, null)
    override suspend fun addTag(tag: Tag): Long = 1L
    override suspend fun updateTag(tag: Tag): Int = 1
    override suspend fun deleteTag(from: Tag, to: Tag?) {}
    override fun observeTags(): Flow<List<Tag>> = emptyFlow()
    override suspend fun getTagById(id: Long): Tag? = null
    override suspend fun addPerson(person: Person): Long = 1L
    override suspend fun updatePerson(person: Person): Int = 1
    override suspend fun deletePerson(from: Person, to: Person?) {}
    override fun observePersons(): Flow<List<Person>> = emptyFlow()
    override suspend fun getPersonById(id: Long): Person? = null
    override suspend fun addSource(source: Source): Long = 1L
    override suspend fun updateSource(source: Source): Int = 1
    override suspend fun deleteSource(from: Source, to: Source?) {}
    override fun observeSources(): Flow<List<Source>> = emptyFlow()
    override fun observeSource(sourceId: Long): Flow<Source?> = emptyFlow()
    override suspend fun getSourceById(id: Long): Source? = Source(id = id, name = "Src", balance = 0L, type = 0, colorId = 0, iconId = 0)
    override suspend fun getSourceByIdentifier(identifier: String): Source? = null
    override suspend fun getDefaultSource(): Source? = Source(id = 1, name = "DefSrc", balance = 0L, type = 0, colorId = 0, iconId = 0)
    override fun observeMostUsedCategories(type: TransactionType?, limit: Long): Flow<List<Category>> = emptyFlow()
    override fun observeMostUsedSources(limit: Long): Flow<List<Source>> = emptyFlow()
    override fun observeMostUsedTags(limit: Long): Flow<List<Tag>> = emptyFlow()
    override fun observeMostUsedPersons(limit: Long): Flow<List<Person>> = emptyFlow()
    override fun searchCategories(query: String): Flow<List<Category>> = emptyFlow()
    override fun searchSources(query: String): Flow<List<Source>> = emptyFlow()
    override fun searchPersons(query: String): Flow<List<Person>> = emptyFlow()
    override fun searchTags(query: String): Flow<List<Tag>> = emptyFlow()
    override fun getRecentSearches(): Flow<List<String>> = emptyFlow()
    override suspend fun saveRecentSearch(query: String) {}
    override suspend fun deleteRecentSearch(query: String) {}
    override suspend fun updateCategoryPositions(positions: Map<Long, Int>) {}
    override suspend fun updateSourcePositions(positions: Map<Long, Int>) {}
    override suspend fun updateTagPositions(positions: Map<Long, Int>) {}
    override suspend fun updatePersonPositions(positions: Map<Long, Int>) {}
    override suspend fun getTransactionAmountRange(): Pair<Long, Long> = 0L to 0L
    override suspend fun getTransactionCount(): Long = 0L
    override suspend fun getAllTransactions(): List<Transaction> = emptyList()
}

open class FakeAchievementRepository : AchievementRepository {
    override fun observeStreak(): Flow<Streak> = emptyFlow()
    override fun observeAchievements(): Flow<List<Achievement>> = emptyFlow()
    override suspend fun getStreak(): Streak = Streak(1, 1, null)
    override suspend fun updateStreak(streak: Streak) {}
    override suspend fun initializeAchievements(achievements: List<Pair<AchievementType, Int>>) {}
    override suspend fun getAchievement(type: AchievementType): Achievement? = null
    override suspend fun updateAchievementProgress(type: AchievementType, progress: Int, isUnlocked: Boolean) {}
    override suspend fun unlockAchievement(type: AchievementType) {}
}

open class FakeBudgetRepository : BudgetRepository {
    override fun observeBudgetsWithProgress(from: Long, to: Long): Flow<List<BudgetWithProgress>> = emptyFlow()
    override suspend fun getBudgetWithProgressByCategory(categoryId: Long): BudgetWithProgress? = null
    override suspend fun addBudget(budget: Budget): Long = 1L
    override suspend fun updateBudget(budget: Budget): Int = 1
    override suspend fun deleteBudget(id: Long) {}
    override suspend fun getBudgetByCategoryId(categoryId: Long): Budget? = null
    override suspend fun getSpentAmountByCategory(categoryId: Long, from: Long, to: Long): Long = 0L
    override suspend fun hasAnyBudgets(): Boolean = false
}

open class FakeNotificationManager : com.kazemieh.domain.notification.NotificationManager, com.kazemieh.domain.notification.NotificationScheduler {
    override fun showBudgetAlert(categoryId: Int, categoryName: String, progressPercentage: Int) {}
    override fun createChannels() {}
    override fun createChannel(id: String, name: String, importance: Int) {}
    override fun showNotification(id: Int, title: String, message: String, channelId: String, extraId: Long) {}
    override fun hasPermission(): Boolean = true
    override fun openSettings() {}
    override fun showStickyNotification(id: Int, title: String, message: String, smsDraftId: Long) {}
    override fun showInstallmentNotification(id: Int, title: String, message: String, installmentId: Long) {}
    override fun showQuickAddNotification(title: String, message: String, actionLabel: String) {}
    override fun cancelNotification(id: Int) {}
    
    override fun scheduleReminder(id: String, title: String, message: String, scheduledTime: kotlinx.datetime.LocalDateTime, channelId: String, extraId: Long?) {}
    override fun cancelReminder(id: String) {}
}


open class FakeDebtRepository : DebtRepository {
    private val debts = mutableMapOf<Long, DebtWithRelations>()
    override suspend fun insertDebt(debt: Debt, tagIds: List<Long>): Long {
        debts[debt.id] = DebtWithRelations(debt, Person(1, "Person", null), Category(1, "Cat", null, TransactionType.EXPENSE, 0, 0, 0, null), Source(id = 10, name = "Source", balance = 0L, type = 0, colorId = 0, iconId = 0), emptyList())
        return debt.id
    }
    override suspend fun updateDebt(debt: Debt, tagIds: List<Long>): Int {
        debts[debt.id] = debts[debt.id]!!.copy(debt = debt)
        return 1
    }
    override suspend fun settleDebt(id: Long) {
        val d = debts[id] ?: return
        debts[id] = d.copy(debt = d.debt.copy(isSettled = true))
    }
    override suspend fun deleteDebt(id: Long) { debts.remove(id) }
    override suspend fun getDebtById(id: Long): Debt? = debts[id]?.debt
    
    override fun observeAllDebts(): Flow<List<DebtWithRelations>> = flowOf(debts.values.toList())
    override fun observeDebtsByPerson(personId: Long): Flow<List<DebtWithRelations>> = flowOf(debts.values.filter { it.person.id == personId })
}

open class FakeInstallmentRepository : InstallmentRepository {
    private val installments = mutableMapOf<Long, InstallmentWithRelations>()
    override suspend fun insertInstallment(installment: Installment, tagIds: List<Long>, personIds: List<Long>): Long {
        installments[installment.id] = InstallmentWithRelations(installment, Category(1, "Cat", null, TransactionType.EXPENSE, 0, 0, 0, null), Source(id = 10, name = "Source", balance = 0L, type = 0, colorId = 0, iconId = 0), emptyList(), emptyList())
        return installment.id
    }
    override suspend fun updateInstallment(installment: Installment, tagIds: List<Long>, personIds: List<Long>) {
        installments[installment.id] = installments[installment.id]!!.copy(installment = installment)
    }
    override suspend fun deleteInstallment(id: Long) { installments.remove(id) }
    override suspend fun getInstallmentWithRelations(id: Long): InstallmentWithRelations? = installments[id]
    override fun observeInstallments(): Flow<List<InstallmentWithRelations>> = flowOf(installments.values.toList())
    override suspend fun getInstallmentById(id: Long): Installment? = installments[id]?.installment
}

open class FakeFixedExpenseRepository : FixedExpenseRepository {
    private val fixed = mutableMapOf<Long, FixedExpense>()
    override suspend fun insertFixedExpense(expense: FixedExpense): Long {
        fixed[expense.id] = expense
        return expense.id
    }
    override suspend fun updateFixedExpense(expense: FixedExpense) {
        fixed[expense.id] = expense
    }
    override suspend fun updateNextDueDate(id: Long, nextDueDate: Long) {
        fixed[id] = fixed[id]!!.copy(nextDueDate = nextDueDate)
    }
    override suspend fun deleteFixedExpense(id: Long) { fixed.remove(id) }
    override suspend fun getFixedExpenseById(id: Long): FixedExpense? = fixed[id]
    override fun observeAllFixedExpenses(): Flow<List<FixedExpense>> = flowOf(fixed.values.toList())
    override fun observeFixedExpensesFiltered(query: String?, categoryIds: List<Long>, sourceIds: List<Long>, tagIds: List<Long>, personIds: List<Long>): Flow<List<FixedExpense>> = flowOf(fixed.values.toList())
}
