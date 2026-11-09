package com.example.trackio.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String, // e.g. "Restaurant", "ShoppingBag", "MenuBook", "DirectionsBus", "Receipt", "CardGiftcard", "Engineering", "Category"
    val isDefault: Boolean = false,
    val colorHex: String = "#6366F1"
)
