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

    // Feature Usage
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
