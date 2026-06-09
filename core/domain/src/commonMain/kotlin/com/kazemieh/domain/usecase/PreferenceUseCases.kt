package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

data class PreferenceUseCases(
    val getBooleanPreference: GetBooleanPreferenceUseCase,
    val setBooleanPreference: SetBooleanPreferenceUseCase,
    val getStringPreference: GetStringPreferenceUseCase,
    val setStringPreference: SetStringPreferenceUseCase,
    val getStringFlow: GetStringFlowUseCase
)

class GetBooleanPreferenceUseCase(private val repository: PreferenceRepository) {
    operator fun invoke(key: String, defaultValue: Boolean): Boolean = repository.getBoolean(key, defaultValue)
}

class SetBooleanPreferenceUseCase(private val repository: PreferenceRepository) {
    operator fun invoke(key: String, value: Boolean) = repository.putBoolean(key, value)
}

class GetStringPreferenceUseCase(private val repository: PreferenceRepository) {
    operator fun invoke(key: String, defaultValue: String): String = repository.getString(key, defaultValue)
}

class SetStringPreferenceUseCase(private val repository: PreferenceRepository) {
    operator fun invoke(key: String, value: String) = repository.putString(key, value)
}

class GetStringFlowUseCase(private val repository: PreferenceRepository) {
    operator fun invoke(key: String, defaultValue: String): Flow<String> = repository.getStringFlow(key, defaultValue)
}
