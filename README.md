# FinTrack

<div align="center">
  <img src="https://raw.githubusercontent.com/puriakazemieh/FinTrack/refs/heads/develop/app/src/main/res/drawable/fintrack.png" width="120"/>
  <h3>مدیریت مالی هوشمند | Smart Personal Finance Manager</h3>
  <p>Track your income, expenses, and account balances across all your devices — helping you make smarter financial decisions.</p>
</div>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.3.0-purple?logo=kotlin" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Compose_Multiplatform-1.10.0-blue?logo=jetpackcompose" alt="Compose Multiplatform"/>
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-green" alt="Platforms"/>
  <img src="https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI-red" alt="Architecture"/>
  <img src="https://img.shields.io/badge/API-SQLDelight%20%7C%20Ktor%20%7C%20Koin-orange" alt="Stack"/>
</p>

---

## ✨ Key Features

### 💰 Financial Management
- **Transactions** — Add and manage **income**, **expenses**, and **transfers** with categories, sources, dates, and notes
- **Dashboard** — Real-time overview of total balance, income, and expenses with beautiful charts
- **Categories** — Customizable transaction categories with icons and colors
- **Financial Sources** — Manage bank accounts, cards, and wallets with balance tracking
- **Tags & Persons** — Tag transactions and link them to people for better organization
- **Search** — Full-text search across all transactions, categories, sources, and more

### 📊 Planning & Budgeting
- **Budget** — Set monthly budgets for categories and track spending
- **Savings Goals** — Define and track financial goals
- **Installments** — Manage installment payments with reminders
- **Fixed Expenses** — Track recurring monthly expenses
- **Debt Management** — Track debts and receivables
- **Checks** — Manage check transactions

### 🤖 Smart Features
- **AI Advisor** — Get financial insights and recommendations powered by AI
- **SMS Reader** — Auto-detect bank transactions from SMS messages
- **Currency Converter** — Real-time currency conversion
- **Exchange Rates** — Live foreign exchange rate tracking
- **Financial Calendar** — View all financial events on a calendar

### 🎮 Gamification & Engagement
- **Achievements** — Unlock achievements as you reach financial milestones
- **XP System** — Earn experience points for financial activities
- **Shopping List** — Create and manage shopping lists

### 🔒 Security & Privacy
- **App Lock** — Secure the app with PIN or biometric authentication
- **Hide Balance** — Option to hide sensitive balance information

### 🔄 Data Management
- **Backup & Restore** — Export and import your financial data
- **Sync** — Synchronize data across multiple devices
- **Notes** — Attach notes to your financial records

### 🎨 Personalization
- **Themes** — Multiple themes (Dark, Light, Glass) with auto-switch based on time or system
- **Accent Colors** — Customizable accent color palette
- **Text Scale** — Adjustable text size for accessibility
- **Custom Bottom Bar** — Rearrange navigation tabs to your preference
- **RTL Support** — Full Persian/Farsi language and right-to-left layout support

---

## 🏗 Project Architecture

FinTrack follows **Modular Clean Architecture** with **MVI (Model-View-Intent)** pattern, built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**.

```
FinTrack/
├── app/                              ← Android entry point
├── composeApp/                       ← Main UI aggregator (App.kt, Navigation, Entry Points)
│
├── core/
│   ├── common/                       ← Base models, utilities, date helpers, logging
│   ├── domain/                       ← Business logic (Repository interfaces, UseCases)
│   ├── data/                         ← Repository implementations
│   ├── data-contract/                ← DataSource interfaces (avoids circular deps)
│   ├── database/                     ← SQLDelight database + LocalDataSource implementations
│   ├── designsystem/                 ← Shared UI components, theme (FintrackTheme), resources
│   ├── network/                      ← Ktor HTTP client
│   ├── preferences/                  ← User preferences (Multiplatform Settings)
│   ├── money/                        ← Currency and money formatting logic
│   ├── jalali/                       ← Persian (Jalali) calendar support
│   └── storage/                      ← File storage utilities
│
├── feature-container/                ← Main screens (Dashboard, Transactions, Profile, Tools, Onboarding)
├── feature-share/                    ← Shared feature modules (22 modules)
│   ├── transaction/                  ├── category/          ├── source/
│   ├── tags/                         ├── person/            ├── budget/
│   ├── goals/                        ├── installment/       ├── debt/
│   ├── check/                        ├── fixed-expense/     ├── asset/
│   ├── ai-insights/                  ├── gamification/      ├── lock/
│   ├── search/                       ├── notes/             ├── shopping/
│   ├── sms-reader/                   ├── backup-export/     ├── sync/
│   ├── notifications/                ├── utilities/         └── widget/
│
└── server/                           ← Ktor backend server
```

### Dependency Flow

```
UI (Features) → core:domain → core:common
UI (Features) → core:designsystem
core:data → core:domain
core:data → core:data-contract ← core:database
```

---

## 🛠 Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin 2.3 |
| **UI Framework** | Compose Multiplatform 1.10 |
| **Architecture** | Clean Architecture + MVI (State / Intent / Effect) |
| **Database** | SQLDelight 2.2 (cross-platform) |
| **Dependency Injection** | Koin 4.1 |
| **Networking** | Ktor 3.3 (Client + Server) |
| **Serialization** | Kotlinx Serialization |
| **Date & Time** | Kotlinx Datetime + Jalali Calendar |
| **Async** | Coroutines + Flow |
| **Navigation** | Type-Safe Navigation Compose |
| **Preferences** | Multiplatform Settings |
| **Logging** | Kermit |
| **Image Loading** | Image Loader |
| **Build System** | Gradle KTS + Version Catalog (libs.versions.toml) |

---

## 📱 Supported Platforms

| Platform | Status |
|----------|--------|
| **Android** | ✅ (minSdk 24, targetSdk 36) |
| **iOS** | ✅ (XCFramework: iosX64, iosArm64, iosSimulatorArm64) |
| **Desktop** | ✅ (JVM — Windows/MSI, macOS/DMG, Linux/Deb) |
| **Web** | ✅ (JS/Browser with Webpack) |

---

## 🚀 Getting Started

### Prerequisites
- JDK 17+
- Android Studio (latest)
- Xcode (for iOS builds)

### Clone & Build

```bash
git clone https://github.com/puriakazemieh/FinTrack.git
cd FinTrack

# Android
./gradlew :app:assembleDebug

# Desktop
./gradlew :composeApp:run

# Web
./gradlew :composeApp:jsBrowserDevelopmentRun

# iOS (requires macOS)
./gradlew :composeApp:iosSimulatorArm64MainBinaries
```

---

## 🧠 MVI Pattern

Every screen follows a strict MVI-inspired protocol:

```kotlin
// State — Immutable UI state
data class FeatureState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: String? = null
)

// Intent — User actions
sealed interface FeatureIntent {
    data object Load : FeatureIntent
    data class Delete(val id: Long) : FeatureIntent
}

// Effect — One-time side effects
sealed interface FeatureEffect {
    data class ShowMessage(val message: UiText) : FeatureEffect
    data object NavigateBack : FeatureEffect
}

// ViewModel
class FeatureViewModel : ViewModel() {
    private val _state = MutableStateFlow(FeatureState())
    val state = _state.asStateFlow()

    private val _effect = Channel<FeatureEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: FeatureIntent) { /* handle */ }
}
```

---

## 🌐 Localization

- **RTL Support** — Full right-to-left layout for Persian/Farsi
- **Jalali Calendar** — Persian date system support
- **Persian Numbers** — Proper Persian numeral formatting
- **Multi-language** — String resources via Compose Resources

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

<div align="center">
  <p>Made with ❤️ by <a href="https://github.com/puriakazemieh">Puria Kazemieh</a></p>
</div>