package com.kazemieh.goals.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Goal
import com.kazemieh.domain.usecase.GoalUseCases
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddGoalViewModel(
    private val goalUseCases: GoalUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(AddGoalState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddGoalEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AddGoalIntent) {
        when (intent) {
            is AddGoalIntent.InitialData -> {
                intent.goal?.let { goal ->
                    _state.update {
                        it.copy(
                            id = goal.id,
                            name = goal.name,
                            targetAmount = goal.targetAmount.toString(),
                            savedAmount = goal.savedAmount.toString(),
                            iconId = goal.iconId,
                            colorId = goal.colorId,
                            endDate = goal.endDate ?: "",
                            monthlyTarget = goal.monthlyTarget.toString()
                        )
                    }
                }
            }
            is AddGoalIntent.UpdateName -> _state.update { it.copy(name = intent.name) }
            is AddGoalIntent.UpdateTargetAmount -> _state.update { it.copy(targetAmount = intent.amount) }
            is AddGoalIntent.UpdateSavedAmount -> _state.update { it.copy(savedAmount = intent.amount) }
            is AddGoalIntent.UpdateEndDate -> _state.update { it.copy(endDate = intent.date) }
            is AddGoalIntent.UpdateMonthlyTarget -> _state.update { it.copy(monthlyTarget = intent.amount) }
            is AddGoalIntent.UpdateColorIcon -> _state.update { it.copy(colorId = intent.colorId, iconId = intent.iconId) }
            AddGoalIntent.SaveGoal -> saveGoal()
        }
    }

    private fun saveGoal() {
        viewModelScope.launch {
            val currentState = _state.value
            val goal = Goal(
                id = currentState.id,
                name = currentState.name,
                targetAmount = currentState.targetAmount.toLongOrNull() ?: 0L,
                savedAmount = currentState.savedAmount.toLongOrNull() ?: 0L,
                iconId = currentState.iconId,
                colorId = currentState.colorId,
                endDate = currentState.endDate.ifBlank { null },
                monthlyTarget = currentState.monthlyTarget.toLongOrNull() ?: 0L
            )
            
            if (goal.id == 0L) {
                goalUseCases.addGoal(goal)
            } else {
                goalUseCases.updateGoal(goal)
            }
            _effect.send(AddGoalEffect.GoalSaved)
        }
    }
}
