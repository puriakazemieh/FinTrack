# Project Conventions Guide - FinTrack

## 1. Screen Structure
Every screen should follow this template:
- **Naming**: `[FeatureName]Screen.kt`
- **ViewModel**: Injected via `koinViewModel()`.
- **State Collection**: Use `collectAsStateWithLifecycle()`.
- **Layout**: Usually a `Box` as root to host the content and overlays (BottomSheets/Dialogs).

## 2. ViewModel Structure (MVI)
ViewModels must implement:
- **State**: A data class representing the immutable UI state.
- **Intent**: A sealed interface for all user actions.
- **Effect**: A sealed interface for one-time side effects (Navigation, Snackbar).
- **onIntent(intent)**: The entry point for all UI actions.

Example:
```kotlin
class FeatureViewModel(...) : ViewModel() {
    private val _state = MutableStateFlow(FeatureState())
    val state = _state.asStateFlow()

    private val _effect = Channel<FeatureEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: FeatureIntent) { ... }
}
```

## 3. Navigation Pattern
- Use **Type-Safe Navigation**.
- Define routes in `Screen.kt` as `@Serializable` classes/objects.
- Navigation logic should reside in the UI layer (usually handling `Effect.Navigate`).

## 4. Repository & UseCase Pattern
- **Repository**: Define interface in `core:domain` and implementation in `core:data`.
- **UseCase**: Single-purpose class in `core:domain` with an `invoke` operator.
- UseCases should be grouped (e.g., `TransactionUseCaseGroup`) if they are numerous.

## 5. Koin Module Organization
- Each module should have a `di` package.
- Define `val featureModule = module { ... }`.
- Add the module to the list in `App.kt` -> `initKoin`.

## 6. Resource & String Handling
- NEVER use hardcoded strings in UI.
- Use `Res.string.key` with `stringResource()`.
- For ViewModels, use the `UiText` wrapper to send strings to the UI.

## 7. Reusable Components
- Check `:core:designsystem` before creating new UI components.
- Use `FintrackTheme` and `LocalSpacing` for consistency.

## 8. Form Handling
- Validation logic belongs in the `ViewModel`.
- Error flags should be part of the `State`.
- Use `FintrackOutlinedTextField` for input.

## 9. Loading/Error/Success
- `isLoading` flag in State to show progress indicators.
- `Effect.ShowMessage` for errors/success messages via Snackbar.
- Prefer `runCatching` for repository calls in ViewModels.
