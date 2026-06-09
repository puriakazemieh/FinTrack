package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.PreferenceRepository

data class PreferenceUseCases(
    val getBooleanPreference: GetBooleanPreferenceUseCase,
    val setBooleanPreference: SetBooleanPreferenceUseCase
)

class GetBooleanPreferenceUseCase(private val repository: PreferenceRepository) {
    operator fun invoke(key: String, defaultValue: Boolean): Boolean = repository.getBoolean(key, defaultValue)
}

class SetBooleanPreferenceUseCase(private val repository: PreferenceRepository) {
    operator fun invoke(key: String, value: Boolean) = repository.putBoolean(key, value)
}
