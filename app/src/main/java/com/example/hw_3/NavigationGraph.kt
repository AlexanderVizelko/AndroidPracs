package com.example.hw_3

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.hw_3.screens.Screen1


@Composable
fun NavigationGraph(
    navController: NavHostController,
    onBottomBarVisibilityChanged: (Boolean) -> Unit
) {
    NavHost(navController, startDestination = Routes.Screen1.route) {
        composable(Routes.Screen1.route) {
            onBottomBarVisibilityChanged(true)
            Screen1(navController = navController)
        }
        composable(Routes.Screen2.route) {
            onBottomBarVisibilityChanged(true)
            Screen2()
        }
        composable(Routes.Screen3.route) {
            onBottomBarVisibilityChanged(true)
            Screen3()
        }
        composable(
            route = Routes.QuoteDetail.route,
            arguments = listOf(navArgument("quoteIndex") {
                type = androidx.navigation.NavType.IntType
            })
        ) { backStackEntry ->
            onBottomBarVisibilityChanged(false)
            val quoteIndex = backStackEntry.arguments?.getInt("quoteIndex") ?: 0
            QuoteDetailScreen(
                quoteIndex = quoteIndex,
                navController = navController
            )
        }
    }
}