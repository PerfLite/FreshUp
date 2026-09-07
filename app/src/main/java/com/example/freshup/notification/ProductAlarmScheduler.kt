package com.example.freshup.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.freshup.FreshUpApp
import com.example.freshup.data.database.ProductEntity
import java.util.concurrent.TimeUnit

object ProductAlarmScheduler {
    const val ACTION = "com.example.freshup.NOTIFY_PRODUCT"
    const val EXTRA_PRODUCT_ID = "extra_product_id"

    private const val DAY_MS = 24L * 60 * 60 * 1000

    // Момент уведомления: за notifyDaysBefore дней до истечения срока.
    fun notifyAtMillis(entity: ProductEntity): Long =
        entity.expirationTimestamp - entity.notifyDaysBefore * DAY_MS

    fun scheduleOne(context: Context, id: Int, notifyAtMillis: Long) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val pi = pendingIntent(context, id)
        // На API 31+ точные будильники требуют разрешения SCHEDULE_EXACT_ALARM.
        // Если его нет — используем нестрогое срабатывание (тоже работает в Doze).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !am.canScheduleExactAlarms()
        ) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notifyAtMillis, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notifyAtMillis, pi)
        }
    }

    fun cancelOne(context: Context, id: Int) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        am.cancel(pendingIntent(context, id))
    }

    // Перепланируем все активные продукты (например, после перезагрузки).
    suspend fun scheduleAll(context: Context) {
        val dao = (context.applicationContext as FreshUpApp).database.productDao()
        dao.getActiveProductsList().forEach { entity ->
            scheduleOne(context, entity.id, notifyAtMillis(entity))
        }
    }

    private fun pendingIntent(context: Context, id: Int): PendingIntent {
        val intent = Intent(context, ExpiryAlarmReceiver::class.java).apply {
            action = ACTION
            putExtra(EXTRA_PRODUCT_ID, id)
        }
        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
