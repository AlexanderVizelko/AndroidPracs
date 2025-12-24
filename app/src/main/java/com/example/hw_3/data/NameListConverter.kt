package com.example.hw_3.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Конвертер для преобразования List<String> в JSON для Room
class NameListConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromNameList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toNameList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}

