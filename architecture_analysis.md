# Architecture Analysis - FinTrack

## 1. Repository Structure Analysis
FinTrack is built using **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, following a **Modular Clean Architecture** pattern. It supports Android, iOS, Desktop (JVM), and Web (JS).

### Module Map
- **`:app`**: Android-specific entry point and application class.
- **`:composeApp`**: Main aggregator module. Contains `AppNavHost`, global UI state, and entry points for all platforms (androidMain, iosMain, jvmMain, webMain).
- **`:core`**
    - **`:common`**: Low-level utilities, base models (Transaction, Category, etc.), logging, and date helpers. No dependencies on other modules.
    - **`:data`**: Implementations of Repository interfaces. Orchestrates between local and (potentially) remote data sources.
    - **`:data-contract`**: Interfaces for DataSources. Used to break circular dependencies between `:core:data` and `:core:database`.
    - **`:database`**: SQLDelight database implementation and LocalDataSource implementations.
    - **`:designsystem`**: Shared UI components, theme (FintrackTheme), spacing, and resources (strings, icons, fonts).
    - **`:domain`**: Pure Kotlin module. Contains Repository interfaces and UseCases.
- **`:feature-container`** (Higher-level UI aggregators)
    - **`:dashboard`**: Main dashboard screen.
    - **`:report`**: Reporting and filtering screens.
    - **`:setting`**: App settings.
- **`:feature-share`** (Shared feature components)
    - **`:transaction`**: Transaction CRUD and list UI.
    - **`:category`**: Category management UI.
    - **`:source`**: Financial source management UI.
    - **`:tags`**: Tag management UI.
    - **`:person`**: Person/Contact management UI.

## 2. Dependency Flow
The project follows a "Circular Dependency Avoidance" strategy:
`UI (Features) -> `:core:domain` -> `:core:common`
`UI (Features) -> `:core:designsystem`
`:core:data` -> `:core:domain`
`:core:data` -> `:core:data-contract`
`:core:database` -> `:core:data-contract`
`:composeApp` depends on all feature and core modules to assemble the final application.

## 3. Core Systems
- **Navigation**: Uses `navigation-compose` with **Type-Safe Navigation** (Kotlin Serialization). Routes are defined in `Screen.kt`.
- **Dependency Injection**: Uses **Koin**. Modules are defined per feature/core layer and aggregated in `App.kt`.
- **State Management**: **MVI-inspired pattern**. ViewModels expose a `StateFlow<State>` and a `Flow<Effect>`, and accept `Intent` via an `onIntent` function.
- **Networking**: Currently local-only. No networking module is active in `settings.gradle.kts`.
- **Resources**: Uses **Compose Multiplatform Resources** (`org.jetbrains.compose.resources`). All strings and icons are in `:core:designsystem`.

## 4. Architectural Risks & Inconsistencies
- **God Repository**: `TransactionRepository` handles Transactions, Categories, Sources, Tags, and Persons. This violates the Interface Segregation Principle.
- **Feature Coupling**: `feature-share:transaction` depends on multiple other `feature-share` modules for their "Picker" BottomSheets.
- **Direct DataSource Dependency**: Some logic might be leaking from Domain to Data or vice versa due to the God Repository structure.

## 5. Recommendations for Scalability
- **Split Repositories**: Create separate repositories for `Category`, `Source`, `Tag`, and `Person`.
- **Introduce Service/Manager Layer**: For complex calculations like balance impact, move logic out of repositories into domain services.
- **UI Contract Module**: Create a module for shared UI interfaces if feature coupling becomes a problem.
- **Networking Ready**: Define a `:core:network` module with Ktor to be ready for sync features.
