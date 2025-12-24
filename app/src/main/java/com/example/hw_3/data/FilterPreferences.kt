package com.example.hw_3.data

// Модель для настроек фильтрации именин
data class FilterPreferences(
    val selectedMonth: Int? = null, // null означает "все месяцы", 1-12 для конкретного месяца
    val selectedDay: Int? = null,   // null означает "все дни", 1-31 для конкретного дня
    val nameSearch: String = ""     // Пустая строка означает "все имена"
)

