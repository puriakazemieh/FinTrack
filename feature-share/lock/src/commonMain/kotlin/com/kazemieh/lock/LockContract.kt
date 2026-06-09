package com.kazemieh.lock

data class LockState(
    val pin: String = "",
    val error: String? = null,
    val isLocked: Boolean = true,
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isLockEnabled: Boolean = false,
    val mode: LockMode = LockMode.UNLOCK
)

enum class LockMode {
    UNLOCK, // Normal app entry
    CREATE, // Setting up new PIN
    CONFIRM // Confirming new PIN
}

enum class KeypadKey {
    DIGIT_0, DIGIT_1, DIGIT_2, DIGIT_3, DIGIT_4, DIGIT_5, DIGIT_6, DIGIT_7, DIGIT_8, DIGIT_9,
    DELETE, BIOMETRIC;

    override fun toString(): String {
        return when (this) {
            DIGIT_0 -> "0"
            DIGIT_1 -> "1"
            DIGIT_2 -> "2"
            DIGIT_3 -> "3"
            DIGIT_4 -> "4"
            DIGIT_5 -> "5"
            DIGIT_6 -> "6"
            DIGIT_7 -> "7"
            DIGIT_8 -> "8"
            DIGIT_9 -> "9"
            else -> name
        }
    }
}

sealed interface LockIntent {
    data class KeyPressed(val key: KeypadKey) : LockIntent
    data object AuthenticateBiometric : LockIntent
}

sealed interface LockEffect {
    data object Success : LockEffect
    data class Error(val message: String) : LockEffect
    data object TriggerBiometric : LockEffect
}
