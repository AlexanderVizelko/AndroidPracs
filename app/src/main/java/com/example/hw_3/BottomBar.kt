package com.example.hw_3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(
    //контроллер навигации для перехода между экранами
    navController: NavHostController,
    state: Boolean,
    modifier: Modifier = Modifier
) {
    val screens = listOf(
        BottomNavigationItems.Screen1,
        BottomNavigationItems.Screen2,
        BottomNavigationItems.Screen3
    )

    //Создает контейнер навигационной панели с современным дизайном
    NavigationBar(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1976D2),
                        Color(0xFF1565C0)
                    )
                )
            ),
        containerColor = Color.Transparent,
    ) {
        //получает текущую запись в стеке навигации как состояние
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        screens.forEach { screen ->
            //отдельный элемент навигационной панели
            NavigationBarItem(
                label = {
                    Text(
                        text = screen.title!!,
                        fontWeight = if (currentRoute == screen.route) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                icon = {
                    Icon(
                        imageVector = screen.icon!!,
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                selected = currentRoute == screen.route,
                onClick = {
                    //переход на указанный маршрут
                    navController.navigate(screen.route) {
                        //очищает стек до стартового destination
                        popUpTo(navController.graph.findStartDestination().id) {
                            //сохраняет состояние очищаемых экранов
                            saveState = true
                        }
                        //предотвращает множественные копии одного экрана
                        launchSingleTop = true
                        //восстанавливает сохраненное состояние
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    unselectedTextColor = Color.White.copy(alpha = 0.7f),
                    selectedTextColor = Color.White, // Белый текст
                    selectedIconColor = Color(0xFF1976D2), // Синяя иконка
                    unselectedIconColor = Color.White.copy(alpha = 0.7f),
                    indicatorColor = Color.White // Белый фон выделения
                ),
            )
        }
    }
}
