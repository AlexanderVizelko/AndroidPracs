package com.example.hw_3.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// DAO для работы с избранными именинами
@Dao
interface FavoriteNameDayDao {
    // Получить все избранные именины
    @Query("SELECT * FROM favorite_name_days ORDER BY month, day ASC")
    fun getAllFavorites(): Flow<List<FavoriteNameDay>>
    
    // Проверить, является ли именины избранными по дню и месяцу
    @Query("SELECT * FROM favorite_name_days WHERE day = :day AND month = :month LIMIT 1")
    suspend fun getFavoriteByDate(day: Int, month: Int): FavoriteNameDay?
    
    // Проверить, существует ли избранное по дню и месяцу (для проверки)
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_name_days WHERE day = :day AND month = :month)")
    suspend fun isFavorite(day: Int, month: Int): Boolean
    
    // Добавить в избранное
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteNameDay): Long
    
    // Удалить из избранного
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteNameDay)
    
    // Удалить из избранного по дню и месяцу
    @Query("DELETE FROM favorite_name_days WHERE day = :day AND month = :month")
    suspend fun deleteFavoriteByDate(day: Int, month: Int)
    
    // Удалить все избранное
    @Query("DELETE FROM favorite_name_days")
    suspend fun deleteAllFavorites()
}


