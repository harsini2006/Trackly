package com.example.trackio.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object QuickActionNotificationHelper {

    const val CHANNEL_ID = "trackio_quick_actions"
    const val CHANNEL_NAME = "Quick Spend Actions"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Quick 1-tap categorization buttons after captured transactions"
                enableVibration(true)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun postQuickActionNotification(
        context: Context,
        transactionId: Long,
        amount: Double,
        vendorName: String
    ) {
        createNotificationChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = (transactionId % 10000).toInt() + 1000

        fun createActionPendingIntent(categoryId: Long?, tag: String?, requestCode: Int): PendingIntent {
            val intent = Intent(context, QuickActionReceiver::class.java).apply {
                action = QuickActionReceiver.ACTION_CATEGORIZE
                putExtra(QuickActionReceiver.EXTRA_TRANSACTION_ID, transactionId)
                if (categoryId != null) putExtra(QuickActionReceiver.EXTRA_CATEGORY_ID, categoryId)
                if (tag != null) putExtra(QuickActionReceiver.EXTRA_TAG, tag)
                putExtra(QuickActionReceiver.EXTRA_NOTIFICATION_ID, notifId)
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("₹${amount.toInt()} paid to $vendorName")
            .setContentText("What was this for? Tap to categorize:")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "Food", createActionPendingIntent(1L, null, notifId * 10 + 1))
            .addAction(0, "Shopping", createActionPendingIntent(2L, null, notifId * 10 + 2))
            .addAction(0, "Stationery", createActionPendingIntent(3L, null, notifId * 10 + 3))
            .addAction(0, "Birthday", createActionPendingIntent(null, "Birthday", notifId * 10 + 4))
            .addAction(0, "Project", createActionPendingIntent(null, "Project", notifId * 10 + 5))
            .build()

        nm.notify(notifId, notif)
    }
}
