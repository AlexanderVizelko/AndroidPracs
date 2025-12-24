package com.example.hw_3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw_3.api.RetrofitClient
import com.example.hw_3.data.FilterPreferences
import com.example.hw_3.data.NameDay
import com.example.hw_3.data.SvatkyResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class QuoteViewModel : ViewModel() {
    // Приватный изменяемый поток для всех именин (без фильтрации)
    private val _allQuotes = MutableStateFlow<List<NameDay>>(emptyList())
    
    // Текущие фильтры
    private val _filterPreferences = MutableStateFlow<FilterPreferences>(FilterPreferences())
    
    // Приватный изменяемый поток для отфильтрованного списка именин
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
    
    // Функция для применения фильтров к списку именин
    private fun applyFilters(quotes: List<NameDay>, filters: FilterPreferences): List<NameDay> {
        if (quotes.isEmpty()) return emptyList()
        
        // Если все фильтры пустые (дефолтное состояние), возвращаем все данные
        if (filters.selectedMonth == null && filters.selectedDay == null && filters.nameSearch.isEmpty()) {
            return quotes
        }
        
        return quotes.filter { nameDay ->
            // Фильтр по месяцу
            val monthMatches = filters.selectedMonth == null || nameDay.month == filters.selectedMonth
            
            // Фильтр по дню
            val dayMatches = filters.selectedDay == null || nameDay.day == filters.selectedDay
            
            // Фильтр по имени (поиск)
            val nameMatches = filters.nameSearch.isEmpty() || 
                nameDay.names.any { name -> 
                    name.contains(filters.nameSearch.trim(), ignoreCase = true) 
                }
            
            monthMatches && dayMatches && nameMatches
        }
    }
    
    // Обновление фильтров и применение их к текущему списку
    fun updateFilters(filters: FilterPreferences) {
        try {
            _filterPreferences.value = filters
            val allQuotes = _allQuotes.value
            val filtered = if (allQuotes.isNotEmpty()) {
                applyFilters(allQuotes, filters)
            } else {
                emptyList()
            }
            _quotes.value = filtered
            android.util.Log.d("QuoteViewModel", "Filters applied: month=${filters.selectedMonth}, day=${filters.selectedDay}, name=${filters.nameSearch}, result count=${filtered.size}")
        } catch (e: Exception) {
            android.util.Log.e("QuoteViewModel", "Error updating filters: ${e.message}", e)
            // В случае ошибки оставляем текущие данные
        }
    }
    
    init {
        // Сразу показываем fallback данные при создании ViewModel
        val fallbackData = generateFallbackNameDays(20)
        _allQuotes.value = fallbackData
        _quotes.value = fallbackData
    }
    
    // Публичная функция для инициации загрузки именин
    fun fetchQuotes(count: Int = 30, forceReload: Boolean = false) {
        // Если данные уже загружены, не делаем запрос (если не принудительная перезагрузка)
        if (!forceReload && _allQuotes.value.size >= count) {
            android.util.Log.d("NameDayViewModel", "Using cached data, skipping network request")
            // Применяем текущие фильтры к кэшированным данным
            _quotes.value = applyFilters(_allQuotes.value, _filterPreferences.value)
            return
        }
        
        _isLoading.value = true
        _error.value = null

        // Запускает корутину в scope ViewModel с использованием Dispatchers.IO для сетевых операций
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nameDayList = mutableListOf<NameDay>()
                var successCount = 0
                
                // Загружаем данные с API для разных дат параллельно
                val maxApiRequests = minOf(count, 20) // Ограничиваем количество запросов до 20
                
                // Загружаем параллельно для ускорения
                val requests = (0 until maxApiRequests).map { index ->
                    async {
                        try {
                            val tempCalendar = Calendar.getInstance()
                            tempCalendar.add(Calendar.DAY_OF_MONTH, index + 1)
                            val day = tempCalendar.get(Calendar.DAY_OF_MONTH)
                            val month = tempCalendar.get(Calendar.MONTH) + 1
                            
                            val response = RetrofitClient.quoteApiService.getNameDayByDate(day, month)
                            if (response.isSuccessful) {
                                response.body()?.let { svatkyResponse ->
                                    convertToNameDay(svatkyResponse)
                                }
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("NameDayViewModel", "Error loading name day ${index + 1}: ${e.message}")
                            null
                        }
                    }
                }
                
                // Ждем результаты всех запросов
                val results = requests.mapNotNull { it.await() }
                
                // Добавляем успешно загруженные данные
                results.forEach { nameDay ->
                    if (!nameDayList.any { it.day == nameDay.day && it.month == nameDay.month }) {
                        nameDayList.add(nameDay)
                        successCount++
                    }
                }
                
                // Если данных недостаточно, добавляем fallback
                if (nameDayList.size < count) {
                    val fallbackData = generateFallbackNameDays(count - nameDayList.size)
                    fallbackData.forEach { fallbackDay ->
                        if (!nameDayList.any { it.day == fallbackDay.day && it.month == fallbackDay.month }) {
                            nameDayList.add(fallbackDay)
                        }
                    }
                }
                
                // Финальное обновление списка именин
                withContext(Dispatchers.Main) {
                    val finalList = nameDayList.take(count).toList()
                    android.util.Log.d("NameDayViewModel", "Successfully loaded ${finalList.size} name days (${successCount} from API, ${finalList.size - successCount} fallback)")
                    _allQuotes.value = finalList
                    // Применяем текущие фильтры
                    val filtered = applyFilters(finalList, _filterPreferences.value)
                    _quotes.value = filtered
                    android.util.Log.d("NameDayViewModel", "After filtering: ${filtered.size} items")
                    _error.value = null
                }
            } catch (e: Exception) {
                android.util.Log.e("NameDayViewModel", "Fatal error: ${e.message}", e)
                // Используем fallback данные даже при ошибке
                withContext(Dispatchers.Main) {
                    val fallbackData = generateFallbackNameDays(count)
                    _allQuotes.value = fallbackData
                    _quotes.value = applyFilters(fallbackData, _filterPreferences.value)
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
            _allQuotes.value = emptyList()
            _quotes.value = emptyList()
            fetchQuotes(forceReload = true)
        }
    }
    
    // Получить текущие фильтры
    fun getCurrentFilters(): FilterPreferences = _filterPreferences.value
}
