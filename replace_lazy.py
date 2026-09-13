import os
import glob

files = [
    'feature-share/budget/src/commonMain/kotlin/com/kazemieh/budget/ui/add/AddBudgetBottomSheet.kt',
    'feature-share/category/src/commonMain/kotlin/com/kazemieh/category/ui/add/AddCategoryBottomSheet.kt',
    'feature-share/check/src/commonMain/kotlin/com/kazemieh/check/ui/add/AddCheckBottomSheet.kt',
    'feature-share/debt/src/commonMain/kotlin/com/kazemieh/debt/ui/add/AddDebtBottomSheet.kt',
    'feature-share/fixed-expense/src/commonMain/kotlin/com/kazemieh/fixed_expense/ui/detail/AddFixedExpenseBottomSheet.kt',
    'feature-share/goals/src/commonMain/kotlin/com/kazemieh/goals/presentation/add/AddGoalBottomSheet.kt',
    'feature-share/installment/src/commonMain/kotlin/com/kazemieh/installment/ui/add/AddInstallmentBottomSheet.kt',
    'feature-share/ledger/src/commonMain/kotlin/com/kazemieh/transaction/ui/add/AddTransactionBottomSheet.kt',
    'feature-share/person/src/commonMain/kotlin/com/kazemieh/person/ui/add/AddPersonBottomSheet.kt',
    'feature-share/source/src/commonMain/kotlin/com/kazemieh/financialsource/ui/add/AddSourceBottomSheet.kt',
    'feature-share/tags/src/commonMain/kotlin/com/kazemieh/tag/ui/add/AddTagBottomSheet.kt'
]

for fpath in files:
    if os.path.exists(fpath):
        with open(fpath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Replace LazyColumn(modifier = Modifier.fillMaxWidth(), with LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f),
        content = content.replace('LazyColumn(\n            modifier = Modifier.fillMaxWidth(),', 'LazyColumn(\n            modifier = Modifier.fillMaxWidth().weight(1f),')
        content = content.replace('LazyColumn(\n        modifier = Modifier.fillMaxWidth(),', 'LazyColumn(\n        modifier = Modifier.fillMaxWidth().weight(1f),')
        content = content.replace('LazyColumn(\n        modifier = Modifier\n            .fillMaxWidth(),', 'LazyColumn(\n        modifier = Modifier\n            .fillMaxWidth().weight(1f),')

        with open(fpath, 'w', encoding='utf-8') as f:
            f.write(content)
