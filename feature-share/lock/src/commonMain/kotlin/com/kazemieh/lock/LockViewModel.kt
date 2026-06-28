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

    private var firstTempPin: String = ""

    init {
        val isLockEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_LOCK_ENABLED, false)
        val isBiometricEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_BIOMETRIC_ENABLED, false)
        val securityQuestion = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_SECURITY_QUESTION, "")
        _state.update { 
            it.copy(
                isLockEnabled = isLockEnabled, 
                isBiometricEnabled = isBiometricEnabled,
                isLocked = isLockEnabled,
                isInitialized = true,
                securityQuestion = securityQuestion
            ) 
        }
    }

    fun onIntent(intent: LockIntent) {
        when (intent) {
            is LockIntent.Init -> {
                _state.update { it.copy(mode = intent.mode, pin = "", error = null, subtitle = intent.subtitle) }
                if (intent.mode == LockMode.UNLOCK) {
                    if (_state.value.isLockEnabled && _state.value.isBiometricEnabled) {
                        viewModelScope.launch { _effect.send(LockEffect.TriggerBiometric) }
                    }
                } else {
                    _state.update { it.copy(isLocked = true) }
                }
            }
            is LockIntent.KeyPressed -> {
                handleKeypad(intent.key)
            }
            LockIntent.AuthenticateBiometric -> {
                viewModelScope.launch {
                    _effect.send(LockEffect.TriggerBiometric)
                }
            }
            LockIntent.ForgotPasswordClicked -> {
                _state.update { it.copy(showResetDialog = true, resetAnswer = "", error = null) }
            }
            is LockIntent.SecurityAnswerChanged -> {
                _state.update { it.copy(resetAnswer = intent.answer, error = null) }
            }
            LockIntent.ResetPIN -> {
                verifySecurityAnswer()
            }
            LockIntent.DismissResetDialog -> {
                _state.update { it.copy(showResetDialog = false) }
            }
        }
    }

    private fun verifySecurityAnswer() {
        val savedHashedAnswer = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_SECURITY_ANSWER, "")
        if (hashPin(_state.value.resetAnswer) == savedHashedAnswer) {
            // Reset PIN: Disable lock and clear PIN
            preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_LOCK_ENABLED, false)
            preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_HASHED_PIN, "")
            _state.update { it.copy(isLockEnabled = false, isLocked = false, showResetDialog = false, pin = "") }
            viewModelScope.launch { _effect.send(LockEffect.Success) }
        } else {
            _state.update { it.copy(error = "lock_error_wrong_answer") }
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
                viewModelScope.launch { _effect.send(LockEffect.TriggerBiometric) }
            }
            else -> {
                if (_state.value.pin.length < 4) {
                    val newPin = _state.value.pin + key.toString()
                    _state.update { it.copy(pin = newPin, error = null) }
                    if (newPin.length == 4) {
                        processCompletePin(newPin)
                    }
                }
            }
        }
    }

    private fun processCompletePin(pin: String) {
        when (_state.value.mode) {
            LockMode.UNLOCK, LockMode.VERIFY_BEFORE_DISABLE -> {
                verifyPin(pin)
            }
            LockMode.CREATE -> {
                firstTempPin = pin
                _state.update { it.copy(mode = LockMode.CONFIRM, pin = "") }
            }
            LockMode.CONFIRM -> {
                if (pin == firstTempPin) {
                    saveNewPin(pin)
                } else {
                    _state.update { it.copy(pin = "", error = "lock_error_mismatch", mode = LockMode.CREATE) }
                }
            }
        }
    }

    private fun verifyPin(pin: String) {
        val savedHashedPin = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_HASHED_PIN, "")
        if (savedHashedPin.isEmpty()) {
            unlock()
            return
        }

        if (hashPin(pin) == savedHashedPin) {
            unlock()
        } else {
            _state.update { it.copy(pin = "", error = "lock_error_pin") }
        }
    }

    private fun saveNewPin(pin: String) {
        val hashed = hashPin(pin)
        preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_HASHED_PIN, hashed)
        preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_LOCK_ENABLED, true)
        _state.update { it.copy(isLockEnabled = true) }
        unlock()
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
        val salt = "FinTrack_2026_Secure_Salt"
        var hash = 7L
        val combined = pin + salt
        for (char in combined) {
            hash = 31 * hash + char.code.toLong()
        }
        return hash.toString(16)
    }
}
