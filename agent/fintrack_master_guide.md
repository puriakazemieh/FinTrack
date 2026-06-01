# FinTrack: Comprehensive Architecture & Development Guide

This document provides a full overview of the FinTrack project, its architecture, development conventions, and current technical status.

---

## 1. Project Overview & Architecture

### Stack
- **Kotlin Multiplatform (KMP)**
- **Compose Multiplatform** (Android, iOS, Desktop, Web)
- **Koin** (Dependency Injection)
- **SQLDelight** (Local Database)
- **Jetpack Lifecycle** (Shared ViewModels)
- **Navigation Compose** (Type-Safe Navigation)

### Module Map
- **`:composeApp`**: The aggregator. Contains platform entry points and global navigation (`AppNavHost`).
- **`:core`**
    - **`:common`**: Low-level models and utilities.
    - **`:domain`**: Business logic, UseCases, and Repository interfaces.
    - **`:data`**: Repository implementations.
    - **`:database`**: SQLDelight setup and LocalDataSources.
    - **`:data-contract`**: DataSource interfaces to prevent circular deps.
    - **`:designsystem`**: Theme, shared components, and resources.
- **`:feature-container`**: Main screens (Dashboard, Report, Setting).
- **`:feature-share`**: Shared modules (Transaction, Category, Source, Tags, Person).

### Dependency Flow
`UI (Features) -> Domain -> Common`
`Data -> Domain`
`Data -> Data-Contract <- Database`

---

## 2. Actual develop module layout (from settings.gradle.kts)

```
:app
:composeApp
:core:common · :core:data · :core:data-contract · :core:database · :core:designsystem · :core:domain
:feature-share:transaction · :feature-share:category · :feature-share:source · :feature-share:tags · :feature-share:person
:feature-container:report · :feature-container:dashboard · :feature-container:setting
```

> این handoff دقیقاً روی همین ماژول‌ها بنا می‌شود. ماژول‌های جدید با همین الگو (`:feature-share:*`، `:feature-container:*`، `:core:*`) اضافه می‌شوند.
> EN: New modules follow the same conventions. Renames: `:feature-container:report → :feature-container:transactions`, `:feature-container:setting → :feature-container:profile`, plus new `:feature-container:tools`.

---

## 3. Workflow - روش کار با Gemini
کل `design_handoff/` را در ریشه کپی کن (gitignore). برای هر فاز بلوک «Gemini prompt» را عیناً بده. بعد از هر فاز: `./gradlew assembleDebug` → تست دستگاه → commit → فاز بعد. هر فاز اپ را در حالت **پایدار و قابل‌انتشار** نگه می‌دارد.
EN: Copy `design_handoff/` to root (gitignore). Feed each phase's prompt verbatim. After each: build → test → commit → next. Every phase leaves a shippable app.

---

## 4. Development Conventions

### Screen Pattern
- Name: `[Name]Screen.kt`
- Use `koinViewModel()` and `collectAsStateWithLifecycle()`.
- Root should be a `Box` for layering UI and Overlays.

### MVI State Management
Every screen/feature uses an MVI-inspired pattern:
- **State**: Immutable data class.
- **Intent**: User actions (Sealed Interface).
- **Effect**: One-time side effects like Navigation or Snakbars (Sealed Interface).
- **ViewModel**: Shared across platforms, handling `onIntent`.

### UseCase & Repository
- **UseCases**: Single responsibility, `invoke` operator.
- **Repositories**: Interface in Domain, Implementation in Data.
- **DataSources**: Interfaces in Data-Contract, Implementation in Database.

### Resource Management
- **Strings/Icons**: Use `compose-resources` in `:core:designsystem`.
- **UI Strings**: Use `UiText` wrapper for ViewModel-to-UI communication.
- **Theme**: Always use `FintrackTheme` and `LocalSpacing`.

---

## 5. Reusable Implementation Patterns

| Pattern | Implementation Location | How it Works |
| :--- | :--- | :--- |
| **Pickers** | `feature-share:[module]:ui:list` | BottomSheets used to select categories, sources, etc. |
| **MVI Flow** | ViewModels | `onIntent` -> State Update -> UI Recomposition. |
| **Error Handling** | ViewModels / UI | `runCatching` in VM -> `Effect.ShowMessage` -> Snackbar in UI. |
| **Validation** | ViewModels | Logic in VM updates `isError` flags in the State. |
| **Navigation** | `composeApp:navigation` | Type-safe classes in `Screen.kt`. |

---

## 6. Technical Debt & Scalability Review

### Important Issues
1.  **God Repository**: `TransactionRepository` is overloaded. It should be split into `Transaction`, `Category`, `Source`, etc.
2.  **Logic Leak**: Balance calculation logic should be moved from the data layer to Domain UseCases.
3.  **Error Handling**: Standardize on a `Result` or `Either` type for all data operations.

### Scalability Recommendations
- **Decouple Features**: Reduce dependencies between `feature-share` modules by using a shared UI-Contract or event-based communication.
- **Service Layer**: Introduce a Domain Service layer for multi-repository coordination.
- **Networking**: Prepare a `:core:network` module with Ktor for future synchronization features.

---

## 7. Summary for New Contributors
- Follow the **MVI** pattern strictly in ViewModels.
- Put **business logic** in UseCases, not ViewModels or Repositories.
- Use **shared resources** from `:core:designsystem`.
- Ensure all new logic is **commonMain** compatible.
- Check the **module map** before adding new dependencies to avoid circularity.
