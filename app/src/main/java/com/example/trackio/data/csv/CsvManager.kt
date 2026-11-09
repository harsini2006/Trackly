package com.example.trackio.data.csv

import com.example.trackio.data.local.entity.TransactionDirection
import com.example.trackio.data.local.entity.TransactionEntity
import com.example.trackio.data.local.entity.TransactionSource
import com.example.trackio.data.local.entity.TransactionStatus
import com.example.trackio.data.repository.TrackioRepository
import kotlinx.coroutines.flow.firstOrNull
import java.io.BufferedReader
import java.io.StringReader

data class ImportResult(
    val importedCount: Int,
    val duplicateSkippedCount: Int,
    val failedCount: Int
)

object CsvManager {

    fun exportToCsv(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        sb.append("id,amount,direction,vendor,tag,timestamp,upiRef,source,status\n")
        transactions.forEach { t ->
            val cleanVendor = t.vendorName.replace("\"", "\"\"")
            val cleanTag = (t.tag ?: "").replace("\"", "\"\"")
            val cleanUpi = (t.upiRef ?: "").replace("\"", "\"\"")
            sb.append("${t.id},${t.amount},${t.direction},\"$cleanVendor\",\"$cleanTag\",${t.timestamp},\"$cleanUpi\",${t.source},${t.status}\n")
        }
        return sb.toString()
    }

    suspend fun importFromCsv(csvContent: String, repository: TrackioRepository): ImportResult {
        var imported = 0
        var duplicates = 0
        var failed = 0

        val reader = BufferedReader(StringReader(csvContent))
        var line: String? = reader.readLine() // Header

        while (true) {
            line = reader.readLine() ?: break
            if (line.isBlank()) continue

            try {
                val parts = parseCsvLine(line)
                if (parts.size >= 4) {
                    val amount = parts.getOrNull(1)?.toDoubleOrNull()
                    val direction = try {
                        TransactionDirection.valueOf(parts.getOrNull(2)?.trim()?.uppercase() ?: "PAID")
                    } catch (e: Exception) {
                        TransactionDirection.PAID
                    }
                    val vendor = parts.getOrNull(3)?.trim()?.ifBlank { "Statement Import" } ?: "Statement Import"
                    val tag = parts.getOrNull(4)?.trim()?.ifBlank { null }
                    val timestamp = parts.getOrNull(5)?.toLongOrNull() ?: System.currentTimeMillis()
                    val upiRef = parts.getOrNull(6)?.trim()?.ifBlank { null }

                    if (amount != null && amount > 0) {
                        val newId = repository.quickAddTransaction(
                            amount = amount,
                            vendorName = vendor,
                            categoryId = null,
                            tag = tag,
                            upiRef = upiRef,
                            source = TransactionSource.IMPORT,
                            status = TransactionStatus.CONFIRMED
                        )
                        if (newId > 0) {
                            imported++
                        } else {
                            duplicates++
                        }
                    } else {
                        failed++
                    }
                } else {
                    failed++
                }
            } catch (e: Exception) {
                failed++
            }
        }

        return ImportResult(imported, duplicates, failed)
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            if (ch == '\"') {
                inQuotes = !inQuotes
            } else if (ch == ',' && !inQuotes) {
                result.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(ch)
            }
        }
        result.add(sb.toString().trim())
        return result
    }
}
