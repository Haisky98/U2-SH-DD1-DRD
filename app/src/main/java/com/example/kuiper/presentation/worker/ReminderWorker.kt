package com.example.kuiper.presentation.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kuiper.presentation.data.StepPrefs
import kotlinx.coroutines.flow.first

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = StepPrefs(context)
        val goal = prefs.dailyGoal.first() ?: return Result.success()
        val currentSteps = prefs.currentSteps.first()
        val remaining = (goal - currentSteps).coerceAtLeast(0)

        if (remaining > 0) {
            sendNotification(remaining)
        }
        return Result.success()
    }

    private fun sendNotification(remaining: Int) {
        val channelId = "step_reminder_channel"
        val channel = NotificationChannel(
            channelId,
            "Recordatorio de Pasos",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Sigue avanzando!")
            .setContentText("Te faltan $remaining pasos para llegar a তোমার meta de hoy.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(1001, notification)
        }
    }
}