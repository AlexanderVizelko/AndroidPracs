package com.example.hw_3.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.hw_3.data.Profile
import com.example.hw_3.preferences.ProfileManager
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val profileManager = remember { ProfileManager(context) }
    val scope = rememberCoroutineScope()
    
    val profile by profileManager.profile.collectAsState(initial = Profile())
    
    var fullName by remember { mutableStateOf("") }
    var resumeUrl by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<String?>(null) }
    
    // Синхронизируем состояние с профилем при первой загрузке
    LaunchedEffect(Unit) {
        fullName = profile.fullName
        resumeUrl = profile.resumeUrl
        position = profile.position
        avatarUri = profile.avatarUri
    }
    
    
    // Выбор изображения из галереи
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            avatarUri = it.toString()
        }
    }
    
    // Съемка фото
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Фото успешно снято, URI уже установлен
        }
    }
    
    // Временный URI для фото с камеры
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Диалог выбора источника фото
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Флаг для отслеживания, какой источник был выбран
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    // Запрос разрешений с автоматическим продолжением после получения
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && pendingAction != null) {
            pendingAction?.invoke()
            pendingAction = null
        }
    }
    
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && pendingAction != null) {
            pendingAction?.invoke()
            pendingAction = null
        }
    }
    
    fun requestImageFromGallery() {
        val openGallery: () -> Unit = {
            galleryLauncher.launch("image/*")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - разрешение на чтение медиа
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                pendingAction = openGallery
                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Старые версии Android
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                pendingAction = openGallery
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    
    fun requestImageFromCamera() {
        val openCamera: () -> Unit = {
            // Создаем временный файл для фото
            val photoFile = java.io.File(
                context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES) 
                    ?: context.filesDir,
                "profile_photo_${System.currentTimeMillis()}.jpg"
            )
            // Убеждаемся, что директория существует
            photoFile.parentFile?.mkdirs()
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraImageUri = uri
            avatarUri = uri.toString()
            cameraLauncher.launch(uri)
        }
        
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            pendingAction = openCamera
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Отмена")
            }
            Text(
                text = "Редактирование профиля",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = {
                    scope.launch {
                        profileManager.saveProfile(
                            Profile(
                                fullName = fullName,
                                avatarUri = avatarUri,
                                resumeUrl = resumeUrl,
                                position = position
                            )
                        )
                        navController.popBackStack()
                    }
                }
            ) {
                Text("Готово")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Аватар с возможностью изменения
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(120.dp)
                .clip(CircleShape)
                .clickable { showImageSourceDialog = true }
        ) {
            if (avatarUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse(avatarUri)),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fullName.takeIf { it.isNotEmpty() }?.firstOrNull()?.toString() ?: "?",
                            fontSize = 48.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Иконка редактирования
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Изменить фото",
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Поле ФИО
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("ФИО") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Поле должности
        OutlinedTextField(
            value = position,
            onValueChange = { position = it },
            label = { Text("Должность") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Поле URL резюме
        OutlinedTextField(
            value = resumeUrl,
            onValueChange = { resumeUrl = it },
            label = { Text("URL резюме/портфолио") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("https://example.com/resume.pdf") }
        )
        
        Spacer(modifier = Modifier.weight(1f))
    }
    
    // Диалог выбора источника фото
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Выберите источник") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            requestImageFromGallery()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Галерея")
                    }
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            requestImageFromCamera()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Camera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Камера")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

