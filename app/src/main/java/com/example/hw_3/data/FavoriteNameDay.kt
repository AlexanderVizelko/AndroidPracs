package com.example.hw_3.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

// Entity для избранных именин в базе данных Room
@Entity(tableName = "favorite_name_days")
@TypeConverters(NameListConverter::class)
data class FavoriteNameDay(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val day: Int,
    val month: Int,
    val names: List<String>,
    val displayDate: String
) {
    // Преобразование в NameDay для использования в UI
    fun toNameDay(): NameDay {
        return NameDay(
            day = day,
            month = month,
            names = names
        )
    }
    
    companion object {
        // Преобразование NameDay в FavoriteNameDay
        fun fromNameDay(nameDay: NameDay): FavoriteNameDay {
            return FavoriteNameDay(
                day = nameDay.day,
                month = nameDay.month,
                names = nameDay.names,
                displayDate = nameDay.displayDate
            )
        }
    }
}


