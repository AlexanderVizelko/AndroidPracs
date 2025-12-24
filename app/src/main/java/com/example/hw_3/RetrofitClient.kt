package com.example.hw_3.api

import com.example.hw_3.data.SvatkyResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // API для именин
    private const val BASE_URL = "https://svatky.adresa.info/"

    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(SvatkyResponse::class.java, SvatkyResponseDeserializer())
            .create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClientProvider.client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val quoteApiService: QuoteApiService by lazy {
        retrofit.create(QuoteApiService::class.java)
    }
}