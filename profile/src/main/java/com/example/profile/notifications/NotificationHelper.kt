package com.example.profile.notifications

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

object NotificationHelper {
    private const val REQUEST_CODE = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                PairNotificationReceiver.CHANNEL_ID,
                "Уведомления о парах",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о начале любимой пары"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(context: Context, timeString: String, userName: String, mainActivityClass: Class<out Activity>) {
        // Парсим время в формате HH:mm
        val timeParts = timeString.split(":")
        if (timeParts.size != 2) return
        
        val hour = timeParts[0].toIntOrNull() ?: return
        val minute = timeParts[1].toIntOrNull() ?: return
        
        if (hour !in 0..23 || minute !in 0..59) return

        // Отменяем предыдущее уведомление, если оно было установлено
        cancelNotification(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Проверяем разрешение на точные будильники для Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                android.util.Log.w("NotificationHelper", "Cannot schedule exact alarms - permission not granted")
                // Показываем сообщение пользователю
                android.widget.Toast.makeText(
                    context,
                    "Для установки уведомлений требуется разрешение на точные будильники. Откройте настройки приложения.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                // Открываем настройки приложения
                try {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("NotificationHelper", "Error opening settings: ${e.message}", e)
                }
                return
            }
        }
        val intent = Intent(context, PairNotificationReceiver::class.java).apply {
            putExtra(PairNotificationReceiver.EXTRA_USER_NAME, userName)
            putExtra(PairNotificationReceiver.EXTRA_MAIN_ACTIVITY_CLASS, mainActivityClass.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Устанавливаем время на сегодня
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // Если время уже прошло сегодня, устанавливаем на завтра
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Используем setAlarmClock, который не требует специального разрешения
        // и показывает уведомление в системных часах
        val showIntent = Intent(context, mainActivityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE + 1,
            showIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, showPendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun cancelNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PairNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    fun isValidTimeFormat(timeString: String): Boolean {
        if (timeString.isBlank()) return false
        
        val timeParts = timeString.split(":")
        if (timeParts.size != 2) return false
        
        val hour = timeParts[0].toIntOrNull() ?: return false
        val minute = timeParts[1].toIntOrNull() ?: return false
        
        return hour in 0..23 && minute in 0..59
    }
}

