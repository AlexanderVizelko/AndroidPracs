package com.example.profile.notifications

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class PairNotificationReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "pair_notification_channel"
        const val NOTIFICATION_ID = 1
        const val EXTRA_USER_NAME = "user_name"
        const val EXTRA_MAIN_ACTIVITY_CLASS = "main_activity_class"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val userName = try {
            intent.getStringExtra(EXTRA_USER_NAME) ?: "Пользователь"
        } catch (e: Exception) {
            "Пользователь"
        }
        
        val activityClassName = try {
            intent.getStringExtra(EXTRA_MAIN_ACTIVITY_CLASS)
        } catch (e: Exception) {
            null
        }
        
        // Создаем Intent для открытия приложения при нажатии на уведомление
        val mainIntent = if (!activityClassName.isNullOrBlank()) {
            try {
                val activityClass = Class.forName(activityClassName) as? Class<out Activity>
                if (activityClass != null) {
                    Intent(context, activityClass).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
        
        val finalIntent = mainIntent ?: run {
            // Если не удалось создать Intent, создаем базовый Intent для запуска приложения
            val packageManager = context.packageManager
            packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        
        if (finalIntent != null) {
            showNotification(context, userName, finalIntent)
        }
    }
    
    private fun showNotification(context: Context, userName: String, intent: Intent) {
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Начало пары")
            .setContentText("Уважаемый(ая) $userName, начинается ваша любимая пара!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}

