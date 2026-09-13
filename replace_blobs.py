import re

with open('composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'''            androidx\.compose\.foundation\.layout\.Box\(
                modifier = androidx\.compose\.ui\.Modifier
                    \.fillMaxSize\(\)
                    \.background\(androidx\.compose\.material3\.MaterialTheme\.colorScheme\.background\)
            \) \{
                if \(isReady\) \{'''

replacement = '''            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
            ) {
                com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs()
                if (isReady) {'''

content = re.sub(pattern, replacement, content)

with open('composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt', 'w', encoding='utf-8') as f:
    f.write(content)
