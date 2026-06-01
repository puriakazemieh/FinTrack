# FinTrack

A modern **personal finance management app** to track your income, expenses, and account balances — helping you make smarter financial decisions.

<div align="center">
  <img src="https://raw.githubusercontent.com/puriakazemieh/FinTrack/refs/heads/develop/app/src/main/res/drawable/fintrack.png" width="120"/>
  <p>A modern personal finance management app</p>
</div>

### ✨ Key Features

- Add and manage **income** and **expenses** with categories, sources, dates, and notes
- **Dashboard** with total balance, income, and expense overview
- **Report screen** with multi-filter support (source, category, transaction type, time range)
- Modular Clean Architecture with **MVI pattern** and Jetpack Compose UI
- Fully ready for future migration to **Kotlin Multiplatform (KMP)**
- Simple, intuitive, and modern Material 3 design

---

## 🏗 Project Structure

```
app/                    ← Main Android app module    
core/                   ← Shared modules (common, model, data, database, designsystem)  feature/                ← Feature modules (transaction, category, tag, dashboard, reports)  

```


## 🚀 Getting Started

Clone and build:

```
git clone https://github.com/puriakazemieh/FinTrack.git  
cd FinTrack  
./gradlew clean assembleDebug  
```

## ⚙️ Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** Clean Architecture + MVI
- **Database:** Room
- **Dependency Injection:** Koin
- **Coroutines & Flow** for reactive data handling
- **Gradle KTS + Version Catalog** for dependency management

---

## 🔮 Roadmap

## Platforms
- Android
- iOS
- Desktop
- Web

## Stack
- Compose Multiplatform
- Koin
- Ktor
- Coroutines
- Flow

## Module Rules
- commonMain first
- no Android APIs in shared modules
- immutable UI state
- business logic in domain
- repositories in data layer

## Navigation
[explain briefly]

## State Management
[explain briefly]

## DI
[explain briefly]