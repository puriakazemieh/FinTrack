# Technical Debt & Architecture Consistency Review

## Critical Issues
*None identified currently that prevent the app from functioning.*

## Important Issues

### 1. God Repository / DataSource
- **Problem**: `TransactionRepository` and `TransactionLocalDataSource` handle five different domains (Transactions, Categories, Sources, Tags, Persons).
- **Impact**: Hard to maintain, test, and scale. Violates SOLID principles.
- **Fix**: Refactor into domain-specific repositories and data sources.

### 2. Missing Centralized Error Handling
- **Problem**: Error handling is fragmented across ViewModels using `runCatching`.
- **Impact**: Inconsistent error reporting and potential for unhandled exceptions in the data layer.
- **Fix**: Use a standard `Result` or `Either` type for all Repository/UseCase returns.

### 3. Logic in Data Layer (Potential)
- **Problem**: `TransactionLocalDataSourceImpl` might be handling business rules like balance updates.
- **Impact**: Business logic is hidden in SQL/Database implementation, making it hard to change or test independently of the DB.
- **Fix**: Move balance calculation logic to Domain UseCases or Services.

## Optional / Future-Proofing

### 4. High Feature Coupling
- **Problem**: `feature-share:transaction` depends on many other share modules for Pickers.
- **Impact**: Tight coupling makes it harder to use features in isolation.
- **Fix**: Extract Pickers to a `feature-share:common-ui` or use a more decoupled navigation/bottomsheet strategy.

### 5. Large ViewModel States
- **Problem**: `AddTransactionState` is quite large.
- **Impact**: Potential for unnecessary recompositions if not handled carefully (though Compose is generally efficient).
- **Fix**: Group related fields into nested data classes.

### 6. Missing Networking Structure
- **Problem**: No Ktor/Networking setup despite being a KMP project.
- **Impact**: High effort to add cloud sync later.
- **Fix**: Setup a basic `:core:network` module with Ktor.
