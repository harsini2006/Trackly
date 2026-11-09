package com.example.trackio

import com.example.trackio.data.local.entity.TransactionDirection
import com.example.trackio.service.parser.UpiNotificationParserManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpiNotificationParserTest {

    @Test
    fun testGooglePayPaidTransaction() {
        val result = UpiNotificationParserManager.parse(
            packageName = "com.google.android.apps.nbu.paisa.user",
            title = "Google Pay",
            text = "Paid ₹340 to Chaayos. UPI Ref: 412345678901"
        )

        assertTrue(result.isSuccess)
        assertEquals(340.0, result.amount!!, 0.01)
        assertEquals("Chaayos", result.payeeOrVendor)
        assertEquals("412345678901", result.upiRef)
        assertEquals(TransactionDirection.PAID, result.direction)
        assertEquals("Google Pay", result.sourceApp)
    }

    @Test
    fun testPhonePePaidTransaction() {
        val result = UpiNotificationParserManager.parse(
            packageName = "com.phonepe.app",
            title = "PhonePe",
            text = "Payment of ₹150 to Zomato successful. Txn ID: T26102712345"
        )

        assertTrue(result.isSuccess)
        assertEquals(150.0, result.amount!!, 0.01)
        assertEquals("Zomato", result.payeeOrVendor)
        assertEquals("T26102712345", result.upiRef)
        assertEquals(TransactionDirection.PAID, result.direction)
        assertEquals("PhonePe", result.sourceApp)
    }

    @Test
    fun testPaytmPaidTransaction() {
        val result = UpiNotificationParserManager.parse(
            packageName = "net.one97.paytm",
            title = "Paytm",
            text = "Paid ₹85 to Sharma General Store"
        )

        assertTrue(result.isSuccess)
        assertEquals(85.0, result.amount!!, 0.01)
        assertEquals("Sharma General Store", result.payeeOrVendor)
        assertEquals(TransactionDirection.PAID, result.direction)
        assertEquals("Paytm", result.sourceApp)
    }

    @Test
    fun testReceivedPayment_AllowanceFromParents() {
        val result = UpiNotificationParserManager.parse(
            packageName = "com.google.android.apps.nbu.paisa.user",
            title = "Google Pay",
            text = "Received ₹5,000 from Dad via UPI. UPI Ref: 987654321000"
        )

        assertTrue(result.isSuccess)
        assertEquals(5000.0, result.amount!!, 0.01)
        assertEquals("Dad", result.payeeOrVendor)
        assertEquals("987654321000", result.upiRef)
        assertEquals(TransactionDirection.RECEIVED, result.direction)
    }

    @Test
    fun testNeedsReviewFallback_WhenAmbiguous() {
        // Notification with no clear vendor or amount
        val result = UpiNotificationParserManager.parse(
            packageName = "com.phonepe.app",
            title = "PhonePe",
            text = "Your pending request was completed"
        )

        assertFalse(result.isSuccess) // Routed to Needs Review queue
        assertNotNull(result.rawText)
    }

    @Test
    fun testPaymentPackageDetection() {
        assertTrue(UpiNotificationParserManager.isPaymentApp("com.google.android.apps.nbu.paisa.user"))
        assertTrue(UpiNotificationParserManager.isPaymentApp("com.phonepe.app"))
        assertTrue(UpiNotificationParserManager.isPaymentApp("net.one97.paytm"))
        assertTrue(UpiNotificationParserManager.isPaymentApp("in.org.npci.upiapp"))
        assertFalse(UpiNotificationParserManager.isPaymentApp("com.whatsapp"))
        assertFalse(UpiNotificationParserManager.isPaymentApp("com.instagram.android"))
    }
}
