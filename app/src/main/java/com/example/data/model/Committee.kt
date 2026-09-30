package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

data class CommitteeMonth(
    val monthNumber: Int,
    val monthLabel: String = "",
    val winningBid: Double = 0.0,
    val netPotValue: Double = 0.0,
    val installmentPerPerson: Double = 0.0,
    val isPaid: Boolean = false,
    val winnerName: String = "",
    val remarks: String = ""
)

@Entity(tableName = "committees")
data class Committee(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val totalAmount: Double,
    val totalMembers: Int = 12,
    val totalMonths: Int = 12,
    val startDate: String,
    val baseMonthlyInstallment: Double,
    val currentMonth: Int = 1,
    val hasTakenPot: Boolean = false,
    val potTakenMonth: Int? = null,
    val potTakenAmount: Double? = null,
    val remarks: String = "",
    val months: List<CommitteeMonth> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)
