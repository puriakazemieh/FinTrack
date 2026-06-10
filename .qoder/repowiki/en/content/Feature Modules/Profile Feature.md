# Profile Feature

<cite>
**Referenced Files in This Document**
- [ProfileScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileScreen.kt)
- [ProfileViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileViewModel.kt)
- [ProfileEditScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditScreen.kt)
- [ProfileEditViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditViewModel.kt)
- [ProfileEditContract.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditContract.kt)
- [ThemeAndCurrencyScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyScreen.kt)
- [ThemeAndCurrencyViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyViewModel.kt)
- [PreferenceRepository.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/PreferenceRepository.kt)
- [PreferenceRepositoryImpl.kt](file://core/data/src/commonMain/kotlin/com/kazemieh/data/repository/PreferenceRepositoryImpl.kt)
- [PreferenceUseCases.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/usecase/PreferenceUseCases.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [SettingsObserver.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/SettingsObserver.kt)
- [AppTheme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/AppTheme.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [CurrencyProvider.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/CurrencyProvider.kt)
- [Currency.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/Currency.kt)
- [LockViewModel.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/LockViewModel.kt)
- [LockContract.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/LockContract.kt)
- [BiometricAuthenticator.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/BiometricAuthenticator.kt)
- [PINScreen.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/PINScreen.kt)
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
This document describes the Profile feature module responsible for user profile management, appearance customization, and application settings. It covers:
- Profile viewing and editing flows
- Theme selection and currency preferences
- MVVM implementation with dedicated view models
- Integration with preference repositories and settings observers
- Real-time UI updates and cross-platform settings synchronization
- Security integration with biometric authentication and PIN protection
- Impact of profile changes on other application components

## Project Structure
The Profile feature resides under feature-container/profile and integrates with core modules for preferences, design system, and money formatting. The module exposes screens and view models for:
- Profile viewing and editing
- Theme and currency customization
- Preference persistence and observation

```mermaid
graph TB
subgraph "Profile Feature"
PS["ProfileScreen.kt"]
PVM["ProfileViewModel.kt"]
PES["ProfileEditScreen.kt"]
PEVM["ProfileEditViewModel.kt"]
PEC["ProfileEditContract.kt"]
TCS["ThemeAndCurrencyScreen.kt"]
TCVM["ThemeAndCurrencyViewModel.kt"]
end
subgraph "Core Preferences"
PR["PreferenceRepository.kt"]
PRI["PreferenceRepositoryImpl.kt"]
PUC["PreferenceUseCases.kt"]
FP["FinTrackPreferences.kt"]
SO["SettingsObserver.kt"]
end
subgraph "Design System"
AT["AppTheme.kt"]
TH["Theme.kt"]
CP["CurrencyProvider.kt"]
end
subgraph "Money Formatting"
CUR["Currency.kt"]
end
PS --> PVM
PES --> PEVM
TCS --> TCVM
PVM --> PR
PEVM --> PR
TCVM --> PR
PR --> PRI
PRI --> FP
PRI --> SO
TCVM --> AT
TCVM --> TH
TCVM --> CP
CP --> CUR
```

**Diagram sources**
- [ProfileScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileScreen.kt)
- [ProfileViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileViewModel.kt)
- [ProfileEditScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditScreen.kt)
- [ProfileEditViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditViewModel.kt)
- [ProfileEditContract.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditContract.kt)
- [ThemeAndCurrencyScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyScreen.kt)
- [ThemeAndCurrencyViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyViewModel.kt)
- [PreferenceRepository.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/PreferenceRepository.kt)
- [PreferenceRepositoryImpl.kt](file://core/data/src/commonMain/kotlin/com/kazemieh/data/repository/PreferenceRepositoryImpl.kt)
- [PreferenceUseCases.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/usecase/PreferenceUseCases.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [SettingsObserver.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/SettingsObserver.kt)
- [AppTheme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/AppTheme.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [CurrencyProvider.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/CurrencyProvider.kt)
- [Currency.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/Currency.kt)

**Section sources**
- [ProfileScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileScreen.kt)
- [ProfileEditScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditScreen.kt)
- [ThemeAndCurrencyScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyScreen.kt)

## Core Components
- ProfileScreen: Displays current user profile data and navigates to edit screen.
- ProfileViewModel: Manages profile state, loads user data, and triggers edits via use cases.
- ProfileEditScreen: Presents editable fields for profile data and handles validation.
- ProfileEditViewModel: Coordinates form state, validation, and persistence of profile changes.
- ThemeAndCurrencyScreen: Allows selecting theme and currency, updating global appearance and formatting.
- ThemeAndCurrencyViewModel: Handles theme/currency selection, persists preferences, and observes changes.

Key responsibilities:
- View models encapsulate UI logic and state management.
- Preference repositories persist and observe settings across platforms.
- Design system components apply theme and currency formatting globally.

**Section sources**
- [ProfileViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileViewModel.kt)
- [ProfileEditViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditViewModel.kt)
- [ThemeAndCurrencyViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyViewModel.kt)

## Architecture Overview
The Profile feature follows MVVM with reactive preference updates:
- Screens observe state from view models.
- View models use use cases to interact with repositories.
- Repositories persist preferences and notify observers.
- Design system applies theme and currency formatting.

```mermaid
sequenceDiagram
participant UI as "ProfileScreen"
participant VM as "ProfileViewModel"
participant UC as "PreferenceUseCases"
participant Repo as "PreferenceRepository"
participant Store as "FinTrackPreferences"
participant DS as "DesignSystem"
UI->>VM : "Load profile"
VM->>UC : "Observe user profile"
UC->>Repo : "Load profile data"
Repo->>Store : "Read preferences"
Store-->>Repo : "Profile state"
Repo-->>UC : "Profile state"
UC-->>VM : "Profile state"
VM-->>UI : "Render profile data"
UI->>VM : "Navigate to edit"
VM-->>UI : "Open edit screen"
UI->>VM : "Submit changes"
VM->>UC : "Update profile"
UC->>Repo : "Persist changes"
Repo->>Store : "Write preferences"
Store-->>Repo : "Success"
Repo-->>UC : "Success"
UC-->>VM : "Success"
VM-->>UI : "Close edit and refresh"
Repo-->>DS : "Notify settings change"
DS-->>UI : "Reapply theme/currency"
```

**Diagram sources**
- [ProfileScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileScreen.kt)
- [ProfileViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileViewModel.kt)
- [PreferenceUseCases.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/usecase/PreferenceUseCases.kt)
- [PreferenceRepository.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/PreferenceRepository.kt)
- [PreferenceRepositoryImpl.kt](file://core/data/src/commonMain/kotlin/com/kazemieh/data/repository/PreferenceRepositoryImpl.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [AppTheme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/AppTheme.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

## Detailed Component Analysis

### ProfileScreen and ProfileViewModel
- ProfileScreen renders user profile fields and provides navigation to the edit screen.
- ProfileViewModel loads profile data via use cases, manages loading and error states, and exposes commands to open the edit screen.

Implementation highlights:
- Reactive state rendering from view model.
- Navigation command pattern for edit flow.
- Integration with preference use cases for data loading.

**Section sources**
- [ProfileScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileScreen.kt)
- [ProfileViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileViewModel.kt)

### ProfileEditScreen and ProfileEditViewModel
- ProfileEditScreen displays editable fields for profile data and validates inputs before submission.
- ProfileEditViewModel manages form state, validation rules, and persists changes through use cases.

Form handling patterns:
- Field-level validation with error messages surfaced to the UI.
- Submission guarded by validation checks.
- Use of contract types to define form intents and state transitions.

```mermaid
flowchart TD
Start(["Open Edit Form"]) --> LoadData["Load current profile data"]
LoadData --> RenderForm["Render editable fields"]
RenderForm --> InputChange["User modifies field(s)"]
InputChange --> Validate["Validate inputs"]
Validate --> Valid{"All valid?"}
Valid --> |No| ShowErrors["Show validation errors"]
ShowErrors --> WaitInput["Wait for corrections"]
WaitInput --> InputChange
Valid --> |Yes| Submit["Submit changes"]
Submit --> Persist["Persist via use cases"]
Persist --> Success["Success response"]
Success --> Close["Close edit and refresh profile"]
Close --> End(["Done"])
```

**Diagram sources**
- [ProfileEditScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditScreen.kt)
- [ProfileEditViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditViewModel.kt)
- [ProfileEditContract.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditContract.kt)

**Section sources**
- [ProfileEditScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditScreen.kt)
- [ProfileEditViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditViewModel.kt)
- [ProfileEditContract.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditContract.kt)

### ThemeAndCurrencyScreen and ThemeAndCurrencyViewModel
- ThemeAndCurrencyScreen allows users to select application theme and currency.
- ThemeAndCurrencyViewModel handles selection, persists preferences, and triggers global UI updates.

Theme and currency mechanisms:
- Theme selection updates global theme definitions.
- Currency selection configures currency provider for consistent formatting across the app.
- Changes propagate through settings observer to all screens.

```mermaid
sequenceDiagram
participant UI as "ThemeAndCurrencyScreen"
participant VM as "ThemeAndCurrencyViewModel"
participant Repo as "PreferenceRepository"
participant Store as "FinTrackPreferences"
participant DS as "DesignSystem"
UI->>VM : "Select theme"
VM->>Repo : "Save theme preference"
Repo->>Store : "Write theme setting"
Store-->>Repo : "Success"
Repo-->>VM : "Success"
VM-->>UI : "Confirm selection"
UI->>VM : "Select currency"
VM->>Repo : "Save currency preference"
Repo->>Store : "Write currency setting"
Store-->>Repo : "Success"
Repo-->>VM : "Success"
VM-->>UI : "Confirm selection"
Repo-->>DS : "Notify settings change"
DS-->>UI : "Reapply theme and currency"
```

**Diagram sources**
- [ThemeAndCurrencyScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyScreen.kt)
- [ThemeAndCurrencyViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyViewModel.kt)
- [PreferenceRepository.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/PreferenceRepository.kt)
- [PreferenceRepositoryImpl.kt](file://core/data/src/commonMain/kotlin/com/kazemieh/data/repository/PreferenceRepositoryImpl.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [AppTheme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/AppTheme.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [CurrencyProvider.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/CurrencyProvider.kt)

**Section sources**
- [ThemeAndCurrencyScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyScreen.kt)
- [ThemeAndCurrencyViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyViewModel.kt)

### Preference Repositories and Settings Synchronization
- PreferenceRepository defines the abstraction for reading/writing preferences.
- PreferenceRepositoryImpl implements persistence using FinTrackPreferences and notifies observers.
- PreferenceUseCases orchestrate operations for profile and settings.
- SettingsObserver propagates changes to subscribers for real-time UI updates.

```mermaid
classDiagram
class PreferenceRepository {
+loadProfile()
+saveProfile(data)
+loadTheme()
+saveTheme(theme)
+loadCurrency()
+saveCurrency(currency)
}
class PreferenceRepositoryImpl {
-finTrackPreferences : FinTrackPreferences
+loadProfile()
+saveProfile(data)
+loadTheme()
+saveTheme(theme)
+loadCurrency()
+saveCurrency(currency)
}
class FinTrackPreferences {
+observeSettings()
+setTheme(theme)
+setCurrency(currency)
}
class SettingsObserver {
+subscribe(listener)
+notify()
}
PreferenceRepository <|.. PreferenceRepositoryImpl
PreferenceRepositoryImpl --> FinTrackPreferences : "persists"
PreferenceRepositoryImpl --> SettingsObserver : "notifies"
```

**Diagram sources**
- [PreferenceRepository.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/PreferenceRepository.kt)
- [PreferenceRepositoryImpl.kt](file://core/data/src/commonMain/kotlin/com/kazemieh/data/repository/PreferenceRepositoryImpl.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [SettingsObserver.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/SettingsObserver.kt)

**Section sources**
- [PreferenceRepository.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/PreferenceRepository.kt)
- [PreferenceRepositoryImpl.kt](file://core/data/src/commonMain/kotlin/com/kazemieh/data/repository/PreferenceRepositoryImpl.kt)
- [PreferenceUseCases.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/usecase/PreferenceUseCases.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [SettingsObserver.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/SettingsObserver.kt)

### Security Integration: Biometric Authentication and PIN Protection
- Profile changes can trigger security gates managed by the lock feature.
- LockViewModel coordinates authentication flows and protects sensitive actions.
- BiometricAuthenticator and PINScreen provide platform-specific secure entry mechanisms.

Impact on profile changes:
- Editing sensitive profile data may require biometric verification or PIN entry.
- After successful authentication, changes are persisted and UI updates are applied.

```mermaid
sequenceDiagram
participant UI as "ProfileEditScreen"
participant VM as "ProfileEditViewModel"
participant LV as "LockViewModel"
participant BA as "BiometricAuthenticator"
participant PIN as "PINScreen"
UI->>VM : "Submit profile update"
VM->>LV : "Request authentication"
LV->>BA : "Start biometric auth"
alt "Biometric supported"
BA-->>LV : "Authenticated"
else "Fallback to PIN"
LV->>PIN : "Show PIN screen"
PIN-->>LV : "PIN verified"
end
LV-->>VM : "Access granted"
VM->>VM : "Persist changes"
VM-->>UI : "Update complete"
```

**Diagram sources**
- [ProfileEditScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditScreen.kt)
- [ProfileEditViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditViewModel.kt)
- [LockViewModel.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/LockViewModel.kt)
- [LockContract.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/LockContract.kt)
- [BiometricAuthenticator.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/BiometricAuthenticator.kt)
- [PINScreen.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/PINScreen.kt)

**Section sources**
- [LockViewModel.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/LockViewModel.kt)
- [LockContract.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/LockContract.kt)
- [BiometricAuthenticator.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/BiometricAuthenticator.kt)
- [PINScreen.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/PINScreen.kt)

## Dependency Analysis
- Profile screens depend on their respective view models for state and commands.
- View models depend on use cases and repositories for data operations.
- Repositories depend on FinTrackPreferences for persistence and SettingsObserver for notifications.
- ThemeAndCurrencyViewModel depends on design system components for applying theme and currency formatting.
- Money formatting depends on CurrencyProvider and Currency models.

```mermaid
graph LR
PS["ProfileScreen"] --> PVM["ProfileViewModel"]
PES["ProfileEditScreen"] --> PEVM["ProfileEditViewModel"]
TCS["ThemeAndCurrencyScreen"] --> TCVM["ThemeAndCurrencyViewModel"]
PVM --> PUC["PreferenceUseCases"]
PEVM --> PUC
TCVM --> PUC
PUC --> PR["PreferenceRepository"]
PR --> PRI["PreferenceRepositoryImpl"]
PRI --> FP["FinTrackPreferences"]
PRI --> SO["SettingsObserver"]
TCVM --> AT["AppTheme"]
TCVM --> TH["Theme"]
TCVM --> CP["CurrencyProvider"]
CP --> CUR["Currency"]
```

**Diagram sources**
- [ProfileScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileScreen.kt)
- [ProfileViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileViewModel.kt)
- [ProfileEditScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditScreen.kt)
- [ProfileEditViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditViewModel.kt)
- [ThemeAndCurrencyScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyScreen.kt)
- [ThemeAndCurrencyViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyViewModel.kt)
- [PreferenceUseCases.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/usecase/PreferenceUseCases.kt)
- [PreferenceRepository.kt](file://core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/PreferenceRepository.kt)
- [PreferenceRepositoryImpl.kt](file://core/data/src/commonMain/kotlin/com/kazemieh/data/repository/PreferenceRepositoryImpl.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [SettingsObserver.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/SettingsObserver.kt)
- [AppTheme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/AppTheme.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [CurrencyProvider.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/CurrencyProvider.kt)
- [Currency.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/Currency.kt)

**Section sources**
- [ProfileViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileViewModel.kt)
- [ProfileEditViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditViewModel.kt)
- [ThemeAndCurrencyViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyViewModel.kt)

## Performance Considerations
- Prefer immutable state updates in view models to minimize recompositions.
- Debounce or batch preference writes to avoid excessive disk I/O.
- Use lazy composition for heavy lists in profile screens.
- Cache frequently accessed preferences in memory to reduce repository calls.
- Apply selective UI updates when settings change to avoid full re-rendering.

## Troubleshooting Guide
Common issues and resolutions:
- Profile not updating after edit:
  - Verify use case invocation and repository write success.
  - Ensure SettingsObserver notifies listeners and UI recomposes.
- Theme or currency not applied:
  - Confirm ThemeAndCurrencyViewModel writes preferences and design system components react.
  - Check FinTrackPreferences keys and value serialization.
- Authentication failures during profile edit:
  - Validate biometric availability and permissions.
  - Ensure LockViewModel handles fallback to PIN correctly.
- Validation errors not visible:
  - Confirm form state updates and error messages are propagated to UI.
  - Review ProfileEditContract intent/state transitions.

**Section sources**
- [ProfileEditViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ProfileEditViewModel.kt)
- [ThemeAndCurrencyViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyViewModel.kt)
- [SettingsObserver.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/SettingsObserver.kt)
- [LockViewModel.kt](file://feature-share/lock/src/commonMain/kotlin/com/kazemieh/lock/LockViewModel.kt)

## Conclusion
The Profile feature module provides a cohesive, MVVM-based solution for managing user profiles, appearance, and application settings. Through preference repositories, settings observers, and design system integrations, it ensures consistent, real-time UI updates across platforms. Security integration with biometric authentication and PIN protection safeguards sensitive profile changes. The modular architecture supports maintainability, testability, and cross-platform compatibility.