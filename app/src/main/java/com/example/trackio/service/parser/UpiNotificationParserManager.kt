package com.example.trackio.service.parser

import com.example.trackio.data.local.entity.TransactionDirection
import java.util.regex.Pattern

object UpiNotificationParserManager {

    val KNOWN_PAYMENT_PACKAGES = setOf(
        "com.google.android.apps.nbu.paisa.user", // Google Pay
        "com.phonepe.app",                       // PhonePe
        "net.one97.paytm",                       // Paytm
        "in.org.npci.upiapp",                    // BHIM UPI
        "com.dreamplug.androidapp",              // CRED
        "in.amazon.mShop.android.shopping"       // Amazon Pay
    )

    private val AMOUNT_PATTERN = Pattern.compile(
        """(?:₹|Rs\.?|INR)\s*([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
        Pattern.CASE_INSENSITIVE
    )

    private val UPI_REF_PATTERN = Pattern.compile(
        """(?:UPI\s*Ref(?:\s*no\.?)?|Ref(?:\s*no\.?)?|UTR|Txn\s*ID|Txn\s*no\.?)\s*[:#\-]?\s*([A-Za-z0-9]{10,22})""",
        Pattern.CASE_INSENSITIVE
    )

    private val VENDOR_PAID_PATTERNS = listOf(
        // "Paid ₹120 to Chaayos" or "paid to Swiggy"
        Pattern.compile("""(?:paid|sent|transferred)\s+(?:₹[0-9,.]+|Rs\.?[0-9,.]+|INR\s*[0-9,.]+)?\s*(?:to)\s+([^.\n,]+?)(?:\s*(?:using|via|UPI|Ref|\.|$))""", Pattern.CASE_INSENSITIVE),
        // "Payment of ₹150 to Zomato"
        Pattern.compile("""(?:payment\s+of\s+[^to]+to)\s+([^.\n,]+?)(?:\s*(?:successful|using|via|UPI|Ref|\.|$))""", Pattern.CASE_INSENSITIVE),
        // "Payment to Blinkit"
        Pattern.compile("""(?:payment\s+to)\s+([^.\n,]+?)(?:\s*(?:of|for|successful|using|via|UPI|Ref|\.|$))""", Pattern.CASE_INSENSITIVE),
        // "Money sent to Priya"
        Pattern.compile("""(?:money\s+sent\s+to)\s+([^.\n,:]+?)(?:\s*(?:[:\-]|using|via|UPI|Ref|\.|$))""", Pattern.CASE_INSENSITIVE)
    )

    private val VENDOR_RECEIVED_PATTERNS = listOf(
        // "Received ₹500 from Dad"
        Pattern.compile("""(?:received|credited)\s+(?:₹[0-9,.]+|Rs\.?[0-9,.]+|INR\s*[0-9,.]+)?\s*(?:from)\s+([^.\n,]+?)(?:\s*(?:using|via|UPI|Ref|\.|$))""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:money\s+received\s+from)\s+([^.\n,:]+?)(?:\s*(?:[:\-]|using|via|UPI|Ref|\.|$))""", Pattern.CASE_INSENSITIVE)
    )

    fun isPaymentApp(packageName: String): Boolean {
        return KNOWN_PAYMENT_PACKAGES.contains(packageName) ||
                packageName.contains("upi", ignoreCase = true) ||
                packageName.contains("pay", ignoreCase = true)
    }

    fun parse(
        packageName: String,
        title: String,
        text: String,
        subText: String? = null
    ): ParsedNotificationResult {
        val combinedText = "$title $text ${subText ?: ""}".trim()
        val sourceApp = resolveAppName(packageName)

        // 1. Detect Direction (PAID vs RECEIVED)
        val isReceived = combinedText.contains("received", ignoreCase = true) ||
                combinedText.contains("credited", ignoreCase = true) ||
                combinedText.contains("from", ignoreCase = true) && !combinedText.contains("paid to", ignoreCase = true)

        val direction = if (isReceived) TransactionDirection.RECEIVED else TransactionDirection.PAID

        // 2. Extract Amount
        val amount = extractAmount(combinedText)

        // 3. Extract Vendor / Payee
        val vendor = extractVendor(combinedText, isReceived)

        // 4. Extract UPI Reference Number
        val upiRef = extractUpiRef(combinedText)

        val isSuccess = amount != null && amount > 0 && !vendor.isNullOrBlank()

        return ParsedNotificationResult(
            amount = amount,
            direction = direction,
            payeeOrVendor = vendor?.trim(),
            upiRef = upiRef?.trim(),
            isSuccess = isSuccess,
            rawText = combinedText,
            sourceApp = sourceApp
        )
    }

    private fun extractAmount(text: String): Double? {
        val matcher = AMOUNT_PATTERN.matcher(text)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            return amountStr?.toDoubleOrNull()
        }
        return null
    }

    private fun extractVendor(text: String, isReceived: Boolean): String? {
        val patterns = if (isReceived) VENDOR_RECEIVED_PATTERNS else VENDOR_PAID_PATTERNS
        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim()
                if (!candidate.isNullOrBlank() && candidate.length > 1) {
                    // Clean trailing words like "successful", "completed"
                    return candidate
                        .replace(Regex("""(?i)\s*(successful|completed|via\s+upi|using\s+upi)"""), "")
                        .trim()
                }
            }
        }
        return null
    }

    private fun extractUpiRef(text: String): String? {
        val matcher = UPI_REF_PATTERN.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    private fun resolveAppName(packageName: String): String {
        return when {
            packageName.contains("nbu.paisa") -> "Google Pay"
            packageName.contains("phonepe") -> "PhonePe"
            packageName.contains("paytm") -> "Paytm"
            packageName.contains("npci") -> "BHIM UPI"
            packageName.contains("dreamplug") -> "CRED"
            packageName.contains("amazon") -> "Amazon Pay"
            else -> "UPI App"
        }
    }
}
