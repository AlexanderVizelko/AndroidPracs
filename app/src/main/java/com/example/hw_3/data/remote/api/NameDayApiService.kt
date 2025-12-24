package com.example.hw_3.data.remote.api

import com.example.hw_3.data.remote.dto.SvatkyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// API интерфейс для получения именин
interface NameDayApiService {
    // Получение именин для конкретной даты с query параметрами
    // API возвращает массив, даже для одной даты
    @GET("json")
    suspend fun getNameDayByDate(
        @Query("d") day: Int,
        @Query("m") month: Int,
        @Query("lang") lang: String = "sk"
    ): Response<List<SvatkyResponse>>
    
    // Получение списка именин (если API поддерживает)
    @GET("json")
    suspend fun getNameDays(
        @Query("lang") lang: String = "sk"
    ): Response<List<SvatkyResponse>>
}

