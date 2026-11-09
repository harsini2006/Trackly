package com.example.trackio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist")
data class WishlistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetPrice: Double,
    val notesOrUrl: String? = null,
    val isPurchased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
