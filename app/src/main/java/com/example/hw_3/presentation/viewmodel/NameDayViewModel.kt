package com.example.hw_3.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw_3.domain.usecase.GetNameDaysUseCase
import com.example.hw_3.presentation.ui.state.NameDayUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NameDayViewModel(
    private val getNameDaysUseCase: GetNameDaysUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<NameDayUiState>(NameDayUiState.Empty)
    val uiState: StateFlow<NameDayUiState> = _uiState.asStateFlow()
    
    private var isLoading = false
    
    fun fetchNameDays(count: Int = 30, lang: String = "sk") {
        // Предотвращаем множественные одновременные запросы
        if (isLoading) {
            android.util.Log.d("NameDayViewModel", "Request already in progress, skipping")
            return
        }
        
        viewModelScope.launch {
            try {
                isLoading = true
                _uiState.value = NameDayUiState.Loading
                android.util.Log.d("NameDayViewModel", "Starting to fetch name days, count=$count")
                
                val result = withContext(Dispatchers.IO) {
                    getNameDaysUseCase(count, lang)
                }
                
                result.onSuccess { nameDays ->
                    android.util.Log.d("NameDayViewModel", "Successfully loaded ${nameDays.size} name days")
                    if (nameDays.isEmpty()) {
                        _uiState.value = NameDayUiState.Empty
                    } else {
                        _uiState.value = NameDayUiState.Success(nameDays)
                    }
                }.onFailure { exception ->
                    android.util.Log.e("NameDayViewModel", "Error loading name days: ${exception.message}", exception)
                    _uiState.value = NameDayUiState.Error(
                        exception.message ?: "Произошла неизвестная ошибка"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("NameDayViewModel", "Unexpected error: ${e.message}", e)
                _uiState.value = NameDayUiState.Error(
                    e.message ?: "Произошла неизвестная ошибка"
                )
            } finally {
                isLoading = false
            }
        }
    }
    
    fun retry(count: Int = 30, lang: String = "sk") {
        fetchNameDays(count, lang)
    }
}

