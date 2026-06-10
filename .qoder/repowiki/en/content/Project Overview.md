# Project Overview

<cite>
**Referenced Files in This Document**
- [README.md](file://README.md)
- [FinTrackApplication.kt](file://app/src/main/java/com/kazemieh/fintrack/FinTrackApplication.kt)
- [App.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt)
- [Transaction.kt](file://core/common/src/commonMain/kotlin/com/kazemieh/common/model/Transaction.kt)
- [AddTransactionUseCase.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/usecase/AddTransactionUseCase.kt)
- [DashboardViewModel.kt](file://feature-container/dashboard/src/commonMain/kotlin/com/kazemieh/dashboard/DashboardViewModel.kt)
- [TransactionsViewModel.kt](file://feature-container/transactions/src/commonMain/kotlin/com/kazemieh/transactions/TransactionsViewModel.kt)
- [TransactionRepository.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/TransactionRepository.kt)
- [TransactionRepositoryImpl.kt](file://core/data/src/commonMain/kotlin/com/kazemieh/data/repository/TransactionRepositoryImpl.kt)
- [OnboardingViewModel.kt](file://feature-container/onboarding/src/commonMain/kotlin/com/kazemieh/onboarding/ui/OnboardingViewModel.kt)
- [DriverFactory.kt](file://core/database/src/commonMain/kotlin/com/kazemieh/database/DriverFactory.kt)
- [composeApp/build.gradle.kts](file://composeApp/build.gradle.kts)
- [PersianDateConverterImpl.kt](file://core/common/src/commonMain/kotlin/com/kazemieh/common/persiandatetime/PersianDateConverterImpl.kt)
- [MoneyFormatter.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/MoneyFormatter.kt)
- [CalculatorParser.kt](file://core/common/src/commonMain/kotlin/com/kazemieh/common/util/CalculatorParser.kt)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Core Components](#core-components)
4. [Architecture Overview](#architecture-overview)
5. [Detailed Component Analysis](#detailed-component-analysis)
6. [Dependency Analysis](#dependency-analysis)
7. [Performance Considerations](#performance-considerations)
8. [Troubleshooting Guide](#troubleshooting-guide)
9. [Conclusion](#conclusion)

## Introduction
FinTrack is a modern personal finance management application designed to help users track income, expenses, and account balances across platforms. It emphasizes a clean, modular architecture with Kotlin Multiplatform (KMP) readiness, a Persian localization layer, and a Material 3–driven Compose UI. The project’s roadmap targets full KMP support for Android, iOS, Desktop, and Web, enabling a single codebase to power diverse environments while maintaining platform-specific capabilities.

Key value proposition:
- Cross-platform personal finance tracking with a unified UI and data model
- Persian calendar and currency localization for Persian-speaking users
- Modular Clean Architecture with MVI-style state management for predictable UI updates
- Reactive data flows powered by Kotlin Coroutines and Flow
- Practical workflows for everyday tasks: adding transactions, viewing dashboards, and generating filtered reports

Target audience:
- Persian-speaking individuals seeking a localized, privacy-first finance tracker
- Developers who value modular architecture, testability, and multiplatform scalability

Differentiators:
- Persian localization integrated at the date/time converter, formatter, and UI resource layers
- Strong separation of concerns via Clean Architecture and explicit repository boundaries
- MVI-inspired view models with immutable UI state and deterministic intents
- KMP-first Gradle setup with shared modules and expect/actual drivers for SQLDelight

Positioning:
- A developer-friendly, extensible personal finance app that evolves toward full Kotlin Multiplatform support
- Suitable for both beginners (guided onboarding and simple flows) and advanced users (deep filtering, reporting, and localization)

## Project Structure
FinTrack organizes functionality into layered modules:
- app: Android application entry point and platform-specific initialization
- composeApp: Compose Multiplatform entry point and DI bootstrap for shared UI
- core: Shared libraries for models, domain logic, data access, database, design system, money formatting, preferences, and storage
- feature-container: Container modules for screens like dashboard, onboarding, transactions, profile, and tools
- feature-share: Feature modules for entities like transactions, categories, tags, sources, persons, search, lock, and notifications

```mermaid
graph TB
subgraph "Platform Entry"
APP["Android App<br/>FinTrackApplication.kt"]
COMPOSEAPP["Compose Multiplatform<br/>App.kt"]
end
subgraph "Core Modules"
COMMON["core/common<br/>models, utils, Persian datetime"]
DOMAIN["core/domain<br/>use cases, repository interfaces"]
DATA["core/data<br/>repositories, data sources"]
DATABASE["core/database<br/>SQLDelight, driver factory"]
DESIGN["core/designsystem<br/>theme, typography, components"]
MONEY["core/money<br/>currency formatter"]
PREFERENCES["core/preferences<br/>settings wrapper"]
STORAGE["core/storage<br/>image storage"]
end
subgraph "Features"
DASHBOARD["feature-container/dashboard<br/>DashboardViewModel.kt"]
ONBOARD["feature-container/onboarding<br/>OnboardingViewModel.kt"]
TRANSCONTAINER["feature-container/transactions<br/>TransactionsViewModel.kt"]
end
APP --> COMPOSEAPP
COMPOSEAPP --> COMMON
COMPOSEAPP --> DOMAIN
COMPOSEAPP --> DATA
COMPOSEAPP --> DATABASE
COMPOSEAPP --> DESIGN
COMPOSEAPP --> MONEY
COMPOSEAPP --> PREFERENCES
COMPOSEAPP --> STORAGE
DOMAIN --> DATA
DATA --> DATABASE
COMMON --> DOMAIN
COMMON --> DATA
COMMON --> MONEY
COMMON --> PREFERENCES
COMMON --> STORAGE
DASHBOARD --> DOMAIN
ONBOARD --> DOMAIN
TRANSCONTAINER --> DOMAIN
```

**Diagram sources**
- [FinTrackApplication.kt:14-22](file://app/src/main/java/com/kazemieh/fintrack/FinTrackApplication.kt#L14-L22)
- [App.kt:94-133](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt#L94-L133)
- [DriverFactory.kt:1-7](file://core/database/src/commonMain/kotlin/com/kazemieh/database/DriverFactory.kt#L1-L7)

**Section sources**
- [README.md:21-27](file://README.md#L21-L27)
- [composeApp/build.gradle.kts:17-116](file://composeApp/build.gradle.kts#L17-L116)

## Core Components
- Transaction model and type: Defines income, expense, and transfer semantics with serialization and formatting helpers for Persian digits and amounts.
- Use cases: Encapsulate business operations like adding transactions with balance impact calculations.
- View models: Manage UI state using MVI-style intents and immutable state snapshots.
- Repositories: Define contracts for data access and expose reactive streams for lists and aggregates.
- Localization: Persian date conversion and money formatting for culturally appropriate display.
- Onboarding: Guided setup to initialize default data and preferences.

Practical examples:
- Recording a transaction: Use AddTransactionUseCase with a Transaction payload and related identifiers; the use case computes balance impacts and persists the record.
- Dashboard analytics: Observe totals and visibility toggles via DashboardViewModel intents and state.
- Report generation: Filter transactions by type, categories, sources, tags, persons, and date ranges using TransactionsViewModel filters and date helpers.

**Section sources**
- [Transaction.kt:7-36](file://core/common/src/commonMain/kotlin/com/kazemieh/common/model/Transaction.kt#L7-L36)
- [AddTransactionUseCase.kt:7-18](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/usecase/AddTransactionUseCase.kt#L7-L18)
- [DashboardViewModel.kt:13-83](file://feature-container/dashboard/src/commonMain/kotlin/com/kazemieh/dashboard/DashboardViewModel.kt#L13-L83)
- [TransactionsViewModel.kt:51-526](file://feature-container/transactions/src/commonMain/kotlin/com/kazemieh/transactions/TransactionsViewModel.kt#L51-L526)
- [TransactionRepository.kt:16-93](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/TransactionRepository.kt#L16-L93)
- [TransactionRepositoryImpl.kt:22-235](file://core/data/src/commonMain/kotlin/com/kazemieh/data/repository/TransactionRepositoryImpl.kt#L22-L235)
- [PersianDateConverterImpl.kt:12-104](file://core/common/src/commonMain/kotlin/com/kazemieh/common/persiandatetime/PersianDateConverterImpl.kt#L12-L104)
- [MoneyFormatter.kt:5-14](file://core/money/src/commonMain/kotlin/com/kazemieh/money/MoneyFormatter.kt#L5-L14)
- [OnboardingViewModel.kt:15-70](file://feature-container/onboarding/src/commonMain/kotlin/com/kazemieh/onboarding/ui/OnboardingViewModel.kt#L15-L70)

## Architecture Overview
FinTrack follows Clean Architecture with distinct layers:
- Presentation: Compose UI with MVI-style view models
- Domain: Use cases and repository interfaces
- Data: Repository implementations delegating to local data sources
- Infrastructure: SQLDelight database and DI via Koin

```mermaid
graph TB
UI["Compose UI Screens<br/>Dashboard, Transactions, Onboarding"]
VM_DASH["DashboardViewModel.kt"]
VM_TRANS["TransactionsViewModel.kt"]
VM_ONB["OnboardingViewModel.kt"]
UC["Domain Use Cases<br/>AddTransactionUseCase.kt"]
REPO_INTF["TransactionRepository.kt"]
REPO_IMPL["TransactionRepositoryImpl.kt"]
DS["Local Data Source<br/>SQLDelight"]
DB["SQLDelight Database<br/>DriverFactory.kt"]
UI --> VM_DASH
UI --> VM_TRANS
UI --> VM_ONB
VM_DASH --> UC
VM_TRANS --> UC
VM_ONB --> UC
UC --> REPO_INTF
REPO_INTF --> REPO_IMPL
REPO_IMPL --> DS
DS --> DB
```

**Diagram sources**
- [DashboardViewModel.kt:13-16](file://feature-container/dashboard/src/commonMain/kotlin/com/kazemieh/dashboard/DashboardViewModel.kt#L13-L16)
- [TransactionsViewModel.kt:51-53](file://feature-container/transactions/src/commonMain/kotlin/com/kazemieh/transactions/TransactionsViewModel.kt#L51-L53)
- [OnboardingViewModel.kt:15-17](file://feature-container/onboarding/src/commonMain/kotlin/com/kazemieh/onboarding/ui/OnboardingViewModel.kt#L15-L17)
- [AddTransactionUseCase.kt:7-9](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/usecase/AddTransactionUseCase.kt#L7-L9)
- [TransactionRepository.kt:16-16](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/TransactionRepository.kt#L16-L16)
- [TransactionRepositoryImpl.kt:22-25](file://core/data/src/commonMain/kotlin/com/kazemieh/data/repository/TransactionRepositoryImpl.kt#L22-L25)
- [DriverFactory.kt:1-7](file://core/database/src/commonMain/kotlin/com/kazemieh/database/DriverFactory.kt#L1-L7)

## Detailed Component Analysis

### Transaction Recording Workflow
This sequence illustrates adding a transaction with balance impact calculation and persistence.

```mermaid
sequenceDiagram
participant UI as "Compose UI"
participant VM as "TransactionsViewModel.kt"
participant UC as "AddTransactionUseCase.kt"
participant REPO as "TransactionRepository.kt"
participant IMPL as "TransactionRepositoryImpl.kt"
participant DS as "Local Data Source"
participant DB as "SQLDelight Database"
UI->>VM : "User submits transaction form"
VM->>UC : "invoke(transaction, tagIds, personIds)"
UC->>REPO : "addTransactionWithBalance(..., balanceDeltas)"
REPO->>IMPL : "delegate call"
IMPL->>DS : "persist transaction and relations"
DS->>DB : "execute SQL"
DB-->>DS : "insert success"
DS-->>IMPL : "return new id"
IMPL-->>REPO : "return new id"
REPO-->>UC : "return new id"
UC-->>VM : "return new id"
VM-->>UI : "update state and close bottom sheet"
```

**Diagram sources**
- [TransactionsViewModel.kt:77-84](file://feature-container/transactions/src/commonMain/kotlin/com/kazemieh/transactions/TransactionsViewModel.kt#L77-L84)
- [AddTransactionUseCase.kt:10-17](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/usecase/AddTransactionUseCase.kt#L10-L17)
- [TransactionRepository.kt:18-35](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/TransactionRepository.kt#L18-L35)
- [TransactionRepositoryImpl.kt:66-85](file://core/data/src/commonMain/kotlin/com/kazemieh/data/repository/TransactionRepositoryImpl.kt#L66-L85)

**Section sources**
- [AddTransactionUseCase.kt:7-18](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/usecase/AddTransactionUseCase.kt#L7-L18)
- [TransactionRepository.kt:16-93](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/TransactionRepository.kt#L16-L93)
- [TransactionRepositoryImpl.kt:22-235](file://core/data/src/commonMain/kotlin/com/kazemieh/data/repository/TransactionRepositoryImpl.kt#L22-L235)

### Dashboard Analytics State Flow
The dashboard uses MVI intents to toggle overlays and manage visibility of balance and charts.

```mermaid
flowchart TD
Start(["User opens Dashboard"]) --> Init["Load preferences<br/>theme and currency"]
Init --> Ready{"Database initialized?"}
Ready --> |No| Wait["Wait for initialization"]
Ready --> |Yes| Decide["Decide start destination<br/>Onboarding vs BottomBarGraph"]
Decide --> Render["Render FintrackTheme<br/>LockGate wrapper"]
Render --> StateChange["User toggles visibility or animations"]
StateChange --> Update["Update DashboardState<br/>showAddTransaction, isBalanceVisible, enableAnimationChart"]
Update --> Render
```

**Diagram sources**
- [App.kt:72-92](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt#L72-L92)
- [DashboardViewModel.kt:18-54](file://feature-container/dashboard/src/commonMain/kotlin/com/kazemieh/dashboard/DashboardViewModel.kt#L18-L54)

**Section sources**
- [DashboardViewModel.kt:13-83](file://feature-container/dashboard/src/commonMain/kotlin/com/kazemieh/dashboard/DashboardViewModel.kt#L13-L83)
- [App.kt:55-92](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt#L55-L92)

### Report Generation and Filtering
Transactions screen supports multi-dimensional filtering and date range selection with Persian-aware labels.

```mermaid
flowchart TD
Open(["Open Transactions Screen"]) --> LoadDefaults["Initialize filters<br/>this month, all types"]
LoadDefaults --> ApplyType["Select transaction type"]
ApplyType --> ToggleSources["Toggle sources sheet"]
ToggleSources --> SelectSources["Select sources"]
SelectSources --> ToggleCategories["Toggle categories sheet"]
ToggleCategories --> SelectCategories["Select categories"]
SelectCategories --> ToggleTags["Toggle tags sheet"]
ToggleTags --> SelectTags["Select tags"]
SelectTags --> TogglePersons["Toggle persons sheet"]
TogglePersons --> SelectPersons["Select persons"]
SelectPersons --> ToggleDates["Toggle date sheet"]
ToggleDates --> ChooseRange["Choose preset or custom range"]
ChooseRange --> Submit["Apply filters"]
Submit --> UpdateState["Update TransactionsState<br/>with selected filters"]
UpdateState --> Render["Render filtered list and charts"]
```

**Diagram sources**
- [TransactionsViewModel.kt:58-61](file://feature-container/transactions/src/commonMain/kotlin/com/kazemieh/transactions/TransactionsViewModel.kt#L58-L61)
- [TransactionsViewModel.kt:77-336](file://feature-container/transactions/src/commonMain/kotlin/com/kazemieh/transactions/TransactionsViewModel.kt#L77-L336)

**Section sources**
- [TransactionsViewModel.kt:51-526](file://feature-container/transactions/src/commonMain/kotlin/com/kazemieh/transactions/TransactionsViewModel.kt#L51-L526)

### Persian Localization and Money Formatting
- Persian date conversion: Converts Gregorian dates to Persian calendar and vice versa using a dedicated converter implementation.
- Money formatting: Formats amounts with Persian digits and optional currency symbol.
- UI text resources: Uses localized strings for date range labels and period summaries.

```mermaid
classDiagram
class PersianDateConverterImpl {
+fromGregorian(date) PersianDateTime
+toGregorian(date) LocalDateTime
}
class MoneyFormatter {
+format(amount, currency, includeSymbol) String
}
class Transaction {
+amount : Int
+amountTransfer : Int
+type : TransactionType
+amountTransferFormated : String
}
PersianDateConverterImpl ..> Transaction : "used for Persian month/day labels"
MoneyFormatter ..> Transaction : "formats amounts"
```

**Diagram sources**
- [PersianDateConverterImpl.kt:12-104](file://core/common/src/commonMain/kotlin/com/kazemieh/common/persiandatetime/PersianDateConverterImpl.kt#L12-L104)
- [MoneyFormatter.kt:5-14](file://core/money/src/commonMain/kotlin/com/kazemieh/money/MoneyFormatter.kt#L5-L14)
- [Transaction.kt:22-24](file://core/common/src/commonMain/kotlin/com/kazemieh/common/model/Transaction.kt#L22-L24)

**Section sources**
- [PersianDateConverterImpl.kt:12-104](file://core/common/src/commonMain/kotlin/com/kazemieh/common/persiandatetime/PersianDateConverterImpl.kt#L12-L104)
- [MoneyFormatter.kt:5-14](file://core/money/src/commonMain/kotlin/com/kazemieh/money/MoneyFormatter.kt#L5-L14)
- [TransactionsViewModel.kt:338-415](file://feature-container/transactions/src/commonMain/kotlin/com/kazemieh/transactions/TransactionsViewModel.kt#L338-L415)

### Onboarding and First Run Experience
Onboarding guides users through steps, optionally allowing them to define a custom financial source and seed default data.

```mermaid
sequenceDiagram
participant UI as "Onboarding UI"
participant VM as "OnboardingViewModel.kt"
participant UC as "SeedDataUseCase.kt"
participant PREF as "Preferences"
UI->>VM : "User taps Next/Skip/Finish"
VM->>VM : "Update state (currentStep, sourceName, sourceBalance)"
VM->>UC : "seedDataUseCase(customSource)"
UC->>PREF : "write first-run flag and defaults"
UC-->>VM : "success"
VM-->>UI : "NavigateToDashboard"
```

**Diagram sources**
- [OnboardingViewModel.kt:25-69](file://feature-container/onboarding/src/commonMain/kotlin/com/kazemieh/onboarding/ui/OnboardingViewModel.kt#L25-L69)

**Section sources**
- [OnboardingViewModel.kt:15-90](file://feature-container/onboarding/src/commonMain/kotlin/com/kazemieh/onboarding/ui/OnboardingViewModel.kt#L15-L90)

## Dependency Analysis
- DI bootstrap: App.kt initializes Koin and loads all feature and core modules, ensuring database initialization and theme/currency preferences are available early.
- Platform expectations: DriverFactory defines an expect declaration for SQLDelight driver creation, enabling platform-specific implementations.
- Build configuration: composeApp/build.gradle.kts declares Android/iOS/Desktop/Web targets and includes all shared modules, preparing for full KMP rollout.

```mermaid
graph LR
APP["FinTrackApplication.kt"] --> INIT["initKoin() in App.kt"]
INIT --> MODS["Modules list<br/>common, data, domain, database, designsystem, money, preferences, storage"]
MODS --> DBINIT["DatabaseInitializer"]
MODS --> THEMES["Theme/Currency Preferences"]
MODS --> FEATURES["Feature Modules<br/>dashboard, transactions, onboarding"]
```

**Diagram sources**
- [FinTrackApplication.kt:17-21](file://app/src/main/java/com/kazemieh/fintrack/FinTrackApplication.kt#L17-L21)
- [App.kt:94-133](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt#L94-L133)
- [DriverFactory.kt:6-6](file://core/database/src/commonMain/kotlin/com/kazemieh/database/DriverFactory.kt#L6-L6)

**Section sources**
- [FinTrackApplication.kt:10-22](file://app/src/main/java/com/kazemieh/fintrack/FinTrackApplication.kt#L10-L22)
- [App.kt:94-133](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt#L94-L133)
- [composeApp/build.gradle.kts:17-116](file://composeApp/build.gradle.kts#L17-L116)

## Performance Considerations
- Reactive streams: Use Flow-based repositories to avoid blocking and to propagate updates efficiently to UI.
- Immutable state: MVI-style state updates minimize UI recompositions and reduce accidental mutations.
- Localization computations: Prefer cached or precomputed localized strings for date ranges to avoid repeated conversions during scrolling.
- Filtering: Apply filters at the data layer (SQLDelight) to limit UI rendering work and improve responsiveness.

## Troubleshooting Guide
- Database initialization: Ensure DatabaseInitializer runs before rendering screens; otherwise, UI may attempt to query uninitialized tables.
- Theme and currency preferences: Verify preferences are loaded before composing themed UI; otherwise, defaults are applied.
- Platform driver: Confirm the expect/actual driver is correctly configured per platform; missing implementations prevent DB creation.
- Onboarding completion: If navigation to the dashboard fails after onboarding, check SeedDataUseCase execution and preference writes.

**Section sources**
- [App.kt:72-92](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt#L72-L92)
- [DriverFactory.kt:6-6](file://core/database/src/commonMain/kotlin/com/kazemieh/database/DriverFactory.kt#L6-L6)
- [OnboardingViewModel.kt:47-69](file://feature-container/onboarding/src/commonMain/kotlin/com/kazemieh/onboarding/ui/OnboardingViewModel.kt#L47-L69)

## Conclusion
FinTrack delivers a modern, cross-platform personal finance experience grounded in Clean Architecture and MVI. Its Persian localization and KMP-focused structure position it for broad reach while maintaining developer productivity and user-centric design. The documented workflows—transaction recording, dashboard analytics, and report generation—provide clear entry points for both beginners and experienced developers to engage with the codebase and extend functionality.