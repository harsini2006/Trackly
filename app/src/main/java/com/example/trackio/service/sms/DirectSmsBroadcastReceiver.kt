package com.example.trackio.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.trackio.service.ProcessNotificationWorker

class DirectSmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val sb = StringBuilder()
            var sender = ""
            var timestamp = System.currentTimeMillis()

            for (sms in messages) {
                sender = sms.displayOriginatingAddress ?: ""
                sb.append(sms.displayMessageBody)
                timestamp = sms.timestampMillis
            }

            // Only process bank SMS messages for user privacy
            if (!BankSmsParser.isBankSender(sender)) {
                return
            }

            val body = sb.toString()
            val parsed = BankSmsParser.parseSms(sender, body, timestamp)

            if (parsed.amount != null && parsed.amount > 0) {
                val inputData = Data.Builder()
                    .putString(ProcessNotificationWorker.KEY_PACKAGE_NAME, "sms.$sender")
                    .putString(ProcessNotificationWorker.KEY_TITLE, "Bank SMS: $sender")
                    .putString(ProcessNotificationWorker.KEY_TEXT, body)
                    .putLong(ProcessNotificationWorker.KEY_TIMESTAMP, timestamp)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<ProcessNotificationWorker>()
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }
}
