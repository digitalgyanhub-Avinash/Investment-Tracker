package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "holdings")
data class Holding(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val platform: String,
    val category: String,
    val breakdownRaw: String = "",
    val total: Double,
    val tillDate: String,
    val remarks: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
