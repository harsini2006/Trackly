package com.example.trackio.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionDirection {
    PAID,
    RECEIVED
}

enum class TransactionSource {
    NOTIFICATION,
    MANUAL,
    IMPORT,
    SMS
}

enum class TransactionStatus {
    CONFIRMED,
    NEEDS_REVIEW
}

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["upiRef"], unique = false),
        Index(value = ["timestamp"]),
        Index(value = ["categoryId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val direction: TransactionDirection = TransactionDirection.PAID,
    val vendorName: String,
    val categoryId: Long? = null,
    val tag: String? = null, // e.g. "Priya's birthday" or "Final year project"
    val timestamp: Long = System.currentTimeMillis(),
    val upiRef: String? = null,
    val source: TransactionSource = TransactionSource.MANUAL,
    val status: TransactionStatus = TransactionStatus.CONFIRMED,
    val rawContent: String? = null
)
