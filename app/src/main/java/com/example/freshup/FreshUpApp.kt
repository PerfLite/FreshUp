package com.example.freshup

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.freshup.data.database.AppDatabase
import com.example.freshup.notification.NotificationHelper
import com.example.freshup.notification.NotificationWorker
import com.example.freshup.notification.ProductAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class FreshUpApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        scheduleExpiringCheck()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            ProductAlarmScheduler.scheduleAll(this@FreshUpApp)
        }
    }

    private fun scheduleExpiringCheck() {
        // Проверка раз в день. KEEP — не плодим дубликаты при перезапуске приложения.
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
