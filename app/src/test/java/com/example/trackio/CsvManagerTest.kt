package com.example.trackio

import com.example.trackio.data.csv.CsvManager
import com.example.trackio.data.local.entity.TransactionDirection
import com.example.trackio.data.local.entity.TransactionEntity
import com.example.trackio.data.local.entity.TransactionSource
import com.example.trackio.data.local.entity.TransactionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvManagerTest {

    @Test
    fun testExportToCsvFormat() {
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                amount = 250.0,
                direction = TransactionDirection.PAID,
                vendorName = "Swiggy, Bangalore",
                tag = "Birthday treat",
                timestamp = 1730000000000L,
                upiRef = "412345678901",
                source = TransactionSource.NOTIFICATION,
                status = TransactionStatus.CONFIRMED
            ),
            TransactionEntity(
                id = 2,
                amount = 120.0,
                direction = TransactionDirection.PAID,
                vendorName = "Chai Point",
                tag = null,
                timestamp = 1730000500000L,
                upiRef = null,
                source = TransactionSource.MANUAL,
                status = TransactionStatus.CONFIRMED
            )
        )

        val csv = CsvManager.exportToCsv(transactions)

        assertTrue(csv.startsWith("id,amount,direction,vendor,tag,timestamp,upiRef,source,status"))
        assertTrue(csv.contains("\"Swiggy, Bangalore\""))
        assertTrue(csv.contains("412345678901"))
        assertTrue(csv.contains("\"Birthday treat\""))
    }
}
