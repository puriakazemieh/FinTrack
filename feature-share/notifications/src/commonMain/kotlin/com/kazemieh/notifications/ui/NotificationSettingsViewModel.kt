package com.kazemieh.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.preferences.FinTrackPreferences
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.notif_quick_add_action
import fintrack.core.designsystem.generated.resources.notif_quick_add_message
import fintrack.core.designsystem.generated.resources.notif_quick_add_title
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class NotificationSettingsViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val preferenceUseCases: PreferenceUseCases,
    private val notificationManager: NotificationManager
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationSettingsState())
    val state: StateFlow<NotificationSettingsState> = _state

    private val _effect = Channel<NotificationSettingsEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadSettings()
        checkPermission()
    }

    private fun loadSettings() {
        _state.update {
            it.copy(
                isBudgetNotifEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_NOTIF_BUDGET_ENABLED, true),
                isInstallmentNotifEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_NOTIF_INSTALLMENT_ENABLED, true),
                isChequeNotifEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_NOTIF_CHEQUE_ENABLED, true),
                isQuickAddNotifEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_QUICK_ADD_NOTIF_ENABLED, false),
                isQuietHoursEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_NOTIF_QUIET_HOURS_ENABLED, false),
                quietStart = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_NOTIF_QUIET_START, "22:00"),
                quietEnd = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_NOTIF_QUIET_END, "08:00")
            )
        }
    }

    private fun checkPermission() {
        // Initial check, will be augmented by UI rationale state
        if (!notificationManager.hasPermission()) {
            // Wait for RefreshPermissionStatus from UI
        }
    }

    fun onIntent(intent: NotificationSettingsIntent) {
        when (intent) {
            NotificationSettingsIntent.ToggleBudgetNotif -> {
                if (notificationManager.hasPermission()) {
                    val newValue = !_state.value.isBudgetNotifEnabled
                    preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_NOTIF_BUDGET_ENABLED, newValue)
                    _state.update { it.copy(isBudgetNotifEnabled = newValue) }
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("notification_settings_changed", mapOf("setting" to "budget", "enabled" to newValue.toString())))
                } else {
                    // Trigger flow via UI check
                    _state.update { it.copy(triggerSystemPermissionRequest = true) }
                }
            }
            NotificationSettingsIntent.ToggleInstallmentNotif -> {
                if (notificationManager.hasPermission()) {
                    val newValue = !_state.value.isInstallmentNotifEnabled
                    preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_NOTIF_INSTALLMENT_ENABLED, newValue)
                    _state.update { it.copy(isInstallmentNotifEnabled = newValue) }
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("notification_settings_changed", mapOf("setting" to "installment", "enabled" to newValue.toString())))
                } else {
                    _state.update { it.copy(triggerSystemPermissionRequest = true) }
                }
            }
            NotificationSettingsIntent.ToggleChequeNotif -> {
                if (notificationManager.hasPermission()) {
                    val newValue = !_state.value.isChequeNotifEnabled
                    preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_NOTIF_CHEQUE_ENABLED, newValue)
                    _state.update { it.copy(isChequeNotifEnabled = newValue) }
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("notification_settings_changed", mapOf("setting" to "cheque", "enabled" to newValue.toString())))
                } else {
                    _state.update { it.copy(triggerSystemPermissionRequest = true) }
                }
            }
            NotificationSettingsIntent.ToggleQuickAddNotif -> {
                if (notificationManager.hasPermission()) {
                    val newValue = !_state.value.isQuickAddNotifEnabled
                    preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_QUICK_ADD_NOTIF_ENABLED, newValue)
                    _state.update { it.copy(isQuickAddNotifEnabled = newValue) }
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("notification_settings_changed", mapOf("setting" to "quick_add", "enabled" to newValue.toString())))
                    if (newValue) {
                        viewModelScope.launch {
                            notificationManager.showQuickAddNotification(
                                title = getString(Res.string.notif_quick_add_title),
                                message = getString(Res.string.notif_quick_add_message),
                                actionLabel = getString(Res.string.notif_quick_add_action)
                            )
                        }
                    } else {
                        notificationManager.cancelNotification(NotificationManager.ID_QUICK_ADD)
                    }
                } else {
                    _state.update { it.copy(triggerSystemPermissionRequest = true) }
                }
            }
            NotificationSettingsIntent.ToggleQuietHours -> {
                val newValue = !_state.value.isQuietHoursEnabled
                preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_NOTIF_QUIET_HOURS_ENABLED, newValue)
                _state.update { it.copy(isQuietHoursEnabled = newValue) }
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("notification_settings_changed", mapOf("setting" to "quiet_hours", "enabled" to newValue.toString())))
            }
            is NotificationSettingsIntent.SetQuietStart -> {
                preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_NOTIF_QUIET_START, intent.time)
                _state.update { it.copy(quietStart = intent.time) }
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("notification_settings_changed", mapOf("setting" to "quiet_start")))
            }
            is NotificationSettingsIntent.SetQuietEnd -> {
                preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_NOTIF_QUIET_END, intent.time)
                _state.update { it.copy(quietEnd = intent.time) }
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("notification_settings_changed", mapOf("setting" to "quiet_end")))
            }
            is NotificationSettingsIntent.RefreshPermissionStatus -> {
                if (!notificationManager.hasPermission() && intent.shouldShowRationale) {
                    _state.update { it.copy(showPermissionRationale = true) }
                }
            }
            is NotificationSettingsIntent.RequestPermission -> {
                if (intent.isRationaleShown) {
                    _state.update { it.copy(triggerSystemPermissionRequest = true, showPermissionRationale = false) }
                } else {
                    val hasRequestedBefore = preferenceUseCases.getBooleanPreference("has_requested_notif_permission", false)
                    if (hasRequestedBefore && !notificationManager.hasPermission()) {
                        notificationManager.openSettings()
                    } else {
                        _state.update { it.copy(triggerSystemPermissionRequest = true, showPermissionRationale = false) }
                        preferenceUseCases.setBooleanPreference("has_requested_notif_permission", true)
                    }
                }
            }
            is NotificationSettingsIntent.OnPermissionResult -> {
                _state.update { it.copy(triggerSystemPermissionRequest = false) }
                if (intent.granted) {
                    _state.update { it.copy(showPermissionRationale = false) }
                }
            }
            NotificationSettingsIntent.DismissPermissionRationale -> {
                _state.update { it.copy(showPermissionRationale = false) }
            }
        }
    }
}
