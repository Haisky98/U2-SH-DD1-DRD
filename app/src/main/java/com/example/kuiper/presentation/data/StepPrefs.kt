package com.example.kuiper.presentation.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "step_settings")

class StepPrefs(private val context: Context) {
    companion object {
        val DAILY_GOAL_KEY = intPreferencesKey("daily_goal")
        val CURRENT_STEPS_KEY = intPreferencesKey("current_steps")
    }

    val dailyGoal: Flow<Int?> = context.dataStore.data.map { it[DAILY_GOAL_KEY] }
    val currentSteps: Flow<Int> = context.dataStore.data.map { it[CURRENT_STEPS_KEY] ?: 0 }

    suspend fun saveGoal(goal: Int) {
        context.dataStore.edit { it[DAILY_GOAL_KEY] = goal }
    }

    suspend fun saveSteps(steps: Int) {
        context.dataStore.edit { it[CURRENT_STEPS_KEY] = steps }
    }
}