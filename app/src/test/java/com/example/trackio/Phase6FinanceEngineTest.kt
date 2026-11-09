package com.example.trackio

import com.example.trackio.data.local.entity.ProjectEntity
import com.example.trackio.data.local.entity.TransactionDirection
import com.example.trackio.data.local.entity.TransactionEntity
import com.example.trackio.data.local.entity.TransactionStatus
import com.example.trackio.data.local.entity.WishlistItemEntity
import com.example.trackio.domain.SafeSpendCalculationResult
import com.example.trackio.domain.SpendStatusColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase6FinanceEngineTest {

    @Test
    fun testWishlistOpportunityCostCalculation() {
        val calc = SafeSpendCalculationResult(
            allowance = 10000.0,
            spentSoFar = 4000.0,
            upcomingFixedAmount = 0.0,
            remainingBalance = 6000.0,
            daysLeftInCycle = 20,
            daysTotalInCycle = 30,
            safeDailySpend = 300.0,
            todaySpent = 0.0,
            todayRemaining = 300.0,
            statusColor = SpendStatusColor.GREEN,
            statusMessage = "All good"
        )

        val item = WishlistItemEntity(
            name = "Wireless Headphones",
            targetPrice = 1500.0
        )

        val remainingBuffer = (calc.allowance - calc.spentSoFar).coerceAtLeast(0.0)
        assertEquals(6000.0, remainingBuffer, 0.01)

        val impactPct = ((item.targetPrice / remainingBuffer) * 100).toInt()
        assertEquals(25, impactPct) // 1500 / 6000 = 25%

        val newSafeDaily = ((remainingBuffer - item.targetPrice) / calc.daysLeftInCycle).coerceAtLeast(0.0)
        assertEquals(225.0, newSafeDaily, 0.01) // (6000 - 1500) / 20 = 225
        assertTrue(remainingBuffer >= item.targetPrice)
    }

    @Test
    fun testWishlistOverBudgetExceeded() {
        val calc = SafeSpendCalculationResult(
            allowance = 10000.0,
            spentSoFar = 9500.0,
            upcomingFixedAmount = 0.0,
            remainingBalance = 500.0,
            daysLeftInCycle = 10,
            daysTotalInCycle = 30,
            safeDailySpend = 50.0,
            todaySpent = 0.0,
            todayRemaining = 50.0,
            statusColor = SpendStatusColor.RED,
            statusMessage = "Tight budget"
        )

        val item = WishlistItemEntity(
            name = "Designer Shoes",
            targetPrice = 2500.0
        )

        val remainingBuffer = (calc.allowance - calc.spentSoFar).coerceAtLeast(0.0)
        assertEquals(500.0, remainingBuffer, 0.01)

        val isAffordableNow = remainingBuffer >= item.targetPrice
        assertFalse(isAffordableNow)

        val shortage = item.targetPrice - remainingBuffer
        assertEquals(2000.0, shortage, 0.01)
    }

    @Test
    fun testProjectBudgetTrackingAndCap() {
        val project = ProjectEntity(
            id = 1,
            name = "Goa Trip",
            capAmount = 5000.0
        )

        val transactions = listOf(
            TransactionEntity(amount = 1200.0, vendorName = "Train", tag = "Goa Trip", direction = TransactionDirection.PAID, status = TransactionStatus.CONFIRMED),
            TransactionEntity(amount = 1800.0, vendorName = "Hostel", tag = "#goa trip", direction = TransactionDirection.PAID, status = TransactionStatus.CONFIRMED),
            TransactionEntity(amount = 500.0, vendorName = "Snacks", tag = "Books", direction = TransactionDirection.PAID, status = TransactionStatus.CONFIRMED)
        )

        val normalizedProject = project.name.trim().lowercase()
        val taggedTxns = transactions.filter { txn ->
            val txnTag = txn.tag?.trim()?.removePrefix("#")?.lowercase()
            txnTag == normalizedProject &&
                    txn.direction == TransactionDirection.PAID &&
                    txn.status == TransactionStatus.CONFIRMED
        }

        val spentAmount = taggedTxns.sumOf { it.amount }
        assertEquals(3000.0, spentAmount, 0.01)

        val cap = project.capAmount ?: 0.0
        val progress = (spentAmount / cap).toFloat()
        assertEquals(0.60f, progress, 0.01f)
        assertFalse(spentAmount > cap)

        val remainingCap = cap - spentAmount
        assertEquals(2000.0, remainingCap, 0.01)
    }

    @Test
    fun testSpendingBurnVelocity() {
        val allowance = 9000.0
        val spentSoFar = 4500.0
        val daysTotal = 30
        val daysLeft = 15

        val daysElapsed = (daysTotal - daysLeft + 1).coerceAtLeast(1) // 16 days
        val actualDailyBurn = spentSoFar / daysElapsed // 4500 / 16 = 281.25
        val baselineDailyBudget = allowance / daysTotal // 9000 / 30 = 300.0

        assertEquals(16, daysElapsed)
        assertEquals(281.25, actualDailyBurn, 0.01)
        assertEquals(300.0, baselineDailyBudget, 0.01)

        val isOverPaced = actualDailyBurn > baselineDailyBudget * 1.15
        assertFalse(isOverPaced) // User is pacing well within budget!
    }
}
