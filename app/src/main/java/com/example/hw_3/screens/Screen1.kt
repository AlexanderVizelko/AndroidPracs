package com.example.hw_3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.hw_3.Routes
import com.example.hw_3.cache.FilterCache
import com.example.hw_3.data.NameDay
import com.example.hw_3.preferences.PreferencesManager
import com.example.hw_3.viewmodel.FavoritesViewModel
import com.example.hw_3.viewmodel.QuoteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen1(
    navController: NavHostController,
    viewModel: QuoteViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    favoritesViewModel: FavoritesViewModel,
    filterCache: FilterCache
) {
    val quotes by viewModel.quotes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Загружаем сохраненные фильтры при первом запуске
    LaunchedEffect(Unit) {
        val savedFilters = withContext(Dispatchers.IO) {
            preferencesManager.getFilterPreferences()
        }
        viewModel.updateFilters(savedFilters)
        // Обновляем кэш фильтров
        filterCache.updateFilters(savedFilters)
        
        // Загружаем данные только если их нет
        if (quotes.isEmpty()) {
            viewModel.fetchQuotes(30) // Загружаем 30 именин
        }
    }
    
    // Отслеживаем изменения в кэше для обновления бейджа
    var hasActiveFilters by remember { mutableStateOf(filterCache.hasActiveFilters()) }
    
    // Ключ для отслеживания изменений в навигации
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    // Обновляем состояние бейджа при возврате на экран
    LaunchedEffect(navBackStackEntry?.id) {
        hasActiveFilters = filterCache.hasActiveFilters()
    }
    
    // Также обновляем при каждом recompose через SideEffect
    androidx.compose.runtime.SideEffect {
        val currentState = filterCache.hasActiveFilters()
        if (currentState != hasActiveFilters) {
            hasActiveFilters = currentState
        }
    }
    

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB),
                        Color(0xFF90CAF9)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Заголовок с градиентом
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1976D2)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "📅 Именины",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    // Кнопка фильтров с бейджем
                    Box {
                        IconButton(
                            onClick = {
                                // Обновляем состояние бейджа перед переходом
                                hasActiveFilters = filterCache.hasActiveFilters()
                                navController.navigate(Routes.FilterScreen.route)
                            },
                            modifier = Modifier
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Фильтры",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        // Бейдж, если есть активные фильтры
                        if (hasActiveFilters) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp),
                                containerColor = Color(0xFFFF1744)
                            ) {
                                Box(modifier = Modifier.size(8.dp))
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = Color(0xFF1976D2),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Загрузка именин...",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚠️",
                                    style = MaterialTheme.typography.displaySmall
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = error!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (error!!.contains("429") || error!!.contains("много запросов") || error!!.contains("секунд")) {
                                            viewModel.retryFetchQuotes(30000)
                                        } else {
                                            viewModel.fetchQuotes()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1976D2)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Попробовать снова")
                                }
                                if (error!!.contains("429") || error!!.contains("много запросов") || error!!.contains("секунд")) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "⏱ Подождите 30 секунд перед следующей попыткой",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (quotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📅",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Нет данных об именинах",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchQuotes() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Загрузить именины")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(quotes) { index, nameDay ->
                        NameDayItem(
                            nameDay = nameDay,
                            index = index,
                            favoritesViewModel = favoritesViewModel,
                            onNameDayClick = {
                                navController.navigate(Routes.QuoteDetail.createRoute(index))
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameDayItem(
    nameDay: NameDay,
    index: Int,
    favoritesViewModel: FavoritesViewModel,
    onNameDayClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Проверяем, является ли избранным
    LaunchedEffect(nameDay) {
        isFavorite = favoritesViewModel.isFavorite(nameDay)
    }
    // Градиенты для карточек
    val gradients = listOf(
        listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
        listOf(Color(0xFFF093FB), Color(0xFFF5576C)),
        listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)),
        listOf(Color(0xFF43E97B), Color(0xFF38F9D7)),
        listOf(Color(0xFFFA709A), Color(0xFFFEE140)),
        listOf(Color(0xFF30CFD0), Color(0xFF330867)),
        listOf(Color(0xFFA8EDEA), Color(0xFFFED6E3)),
        listOf(Color(0xFFD299C2), Color(0xFFFEF9D7))
    )
    val gradient = gradients[index % gradients.size]
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNameDayClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(gradient),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Дата в верхнем левом углу и сердечко
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = nameDay.displayDate,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = "#${index + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Кнопка избранного
                        IconButton(
                            onClick = {
                                favoritesViewModel.toggleFavorite(nameDay)
                                coroutineScope.launch {
                                    isFavorite = favoritesViewModel.isFavorite(nameDay)
                                }
                            },
                            modifier = Modifier.background(
                                Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                                tint = if (isFavorite) Color(0xFFFF1744) else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Имена
                Text(
                    text = nameDay.displayNames,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Подсказка
                Text(
                    text = "Нажмите для подробностей",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
