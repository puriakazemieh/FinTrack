package com.kazemieh.common.analytics

/**
 * Represents a safe-by-construction analytics event.
 * Enforces that no arbitrary strings are used for event names,
 * and prevents sending sensitive financial data.
 */
sealed class ProductEvent(val eventName: String, val params: Map<String, Any> = emptyMap()) {
    // 1. Onboarding & Security
    data object OnboardingStarted : ProductEvent("onboarding_started")
    data object OnboardingCompleted : ProductEvent("onboarding_completed")
    data object AuthPinCreated : ProductEvent("auth_pin_created")
    data object AuthPinChanged : ProductEvent("auth_pin_changed")
    data object AuthBiometricEnabled : ProductEvent("auth_biometric_enabled")
    data object AuthBiometricDisabled : ProductEvent("auth_biometric_disabled")
    data object AppUnlocked : ProductEvent("app_unlocked")
    data object AppLockedTimeout : ProductEvent("app_locked_timeout")

    // 2. Dashboard
    data object DashboardViewed : ProductEvent("dashboard_viewed")
    data object DashboardQuickAddClicked : ProductEvent("dashboard_quick_add_clicked")
    data object DashboardRecentTransactionClicked : ProductEvent("dashboard_recent_transaction_clicked")
    data object DashboardWalletSummaryViewed : ProductEvent("dashboard_wallet_summary_viewed")
    data object DashboardWidgetReordered : ProductEvent("dashboard_widget_reordered")
    data object DashboardWidgetToggled : ProductEvent("dashboard_widget_toggled")

    // 3. Transactions
    data object TransactionListViewed : ProductEvent("transaction_list_viewed")
    data class TransactionCreated(val type: String) : ProductEvent("transaction_created", mapOf("type" to type))
    data object TransactionUpdated : ProductEvent("transaction_updated")
    data object TransactionDeleted : ProductEvent("transaction_deleted")
    data object TransactionFilterApplied : ProductEvent("transaction_filter_applied")
    data object TransactionSearchUsed : ProductEvent("transaction_search_used")
    data object TransactionDuplicateClicked : ProductEvent("transaction_duplicate_clicked")
    data object TransactionSortChanged : ProductEvent("transaction_sort_changed")
    data object TransactionSearchPerformed : ProductEvent("transaction_search_performed")
    data object TransactionReportViewed : ProductEvent("transaction_report_viewed")

    // 4. Financial Sources
    data object SourceListViewed : ProductEvent("source_list_viewed")
    data class SourceCreated(val type: String) : ProductEvent("source_created", mapOf("type" to type))
    data object SourceUpdated : ProductEvent("source_updated")
    data object SourceDeleted : ProductEvent("source_deleted")
    data object SourceTransferInitiated : ProductEvent("source_transfer_initiated")
    data object SourceDefaultChanged : ProductEvent("source_default_changed")
    data object SourceReordered : ProductEvent("source_reordered")

    // 5. Categories
    data object CategoryListViewed : ProductEvent("category_list_viewed")
    data object CategoryCreated : ProductEvent("category_created")
    data object CategoryUpdated : ProductEvent("category_updated")
    data object CategoryDeleted : ProductEvent("category_deleted")
    data object CategoryParentChanged : ProductEvent("category_parent_changed")
    data object CategoryIconChanged : ProductEvent("category_icon_changed")
    data object CategoryReordered : ProductEvent("category_reordered")
    data object CategoryDefaultChanged : ProductEvent("category_default_changed")

    // 6. Tags & Persons
    data object TagListViewed : ProductEvent("tag_list_viewed")
    data object TagCreated : ProductEvent("tag_created")
    data object TagUpdated : ProductEvent("tag_updated")
    data object TagDeleted : ProductEvent("tag_deleted")
    data object PersonListViewed : ProductEvent("person_list_viewed")
    data object PersonCreated : ProductEvent("person_created")
    data object PersonUpdated : ProductEvent("person_updated")
    data object PersonDeleted : ProductEvent("person_deleted")
    data object PersonReordered : ProductEvent("person_reordered")
    data object TagReordered : ProductEvent("tag_reordered")

    // 8. Budgets
    data object BudgetListViewed : ProductEvent("budget_list_viewed")
    data class BudgetCreated(val period: String) : ProductEvent("budget_created", mapOf("period" to period))
    data object BudgetUpdated : ProductEvent("budget_updated")
    data object BudgetDeleted : ProductEvent("budget_deleted")
    data object BudgetExceededWarning : ProductEvent("budget_exceeded_warning")

    // 9. Debts & Checks & Installments
    data object DebtListViewed : ProductEvent("debt_list_viewed")
    data class DebtCreated(val type: String) : ProductEvent("debt_created", mapOf("type" to type))
    data object DebtSettled : ProductEvent("debt_settled")
    data object DebtUpdated : ProductEvent("debt_updated")
    data object DebtDeleted : ProductEvent("debt_deleted")
    data object InstallmentListViewed : ProductEvent("installment_list_viewed")
    data object InstallmentCreated : ProductEvent("installment_created")
    data object InstallmentUpdated : ProductEvent("installment_updated")
    data object InstallmentPaid : ProductEvent("installment_paid")
    data object InstallmentDeleted : ProductEvent("installment_deleted")
    data object CheckListViewed : ProductEvent("check_list_viewed")
    data object CheckCreated : ProductEvent("check_created")
    data object CheckStatusChanged : ProductEvent("check_status_changed")
    data object CheckUpdated : ProductEvent("check_updated")
    data object CheckDeleted : ProductEvent("check_deleted")

    // 10. Fixed Expenses
    data object FixedExpenseListViewed : ProductEvent("fixed_expense_list_viewed")
    data object FixedExpenseCreated : ProductEvent("fixed_expense_created")
    data object FixedExpenseUpdated : ProductEvent("fixed_expense_updated")
    data object FixedExpenseDeleted : ProductEvent("fixed_expense_deleted")
    data object FixedExpenseAutoLogged : ProductEvent("fixed_expense_auto_logged")

    // 11. Assets & Gold/Crypto
    data object AssetListViewed : ProductEvent("asset_list_viewed")
    data class AssetFormOpened(val isEdit: Boolean) : ProductEvent(
        "asset_form_opened", mapOf("is_edit" to isEdit)
    )
    data class AssetFormDismissed(val isEdit: Boolean) : ProductEvent(
        "asset_form_dismissed", mapOf("is_edit" to isEdit)
    )
    data class AssetTypeSelected(val assetType: String) : ProductEvent(
        "asset_type_selected", mapOf("asset_type" to assetType)
    )
    data class AssetMarketPickerOpened(val assetType: String) : ProductEvent(
        "asset_market_picker_opened", mapOf("asset_type" to assetType)
    )
    data class AssetCreated(
        val type: String,
        val operation: String,
        val linkedTransaction: Boolean
    ) : ProductEvent(
        "asset_created",
        mapOf("asset_type" to type, "operation" to operation, "linked_transaction" to linkedTransaction)
    )
    data class AssetUpdated(val type: String) : ProductEvent("asset_updated", mapOf("asset_type" to type))
    data class AssetDeleted(val deleteLinkedTransaction: Boolean) : ProductEvent(
        "asset_deleted", mapOf("delete_linked_transaction" to deleteLinkedTransaction)
    )
    data object AssetSearchStarted : ProductEvent("asset_search_started")
    data object AssetFilterOpened : ProductEvent("asset_filter_opened")
    data class AssetFilterApplied(
        val typeCount: Int,
        val categoryCount: Int,
        val sourceCount: Int,
        val tagCount: Int,
        val personCount: Int
    ) : ProductEvent(
        "asset_filter_applied",
        mapOf(
            "type_count" to typeCount,
            "category_count" to categoryCount,
            "source_count" to sourceCount,
            "tag_count" to tagCount,
            "person_count" to personCount
        )
    )
    data object AssetFilterCleared : ProductEvent("asset_filter_cleared")
    data class AssetActionsOpened(val assetType: String) : ProductEvent(
        "asset_actions_opened", mapOf("asset_type" to assetType)
    )
    data class AssetActionSelected(val action: String) : ProductEvent(
        "asset_action_selected", mapOf("action" to action)
    )
    data class AssetHistoryViewed(val assetType: String) : ProductEvent(
        "asset_history_viewed", mapOf("asset_type" to assetType)
    )
    data object AssetDashboardMoreClicked : ProductEvent("asset_dashboard_more_clicked")
    data class AssetLinkedTransactionCreated(val operation: String) : ProductEvent(
        "asset_linked_transaction_created", mapOf("operation" to operation)
    )
    data class AssetLinkedTransactionFailed(val operation: String) : ProductEvent(
        "asset_linked_transaction_failed", mapOf("operation" to operation)
    )
    data object FxRatesViewed : ProductEvent("fx_rates_viewed")
    data class FxRatesTabSelected(val assetType: String) : ProductEvent(
        "fx_rates_tab_selected", mapOf("asset_type" to assetType)
    )
    data class FxRateDetailsViewed(val assetType: String, val marketCode: String) : ProductEvent(
        "fx_rate_details_viewed", mapOf("asset_type" to assetType, "market_code" to marketCode)
    )
    data class FxRateAddAssetStarted(val assetType: String, val marketCode: String) : ProductEvent(
        "fx_rate_add_asset_started", mapOf("asset_type" to assetType, "market_code" to marketCode)
    )
    data class FxRatesRefreshRequested(val trigger: RefreshTrigger) : ProductEvent(
        "fx_rates_refresh_requested", mapOf("trigger" to trigger.analyticsValue)
    )
    data class FxRatesRefreshCompleted(val rateCount: Int, val trigger: RefreshTrigger) : ProductEvent(
        "fx_rates_refresh_completed", mapOf("rate_count" to rateCount, "trigger" to trigger.analyticsValue)
    )
    data class FxRatesRefreshFailed(val trigger: RefreshTrigger) : ProductEvent(
        "fx_rates_refresh_failed", mapOf("trigger" to trigger.analyticsValue)
    )
    data object AssetDashboardPerformanceViewed : ProductEvent("asset_dashboard_performance_viewed")
    data class AssetRateRefreshRequested(val trigger: RefreshTrigger) : ProductEvent(
        "asset_rate_refresh_requested", mapOf("trigger" to trigger.analyticsValue)
    )
    data class AssetRateRefreshCompleted(val rateCount: Int, val trigger: RefreshTrigger) : ProductEvent(
        "asset_rate_refresh_completed", mapOf("rate_count" to rateCount, "trigger" to trigger.analyticsValue)
    )
    data class AssetRateRefreshFailed(val trigger: RefreshTrigger) : ProductEvent(
        "asset_rate_refresh_failed", mapOf("trigger" to trigger.analyticsValue)
    )
    data class AssetMarketSelected(val assetType: String, val marketCode: String) : ProductEvent(
        "asset_market_selected", mapOf("asset_type" to assetType, "market_code" to marketCode)
    )
    data class AssetTransactionPromptAnswered(val accepted: Boolean) : ProductEvent(
        "asset_transaction_prompt_answered", mapOf("accepted" to accepted)
    )

    // 12. Utilities & Tools
    data object ToolsHubViewed : ProductEvent("tools_hub_viewed")
    data object CurrencyConverterUsed : ProductEvent("currency_converter_used")
    data object CurrencyConverterInputStarted : ProductEvent("currency_converter_input_started")
    data class CurrencyPickerOpened(val side: String) : ProductEvent(
        "currency_picker_opened", mapOf("side" to side)
    )
    data class CurrencyConversionCalculated(val fromCode: String, val toCode: String) : ProductEvent(
        "currency_conversion_calculated", mapOf("from_code" to fromCode, "to_code" to toCode)
    )
    data class CurrencyPairSelected(val side: String, val currencyCode: String) : ProductEvent(
        "currency_pair_selected", mapOf("side" to side, "currency_code" to currencyCode)
    )
    data class CurrencyFavoritePairSelected(val fromCode: String, val toCode: String) : ProductEvent(
        "currency_favorite_pair_selected", mapOf("from_code" to fromCode, "to_code" to toCode)
    )
    data object CurrencyPairSwapped : ProductEvent("currency_pair_swapped")
    data class CurrencyRatesRefreshRequested(val trigger: RefreshTrigger) : ProductEvent(
        "currency_rates_refresh_requested", mapOf("trigger" to trigger.analyticsValue)
    )
    data class CurrencyRatesRefreshCompleted(val rateCount: Int, val trigger: RefreshTrigger) : ProductEvent(
        "currency_rates_refresh_completed", mapOf("rate_count" to rateCount, "trigger" to trigger.analyticsValue)
    )
    data class CurrencyRatesRefreshFailed(val trigger: RefreshTrigger) : ProductEvent(
        "currency_rates_refresh_failed", mapOf("trigger" to trigger.analyticsValue)
    )
    data object AiAdvisorOpened : ProductEvent("ai_advisor_opened")
    data object AiInsightGenerated : ProductEvent("ai_insight_generated")
    data object ShoppingItemAdded : ProductEvent("shopping_item_added")
    data object ShoppingItemPurchased : ProductEvent("shopping_item_purchased")
    data object NoteCreated : ProductEvent("note_created")
    data object NoteUpdated : ProductEvent("note_updated")
    data object NoteDeleted : ProductEvent("note_deleted")
    data object NoteListViewed : ProductEvent("note_list_viewed")
    data class SmsDraftDetected(val sourceMatched: Boolean) : ProductEvent(
        "sms_draft_detected",
        mapOf("source_matched" to sourceMatched)
    )
    data object SmsDraftIgnored : ProductEvent("sms_draft_ignored")
    data object SmsDraftsBulkDeleted : ProductEvent("sms_drafts_bulk_deleted")
    data object SmsDraftQuickRegistered : ProductEvent("sms_draft_quick_registered")
    data object SmsDraftApplied : ProductEvent("sms_draft_applied")

    // 13. Gamification & Achievements
    data object GamificationHubViewed : ProductEvent("gamification_hub_viewed")
    data class AchievementUnlocked(val name: String) : ProductEvent("achievement_unlocked", mapOf("name" to name))
    
    // 14. Financial Goals
    data object GoalListViewed : ProductEvent("goal_list_viewed")
    data object GoalCreated : ProductEvent("goal_created")
    data object GoalUpdated : ProductEvent("goal_updated")
    data object GoalDeleted : ProductEvent("goal_deleted")
    // 13. Backup & Sync
    data object BackupExportedLocal : ProductEvent("backup_exported_local")
    data object BackupRestored : ProductEvent("backup_restored")
    data object SyncStarted : ProductEvent("sync_started")
    data object SyncCompleted : ProductEvent("sync_completed")
    data object SyncFailed : ProductEvent("sync_failed")

    // 14. Settings & Profile
    data object ProfileViewed : ProductEvent("profile_viewed")
    data class ProfileEdited(val hasName: Boolean) : ProductEvent("profile_edited", mapOf("has_name" to hasName))
    data class ThemeChanged(val themeName: String) : ProductEvent("theme_changed", mapOf("theme" to themeName))
    data object BaseCurrencyChanged : ProductEvent("base_currency_changed")
    data object NotificationSettingsChanged : ProductEvent("notification_settings_changed")
    data object NotificationPermissionRequested : ProductEvent("notification_permission_requested")
    data class NotificationPermissionResult(val granted: Boolean) : ProductEvent(
        "notification_permission_result",
        mapOf("granted" to granted)
    )
    data class SmsPermissionResult(val granted: Boolean) : ProductEvent(
        "sms_permission_result",
        mapOf("granted" to granted)
    )
    data class NotificationSettingToggled(val settingType: String, val enabled: Boolean) : ProductEvent("notification_setting_toggled", mapOf("setting_type" to settingType, "enabled" to enabled))
    
    // 15. Lifecycle & Notifications
    data object AppInstalled : ProductEvent("app_installed")
    data object AppUpdated : ProductEvent("app_updated")
    data object AppOpened : ProductEvent("app_opened")
    data class CampaignAttributed(val source: String, val campaign: String) : ProductEvent("campaign_attributed", mapOf("source" to source, "campaign" to campaign))
    data class NotificationReceived(val type: String) : ProductEvent("notification_received", mapOf("type" to type))
    data class NotificationClicked(val type: String) : ProductEvent("notification_clicked", mapOf("type" to type))
    data class NotificationDismissed(val type: String) : ProductEvent("notification_dismissed", mapOf("type" to type))
    data object WidgetAdded : ProductEvent("widget_added")
    data object WidgetRemoved : ProductEvent("widget_removed")
    
    // 16. General Filters
    data class FilterApplied(val featureKey: String) : ProductEvent("filter_applied", mapOf("feature_key" to featureKey))

    // Fallbacks
    data object AccountCreated : ProductEvent("account_created")
    data object FirstTransactionCompleted : ProductEvent("first_transaction_completed")

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

/** Identifies whether a market-rate refresh was automatic or explicitly requested by the user. */
enum class RefreshTrigger(val analyticsValue: String) {
    INITIAL("initial"),
    MANUAL("manual")
}
