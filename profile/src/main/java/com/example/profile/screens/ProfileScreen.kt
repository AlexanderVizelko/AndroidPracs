package com.example.profile.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.profile.data.Profile
import com.example.profile.navigation.ProfileNavigation
import com.example.profile.preferences.ProfileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Composable
fun ProfileScreen(navigation: ProfileNavigation) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profileManager = remember { ProfileManager(context) }
    val profile by profileManager.profile.collectAsState(initial = Profile())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Аватар
        Box {
            if (profile.avatarUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse(profile.avatarUri)),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.fullName.takeIf { it.isNotEmpty() }?.firstOrNull()?.toString() ?: "?",
                            fontSize = 48.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ФИО
        Text(
            text = profile.fullName.ifEmpty { "Не указано" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Должность
        if (profile.position.isNotEmpty()) {
            Text(
                text = profile.position,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Кнопка редактирования
        Button(
            onClick = { navigation.navigateToEditProfile() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Редактировать профиль")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка резюме
        if (profile.resumeUrl.isNotEmpty()) {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        downloadAndOpenResume(context, profile.resumeUrl)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Резюме")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// Функция для скачивания и открытия резюме
suspend fun downloadAndOpenResume(context: android.content.Context, url: String) {
    try {
        withContext(Dispatchers.IO) {
            val urlObj = URL(url)
            val connection = urlObj.openConnection()
            connection.connect()
            
            val inputStream = connection.getInputStream()
            val fileName = url.substringAfterLast("/").takeIf { it.isNotEmpty() } 
                ?: "resume_${System.currentTimeMillis()}.pdf"
            
            val downloadDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir
            downloadDir.mkdirs() // Убеждаемся, что директория существует
            val file = File(downloadDir, fileName)
            
            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }
            
            withContext(Dispatchers.Main) {
                // Открываем файл
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, connection.contentType ?: "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "Не удалось открыть файл. Файл сохранен: ${file.absolutePath}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Ошибка при скачивании: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}


