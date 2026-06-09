package com.kazemieh.profile

import androidx.lifecycle.ViewModel
import com.kazemieh.domain.usecase.PreferenceUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel(
    private val preferenceUseCases: PreferenceUseCases,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _state.update {
            it.copy(
                isDarkModeEnabled = preferenceUseCases.getBooleanPreference(PREF_DARK_MODE, false),
                isFingerprintEnabled = preferenceUseCases.getBooleanPreference(PREF_FINGERPRINT, false),
                isBackupEnabled = preferenceUseCases.getBooleanPreference(PREF_BACKUP, true),
                isPushNotificationsEnabled = preferenceUseCases.getBooleanPreference(PREF_PUSH_NOTIF, true),
                isTransactionAlertsEnabled = preferenceUseCases.getBooleanPreference(PREF_TX_ALERTS, true)
            )
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.ToggleDarkMode -> {
                val newValue = !_state.value.isDarkModeEnabled
                preferenceUseCases.setBooleanPreference(PREF_DARK_MODE, newValue)
                _state.update { it.copy(isDarkModeEnabled = newValue) }
            }
            is ProfileIntent.ToggleFingerprint -> {
                val newValue = !_state.value.isFingerprintEnabled
                preferenceUseCases.setBooleanPreference(PREF_FINGERPRINT, newValue)
                _state.update { it.copy(isFingerprintEnabled = newValue) }
            }
            is ProfileIntent.ToggleBackup -> {
                val newValue = !_state.value.isBackupEnabled
                preferenceUseCases.setBooleanPreference(PREF_BACKUP, newValue)
                _state.update { it.copy(isBackupEnabled = newValue) }
            }
            is ProfileIntent.TogglePushNotifications -> {
                val newValue = !_state.value.isPushNotificationsEnabled
                preferenceUseCases.setBooleanPreference(PREF_PUSH_NOTIF, newValue)
                _state.update { it.copy(isPushNotificationsEnabled = newValue) }
            }
            is ProfileIntent.ToggleTransactionAlerts -> {
                val newValue = !_state.value.isTransactionAlertsEnabled
                preferenceUseCases.setBooleanPreference(PREF_TX_ALERTS, newValue)
                _state.update { it.copy(isTransactionAlertsEnabled = newValue) }
            }
            ProfileIntent.Logout -> {
                // Handle logout logic
            }
        }
    }

    companion object {
        private const val PREF_DARK_MODE = "pref_dark_mode"
        private const val PREF_FINGERPRINT = "pref_fingerprint"
        private const val PREF_BACKUP = "pref_backup"
        private const val PREF_PUSH_NOTIF = "pref_push_notif"
        private const val PREF_TX_ALERTS = "pref_tx_alerts"
    }
}

data class ProfileState(
    val isDarkModeEnabled: Boolean = false,
    val isFingerprintEnabled: Boolean = false,
    val isBackupEnabled: Boolean = true,
    val isPushNotificationsEnabled: Boolean = true,
    val isTransactionAlertsEnabled: Boolean = true,
    val isLoading: Boolean = false
)

sealed interface ProfileIntent {
    data object ToggleDarkMode : ProfileIntent
    data object ToggleFingerprint : ProfileIntent
    data object ToggleBackup : ProfileIntent
    data object TogglePushNotifications : ProfileIntent
    data object ToggleTransactionAlerts : ProfileIntent
    data object Logout : ProfileIntent
}
