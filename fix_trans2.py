import os

fpath = 'feature-share/ledger/src/commonMain/kotlin/com/kazemieh/transaction/ui/add/AddTransactionBottomSheet.kt'
with open(fpath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('import androidx.compose.foundation.layout.fillMaxWidth\n', 'import androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.ColumnScope\n')

with open(fpath, 'w', encoding='utf-8') as f:
    f.write(content)
