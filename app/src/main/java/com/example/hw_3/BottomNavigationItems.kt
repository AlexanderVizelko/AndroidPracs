package com.example.hw_3

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavigationItems (
    val route: String,
    val title: String? = null,
    val icon: ImageVector?=null
) {
    object Screen1 : BottomNavigationItems(
        route = "screen1",
        title = "Именины",
        icon = Icons.Filled.CalendarToday
    )
    object Screen2 : BottomNavigationItems(
        route = "screen2",
        title = "Добавить",
        icon = Icons.Filled.Add
    )
    object Screen3 : BottomNavigationItems(
        route = "screen3",
        title = "Профиль",
        icon = Icons.Filled.Person
    )
}
