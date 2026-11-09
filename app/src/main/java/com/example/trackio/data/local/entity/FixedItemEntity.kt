package com.example.trackio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fixed_items")
data class FixedItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDayOfMonth: Int, // Day of month (1-31)
    val isEnabled: Boolean = true
)
