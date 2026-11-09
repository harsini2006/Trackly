package com.example.trackio.service.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.trackio.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuickActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CATEGORIZE = "com.example.trackio.ACTION_CATEGORIZE"
        const val EXTRA_TRANSACTION_ID = "extra_txn_id"
        const val EXTRA_CATEGORY_ID = "extra_category_id"
        const val EXTRA_TAG = "extra_tag"
        const val EXTRA_NOTIFICATION_ID = "extra_notif_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CATEGORIZE) {
            val txnId = intent.getLongExtra(EXTRA_TRANSACTION_ID, -1)
            val categoryId = intent.getLongExtra(EXTRA_CATEGORY_ID, -1).takeIf { it > 0 }
            val tag = intent.getStringExtra(EXTRA_TAG)
            val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

            // Dismiss notification immediately
            if (notifId > 0) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(notifId)
            }

            if (txnId > 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance(context)
                    val txn = db.transactionDao().getTransactionById(txnId)
                    if (txn != null) {
                        val updated = txn.copy(
                            categoryId = categoryId ?: txn.categoryId,
                            tag = tag ?: txn.tag
                        )
                        db.transactionDao().updateTransaction(updated)
                    }
                }
            }
        }
    }
}
