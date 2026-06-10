# Design System Overview

<cite>
**Referenced Files in This Document**
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [Color.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Color.kt)
- [Dimensions.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Dimensions.kt)
- [FintrackTypography.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackTypography.kt)
- [FintrackShapes.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackShapes.kt)
- [GlassColors.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/GlassColors.kt)
- [AppTheme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/AppTheme.kt)
- [themes.xml](file://app/src/main/res/values/themes.xml)
- [colors.xml](file://app/src/main/res/values/colors.xml)
- [FinTrackHost.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/FinTrackHost.kt)
- [App.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt)
- [BottombarNavigation.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/navigation/navigationBar/BottombarNavigation.kt)
- [FintrackNavigationBar.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/navigation/navigationBar/FintrackNavigationBar.kt)
- [ThemeAndCurrencyScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyScreen.kt)
- [ThemeAndCurrencyViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyViewModel.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [MoneyFormatter.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/MoneyFormatter.kt)
- [Currency.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/Currency.kt)
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
This document describes FinTrack’s design system foundation with a focus on theming and styling. It explains how Material 3 principles are integrated, how the color palette, typography, and dimension scales are organized, and how theme configuration enables consistent visuals across platforms. It also documents theme variants, design token usage, responsive design approaches, and accessibility considerations.

## Project Structure
FinTrack organizes design system assets and logic primarily under the core/designsystem module, with platform-specific theme resources in the app module and Compose-based UI in composeApp. Feature screens consume the design system for consistent styling.

```mermaid
graph TB
subgraph "App Layer"
APP_XML["Android Resources<br/>themes.xml, colors.xml"]
MAIN_ACTIVITY["MainActivity.kt"]
end
subgraph "Compose UI"
COMPOSE_APP["App.kt"]
HOST["FinTrackHost.kt"]
NAV_BOTTOM["BottombarNavigation.kt"]
NAV_BAR["FintrackNavigationBar.kt"]
end
subgraph "Design System Core"
THEME["Theme.kt"]
COLOR["Color.kt"]
DIM["Dimensions.kt"]
TYPO["FintrackTypography.kt"]
SHAPES["FintrackShapes.kt"]
GLASS["GlassColors.kt"]
APP_THEME["AppTheme.kt"]
end
subgraph "Features"
PROFILE_SCREEN["ThemeAndCurrencyScreen.kt"]
PROFILE_VM["ThemeAndCurrencyViewModel.kt"]
end
subgraph "Preferences & Money"
PREFS["FinTrackPreferences.kt"]
MONEY["MoneyFormatter.kt"]
CURRENCY["Currency.kt"]
end
MAIN_ACTIVITY --> APP_XML
COMPOSE_APP --> HOST
HOST --> THEME
HOST --> COLOR
HOST --> DIM
HOST --> TYPO
HOST --> SHAPES
HOST --> GLASS
HOST --> APP_THEME
PROFILE_SCREEN --> THEME
PROFILE_SCREEN --> COLOR
PROFILE_SCREEN --> PREFS
PROFILE_VM --> PREFS
PROFILE_VM --> MONEY
PROFILE_VM --> CURRENCY
```

**Diagram sources**
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [Color.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Color.kt)
- [Dimensions.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Dimensions.kt)
- [FintrackTypography.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackTypography.kt)
- [FintrackShapes.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackShapes.kt)
- [GlassColors.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/GlassColors.kt)
- [AppTheme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/AppTheme.kt)
- [themes.xml](file://app/src/main/res/values/themes.xml)
- [colors.xml](file://app/src/main/res/values/colors.xml)
- [FinTrackHost.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/FinTrackHost.kt)
- [App.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt)
- [BottombarNavigation.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/navigation/navigationBar/BottombarNavigation.kt)
- [FintrackNavigationBar.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/navigation/navigationBar/FintrackNavigationBar.kt)
- [ThemeAndCurrencyScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyScreen.kt)
- [ThemeAndCurrencyViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyViewModel.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [MoneyFormatter.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/MoneyFormatter.kt)
- [Currency.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/Currency.kt)

**Section sources**
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [AppTheme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/AppTheme.kt)
- [themes.xml](file://app/src/main/res/values/themes.xml)
- [colors.xml](file://app/src/main/res/values/colors.xml)
- [FinTrackHost.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/FinTrackHost.kt)
- [App.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt)

## Core Components
This section outlines the foundational building blocks of FinTrack’s design system.

- Theme configuration and variants
  - Centralized theme definition and variant composition are implemented in the design system module. Theme variants enable light/dark modes and brand-consistent palettes.
  - The host composable integrates the design system theme into the UI tree.

- Color system
  - A unified color palette is defined and exposed via design tokens. Color roles map to semantic intents (primary, surface, error) and support dynamic mode switching.

- Typography hierarchy
  - A scalable typography scale defines font families, weights, sizes, and line heights for consistent readability across components.

- Dimension scaling system
  - A spacing and sizing scale ensures proportional layouts and consistent padding/margins/gaps across components and breakpoints.

- Shapes and surfaces
  - Corner radii and elevation-based shadows define surface appearance and interactive states.

- Glassmorphism accents
  - Optional glass-like color overlays enhance depth while preserving content readability.

- Responsive design
  - Layouts adapt to screen sizes and orientations using density-independent units and flexible component sizing.

- Accessibility
  - WCAG-compliant contrast ratios, sufficient touch targets, and semantic color roles support inclusive experiences.

**Section sources**
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [Color.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Color.kt)
- [FintrackTypography.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackTypography.kt)
- [Dimensions.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Dimensions.kt)
- [FintrackShapes.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackShapes.kt)
- [GlassColors.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/GlassColors.kt)
- [AppTheme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/AppTheme.kt)

## Architecture Overview
The design system architecture connects theme configuration, tokens, and UI components across platforms. Platform resources supply base colors and themes, while the design system module provides Compose-native tokens and variants. Features consume these tokens to maintain consistency.

```mermaid
graph TB
RES["Android Resources<br/>themes.xml, colors.xml"]
DS_THEME["Theme.kt"]
DS_COLOR["Color.kt"]
DS_TYPO["FintrackTypography.kt"]
DS_DIM["Dimensions.kt"]
DS_SHAPES["FintrackShapes.kt"]
DS_GLASS["GlassColors.kt"]
DS_APP_THEME["AppTheme.kt"]
UI_HOST["FinTrackHost.kt"]
UI_APP["App.kt"]
UI_NAV["BottombarNavigation.kt"]
UI_NAVBAR["FintrackNavigationBar.kt"]
PREFS["FinTrackPreferences.kt"]
MONEY["MoneyFormatter.kt"]
CUR["Currency.kt"]
UI_APP --> UI_HOST
UI_HOST --> DS_THEME
UI_HOST --> DS_COLOR
UI_HOST --> DS_TYPO
UI_HOST --> DS_DIM
UI_HOST --> DS_SHAPES
UI_HOST --> DS_GLASS
UI_HOST --> DS_APP_THEME
RES --> DS_THEME
RES --> DS_COLOR
PREFS --> UI_NAV
PREFS --> UI_NAVBAR
PREFS --> MONEY
CUR --> MONEY
```

**Diagram sources**
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [Color.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Color.kt)
- [FintrackTypography.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackTypography.kt)
- [Dimensions.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Dimensions.kt)
- [FintrackShapes.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackShapes.kt)
- [GlassColors.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/GlassColors.kt)
- [AppTheme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/AppTheme.kt)
- [themes.xml](file://app/src/main/res/values/themes.xml)
- [colors.xml](file://app/src/main/res/values/colors.xml)
- [FinTrackHost.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/FinTrackHost.kt)
- [App.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt)
- [BottombarNavigation.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/navigation/navigationBar/BottombarNavigation.kt)
- [FintrackNavigationBar.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/navigation/navigationBar/FintrackNavigationBar.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [MoneyFormatter.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/MoneyFormatter.kt)
- [Currency.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/Currency.kt)

## Detailed Component Analysis

### Theme Configuration and Variants
- Purpose: Centralizes theme creation and variant selection (e.g., light/dark) for consistent UI rendering.
- Integration: Host composable applies the design system theme to the entire UI tree.
- Variant behavior: Theme variants switch color roles and typography/sizing tokens based on user preference or system setting.

```mermaid
sequenceDiagram
participant App as "App.kt"
participant Host as "FinTrackHost.kt"
participant Theme as "Theme.kt"
participant AppTheme as "AppTheme.kt"
App->>Host : Build UI
Host->>Theme : Resolve current variant
Theme-->>Host : Variant tokens (colors, typography, shapes)
Host->>AppTheme : Apply theme to CompositionLocal
AppTheme-->>Host : Theme applied
Host-->>App : Render with theme
```

**Diagram sources**
- [App.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt)
- [FinTrackHost.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/FinTrackHost.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [AppTheme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/AppTheme.kt)

**Section sources**
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [AppTheme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/AppTheme.kt)
- [FinTrackHost.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/FinTrackHost.kt)

### Color System and Design Tokens
- Purpose: Provides a single source of truth for all color roles and semantic intents.
- Organization: Tokens map to roles such as primary, surface, background, error, and their variants across modes.
- Usage: Components consume tokens rather than hardcoded values to ensure consistency and easy updates.

```mermaid
flowchart TD
Start(["Resolve Color Token"]) --> Mode{"Current Theme Mode?"}
Mode --> |Light| LightTokens["Light Mode Tokens"]
Mode --> |Dark| DarkTokens["Dark Mode Tokens"]
LightTokens --> Role["Apply Role (Primary/Surface/Error)"]
DarkTokens --> Role
Role --> Output["Resolved Color Value"]
Output --> End(["Use in Component"])
```

**Diagram sources**
- [Color.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Color.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

**Section sources**
- [Color.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Color.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

### Typography Hierarchy
- Purpose: Establishes a readable and scalable typographic system across components.
- Implementation: Tokens define font family, weight, size, and line height for headings, body, and caption styles.
- Usage: Components reference tokens to maintain consistent rhythm and readability.

```mermaid
classDiagram
class TypographyScale {
+heading1
+heading2
+body1
+body2
+caption
}
class Tokens {
+typography TypographyScale
}
Tokens --> TypographyScale : "exposes"
```

**Diagram sources**
- [FintrackTypography.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackTypography.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

**Section sources**
- [FintrackTypography.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackTypography.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

### Dimensions and Spacing Scale
- Purpose: Ensures proportional spacing and sizing across components and breakpoints.
- Implementation: A numeric scale defines paddings, margins, gaps, corner radii, and elevations.
- Usage: Components consume tokens for consistent layout regardless of device or orientation.

```mermaid
flowchart TD
Input["Component Request"] --> Scale["Spacing Scale Lookup"]
Scale --> Size["Tokenized Size"]
Size --> Apply["Apply to Padding/Margin/Radius"]
Apply --> Output["Consistent Layout"]
```

**Diagram sources**
- [Dimensions.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Dimensions.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

**Section sources**
- [Dimensions.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Dimensions.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

### Shapes and Surfaces
- Purpose: Standardizes corner radii and elevation to communicate hierarchy and depth.
- Implementation: Shape tokens define roundedness for cards, chips, buttons, and other surfaces.
- Usage: Components apply shape tokens consistently to reinforce visual coherence.

```mermaid
classDiagram
class ShapeTokens {
+small
+medium
+large
+full
}
class SurfaceTokens {
+elevation0
+elevation1
+elevation2
}
ShapeTokens <.. SurfaceTokens : "combined in theme"
```

**Diagram sources**
- [FintrackShapes.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackShapes.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

**Section sources**
- [FintrackShapes.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackShapes.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

### Glassmorphism Accents
- Purpose: Adds translucent overlays for depth while keeping underlying content readable.
- Implementation: Glass color tokens combine transparency and backdrop blur effects.
- Usage: Applied selectively to modals, snackbars, and floating elements.

```mermaid
flowchart TD
Base["Base Surface"] --> Overlay["Glass Overlay"]
Overlay --> Blend["Blend with Background"]
Blend --> Result["Glass Effect"]
```

**Diagram sources**
- [GlassColors.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/GlassColors.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

**Section sources**
- [GlassColors.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/GlassColors.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

### Platform Theme Integration
- Android resources define base colors and themes that inform the design system tokens.
- The design system consumes these resources to align Compose UI with platform conventions.

```mermaid
graph LR
AND_RES["themes.xml, colors.xml"] --> DS_THEME["Theme.kt"]
DS_THEME --> COMPOSE["Compose UI Tokens"]
COMPOSE --> COMPONENTS["Components"]
```

**Diagram sources**
- [themes.xml](file://app/src/main/res/values/themes.xml)
- [colors.xml](file://app/src/main/res/values/colors.xml)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

**Section sources**
- [themes.xml](file://app/src/main/res/values/themes.xml)
- [colors.xml](file://app/src/main/res/values/colors.xml)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)

### Theme Customization and User Preferences
- Users can change theme and currency settings via the profile feature.
- Preferences drive runtime updates to theme variants and currency formatting.

```mermaid
sequenceDiagram
participant User as "User"
participant Screen as "ThemeAndCurrencyScreen.kt"
participant VM as "ThemeAndCurrencyViewModel.kt"
participant Prefs as "FinTrackPreferences.kt"
participant Money as "MoneyFormatter.kt"
User->>Screen : Change Theme/Currency
Screen->>VM : Update preferences
VM->>Prefs : Persist settings
VM->>Money : Reformat currency display
Prefs-->>Screen : Settings applied
Screen-->>User : Updated UI
```

**Diagram sources**
- [ThemeAndCurrencyScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyScreen.kt)
- [ThemeAndCurrencyViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyViewModel.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [MoneyFormatter.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/MoneyFormatter.kt)

**Section sources**
- [ThemeAndCurrencyScreen.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyScreen.kt)
- [ThemeAndCurrencyViewModel.kt](file://feature-container/profile/src/commonMain/kotlin/com/kazemieh/profile/ThemeAndCurrencyViewModel.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [MoneyFormatter.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/MoneyFormatter.kt)

## Dependency Analysis
The design system components depend on each other to form a cohesive theme. Tokens feed typography, shapes, and colors into the theme, which is then applied by the host composable. Features depend on the design system for consistent styling.

```mermaid
graph TD
COLOR["Color.kt"] --> THEME["Theme.kt"]
TYPO["FintrackTypography.kt"] --> THEME
DIM["Dimensions.kt"] --> THEME
SHAPES["FintrackShapes.kt"] --> THEME
GLASS["GlassColors.kt"] --> THEME
THEME --> HOST["FinTrackHost.kt"]
HOST --> APP["App.kt"]
APP --> NAV["BottombarNavigation.kt"]
APP --> NAVBAR["FintrackNavigationBar.kt"]
PREFS["FinTrackPreferences.kt"] --> NAV
PREFS --> NAVBAR
MONEY["MoneyFormatter.kt"] --> THEME
CUR["Currency.kt"] --> MONEY
```

**Diagram sources**
- [Color.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Color.kt)
- [FintrackTypography.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackTypography.kt)
- [Dimensions.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Dimensions.kt)
- [FintrackShapes.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackShapes.kt)
- [GlassColors.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/GlassColors.kt)
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [FinTrackHost.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/FinTrackHost.kt)
- [App.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt)
- [BottombarNavigation.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/navigation/navigationBar/BottombarNavigation.kt)
- [FintrackNavigationBar.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/navigation/navigationBar/FintrackNavigationBar.kt)
- [FinTrackPreferences.kt](file://core/preferences/src/commonMain/kotlin/com/kazemieh/preferences/FinTrackPreferences.kt)
- [MoneyFormatter.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/MoneyFormatter.kt)
- [Currency.kt](file://core/money/src/commonMain/kotlin/com/kazemieh/money/Currency.kt)

**Section sources**
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [FinTrackHost.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/FinTrackHost.kt)
- [App.kt](file://composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/App.kt)

## Performance Considerations
- Prefer token-based styling over hard-coded values to reduce recomposition overhead and improve consistency.
- Use shape and elevation tokens to minimize layout thrashing during theme switches.
- Keep typography scales concise to avoid excessive font variations that complicate rendering.
- Apply glass effects judiciously to avoid unnecessary blending costs on lower-end devices.

## Troubleshooting Guide
- Theme not applying: Verify the host composable wraps UI with the design system theme and that variant resolution is active.
- Colors appear incorrect: Confirm color tokens resolve to the intended semantic role and that mode switching is functioning.
- Typography mismatch: Ensure components reference the correct tokenized styles and that fonts are properly bundled.
- Spacing inconsistencies: Check that dimension tokens are used uniformly across components and that breakpoints are handled correctly.
- Accessibility issues: Validate contrast ratios and ensure sufficient touch target sizes across variants.

**Section sources**
- [Theme.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Theme.kt)
- [Color.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Color.kt)
- [FintrackTypography.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackTypography.kt)
- [Dimensions.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/Dimensions.kt)
- [FintrackShapes.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/FintrackShapes.kt)
- [GlassColors.kt](file://core/designsystem/src/commonMain/kotlin/com/kazemieh/designsystem/GlassColors.kt)

## Conclusion
FinTrack’s design system establishes a robust, token-driven foundation for consistent theming and styling across platforms. By centralizing theme configuration, color roles, typography, and dimensions, and by integrating user preferences for theme and currency, the system ensures visual coherence and supports accessibility. Following the guidelines herein will help maintain design consistency and scalability as the product evolves.