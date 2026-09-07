package com.example.freshup.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.freshup.FreshUpApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpiryAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ProductAlarmScheduler.EXTRA_PRODUCT_ID, -1)
        if (id < 0) return

        val app = context.applicationContext as? FreshUpApp ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val dao = app.database.productDao()
            val entity = dao.getById(id) ?: return@launch
            if (entity.notificationShown) return@launch

            NotificationHelper.createChannel(context)
            NotificationHelper.showExpiringSoonNotification(context, entity.toDomainModel())
            dao.markNotified(id)
        }
    }
}
