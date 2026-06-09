package com.kazemieh.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.preferences.FinTrackPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ProfileEditViewModel(
    private val preferenceUseCases: PreferenceUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileEditState())
    val state: StateFlow<ProfileEditState> = _state.asStateFlow()

    private val _effect = Channel<ProfileEditEffect>()
    val effect = _effect.receiveAsFlow()

    init {
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
                birthday = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_USER_BIRTHDAY, ""),
                city = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_USER_CITY, ""),
                income = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_USER_INCOME, ""),
                jobTitle = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_USER_JOB, ""),
                financialGoal = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_USER_GOAL, ""),
                avatar = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_USER_AVATAR, "").let { base64 ->
                    if (base64.isNotEmpty()) Base64.decode(base64) else null
                }
            )
        }
        calculateCompletion()
    }

    fun onIntent(intent: ProfileEditIntent) {
        when (intent) {
            is ProfileEditIntent.UpdateFirstName -> {
                _state.update { it.copy(firstName = intent.name) }
                calculateCompletion()
            }
            is ProfileEditIntent.UpdateLastName -> {
                _state.update { it.copy(lastName = intent.family) }
                calculateCompletion()
            }
            is ProfileEditIntent.UpdateEmail -> {
                _state.update { it.copy(email = intent.email) }
                calculateCompletion()
            }
            is ProfileEditIntent.UpdatePhone -> {
                _state.update { it.copy(phone = intent.phone) }
                calculateCompletion()
            }
            is ProfileEditIntent.UpdateBirthday -> {
                _state.update { it.copy(birthday = intent.birthday) }
                calculateCompletion()
            }
            is ProfileEditIntent.UpdateCity -> {
                _state.update { it.copy(city = intent.city) }
                calculateCompletion()
            }
            is ProfileEditIntent.UpdateIncome -> {
                _state.update { it.copy(income = intent.income) }
                calculateCompletion()
            }
            is ProfileEditIntent.UpdateJobTitle -> {
                _state.update { it.copy(jobTitle = intent.job) }
                calculateCompletion()
            }
            is ProfileEditIntent.UpdateFinancialGoal -> {
                _state.update { it.copy(financialGoal = intent.goal) }
                calculateCompletion()
            }
            is ProfileEditIntent.UpdateAvatar -> {
                _state.update { it.copy(avatar = intent.avatar) }
                calculateCompletion()
            }
            ProfileEditIntent.SaveProfile -> saveProfile()
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun saveProfile() {
        val currentState = _state.value
        preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_USER_NAME, currentState.firstName)
        preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_USER_FAMILY, currentState.lastName)
        preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_USER_EMAIL, currentState.email)
        preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_USER_PHONE, currentState.phone)
        preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_USER_BIRTHDAY, currentState.birthday)
        preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_USER_CITY, currentState.city)
        preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_USER_INCOME, currentState.income)
        preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_USER_JOB, currentState.jobTitle)
        preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_USER_GOAL, currentState.financialGoal)
        currentState.avatar?.let {
            preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_USER_AVATAR, Base64.encode(it))
        }

        viewModelScope.launch {
            _effect.send(ProfileEditEffect.ProfileSaved)
        }
    }

    private fun calculateCompletion() {
        val fields = listOf(
            _state.value.firstName,
            _state.value.lastName,
            _state.value.email,
            _state.value.phone,
            _state.value.birthday,
            _state.value.city,
            _state.value.income,
            _state.value.jobTitle,
            _state.value.financialGoal
        )
        val filledCount = fields.count { it.isNotEmpty() } + (if (_state.value.avatar != null) 1 else 0)
        val totalFields = fields.size + 1
        _state.update { it.copy(completionPercentage = filledCount.toFloat() / totalFields) }
    }
}
