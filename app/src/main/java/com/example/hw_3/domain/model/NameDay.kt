package com.example.hw_3.domain.model

// Domain модель - чистая бизнес-логика без зависимостей от фреймворков
data class NameDay(
    val day: Int,
    val month: Int,
    val names: List<String>
) {
    val displayDate: String
        get() = "${String.format("%02d", day)}.${String.format("%02d", month)}"
    
    val displayNames: String
        get() = names.joinToString(", ")
}


