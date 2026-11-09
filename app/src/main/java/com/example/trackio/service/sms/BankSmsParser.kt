package com.example.trackio.service.sms

import com.example.trackio.data.local.entity.TransactionDirection
import com.example.trackio.service.parser.ParsedNotificationResult
import java.util.regex.Pattern

object BankSmsParser {

    private val BANK_SENDER_PATTERNS = listOf(
        "HDFC", "SBI", "ICICI", "AXIS", "KOTAK", "PNB", "BOI", "INDUS", "CANARA", "PAYTM", "UNION"
    )

    private val AMOUNT_PATTERN = Pattern.compile(
        """(?:Rs\.?|INR|₹)\s*([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
        Pattern.CASE_INSENSITIVE
    )

    private val REF_PATTERN = Pattern.compile(
        """(?:Ref(?:\s*no\.?)?|UTR|Txn(?:\s*ID)?|no\.?)\s*[:#\-]?\s*([A-Za-z0-9]{8,22})""",
        Pattern.CASE_INSENSITIVE
    )

    private val VENDOR_PATTERNS = listOf(
        // "to ZOMATO on 28-OCT-26" or "to CHAI POINT"
        Pattern.compile("""(?:to|towards|at|info:)\s+([^.\n,]+?)(?:\s*(?:on|using|ref|via|avl|bal|\.|$))""", Pattern.CASE_INSENSITIVE),
        // "spent on Axis Bank Card at BLINKIT on"
        Pattern.compile("""(?:at)\s+([^.\n,]+?)(?:\s*(?:on|ref|via|avl|bal|\.|$))""", Pattern.CASE_INSENSITIVE)
    )

    fun isBankSender(sender: String): Boolean {
        val upper = sender.uppercase()
        return BANK_SENDER_PATTERNS.any { upper.contains(it) }
    }

    fun parseSms(sender: String, messageBody: String, timestamp: Long = System.currentTimeMillis()): ParsedNotificationResult {
        val upper = messageBody.uppercase()

        val isReceived = upper.contains("CREDITED") || upper.contains("RECEIVED") || upper.contains("DEPOSITED")
        val isDebited = upper.contains("DEBITED") || upper.contains("SENT") || upper.contains("SPENT") || upper.contains("PAID")

        val direction = if (isReceived && !isDebited) TransactionDirection.RECEIVED else TransactionDirection.PAID

        // Extract Amount
        var amount: Double? = null
        val amountMatcher = AMOUNT_PATTERN.matcher(messageBody)
        if (amountMatcher.find()) {
            amount = amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        }

        // Extract Vendor / Merchant
        var vendor: String? = null
        for (pattern in VENDOR_PATTERNS) {
            val vMatcher = pattern.matcher(messageBody)
            if (vMatcher.find()) {
                val candidate = vMatcher.group(1)?.trim()
                if (!candidate.isNullOrBlank() && candidate.length > 1) {
                    vendor = candidate
                        .replace(Regex("""(?i)\s*(A/C|Bank|Card|Ref|Available|Bal|INR|Rs)"""), "")
                        .trim()
                    break
                }
            }
        }

        // Extract Ref
        var ref: String? = null
        val refMatcher = REF_PATTERN.matcher(messageBody)
        if (refMatcher.find()) {
            ref = refMatcher.group(1)?.trim()
        }

        val isSuccess = amount != null && amount > 0

        return ParsedNotificationResult(
            amount = amount,
            direction = direction,
            payeeOrVendor = vendor?.ifBlank { "Bank SMS (${sender.takeLast(6)})" } ?: "Bank SMS",
            upiRef = ref,
            isSuccess = isSuccess,
            rawText = messageBody,
            sourceApp = sender
        )
    }
}
