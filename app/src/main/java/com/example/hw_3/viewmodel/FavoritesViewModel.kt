package com.example.hw_3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw_3.data.NameDay
import com.example.hw_3.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoritesRepository
) : ViewModel() {
    
    // Поток избранных именин - используем Flow напрямую
    val favorites = repository.getAllFavorites()
    
    // Флаг загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Сообщение об ошибке
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Проверить, является ли именины избранными
    suspend fun isFavorite(nameDay: NameDay): Boolean {
        return try {
            repository.isFavorite(nameDay)
        } catch (e: Exception) {
            android.util.Log.e("FavoritesViewModel", "Error checking favorite: ${e.message}", e)
            false
        }
    }
    
    // Добавить в избранное
    fun addToFavorites(nameDay: NameDay) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.addToFavorites(nameDay)
            } catch (e: Exception) {
                _error.value = "Ошибка при добавлении в избранное: ${e.message}"
                android.util.Log.e("FavoritesViewModel", "Error adding favorite: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Удалить из избранного
    fun removeFromFavorites(nameDay: NameDay) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.removeFromFavorites(nameDay)
            } catch (e: Exception) {
                _error.value = "Ошибка при удалении из избранного: ${e.message}"
                android.util.Log.e("FavoritesViewModel", "Error removing favorite: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Переключить избранное
    fun toggleFavorite(nameDay: NameDay) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.toggleFavorite(nameDay)
            } catch (e: Exception) {
                _error.value = "Ошибка при изменении избранного: ${e.message}"
                android.util.Log.e("FavoritesViewModel", "Error toggling favorite: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

