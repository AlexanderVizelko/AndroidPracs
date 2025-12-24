package com.example.hw_3.di

import android.content.Context
import com.example.hw_3.data.remote.NameDayRemoteDataSource
import com.example.hw_3.data.remote.NetworkChecker
import com.example.hw_3.data.remote.RetrofitClient
import com.example.hw_3.data.repository.NameDayRepositoryImpl
import com.example.hw_3.domain.repository.NameDayRepository
import com.example.hw_3.domain.usecase.GetNameDaysUseCase
import com.example.hw_3.domain.usecase.GetNameDayByDateUseCase

// Простой модуль для dependency injection
object AppModule {
    
    private var context: Context? = null
    
    fun init(context: Context) {
        AppModule.context = context.applicationContext
    }
    
    private val nameDayApiService by lazy {
        RetrofitClient.nameDayApiService
    }
    
    private val networkChecker: NetworkChecker by lazy {
        NetworkChecker(context ?: throw IllegalStateException("AppModule not initialized. Call AppModule.init(context) first."))
    }
    
    private val remoteDataSource: NameDayRemoteDataSource by lazy {
        NameDayRemoteDataSource(nameDayApiService, networkChecker)
    }
    
    private val repository: NameDayRepository by lazy {
        NameDayRepositoryImpl(remoteDataSource)
    }
    
    val getNameDaysUseCase: GetNameDaysUseCase by lazy {
        GetNameDaysUseCase(repository)
    }
    
    val getNameDayByDateUseCase: GetNameDayByDateUseCase by lazy {
        GetNameDayByDateUseCase(repository)
    }
}

