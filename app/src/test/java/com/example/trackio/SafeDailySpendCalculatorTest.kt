package com.example.trackio

import com.example.trackio.data.local.entity.CycleType
import com.example.trackio.data.local.entity.FixedItemEntity
import com.example.trackio.data.local.entity.SettingsEntity
import com.example.trackio.domain.SafeDailySpendCalculator
import com.example.trackio.domain.SpendStatusColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SafeDailySpendCalculatorTest {

    @Test
    fun testPrdBaselineCalculation() {
        // PRD 1-line summary: "₹300 left, 10 days to go, so ₹30 a day"
        val result = SafeDailySpendCalculator.calculate(
            allowance = 300.0,
            spentSoFarInCycle = 0.0,
            upcomingFixedItems = 0.0,
            todaySpent = 0.0,
            daysLeftInCycle = 10,
            daysTotalInCycle = 10
        )

        assertEquals(300.0, result.remainingBalance, 0.01)
        assertEquals(30.0, result.safeDailySpend, 0.01)
        assertEquals(30.0, result.todayRemaining, 0.01)
        assertEquals(SpendStatusColor.GREEN, result.statusColor)
    }

    @Test
    fun testUpcomingFixedItemsDeduction() {
        // Allowance = 10,000, Spent so far = 1,000, Upcoming fixed (rent/wifi) = 3,000, Days left = 15
        // Remaining = 10,000 - 1,000 - 3,000 = 6,000
        // Safe daily = 6,000 / 15 = 400.0
        val result = SafeDailySpendCalculator.calculate(
            allowance = 10000.0,
            spentSoFarInCycle = 1000.0,
            upcomingFixedItems = 3000.0,
            todaySpent = 150.0,
            daysLeftInCycle = 15,
            daysTotalInCycle = 30
        )

        assertEquals(6000.0, result.remainingBalance, 0.01)
        assertEquals(400.0, result.safeDailySpend, 0.01)
        assertEquals(250.0, result.todayRemaining, 0.01) // 400 - 150 = 250
        assertEquals(SpendStatusColor.GREEN, result.statusColor)
    }

    @Test
    fun testRolloverRule_UnspentMoneyRaisesTomorrow() {
        // Day 1: Remaining = 1,000, Days left = 10 -> Safe daily = 100
        val day1 = SafeDailySpendCalculator.calculate(
            allowance = 1000.0,
            spentSoFarInCycle = 0.0,
            upcomingFixedItems = 0.0,
            todaySpent = 0.0,
            daysLeftInCycle = 10,
            daysTotalInCycle = 10
        )
        assertEquals(100.0, day1.safeDailySpend, 0.01)

        // User spent 0 on Day 1. On Day 2: Remaining is still 1,000, but Days left = 9!
        val day2 = SafeDailySpendCalculator.calculate(
            allowance = 1000.0,
            spentSoFarInCycle = 0.0,
            upcomingFixedItems = 0.0,
            todaySpent = 0.0,
            daysLeftInCycle = 9,
            daysTotalInCycle = 10
        )
        // 1000 / 9 = 111.11 -> Raises tomorrow's daily spend!
        assertEquals(111.11, day2.safeDailySpend, 0.01)
        assertTrue(day2.safeDailySpend > day1.safeDailySpend)
    }

    @Test
    fun testRolloverRule_OverspendingLowersTomorrow() {
        // Day 1: Allowance = 1,000, Safe daily = 100.
        // User overspends and spends 280 today!
        // On Day 2: Spent so far = 280, Days left = 9 -> Remaining = 720.
        val day2 = SafeDailySpendCalculator.calculate(
            allowance = 1000.0,
            spentSoFarInCycle = 280.0,
            upcomingFixedItems = 0.0,
            todaySpent = 0.0,
            daysLeftInCycle = 9,
            daysTotalInCycle = 10
        )
        // 720 / 9 = 80.0 -> Daily spend decreased to maintain budget!
        assertEquals(80.0, day2.safeDailySpend, 0.01)
        assertTrue(day2.safeDailySpend < 100.0)
    }

    @Test
    fun testStatusColorTransitions() {
        // Safe daily = 100.0
        // Spent 50 -> GREEN
        val greenState = SafeDailySpendCalculator.calculate(
            allowance = 1000.0, spentSoFarInCycle = 50.0, upcomingFixedItems = 0.0,
            todaySpent = 50.0, daysLeftInCycle = 10, daysTotalInCycle = 10
        )
        assertEquals(SpendStatusColor.GREEN, greenState.statusColor)

        // Spent 85 (>= 80%) -> AMBER
        val amberState = SafeDailySpendCalculator.calculate(
            allowance = 1000.0, spentSoFarInCycle = 85.0, upcomingFixedItems = 0.0,
            todaySpent = 85.0, daysLeftInCycle = 10, daysTotalInCycle = 10
        )
        assertEquals(SpendStatusColor.AMBER, amberState.statusColor)

        // Spent 120 (> 100) -> RED
        val redState = SafeDailySpendCalculator.calculate(
            allowance = 1000.0, spentSoFarInCycle = 120.0, upcomingFixedItems = 0.0,
            todaySpent = 120.0, daysLeftInCycle = 10, daysTotalInCycle = 10
        )
        assertEquals(SpendStatusColor.RED, redState.statusColor)
    }

    @Test
    fun testZeroAndNegativeBalanceEdgeCases() {
        // Overspent entire allowance
        val result = SafeDailySpendCalculator.calculate(
            allowance = 500.0,
            spentSoFarInCycle = 650.0,
            upcomingFixedItems = 0.0,
            todaySpent = 150.0,
            daysLeftInCycle = 5,
            daysTotalInCycle = 30
        )

        assertEquals(-150.0, result.remainingBalance, 0.01)
        assertEquals(0.0, result.safeDailySpend, 0.01)
        assertEquals(0.0, result.todayRemaining, 0.01)
        assertEquals(SpendStatusColor.RED, result.statusColor)
    }

    @Test
    fun testCycleRangeMonthlyCalculation() {
        val settings = SettingsEntity(
            allowanceAmount = 5000.0,
            cycleType = CycleType.MONTHLY,
            startDayOfMonth = 1
        )

        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 15, 12, 0, 0)
        }
        val range = SafeDailySpendCalculator.computeCycleRange(settings, cal.timeInMillis)

        assertEquals(31, range.daysTotal)
        assertEquals(17, range.daysLeft) // Oct 15 through Oct 31 = 17 days
    }
}
