package com.example.hw_3.data

import com.google.gson.annotations.SerializedName

// Модель для API svatky.adresa.info
// Формат может быть разным, создаем гибкую модель
data class SvatkyResponse(
    @SerializedName("d") val day: Int?,
    @SerializedName("m") val month: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("names") val names: List<String>?,
    @SerializedName("svatky") val svatky: List<String>?
)

// Упрощенная модель для использования в приложении
data class NameDay(
    val day: Int,
    val month: Int,
    val names: List<String>,
    val displayDate: String = "${String.format("%02d", day)}.${String.format("%02d", month)}"
) {
    val displayNames: String
        get() = names.joinToString(", ")
}

