package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sips")
data class Sip(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val platform: String,
    val instrument: String,
    val amount: Double,
    val frequency: String = "Monthly", // Daily, Weekly, Monthly, Yearly
    val since: String,
    val remarks: String = "",
    val isPaused: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
