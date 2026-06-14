package com.kazemieh.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.lock.LockMode
import com.kazemieh.preferences.FinTrackPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ProfileViewModel(
    private val preferenceUseCases: PreferenceUseCases,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    private val _effect = Channel<ProfileEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadSettings()
        loadProfile()
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun loadProfile() {
        _state.update {
            it.copy(
                firstName = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_USER_NAME, ""),
                lastName = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_USER_FAMILY, ""),
                email = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_USER_EMAIL, ""),
                phone = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_USER_PHONE, ""),
                avatar = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_USER_AVATAR, "").let { base64 ->
                    if (base64.isNotEmpty()) {
                        try {
                            Base64.decode(base64)
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                }
            )
        }
    }

    private fun loadSettings() {
        _state.update {
            it.copy(
                isDarkModeEnabled = preferenceUseCases.getBooleanPreference(PREF_DARK_MODE, false),
                isFingerprintEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_BIOMETRIC_ENABLED, false),
                isLockEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_LOCK_ENABLED, false),
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
                if (!_state.value.isLockEnabled) {
                    viewModelScope.launch {
                        _effect.send(ProfileEffect.ShowLockPIN(LockMode.CREATE, true))
                    }
                } else {
                    val newValue = !_state.value.isFingerprintEnabled
                    preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_BIOMETRIC_ENABLED, newValue)
                    _state.update { it.copy(isFingerprintEnabled = newValue) }
                }
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
            is ProfileIntent.ToggleLock -> {
                val mode = if (_state.value.isLockEnabled) LockMode.VERIFY_BEFORE_DISABLE else LockMode.CREATE
                viewModelScope.launch {
                    _effect.send(ProfileEffect.ShowLockPIN(mode, false))
                }
            }
            is ProfileIntent.Refresh -> {
                loadProfile()
            }
            is ProfileIntent.SetLockState -> {
                preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_LOCK_ENABLED, intent.isEnabled)
                if (!intent.isEnabled) {
                    preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_BIOMETRIC_ENABLED, false)
                    _state.update { it.copy(isLockEnabled = false, isFingerprintEnabled = false) }
                } else {
                    _state.update { it.copy(isLockEnabled = true) }
                }
            }
            ProfileIntent.Logout -> {
                // Handle logout logic
            }
        }
    }

    companion object {
        private const val PREF_DARK_MODE = "pref_dark_mode"
        private const val PREF_BACKUP = "pref_backup"
        private const val PREF_PUSH_NOTIF = "pref_push_notif"
        private const val PREF_TX_ALERTS = "pref_tx_alerts"
    }
}

data class ProfileState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val avatar: ByteArray? = null,
    val isDarkModeEnabled: Boolean = false,
    val isFingerprintEnabled: Boolean = false,
    val isLockEnabled: Boolean = false,
    val isBackupEnabled: Boolean = true,
    val isPushNotificationsEnabled: Boolean = true,
    val isTransactionAlertsEnabled: Boolean = true,
    val isLoading: Boolean = false
)

sealed interface ProfileIntent {
    data object Refresh : ProfileIntent
    data object ToggleDarkMode : ProfileIntent
    data object ToggleFingerprint : ProfileIntent
    data object ToggleBackup : ProfileIntent
    data object TogglePushNotifications : ProfileIntent
    data object ToggleTransactionAlerts : ProfileIntent
    data object ToggleLock : ProfileIntent
    data class SetLockState(val isEnabled: Boolean) : ProfileIntent
    data object Logout : ProfileIntent
}

sealed interface ProfileEffect {
    data class ShowLockPIN(val mode: LockMode, val triggerFingerprint: Boolean) : ProfileEffect
}
