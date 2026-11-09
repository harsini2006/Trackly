package com.example.trackio.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vendors",
    indices = [Index(value = ["name"], unique = true)]
)
data class VendorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val defaultCategoryId: Long? = null
)
