package com.example.hw_3.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.hw_3.data.FilterPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "filter_preferences")

class PreferencesManager(private val context: Context) {
    companion object {
        private val MONTH_KEY = intPreferencesKey("selected_month")
        private val DAY_KEY = intPreferencesKey("selected_day")
        private val NAME_SEARCH_KEY = stringPreferencesKey("name_search")
    }

    val filterPreferences: Flow<FilterPreferences> = context.dataStore.data.map { preferences ->
        FilterPreferences(
            selectedMonth = preferences[MONTH_KEY],
            selectedDay = preferences[DAY_KEY],
            nameSearch = preferences[NAME_SEARCH_KEY] ?: ""
        )
    }

    suspend fun saveFilterPreferences(filterPreferences: FilterPreferences) {
        context.dataStore.edit { preferences ->
            if (filterPreferences.selectedMonth != null) {
                preferences[MONTH_KEY] = filterPreferences.selectedMonth!!
            } else {
                preferences.remove(MONTH_KEY)
            }
            
            if (filterPreferences.selectedDay != null) {
                preferences[DAY_KEY] = filterPreferences.selectedDay!!
            } else {
                preferences.remove(DAY_KEY)
            }
            
            if (filterPreferences.nameSearch.isNotEmpty()) {
                preferences[NAME_SEARCH_KEY] = filterPreferences.nameSearch
            } else {
                preferences.remove(NAME_SEARCH_KEY)
            }
        }
    }
    
    suspend fun getFilterPreferences(): FilterPreferences {
        val preferences = context.dataStore.data.first()
        return FilterPreferences(
            selectedMonth = preferences[MONTH_KEY],
            selectedDay = preferences[DAY_KEY],
            nameSearch = preferences[NAME_SEARCH_KEY] ?: ""
        )
    }
}
