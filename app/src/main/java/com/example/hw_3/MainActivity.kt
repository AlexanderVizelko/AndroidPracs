package com.example.hw_3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.hw_3.cache.FilterCache
import com.example.hw_3.data.AppDatabase
import com.example.hw_3.repository.FavoritesRepository
import com.example.hw_3.ui.theme.Hw3Theme
import com.example.hw_3.viewmodel.FavoritesViewModel
import com.example.hw_3.viewmodel.QuoteViewModel
import com.example.hw_3.notifications.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Создаем канал уведомлений при запуске приложения
        NotificationHelper.createNotificationChannel(this)
        
        setContent {
            Hw3Theme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    var buttonsVisible by remember { mutableStateOf(true) }
    val quoteViewModel: QuoteViewModel = viewModel()
    
    // Инициализация базы данных и репозитория
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Запрашиваем разрешение на уведомления для Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Разрешение получено или отклонено
    }
    
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    val database = remember { AppDatabase.getDatabase(context) }
    val favoritesRepository = remember { FavoritesRepository(database.favoriteNameDayDao()) }
    
    // Инициализация FilterCache через DI
    val filterCache = remember { FilterCache() }
    
    // Создание ViewModelFactory для FavoritesViewModel
    val favoritesViewModel: FavoritesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return FavoritesViewModel(favoritesRepository) as T
            }
        }
    )

    Scaffold(
        bottomBar = {
            if (buttonsVisible) {
                BottomBar(
                    navController = navController,
                    state = buttonsVisible,
                    modifier = Modifier
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavigationGraph(
                navController = navController,
                onBottomBarVisibilityChanged = { isVisible ->
                    buttonsVisible = isVisible
                },
                quoteViewModel = quoteViewModel,
                favoritesViewModel = favoritesViewModel,
                filterCache = filterCache
            )
        }
    }
}