package com.example.hw_3.domain.usecase

import com.example.hw_3.domain.model.NameDay
import com.example.hw_3.domain.repository.NameDayRepository

// Use case для получения именин по конкретной дате
class GetNameDayByDateUseCase(
    private val repository: NameDayRepository
) {
    suspend operator fun invoke(
        day: Int,
        month: Int,
        lang: String = "sk"
    ): Result<NameDay> {
        return repository.getNameDayByDate(day, month, lang)
    }
}


