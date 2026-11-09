package com.example.trackio

import com.example.trackio.data.local.entity.TransactionDirection
import com.example.trackio.service.sms.BankSmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BankSmsParserTest {

    @Test
    fun testHdfcBankSms() {
        val result = BankSmsParser.parseSms(
            sender = "HDFCBK",
            messageBody = "Sent Rs.450.00 from HDFC Bank A/C to ZOMATO on 28-OCT-26. Ref: 5544332211. Bal: Rs.3,200"
        )

        assertTrue(result.isSuccess)
        assertEquals(450.0, result.amount!!, 0.01)
        assertEquals("ZOMATO", result.payeeOrVendor)
        assertEquals("5544332211", result.upiRef)
        assertEquals(TransactionDirection.PAID, result.direction)
    }

    @Test
    fun testSbiBankSms() {
        val result = BankSmsParser.parseSms(
            sender = "SBIINB",
            messageBody = "Dear SBI User, your A/C ending 4589 debited by Rs 120.00 on 28Oct26 by transfer to CHAI POINT. Ref 6655443322"
        )

        assertTrue(result.isSuccess)
        assertEquals(120.0, result.amount!!, 0.01)
        assertEquals("CHAI POINT", result.payeeOrVendor)
        assertEquals("6655443322", result.upiRef)
        assertEquals(TransactionDirection.PAID, result.direction)
    }

    @Test
    fun testIciciBankSms() {
        val result = BankSmsParser.parseSms(
            sender = "ICICIB",
            messageBody = "Acct XX123 debited with INR 2,400.00 on 28-Oct-26. Info: SWIGGY. Available Bal INR 5,120.00"
        )

        assertTrue(result.isSuccess)
        assertEquals(2400.0, result.amount!!, 0.01)
        assertEquals("SWIGGY", result.payeeOrVendor)
        assertEquals(TransactionDirection.PAID, result.direction)
    }

    @Test
    fun testAxisBankSms() {
        val result = BankSmsParser.parseSms(
            sender = "AXISBK",
            messageBody = "INR 350.00 spent on Axis Bank Card at BLINKIT on 28-10-26. Available balance INR 1,200.00"
        )

        assertTrue(result.isSuccess)
        assertEquals(350.0, result.amount!!, 0.01)
        assertEquals("BLINKIT", result.payeeOrVendor)
        assertEquals(TransactionDirection.PAID, result.direction)
    }

    @Test
    fun testCreditedAllowanceSms() {
        val result = BankSmsParser.parseSms(
            sender = "HDFCBK",
            messageBody = "Your A/C ending 4589 is credited by Rs 3,000.00 on 28Oct26 by transfer from RAMESH. Ref 1122334455"
        )

        assertTrue(result.isSuccess)
        assertEquals(3000.0, result.amount!!, 0.01)
        assertEquals(TransactionDirection.RECEIVED, result.direction)
    }

    @Test
    fun testBankSenderValidation() {
        assertTrue(BankSmsParser.isBankSender("HDFCBK"))
        assertTrue(BankSmsParser.isBankSender("SBIINB"))
        assertTrue(BankSmsParser.isBankSender("VK-ICICIB"))
        assertTrue(BankSmsParser.isBankSender("AX-AXISBK"))
        assertTrue(BankSmsParser.isBankSender("KOTAKB"))
        assertFalse(BankSmsParser.isBankSender("+919876543210"))
        assertFalse(BankSmsParser.isBankSender("FRIEND_CHAT"))
    }
}
