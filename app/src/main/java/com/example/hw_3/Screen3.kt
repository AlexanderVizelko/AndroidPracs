package com.example.hw_3

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.hw_3.Routes
import com.example.profile.navigation.ProfileNavigation
import com.example.profile.screens.ProfileScreen

@Composable
fun Screen3(navController: NavController) {
    val navigation = object : ProfileNavigation {
        override fun navigateToEditProfile() {
            navController.navigate(Routes.EditProfile.route)
        }
    }
    ProfileScreen(navigation = navigation)
}