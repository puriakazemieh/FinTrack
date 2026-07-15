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
import kotlin.time.Clock

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
                if (intent.goal != null) {
                intent.goal.let { goal ->
                        _state.update {
                            it.copy(
                                id = goal.id,
                                name = goal.name,
                                targetAmount = goal.targetAmount.toString(),
                                savedAmount = goal.savedAmount.toString(),
                                startDate = goal.startDate,
                                endDate = goal.endDate,
                                recurrence = goal.recurrence,
                                monthlyTarget = goal.monthlyTarget.toString()
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            startDate = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                }
            }
            is AddGoalIntent.UpdateName -> _state.update { it.copy(name = intent.name) }
            is AddGoalIntent.UpdateTargetAmount -> _state.update { it.copy(targetAmount = intent.amount) }
            is AddGoalIntent.UpdateSavedAmount -> _state.update { it.copy(savedAmount = intent.amount) }
            is AddGoalIntent.UpdateStartDate -> _state.update { it.copy(startDate = intent.date) }
            is AddGoalIntent.UpdateEndDate -> _state.update { it.copy(endDate = intent.date) }
            is AddGoalIntent.UpdateRecurrence -> _state.update { it.copy(recurrence = intent.recurrence) }
            is AddGoalIntent.UpdateMonthlyTarget -> _state.update { it.copy(monthlyTarget = intent.amount) }
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
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                recurrence = currentState.recurrence,
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
