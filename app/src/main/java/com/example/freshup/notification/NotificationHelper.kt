package com.example.freshup.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.freshup.MainActivity
import com.example.freshup.R
import com.example.freshup.domain.model.Product

object NotificationHelper {
    const val CHANNEL_ID = "freshup_expiring"
    const val EXTRA_OPEN_PRODUCT_ID = "open_product_id"
    private const val CHANNEL_NAME = "Срок годности"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Напоминания об истечении срока годности продуктов"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showExpiringSoonNotification(context: Context, product: Product) {
        val text = when {
            product.daysLeft <= 0 -> "«${product.name}» — срок истёк! Проверьте продукт."
            product.daysLeft == 1L -> "«${product.name}» истекает завтра."
            else -> "«${product.name}» истекает через ${product.daysLeft} дн."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Свежо — скоро истекает срок")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Тап по уведомлению открывает приложение и подсвечивает продукт.
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        launchIntent.putExtra(EXTRA_OPEN_PRODUCT_ID, product.id)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        val contentIntent = PendingIntent.getActivity(
            context,
            product.id + 1_000_000,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(contentIntent)

        val hasPermission = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                NotificationManagerCompat.from(context)
                    .notify(product.id, builder.build())
            } catch (_: SecurityException) {
                // Разрешение отозвано пользователем — пропускаем.
            }
        }
    }
}
