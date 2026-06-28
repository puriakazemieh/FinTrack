package com.kazemieh.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.domain.notification.NotificationManager
import com.kazemieh.preferences.FinTrackPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(
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
                } else {
                    _state.update { it.copy(triggerSystemPermissionRequest = true) }
                }
            }
            NotificationSettingsIntent.ToggleChequeNotif -> {
                if (notificationManager.hasPermission()) {
                    val newValue = !_state.value.isChequeNotifEnabled
                    preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_NOTIF_CHEQUE_ENABLED, newValue)
                    _state.update { it.copy(isChequeNotifEnabled = newValue) }
                } else {
                    _state.update { it.copy(triggerSystemPermissionRequest = true) }
                }
            }
            NotificationSettingsIntent.ToggleQuietHours -> {
                val newValue = !_state.value.isQuietHoursEnabled
                preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_NOTIF_QUIET_HOURS_ENABLED, newValue)
                _state.update { it.copy(isQuietHoursEnabled = newValue) }
            }
            is NotificationSettingsIntent.SetQuietStart -> {
                preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_NOTIF_QUIET_START, intent.time)
                _state.update { it.copy(quietStart = intent.time) }
            }
            is NotificationSettingsIntent.SetQuietEnd -> {
                preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_NOTIF_QUIET_END, intent.time)
                _state.update { it.copy(quietEnd = intent.time) }
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
