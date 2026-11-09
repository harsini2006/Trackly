package com.example.trackio.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.trackio.service.parser.UpiNotificationParserManager

class TrackioNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return

        // 1. Strict Privacy Check: Only process notifications from known payment apps
        if (!UpiNotificationParserManager.isPaymentApp(packageName)) {
            // Discard immediately without reading or storing
            return
        }

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val effectiveText = if (bigText.isNotBlank()) bigText else text
        if (title.isBlank() && effectiveText.isBlank()) return

        // 2. Hand off to WorkManager for reliable background persistence
        val inputData = Data.Builder()
            .putString(ProcessNotificationWorker.KEY_PACKAGE_NAME, packageName)
            .putString(ProcessNotificationWorker.KEY_TITLE, title)
            .putString(ProcessNotificationWorker.KEY_TEXT, effectiveText)
            .putLong(ProcessNotificationWorker.KEY_TIMESTAMP, sbn.postTime)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ProcessNotificationWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
