package com.alcorte.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alcorte.app.data.AppDatabase
import com.alcorte.app.logic.computeCycle
import com.jeanvidaurre.alcorte.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.get(applicationContext)
        val cards = db.cardDao().getAllOnce()
        val today = LocalDate.now()

        ensureChannel()

        cards.forEach { card ->
            val dates = computeCycle(card, today)
            val daysToCut = ChronoUnit.DAYS.between(today, dates.nextCutoff)
            val daysToDue = ChronoUnit.DAYS.between(today, dates.nextDue)

            if (daysToCut in 0..1) notify(card.hashCode(), "${card.name}: corte ${if (daysToCut == 0L) "hoy" else "mañana"}")
            if (daysToDue in 0..1) notify(card.hashCode() + 1, "${card.name}: pago ${if (daysToDue == 0L) "hoy" else "mañana"}")
        }
        return Result.success()
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recordatorios de Al Corte",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun notify(id: Int, text: String) {
        // En Android 13+ se debe validar el permiso antes de postear
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Al Corte")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "al_corte_reminders"
    }
}
