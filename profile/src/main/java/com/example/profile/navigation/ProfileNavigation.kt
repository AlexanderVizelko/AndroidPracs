package com.example.profile.navigation

/**
 * Интерфейс для навигации в модуле профиля
 * Позволяет избежать прямой зависимости от Routes в app модуле
 */
interface ProfileNavigation {
    /**
     * Навигация на экран редактирования профиля
     */
    fun navigateToEditProfile()
}


