package com.example.hw_3

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.hw_3.cache.FilterCache
import com.example.hw_3.preferences.PreferencesManager
import com.example.profile.screens.EditProfileScreen
import com.example.hw_3.screens.FavoritesScreen
import com.example.hw_3.screens.FilterScreen
import com.example.hw_3.screens.Screen1
import com.example.hw_3.viewmodel.FavoritesViewModel
import com.example.hw_3.viewmodel.QuoteViewModel
import com.example.hw_3.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun NavigationGraph(
    navController: NavHostController,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
    quoteViewModel: QuoteViewModel,
    favoritesViewModel: FavoritesViewModel,
    filterCache: FilterCache
) {
    NavHost(navController, startDestination = Routes.Screen1.route) {
        composable(Routes.Screen1.route) {
            onBottomBarVisibilityChanged(true)
            Screen1(
                navController = navController,
                viewModel = quoteViewModel,
                favoritesViewModel = favoritesViewModel,
                filterCache = filterCache
            )
        }
        composable(Routes.Screen2.route) {
            onBottomBarVisibilityChanged(true)
            Screen2(
                navController = navController,
                favoritesViewModel = favoritesViewModel
            )
        }
        composable(Routes.Screen3.route) {
            onBottomBarVisibilityChanged(true)
            Screen3(navController = navController)
        }
        composable(Routes.EditProfile.route) {
            onBottomBarVisibilityChanged(false)
            EditProfileScreen(
                navController = navController,
                mainActivityClass = MainActivity::class.java
            )
        }
        composable(Routes.FilterScreen.route) {
            onBottomBarVisibilityChanged(false)
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val coroutineScope = rememberCoroutineScope()
            
            // Получаем текущие фильтры безопасно
            val initialFilters = remember { quoteViewModel.getCurrentFilters() }
            
            FilterScreen(
                navController = navController,
                initialFilters = initialFilters,
                filterCache = filterCache,
                onApplyFilters = { filters ->
                    try {
                        // Обновляем кэш фильтров ПЕРВЫМ (до обновления ViewModel)
                        filterCache.updateFilters(filters)
                        
                        // Применяем фильтры сразу (синхронно)
                        quoteViewModel.updateFilters(filters)
                        
                        // Сохраняем фильтры в DataStore асинхронно
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                preferencesManager.saveFilterPreferences(filters)
                            } catch (e: Exception) {
                                Log.e("NavigationGraph", "Error saving filters: ${e.message}", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("NavigationGraph", "Error applying filters: ${e.message}", e)
                    }
                }
            )
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
                navController = navController,
                viewModel = quoteViewModel,
                favoritesViewModel = favoritesViewModel
            )
        }
    }
}