package com.example.hw_3.domain.usecase

import com.example.hw_3.domain.model.NameDay
import com.example.hw_3.domain.repository.NameDayRepository

// Use case для получения списка именин
class GetNameDaysUseCase(
    private val repository: NameDayRepository
) {
    suspend operator fun invoke(
        count: Int = 30,
        lang: String = "sk"
    ): Result<List<NameDay>> {
        return repository.getNameDays(count, lang)
    }
}


