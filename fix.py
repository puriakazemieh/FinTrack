import os

def get_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

fake_repo = get_file('core/domain/src/commonTest/kotlin/com/kazemieh/domain/usecase/FakeTransactionRepository.kt')

# Remove FakeCategoryRepository and FakeSourceRepository
fake_repo = fake_repo.split('open class FakeCategoryRepository')[0]

# Fix errors
fake_repo = fake_repo.replace('override fun cancelAllReminders() {} ', '')
fake_repo = fake_repo.replace('open class FakeGamificationRepository : GamificationRepository', 'open class FakeGamificationRepository : com.kazemieh.domain.repository.GamificationRepository')
fake_repo = fake_repo.replace('DebtWithRelations(debt, Category(1, "Cat", null, TransactionType.EXPENSE, 0, 0, 0, null), Person(1, "Person", null)', 'DebtWithRelations(debt, Person(1, "Person", null), Category(1, "Cat", null, TransactionType.EXPENSE, 0, 0, 0, null)')
fake_repo = fake_repo.replace('suspend fun getDebtById(id: Long): DebtWithRelations?', 'suspend fun getDebtById(id: Long): DebtWithRelations?') # Wait, the error said "getDebtById(id: Long): Debt?" was expected!
fake_repo = fake_repo.replace('suspend fun getDebtById(id: Long): DebtWithRelations? = debts[id]', 'suspend fun getDebtById(id: Long): Debt? = debts[id]?.debt\n    override suspend fun getDebtWithRelationsById(id: Long): DebtWithRelations? = debts[id]')
fake_repo = fake_repo.replace('observeDebtsByPerson(personId: Long): Flow<List<DebtWithRelations>> = flowOf(debts.values.filter { it.personId == personId })', 'observeDebtsByPerson(personId: Long): Flow<List<DebtWithRelations>> = flowOf(debts.values.filter { it.person?.id == personId })')

fake_repo = fake_repo.replace('class FakeInstallmentRepository', 'class FakeInstallmentRepository')
fake_repo = fake_repo.replace('observeInstallments(): Flow<List<InstallmentWithRelations>> = flowOf(installments.values.toList())', 'observeInstallments(): Flow<List<InstallmentWithRelations>> = flowOf(installments.values.toList())\n    override suspend fun getInstallmentById(id: Long): Installment? = installments[id]?.installment')
fake_repo = fake_repo.replace('updateInstallment(installment: Installment, tagIds: List<Long>, personIds: List<Long>): Int {', 'updateInstallment(installment: Installment, tagIds: List<Long>, personIds: List<Long>) {')
fake_repo = fake_repo.replace('installments[installment.id] = installments[installment.id]!!.copy(installment = installment)\n        return 1', 'installments[installment.id] = installments[installment.id]!!.copy(installment = installment)')

fake_repo = fake_repo.replace('suspend fun updateFixedExpense(expense: FixedExpense): Int {', 'suspend fun updateFixedExpense(expense: FixedExpense) {')
fake_repo = fake_repo.replace('fixed[expense.id] = expense\n        return 1', 'fixed[expense.id] = expense')
fake_repo = fake_repo.replace('suspend fun updateNextDueDate(id: Long, nextDueDate: Long): Int {', 'suspend fun updateNextDueDate(id: Long, nextDueDate: Long) {')
fake_repo = fake_repo.replace('fixed[id] = fixed[id]!!.copy(nextDueDate = nextDueDate)\n        return 1', 'fixed[id] = fixed[id]!!.copy(nextDueDate = nextDueDate)')

write_file('core/domain/src/commonTest/kotlin/com/kazemieh/domain/usecase/FakeTransactionRepository.kt', fake_repo)

test_file = get_file('core/domain/src/commonTest/kotlin/com/kazemieh/domain/usecase/IdempotencyTest.kt')
test_file = test_file.replace('timeStamp = 0L', 'createdAt = 0L')
test_file = test_file.replace('createdAt = 0L', 'createdAt = 0L') # oops, fixing...
write_file('core/domain/src/commonTest/kotlin/com/kazemieh/domain/usecase/IdempotencyTest.kt', test_file)
