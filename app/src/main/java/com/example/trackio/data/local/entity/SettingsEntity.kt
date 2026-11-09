package com.example.trackio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CycleType {
    MONTHLY,
    WEEKLY,
    CUSTOM
}

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton row
    val allowanceAmount: Double = 5000.0,
    val cycleType: CycleType = CycleType.MONTHLY,
    val startDayOfMonth: Int = 1, // 1st of month by default
    val customCycleDays: Int = 30,
    val isOnboardingCompleted: Boolean = false,
    val currencySymbol: String = "₹",
    val biometricLockEnabled: Boolean = false,
    val notificationCaptureEnabled: Boolean = false
)
