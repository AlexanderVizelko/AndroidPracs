package com.example.hw_3.data.remote

import com.example.hw_3.data.remote.api.NameDayApiService
import com.example.hw_3.domain.model.NameDay
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Calendar

// Remote data source - работа с API
class NameDayRemoteDataSource(
    private val apiService: NameDayApiService,
    private val networkChecker: com.example.hw_3.data.remote.NetworkChecker
) {
    suspend fun getNameDays(count: Int, lang: String = "sk"): Result<List<NameDay>> {
        // Проверяем наличие интернета перед запросами
        val hasInternetAtStart = networkChecker.isInternetAvailable()
        if (!hasInternetAtStart) {
            android.util.Log.w("NameDayRemoteDataSource", "No internet connection available at start")
            return Result.failure(Exception("Нет подключения к интернету. Пожалуйста, подключитесь к интернету и попробуйте снова."))
        }
        
        return try {
            android.util.Log.d("NameDayRemoteDataSource", "Starting to fetch $count name days")
            val nameDays = mutableListOf<NameDay>()
            val calendar = Calendar.getInstance()
            // Уменьшаем количество запросов, так как API медленный
            val maxRequests = minOf(count, 3) // Ограничиваем до 3 запросов
            
            android.util.Log.d("NameDayRemoteDataSource", "Will make $maxRequests requests")
            
            var successCount = 0
            for (i in 0 until maxRequests) {
                // Проверяем интернет перед каждым запросом
                if (!networkChecker.isInternetAvailable()) {
                    android.util.Log.w("NameDayRemoteDataSource", "Internet connection lost during requests")
                    return Result.failure(Exception("Нет подключения к интернету. Пожалуйста, подключитесь к интернету и попробуйте снова."))
                }
                
                try {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    val month = calendar.get(Calendar.MONTH) + 1
                    
                    android.util.Log.d("NameDayRemoteDataSource", "Requesting day=$day, month=$month (request ${i + 1}/$maxRequests)")
                    
                    // Используем withTimeout для каждого запроса
                    val response = kotlinx.coroutines.withTimeoutOrNull(15000) { // 15 секунд на запрос
                        apiService.getNameDayByDate(day, month, lang)
                    }
                    
                    if (response != null) {
                        android.util.Log.d("NameDayRemoteDataSource", "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
                        
                        if (response.isSuccessful) {
                            // API возвращает массив, берем первый элемент
                            val responseList = response.body()
                            if (responseList != null && responseList.isNotEmpty()) {
                                responseList.firstOrNull()?.toDomainModel()?.let { nameDay ->
                                    // Проверяем на дубликаты
                                    if (!nameDays.any { it.day == nameDay.day && it.month == nameDay.month }) {
                                        nameDays.add(nameDay)
                                        successCount++
                                        android.util.Log.d("NameDayRemoteDataSource", "Added name day: ${nameDay.displayDate} - ${nameDay.displayNames}")
                                    }
                                } ?: android.util.Log.w("NameDayRemoteDataSource", "Cannot convert response to domain model")
                            } else {
                                android.util.Log.w("NameDayRemoteDataSource", "Response body is empty or null")
                            }
                        } else {
                            android.util.Log.w("NameDayRemoteDataSource", "Response not successful: code=${response.code()}, message=${response.message()}")
                        }
                    } else {
                        android.util.Log.w("NameDayRemoteDataSource", "Request timed out for day=$day, month=$month")
                    }
                    
                    // Увеличиваем задержку между запросами, чтобы не перегружать медленный API
                    if (i < maxRequests - 1) {
                        delay(1000) // Увеличиваем задержку до 1 секунды
                    }
                } catch (e: java.net.SocketTimeoutException) {
                    // Специальная обработка таймаутов
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    val month = calendar.get(Calendar.MONTH) + 1
                    android.util.Log.w("NameDayRemoteDataSource", "Timeout loading date (day=$day, month=$month): ${e.message}")
                    // Продолжаем с другими датами
                } catch (e: Exception) {
                    // Продолжаем с другими датами при ошибке
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    val month = calendar.get(Calendar.MONTH) + 1
                    android.util.Log.e("NameDayRemoteDataSource", "Error loading date (day=$day, month=$month): ${e.message}", e)
                }
            }
            
            android.util.Log.d("NameDayRemoteDataSource", "Successfully loaded $successCount out of $maxRequests requests")
            
            android.util.Log.d("NameDayRemoteDataSource", "Loaded ${nameDays.size} name days total")
            
            // Проверяем интернет перед возвратом результата
            val hasInternet = networkChecker.isInternetAvailable()
            
            if (nameDays.isNotEmpty()) {
                // Если есть данные с API - возвращаем их
                Result.success(nameDays.take(count))
            } else {
                // Если данных нет, проверяем интернет
                if (!hasInternet) {
                    // Интернета нет - всегда возвращаем ошибку
                    android.util.Log.w("NameDayRemoteDataSource", "No name days loaded and no internet connection")
                    Result.failure(Exception("Нет подключения к интернету. Пожалуйста, подключитесь к интернету и попробуйте снова."))
                } else {
                    // Интернет есть, но API не отвечает - возвращаем ошибку (без fallback данных)
                    android.util.Log.w("NameDayRemoteDataSource", "No name days loaded from API, but internet is available")
                    Result.failure(Exception("Не удалось загрузить именины. API может быть недоступен или слишком медленный. Проверьте подключение к интернету."))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NameDayRemoteDataSource", "Fatal error: ${e.message}", e)
            // Проверяем интернет при ошибке - если интернета нет, всегда возвращаем ошибку об отсутствии интернета
            val hasInternet = networkChecker.isInternetAvailable()
            if (!hasInternet) {
                android.util.Log.w("NameDayRemoteDataSource", "No internet connection during error handling")
                Result.failure(Exception("Нет подключения к интернету. Пожалуйста, подключитесь к интернету и попробуйте снова."))
            } else {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getNameDayByDate(day: Int, month: Int, lang: String = "sk"): Result<NameDay> {
        // Проверяем наличие интернета перед запросом
        if (!networkChecker.isInternetAvailable()) {
            android.util.Log.w("NameDayRemoteDataSource", "No internet connection available")
            return Result.failure(Exception("Нет подключения к интернету. Пожалуйста, подключитесь к интернету и попробуйте снова."))
        }
        
        return try {
            val response = apiService.getNameDayByDate(day, month, lang)
            if (response.isSuccessful) {
                // API возвращает массив, берем первый элемент
                val responseList = response.body()
                if (responseList != null && responseList.isNotEmpty()) {
                    val nameDay = responseList.firstOrNull()?.toDomainModel()
                    if (nameDay != null) {
                        Result.success(nameDay)
                    } else {
                        Result.failure(Exception("Данные не найдены"))
                    }
                } else {
                    Result.failure(Exception("Данные не найдены"))
                }
            } else {
                Result.failure(Exception("Ошибка API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Генерация fallback данных - именины на ближайшие дни
    private fun generateFallbackNameDays(count: Int): List<NameDay> {
        val calendar = Calendar.getInstance()
        val nameDays = mutableListOf<NameDay>()
        
        // Популярные имена для fallback
        val popularNames = listOf(
            listOf("Ján", "Jana"),
            listOf("Mária", "Marek"),
            listOf("Peter", "Pavol"),
            listOf("Jozef", "Jozefína"),
            listOf("Anna", "Anička"),
            listOf("Michal", "Michaela"),
            listOf("Martin", "Martina"),
            listOf("Tomáš", "Tomáška"),
            listOf("Lukáš", "Lucia"),
            listOf("Jakub", "Jakubka"),
            listOf("Filip", "Filipa"),
            listOf("Matúš", "Matilda"),
            listOf("Samuel", "Samantha"),
            listOf("Daniel", "Daniela"),
            listOf("Adam", "Adriana"),
            listOf("Erik", "Erika"),
            listOf("Patrik", "Patrícia"),
            listOf("Dominik", "Dominika"),
            listOf("Oliver", "Olivia"),
            listOf("Noah", "Noemi")
        )
        
        for (i in 0 until count) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val names = popularNames[i % popularNames.size]
            nameDays.add(NameDay(day = day, month = month, names = names))
        }
        
        return nameDays
    }
}

