package com.example.trackio.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Test & Debug BroadcastReceiver to inject simulated payment notifications
 * for automated Playwright-style E2E validation.
 */
class MockNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.getStringExtra("package") ?: "com.google.android.apps.nbu.paisa.user"
        val title = intent.getStringExtra("title") ?: "Google Pay"
        val text = intent.getStringExtra("text") ?: ""
        val timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis())

        if (text.isBlank()) return

        val inputData = Data.Builder()
            .putString(ProcessNotificationWorker.KEY_PACKAGE_NAME, packageName)
            .putString(ProcessNotificationWorker.KEY_TITLE, title)
            .putString(ProcessNotificationWorker.KEY_TEXT, text)
            .putLong(ProcessNotificationWorker.KEY_TIMESTAMP, timestamp)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ProcessNotificationWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
