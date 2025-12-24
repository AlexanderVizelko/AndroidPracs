package com.example.hw_3.cache

import com.example.hw_3.data.FilterPreferences

/**
 * Класс-кэш для хранения информации о примененных фильтрах.
 * Используется для определения, нужно ли показывать бейдж на кнопке фильтров.
 */
class FilterCache {
    // Текущие примененные фильтры
    private var currentFilters: FilterPreferences = FilterPreferences()
    
    // Проверяет, применены ли какие-либо фильтры (не дефолтное состояние)
    fun hasActiveFilters(): Boolean {
        return currentFilters.selectedMonth != null ||
                currentFilters.selectedDay != null ||
                currentFilters.nameSearch.isNotEmpty()
    }
    
    // Получить текущие фильтры
    fun getCurrentFilters(): FilterPreferences = currentFilters
    
    // Обновить фильтры
    fun updateFilters(filters: FilterPreferences) {
        currentFilters = filters
    }
    
    // Сбросить фильтры (вернуть в дефолтное состояние)
    fun resetFilters() {
        currentFilters = FilterPreferences()
    }
    
    // Проверяет, отличаются ли новые фильтры от текущих
    fun isDifferentFrom(filters: FilterPreferences): Boolean {
        return currentFilters.selectedMonth != filters.selectedMonth ||
                currentFilters.selectedDay != filters.selectedDay ||
                currentFilters.nameSearch != filters.nameSearch
    }
}

