package com.kazemieh.goals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.GoalUseCases
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.preferences.FinTrackPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalViewModel(
    private val goalUseCases: GoalUseCases,
    private val preferenceUseCases: PreferenceUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(GoalState())
    val state = _state.asStateFlow()

    private val _effect = Channel<GoalEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(GoalIntent.LoadGoals)
        loadRoundUpSettings()
    }

    private fun loadRoundUpSettings() {
        _state.update {
            it.copy(
                isRoundUpEnabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_ROUNDUP_ENABLED, false),
                roundUpGoalId = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_ROUNDUP_GOAL_ID, "").toLongOrNull(),
                roundUpUnit = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_ROUNDUP_UNIT, "5000").toLongOrNull() ?: 5000L
            )
        }
    }

    fun onIntent(intent: GoalIntent) {
        when (intent) {
            GoalIntent.LoadGoals -> observeGoals()
            is GoalIntent.UpdateSearchQuery -> _state.update { it.copy(searchQuery = intent.query) }
            is GoalIntent.DeleteGoal -> deleteGoal(intent.id)
            is GoalIntent.ShowAddGoal -> {
                _state.update { it.copy(isAddGoalShow = !it.isAddGoalShow, selectedGoal = intent.goal) }
            }
            is GoalIntent.AddAmountToGoal -> addAmountToGoal(intent.id, intent.amount)
            GoalIntent.ToggleRoundUpSettings -> _state.update { it.copy(showRoundUpSettings = !it.showRoundUpSettings) }
            GoalIntent.ToggleRoundUpEnabled -> {
                val newValue = !_state.value.isRoundUpEnabled
                preferenceUseCases.setBooleanPreference(FinTrackPreferences.PREF_ROUNDUP_ENABLED, newValue)
                _state.update { it.copy(isRoundUpEnabled = newValue) }
            }
            is GoalIntent.SetRoundUpGoal -> {
                preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_ROUNDUP_GOAL_ID, intent.goalId.toString())
                _state.update { it.copy(roundUpGoalId = intent.goalId) }
            }
            is GoalIntent.SetRoundUpUnit -> {
                preferenceUseCases.setStringPreference(FinTrackPreferences.PREF_ROUNDUP_UNIT, intent.unit.toString())
                _state.update { it.copy(roundUpUnit = intent.unit) }
            }
        }
    }

    private fun observeGoals() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            goalUseCases.observeGoals().collect { goals ->
                val totalSaved = goals.sumOf { it.savedAmount }
                val totalTarget = goals.sumOf { it.targetAmount }
                _state.update {
                    it.copy(
                        goals = goals,
                        totalSavedAmount = totalSaved,
                        totalTargetAmount = totalTarget,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun deleteGoal(id: Long) {
        viewModelScope.launch {
            goalUseCases.deleteGoal(id)
        }
    }

    private fun addAmountToGoal(id: Long, amount: Long) {
        viewModelScope.launch {
            val goal = goalUseCases.getGoalById(id)
            if (goal != null) {
                val updatedGoal = goal.copy(savedAmount = goal.savedAmount + amount)
                goalUseCases.updateGoal(updatedGoal)
            }
        }
    }
}
