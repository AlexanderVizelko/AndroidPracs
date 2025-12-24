package com.example.hw_3.domain.repository

import com.example.hw_3.domain.model.NameDay

// Интерфейс репозитория в domain слое
interface NameDayRepository {
    suspend fun getNameDays(count: Int, lang: String = "sk"): Result<List<NameDay>>
    suspend fun getNameDayByDate(day: Int, month: Int, lang: String = "sk"): Result<NameDay>
}


