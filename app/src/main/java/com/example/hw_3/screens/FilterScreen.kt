package com.example.hw_3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.hw_3.cache.FilterCache
import com.example.hw_3.data.FilterPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    navController: NavHostController,
    initialFilters: FilterPreferences,
    filterCache: FilterCache,
    onApplyFilters: (FilterPreferences) -> Unit
) {
    var selectedMonth by remember { mutableStateOf(initialFilters.selectedMonth ?: 0) }
    var selectedDay by remember { mutableStateOf(initialFilters.selectedDay?.toString() ?: "") }
    var nameSearch by remember { mutableStateOf(initialFilters.nameSearch) }
    val coroutineScope = rememberCoroutineScope()

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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Заголовок
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
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
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Фильтры именин",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Фильтр по месяцу
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Месяц",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Выберите месяц для фильтрации (оставьте \"Все\" для отображения всех месяцев)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val months = listOf(
                        "Все" to 0,
                        "Январь" to 1,
                        "Февраль" to 2,
                        "Март" to 3,
                        "Апрель" to 4,
                        "Май" to 5,
                        "Июнь" to 6,
                        "Июль" to 7,
                        "Август" to 8,
                        "Сентябрь" to 9,
                        "Октябрь" to 10,
                        "Ноябрь" to 11,
                        "Декабрь" to 12
                    )
                    
                    months.forEach { (monthName, monthValue) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMonth == monthValue,
                                onClick = { selectedMonth = monthValue }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = monthName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Фильтр по дню
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "День",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Введите день месяца (1-31) или оставьте пустым для всех дней",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = selectedDay,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() } && newValue.toIntOrNull()?.let { it in 1..31 } == true) {
                                selectedDay = newValue
                            }
                        },
                        label = { Text("День месяца (1-31)") },
                        placeholder = { Text("Все дни") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Поиск по имени
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Поиск по имени",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Введите имя для поиска (оставьте пустым для всех имен)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = nameSearch,
                        onValueChange = { nameSearch = it },
                        label = { Text("Имя") },
                        placeholder = { Text("Введите имя для поиска") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Кнопка "Готово"
            Button(
                onClick = {
                    try {
                        val filters = FilterPreferences(
                            selectedMonth = if (selectedMonth == 0) null else selectedMonth,
                            selectedDay = selectedDay.toIntOrNull(),
                            nameSearch = nameSearch.trim()
                        )
                        // Применяем фильтры
                        onApplyFilters(filters)
                        // Возвращаемся назад с небольшой задержкой для завершения операций
                        coroutineScope.launch {
                            delay(50)
                            try {
                                navController.popBackStack()
                            } catch (e: Exception) {
                                android.util.Log.e("FilterScreen", "Error navigating back: ${e.message}", e)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("FilterScreen", "Error applying filters: ${e.message}", e)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {
                Text(
                    text = "Готово",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
