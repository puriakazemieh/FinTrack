import re

with open('core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/component/glass/AddFrame.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("preventSwipeDismiss: Boolean = true,", "preventSwipeDismiss: Boolean = false,")

with open('core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/component/glass/AddFrame.kt', 'w', encoding='utf-8') as f:
    f.write(content)
