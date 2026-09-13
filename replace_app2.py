import re

with open('composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("import androidx.compose.foundation.isSystemInDarkTheme\n", "import androidx.compose.foundation.isSystemInDarkTheme\nimport androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.background\n")

with open('composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt', 'w', encoding='utf-8') as f:
    f.write(content)
