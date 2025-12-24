package com.example.hw_3.repository

import com.example.hw_3.data.FavoriteNameDay
import com.example.hw_3.data.FavoriteNameDayDao
import com.example.hw_3.data.NameDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Repository для работы с избранным
class FavoritesRepository(private val favoriteDao: FavoriteNameDayDao) {
    
    // Получить все избранные именины как Flow<List<NameDay>>
    fun getAllFavorites(): Flow<List<NameDay>> {
        return favoriteDao.getAllFavorites().map { favorites ->
            favorites.map { it.toNameDay() }
        }
    }
    
    // Проверить, является ли именины избранными
    suspend fun isFavorite(nameDay: NameDay): Boolean {
        return favoriteDao.isFavorite(nameDay.day, nameDay.month)
    }
    
    // Добавить в избранное
    suspend fun addToFavorites(nameDay: NameDay): Long {
        val favorite = FavoriteNameDay.fromNameDay(nameDay)
        return favoriteDao.insertFavorite(favorite)
    }
    
    // Удалить из избранного
    suspend fun removeFromFavorites(nameDay: NameDay) {
        favoriteDao.deleteFavoriteByDate(nameDay.day, nameDay.month)
    }
    
    // Переключить избранное (добавить, если нет, удалить если есть)
    suspend fun toggleFavorite(nameDay: NameDay): Boolean {
        val isCurrentlyFavorite = isFavorite(nameDay)
        return if (isCurrentlyFavorite) {
            removeFromFavorites(nameDay)
            false
        } else {
            addToFavorites(nameDay) > 0
            true
        }
    }
}

