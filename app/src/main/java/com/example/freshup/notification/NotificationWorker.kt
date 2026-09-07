package com.example.freshup.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.freshup.FreshUpApp

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dao = (applicationContext as FreshUpApp).database.productDao()

        // Порог индивидуален для каждого продукта: пора уведомлять, когда наступает
        // expirationTimestamp - notifyDaysBefore дней. Запрос уже учитывает это поле.
        val now = System.currentTimeMillis()

        val toNotify = dao.getProductsToNotify(now)
        if (toNotify.isEmpty()) return Result.success()

        NotificationHelper.createChannel(applicationContext)
        toNotify.forEach { entity ->
            NotificationHelper.showExpiringSoonNotification(applicationContext, entity.toDomainModel())
            dao.markNotified(entity.id)
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "freshup_expiring_check"
    }
}
