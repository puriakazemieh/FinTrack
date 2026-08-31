package com.kazemieh.common.analytics

/**
 * Represents a safe-by-construction analytics event.
 * Enforces that no arbitrary strings are used for event names,
 * and prevents sending sensitive financial data.
 */
sealed class ProductEvent(val eventName: String, val params: Map<String, Any> = emptyMap()) {
    data object OnboardingStarted : ProductEvent("onboarding_started")
    data object OnboardingCompleted : ProductEvent("onboarding_completed")
    data object AccountCreated : ProductEvent("account_created")
    
    // Core financial milestones (without amounts or details)
    data object FirstTransactionCompleted : ProductEvent("first_transaction_completed")
    data object BudgetCreated : ProductEvent("budget_created")
    data object InstallmentCreated : ProductEvent("installment_created")
    data object DebtCreated : ProductEvent("debt_created")
    data object CheckCreated : ProductEvent("check_created")
    
    // Backup & Sync
    data object BackupExportCompleted : ProductEvent("backup_export_completed")
    data object RestoreCompleted : ProductEvent("restore_completed")
    data object SyncStarted : ProductEvent("sync_started")
    data object SyncCompleted : ProductEvent("sync_completed")

    // Dashboard
    data object DashboardViewed : ProductEvent("dashboard_viewed")
    data object DashboardWidgetReordered : ProductEvent("dashboard_widget_reordered")
    data object DashboardQuickAddClicked : ProductEvent("dashboard_quick_add_clicked")

    // Transactions
    data object TransactionListViewed : ProductEvent("transaction_list_viewed")
    data class TransactionCreated(val type: String) : ProductEvent("transaction_created", mapOf("type" to type))
    data object TransactionUpdated : ProductEvent("transaction_updated")
    data object TransactionDeleted : ProductEvent("transaction_deleted")
    
    // Settings
    data class ProfileEdited(val hasName: Boolean) : ProductEvent("profile_edited", mapOf("has_name" to hasName))
    data class ThemeChanged(val themeName: String) : ProductEvent("theme_changed", mapOf("theme" to themeName))
    
    // Auth & Security
    data object AppUnlocked : ProductEvent("app_unlocked")
    data object AuthPinCreated : ProductEvent("auth_pin_created")
    
    // Feature Usage (Generic Fallback if not specifically modeled)
    data class FeatureOpened(val featureKey: String) : ProductEvent(
        eventName = "feature_opened", 
        params = mapOf("feature_key" to featureKey)
    )
    
    data class FeatureActionCompleted(val featureKey: String) : ProductEvent(
        eventName = "feature_action_completed", 
        params = mapOf("feature_key" to featureKey)
    )
    
    data class FeatureActionFailed(val featureKey: String, val safeErrorCode: String) : ProductEvent(
        eventName = "feature_action_failed", 
        params = mapOf("feature_key" to featureKey, "safe_error_code" to safeErrorCode)
    )
}
