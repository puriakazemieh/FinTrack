package com.kazemieh.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.preferences.FinTrackPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LockViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
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
        if (verifyHash(_state.value.resetAnswer, savedHashedAnswer)) {
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

        if (verifyHash(pin, savedHashedPin)) {
            // Upgrade any legacy-format hash to the hardened format on successful login.
            if (!savedHashedPin.startsWith("v2:")) {
                preferenceUseCases.setStringPreference(
                    FinTrackPreferences.PREF_HASHED_PIN,
                    createHash(pin)
                )
            }
            unlock()
        } else {
            _state.update { it.copy(pin = "", error = "lock_error_pin") }
        }
    }

    private fun saveNewPin(pin: String) {
        val hashed = createHash(pin)
        val isChanging = _state.value.isLockEnabled
        preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_HASHED_PIN, hashed)
        preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_LOCK_ENABLED, true)
        if (isChanging) {
            analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("auth_pin_changed"))
        } else {
            analytics.track(com.kazemieh.common.analytics.ProductEvent.AuthPinCreated)
        }
        _state.update { it.copy(isLockEnabled = true) }
        unlock()
    }

    private fun unlock() {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.AppUnlocked)
        _state.update { it.copy(isLocked = false, pin = "") }
        viewModelScope.launch { _effect.send(LockEffect.Success) }
    }

    fun onBiometricSuccess() {
        unlock()
    }

    fun onBiometricError(error: String) {
        _state.update { it.copy(error = "biometric_failed") }
    }

    // PIN/answer hashing. This is a dependency-free hardening (random per-hash salt
    // stored inline + a large work factor) over the previous fixed-salt single pass,
    // which defeats rainbow tables and slows brute force. It is NOT a substitute for a
    // platform KDF (PBKDF2/Argon2 via Android Keystore / iOS Keychain) — that remains
    // the recommended follow-up. Legacy hashes still verify and are upgraded on use.
    private fun createHash(input: String): String {
        val salt = kotlin.random.Random.nextLong()
        return "v2:${salt.toString(16)}:${mixHash(input, salt)}"
    }

    private fun verifyHash(input: String, stored: String): Boolean {
        if (stored.startsWith("v2:")) {
            val parts = stored.split(":")
            if (parts.size != 3) return false
            val salt = parts[1].toLongOrNull(16) ?: return false
            return mixHash(input, salt) == parts[2]
        }
        return legacyHash(input) == stored
    }

    private fun mixHash(input: String, salt: Long): String {
        var hash = salt xor 0x5DEECE66DL
        repeat(120_000) {
            for (char in input) {
                hash = 31 * hash + char.code.toLong()
            }
            hash = hash xor (hash ushr 33)
            hash *= -0x61c8864680b583ebL
            hash = hash xor salt
        }
        return hash.toString(16)
    }

    private fun legacyHash(pin: String): String {
        val salt = "FinTrack_2026_Secure_Salt"
        var hash = 7L
        val combined = pin + salt
        for (char in combined) {
            hash = 31 * hash + char.code.toLong()
        }
        return hash.toString(16)
    }
}
