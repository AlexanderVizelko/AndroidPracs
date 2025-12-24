package com.example.hw_3.data.repository

import com.example.hw_3.data.remote.NameDayRemoteDataSource
import com.example.hw_3.domain.model.NameDay
import com.example.hw_3.domain.repository.NameDayRepository

// Реализация репозитория
class NameDayRepositoryImpl(
    private val remoteDataSource: NameDayRemoteDataSource
) : NameDayRepository {
    
    override suspend fun getNameDays(count: Int, lang: String): Result<List<NameDay>> {
        return remoteDataSource.getNameDays(count, lang)
    }
    
    override suspend fun getNameDayByDate(day: Int, month: Int, lang: String): Result<NameDay> {
        return remoteDataSource.getNameDayByDate(day, month, lang)
    }
}


