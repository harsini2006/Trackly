package com.example.trackio.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.trackio.data.local.AppDatabase
import com.example.trackio.data.local.entity.TransactionDirection
import com.example.trackio.data.local.entity.TransactionEntity
import com.example.trackio.data.local.entity.TransactionSource
import com.example.trackio.data.local.entity.TransactionStatus
import com.example.trackio.data.local.entity.VendorEntity
import com.example.trackio.service.parser.UpiNotificationParserManager
import kotlin.math.abs

class ProcessNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_PACKAGE_NAME = "key_package_name"
        const val KEY_TITLE = "key_title"
        const val KEY_TEXT = "key_text"
        const val KEY_TIMESTAMP = "key_timestamp"
    }

    override suspend fun doWork(): Result {
        val packageName = inputData.getString(KEY_PACKAGE_NAME) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: ""
        val text = inputData.getString(KEY_TEXT) ?: ""
        val timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis())

        val parsed = UpiNotificationParserManager.parse(
            packageName = packageName,
            title = title,
            text = text
        )

        val database = AppDatabase.getInstance(applicationContext)
        val transactionDao = database.transactionDao()
        val vendorDao = database.vendorDao()

        val amount = parsed.amount ?: 0.0

        // 1. De-duplication Check by UPI Reference Number
        if (!parsed.upiRef.isNullOrBlank()) {
            val existing = transactionDao.findByUpiRef(parsed.upiRef)
            if (existing != null) {
                // Already captured, ignore duplicate
                return Result.success()
            }
        }

        // 2. De-duplication Check by Amount + Time Window (within 10 minutes)
        if (amount > 0) {
            val windowStart = timestamp - (10 * 60 * 1000)
            val windowEnd = timestamp + (10 * 60 * 1000)
            val duplicate = transactionDao.findDuplicateCandidate(amount, windowStart, windowEnd)
            if (duplicate != null && duplicate.vendorName.equals(parsed.payeeOrVendor, ignoreCase = true)) {
                return Result.success()
            }
        }

        // 3. Resolve Vendor & Category
        var categoryId: Long? = null
        val vendorName = parsed.payeeOrVendor ?: "UPI Transaction"

        if (!parsed.payeeOrVendor.isNullOrBlank()) {
            val existingVendor = vendorDao.findVendorByName(parsed.payeeOrVendor)
            if (existingVendor != null) {
                categoryId = existingVendor.defaultCategoryId
            } else {
                // Auto-seed vendor
                vendorDao.insertVendor(
                    VendorEntity(
                        name = parsed.payeeOrVendor,
                        defaultCategoryId = null
                    )
                )
            }
        }

        // 4. Save Transaction: Confirmed or Needs Review
        val status = if (parsed.isSuccess) {
            TransactionStatus.CONFIRMED
        } else {
            TransactionStatus.NEEDS_REVIEW
        }

        val transaction = TransactionEntity(
            amount = amount,
            direction = parsed.direction,
            vendorName = vendorName,
            categoryId = categoryId,
            tag = null,
            timestamp = timestamp,
            upiRef = parsed.upiRef,
            source = TransactionSource.NOTIFICATION,
            status = status,
            rawContent = if (!parsed.isSuccess) parsed.rawText else null
        )

        val newId = transactionDao.insertTransaction(transaction)

        // Post quick-action notification for rapid 1-tap categorization
        if (amount > 0 && parsed.direction == TransactionDirection.PAID) {
            try {
                com.example.trackio.service.notification.QuickActionNotificationHelper.postQuickActionNotification(
                    context = applicationContext,
                    transactionId = newId,
                    amount = amount,
                    vendorName = vendorName
                )
            } catch (e: Exception) {
                // Ignore notification posting failure if permission not granted
            }
        }

        return Result.success()
    }
}
