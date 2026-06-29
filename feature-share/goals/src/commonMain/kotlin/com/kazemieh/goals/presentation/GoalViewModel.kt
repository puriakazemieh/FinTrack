package com.kazemieh.goals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.GoalUseCases
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalViewModel(
    private val goalUseCases: GoalUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(GoalState())
    val state = _state.asStateFlow()

    private val _effect = Channel<GoalEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(GoalIntent.LoadGoals)
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
