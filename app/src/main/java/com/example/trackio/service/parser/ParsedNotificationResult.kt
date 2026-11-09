package com.example.trackio.service.parser

import com.example.trackio.data.local.entity.TransactionDirection

data class ParsedNotificationResult(
    val amount: Double?,
    val direction: TransactionDirection = TransactionDirection.PAID,
    val payeeOrVendor: String?,
    val upiRef: String?,
    val isSuccess: Boolean,
    val rawText: String,
    val sourceApp: String
)
