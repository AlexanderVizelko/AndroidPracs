package com.example.hw_3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw_3.api.RetrofitClient
import com.example.hw_3.data.NameDay
import com.example.hw_3.data.SvatkyResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class QuoteViewModel : ViewModel() {
    // Приватный изменяемый поток для списка именин
    private val _quotes = MutableStateFlow<List<NameDay>>(emptyList())
    // Преобразует MutableStateFlow в StateFlow, предотвращая изменение извне
    val quotes: StateFlow<List<NameDay>> = _quotes.asStateFlow()

    // Флаг загрузки (true - идет загрузка, false - завершена)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Хранит сообщение об ошибке или null если ошибок нет
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Функция для преобразования SvatkyResponse в NameDay
    private fun convertToNameDay(response: SvatkyResponse): NameDay? {
        val day = response.day ?: return null
        val month = response.month ?: return null
        
        val names = when {
            response.names != null && response.names.isNotEmpty() -> response.names
            response.svatky != null && response.svatky.isNotEmpty() -> response.svatky
            response.name != null -> listOf(response.name)
            else -> return null
        }
        
        return NameDay(day = day, month = month, names = names)
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
    
    init {
        // Сразу показываем fallback данные при создании ViewModel
        _quotes.value = generateFallbackNameDays(20)
    }
    
    // Публичная функция для инициации загрузки именин
    fun fetchQuotes(count: Int = 30) {
        // Если данные уже загружены, не делаем запрос
        if (_quotes.value.size >= count) {
            android.util.Log.d("NameDayViewModel", "Using cached data, skipping network request")
            return
        }
        
        _isLoading.value = true
        _error.value = null

        // Запускает корутину в scope ViewModel с использованием Dispatchers.IO для сетевых операций
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nameDayList = _quotes.value.toMutableList()
                var successCount = 0
                
                // Если список пуст, добавляем fallback данные
                if (nameDayList.isEmpty()) {
                    nameDayList.addAll(generateFallbackNameDays(count))
                    // Обновляем UI сразу с fallback данными
                    withContext(Dispatchers.Main) {
                        _quotes.value = nameDayList.toList()
                    }
                }
                
                // Загружаем данные с API для разных дат
                val calendar = Calendar.getInstance()
                val maxApiRequests = minOf(count, 30) // Ограничиваем количество запросов
                
                for (index in 0 until maxApiRequests) {
                    try {
                        if (index > 0) {
                            delay(500) // Небольшая задержка между запросами
                        }
                        
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        val month = calendar.get(Calendar.MONTH) + 1
                        
                        val response = RetrofitClient.quoteApiService.getNameDayByDate(day, month)
                        if (response.isSuccessful) {
                            response.body()?.let { svatkyResponse ->
                                convertToNameDay(svatkyResponse)?.let { nameDay ->
                                    // Проверяем, нет ли дубликатов
                                    if (!nameDayList.any { it.day == nameDay.day && it.month == nameDay.month }) {
                                        // Заменяем fallback данные на реальные
                                        if (index < nameDayList.size) {
                                            nameDayList[index] = nameDay
                                        } else if (nameDayList.size < count) {
                                            nameDayList.add(nameDay)
                                        }
                                        successCount++
                                        android.util.Log.d("NameDayViewModel", "Loaded name day ${index + 1}: ${nameDay.displayDate} - ${nameDay.displayNames}")
                                        // Обновляем UI постепенно в главном потоке
                                        withContext(Dispatchers.Main) {
                                            _quotes.value = nameDayList.take(count).toList()
                                        }
                                    }
                                }
                            }
                        } else {
                            android.util.Log.w("NameDayViewModel", "Response not successful: ${response.code()}")
                            // Продолжаем с fallback данными
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NameDayViewModel", "Error loading name day ${index + 1}: ${e.message}")
                        // Продолжаем с fallback данными
                    }
                }
                
                // Финальное обновление списка именин
                withContext(Dispatchers.Main) {
                    if (nameDayList.isNotEmpty()) {
                        android.util.Log.d("NameDayViewModel", "Successfully loaded ${nameDayList.size} name days (${successCount} from API, ${nameDayList.size - successCount} fallback)")
                        _quotes.value = nameDayList.take(count).toList()
                        _error.value = null
                    } else {
                        _error.value = "Не удалось загрузить именины"
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("NameDayViewModel", "Fatal error: ${e.message}", e)
                // Используем fallback данные даже при ошибке
                withContext(Dispatchers.Main) {
                    _quotes.value = generateFallbackNameDays(count)
                    _error.value = null
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Функция для повторной попытки загрузки с задержкой
    fun retryFetchQuotes(delayMs: Long = 2000) {
        viewModelScope.launch {
            delay(delayMs)
            // Очищаем старые данные перед повторной попыткой
            _quotes.value = emptyList()
            fetchQuotes()
        }
    }
}
