package com.example.hw_3

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.hw_3.screens.FavoritesScreen
import com.example.hw_3.viewmodel.FavoritesViewModel

@Composable
fun Screen2(
    navController: NavHostController,
    favoritesViewModel: FavoritesViewModel
) {
    FavoritesScreen(
        navController = navController,
        viewModel = favoritesViewModel
    )
}