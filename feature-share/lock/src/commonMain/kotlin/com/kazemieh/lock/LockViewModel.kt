package com.kazemieh.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.preferences.FinTrackPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LockViewModel(
    private val preferenceUseCases: PreferenceUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(LockState())
    val state: StateFlow<LockState> = _state.asStateFlow()

    private val _effect = Channel<LockEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val isLockEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_LOCK_ENABLED, false)
        val isBiometricEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_BIOMETRIC_ENABLED, false)
        
        _state.update { 
            it.copy(
                isLockEnabled = isLockEnabled,
                isBiometricEnabled = isBiometricEnabled,
                isLocked = isLockEnabled
            )
        }
        
        if (isLockEnabled && isBiometricEnabled) {
            viewModelScope.launch {
                _effect.send(LockEffect.TriggerBiometric)
            }
        }
    }

    fun onIntent(intent: LockIntent) {
        when (intent) {
            is LockIntent.KeyPressed -> {
                handleKeypad(intent.key)
            }
            LockIntent.AuthenticateBiometric -> {
                viewModelScope.launch {
                    _effect.send(LockEffect.TriggerBiometric)
                }
            }
        }
    }

    private fun handleKeypad(key: KeypadKey) {
        when (key) {
            KeypadKey.DELETE -> {
                if (_state.value.pin.isNotEmpty()) {
                    _state.update { it.copy(pin = it.pin.dropLast(1)) }
                }
            }
            KeypadKey.BIOMETRIC -> {
                // Handled via LockIntent.AuthenticateBiometric
            }
            else -> {
                if (_state.value.pin.length < 4) {
                    val newPin = _state.value.pin + key.toString()
                    _state.update { it.copy(pin = newPin, error = null) }
                    if (newPin.length == 4) {
                        verifyPin(newPin)
                    }
                }
            }
        }
    }

    private fun verifyPin(pin: String) {
        val savedHashedPin = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_HASHED_PIN, "")
        if (savedHashedPin.isEmpty()) {
            // If no PIN is set but lock is enabled, we might want to force create or just allow
            unlock()
            return
        }

        if (hashPin(pin) == savedHashedPin) {
            unlock()
        } else {
            _state.update { it.copy(pin = "", error = "lock_error_pin") } // String key for UI
        }
    }

    private fun unlock() {
        _state.update { it.copy(isLocked = false, pin = "") }
        viewModelScope.launch { _effect.send(LockEffect.Success) }
    }

    fun onBiometricSuccess() {
        unlock()
    }

    fun onBiometricError(error: String) {
        _state.update { it.copy(error = "biometric_failed") }
    }

    private fun hashPin(pin: String): String {
        // Better than reversed, though ideally we'd use a real SHA-256 lib
        val salt = "FinTrack_2026_Secure_Salt"
        var hash = 7L
        val combined = pin + salt
        for (char in combined) {
            hash = 31 * hash + char.code.toLong()
        }
        return hash.toString(16)
    }
}
