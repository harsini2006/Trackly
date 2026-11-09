package com.example.trackio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val capAmount: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)
