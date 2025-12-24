package com.example.hw_3.data.remote.dto

import com.example.hw_3.domain.model.NameDay
import com.google.gson.annotations.SerializedName

// DTO для API ответа
data class SvatkyResponse(
    @SerializedName("d") val day: Int?,
    @SerializedName("m") val month: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("names") val names: List<String>?,
    @SerializedName("svatky") val svatky: List<String>?
) {
    // Маппинг DTO в domain модель
    fun toDomainModel(): NameDay? {
        val dayValue = day ?: return null
        val monthValue = month ?: return null
        
        val namesList = when {
            names != null && names.isNotEmpty() -> names
            svatky != null && svatky.isNotEmpty() -> svatky
            name != null -> listOf(name)
            else -> return null
        }
        
        return NameDay(day = dayValue, month = monthValue, names = namesList)
    }
}


