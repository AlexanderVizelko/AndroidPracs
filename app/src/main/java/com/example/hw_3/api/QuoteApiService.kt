package com.example.hw_3.api

import com.example.hw_3.data.SvatkyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface QuoteApiService {
    // API для получения именин
    // Формат: https://svatky.adresa.info/json?lang=sk
    @GET("json")
    suspend fun getNameDays(
        @Query("lang") lang: String = "sk"
    ): Response<List<SvatkyResponse>>
    
    // Альтернативный формат - получение именин для конкретной даты
    @GET("json")
    suspend fun getNameDayByDate(
        @Query("d") day: Int,
        @Query("m") month: Int,
        @Query("lang") lang: String = "sk"
    ): Response<SvatkyResponse>
}

