package com.example.trackio.domain

import com.example.trackio.data.local.entity.CycleType
import com.example.trackio.data.local.entity.FixedItemEntity
import com.example.trackio.data.local.entity.SettingsEntity
import java.util.Calendar
import kotlin.math.max

enum class SpendStatusColor {
    GREEN,  // On track
    AMBER,  // Close to limit / above daily plan
    RED     // Over budget
}

data class CycleDateRange(
    val startTimestamp: Long,
    val endTimestamp: Long,
    val daysTotal: Int,
    val daysLeft: Int
)

data class SafeSpendCalculationResult(
    val allowance: Double,
    val spentSoFar: Double,
    val upcomingFixedAmount: Double,
    val remainingBalance: Double,
    val daysLeftInCycle: Int,
    val daysTotalInCycle: Int,
    val safeDailySpend: Double,
    val todaySpent: Double,
    val todayRemaining: Double,
    val statusColor: SpendStatusColor,
    val statusMessage: String
)

object SafeDailySpendCalculator {

    /**
     * Calculates the cycle boundary and days remaining.
     */
    fun computeCycleRange(
        settings: SettingsEntity,
        referenceTime: Long = System.currentTimeMillis()
    ): CycleDateRange {
        val cal = Calendar.getInstance().apply { timeInMillis = referenceTime }

        when (settings.cycleType) {
            CycleType.WEEKLY -> {
                // Weekly cycle: from start of current week (e.g. Monday) to end of week
                val currentCal = Calendar.getInstance().apply {
                    timeInMillis = referenceTime
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val dayOfWeek = currentCal.get(Calendar.DAY_OF_WEEK)
                val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
                currentCal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
                val start = currentCal.timeInMillis

                val endCal = (currentCal.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, 6)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val end = endCal.timeInMillis

                val todayCal = Calendar.getInstance().apply { timeInMillis = referenceTime }
                val daysLeft = max(1, 7 - daysFromMonday)
                return CycleDateRange(start, end, 7, daysLeft)
            }
            CycleType.CUSTOM -> {
                val cycleDays = max(1, settings.customCycleDays)
                val startCal = Calendar.getInstance().apply {
                    timeInMillis = referenceTime
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = (startCal.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, cycleDays - 1)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                return CycleDateRange(startCal.timeInMillis, endCal.timeInMillis, cycleDays, cycleDays)
            }
            CycleType.MONTHLY -> {
                val today = cal.get(Calendar.DAY_OF_MONTH)
                val targetStartDay = settings.startDayOfMonth.coerceIn(1, 28)

                val startCal = Calendar.getInstance().apply {
                    timeInMillis = referenceTime
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (today >= targetStartDay) {
                    // Current month cycle (e.g. today is 15th, cycle started on 1st of this month)
                    startCal.set(Calendar.DAY_OF_MONTH, targetStartDay)
                } else {
                    // Started in previous month
                    startCal.add(Calendar.MONTH, -1)
                    val maxDaysPrevMonth = startCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    startCal.set(Calendar.DAY_OF_MONTH, targetStartDay.coerceAtMost(maxDaysPrevMonth))
                }

                val endCal = (startCal.clone() as Calendar).apply {
                    add(Calendar.MONTH, 1)
                    add(Calendar.DAY_OF_MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                val totalDays = ((endCal.timeInMillis - startCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt() + 1
                val daysPassed = ((referenceTime - startCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                val daysLeft = max(1, totalDays - daysPassed)

                return CycleDateRange(
                    startTimestamp = startCal.timeInMillis,
                    endTimestamp = endCal.timeInMillis,
                    daysTotal = totalDays,
                    daysLeft = daysLeft
                )
            }
        }
    }

    /**
     * Determines which fixed items are upcoming in the remainder of the cycle.
     */
    fun calculateUpcomingFixedItems(
        fixedItems: List<FixedItemEntity>,
        cycleRange: CycleDateRange,
        referenceTime: Long = System.currentTimeMillis()
    ): Double {
        val cal = Calendar.getInstance().apply { timeInMillis = referenceTime }
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)

        return fixedItems
            .filter { it.isEnabled }
            .filter { item ->
                // Item is upcoming if its due day is today or later in the month
                item.dueDayOfMonth >= currentDay
            }
            .sumOf { it.amount }
    }

    /**
     * Core Safe Daily Spend computation following PRD Section 6.2:
     * Remaining = Allowance - Spent so far - Upcoming fixed items
     * Safe daily spend = Remaining ÷ Days left in cycle
     */
    fun calculate(
        allowance: Double,
        spentSoFarInCycle: Double,
        upcomingFixedItems: Double,
        todaySpent: Double,
        daysLeftInCycle: Int,
        daysTotalInCycle: Int
    ): SafeSpendCalculationResult {
        val safeAllowance = max(0.0, allowance)
        val validDaysLeft = max(1, daysLeftInCycle)

        val remainingBalance = safeAllowance - spentSoFarInCycle - upcomingFixedItems
        val rawSafeDaily = if (remainingBalance > 0) remainingBalance / validDaysLeft else 0.0
        val safeDailySpend = (rawSafeDaily * 100).toLong() / 100.0 // 2 decimal places

        val todayRemaining = max(0.0, safeDailySpend - todaySpent)

        // Status color & message logic
        val statusColor: SpendStatusColor
        val statusMessage: String

        when {
            remainingBalance <= 0 -> {
                statusColor = SpendStatusColor.RED
                statusMessage = "Over budget for this cycle"
            }
            todaySpent > safeDailySpend -> {
                statusColor = SpendStatusColor.RED
                statusMessage = "Above today's limit"
            }
            todaySpent >= (safeDailySpend * 0.8) -> {
                statusColor = SpendStatusColor.AMBER
                statusMessage = "Close to today's limit"
            }
            else -> {
                statusColor = SpendStatusColor.GREEN
                statusMessage = "On track"
            }
        }

        return SafeSpendCalculationResult(
            allowance = safeAllowance,
            spentSoFar = spentSoFarInCycle,
            upcomingFixedAmount = upcomingFixedItems,
            remainingBalance = remainingBalance,
            daysLeftInCycle = validDaysLeft,
            daysTotalInCycle = daysTotalInCycle,
            safeDailySpend = safeDailySpend,
            todaySpent = todaySpent,
            todayRemaining = todayRemaining,
            statusColor = statusColor,
            statusMessage = statusMessage
        )
    }

    /**
     * Returns the start of today in milliseconds (00:00:00.000).
     */
    fun getStartOfToday(referenceTime: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = referenceTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Returns the end of today in milliseconds (23:59:59.999).
     */
    fun getEndOfToday(referenceTime: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = referenceTime
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}
