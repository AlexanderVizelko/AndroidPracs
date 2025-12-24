package com.example.hw_3.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // API для именин
    private const val BASE_URL = "https://svatky.adresa.info/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClientProvider.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val quoteApiService: QuoteApiService by lazy {
        retrofit.create(QuoteApiService::class.java)
    }
}