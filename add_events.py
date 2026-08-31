import os

viewmodels = {
    'FAQViewModel.kt': 'faq',
    'NewsViewModel.kt': 'news',
    'SupportViewModel.kt': 'support',
    'EventsViewModel.kt': 'events',
    'PersonDetailViewModel.kt': 'person_detail'
}

for root, dirs, files in os.walk('.'):
    for file in files:
        if file in viewmodels:
            path = os.path.join(root, file)
            key = viewmodels[file]
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            content = content.replace(f'class {file.split(".")[0]}(', f'class {file.split(".")[0]}(\n    private val analytics: com.kazemieh.common.analytics.AnalyticsService,')
            
            if 'init {' in content:
                content = content.replace('init {', f'init {{\n        analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened(\"{key}\"))')
            else:
                content = content.replace('ViewModel() {', f'ViewModel() {{\n\n    init {{\n        analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened(\"{key}\"))\n    }}')

            with open(path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f'Updated {file}')
