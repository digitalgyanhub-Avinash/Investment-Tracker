package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "ipos")
data class Ipo(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val companyName: String,
    val category: String = "Mainboard", // Mainboard, SME
    val platform: String,
    val applicantName: String = "Self",
    val sharesPerLot: Int = 1,
    val cutOffPrice: Double = 0.0,
    val lotsApplied: Int = 1,
    val totalAmountBlocked: Double = 0.0,
    val applyDate: String = "",
    val allotmentDate: String = "",
    val refundDate: String = "",
    val listingDate: String = "",
    val status: String = "Applied", // Applied, Allotted, Not Allotted, Listed
    val allottedLots: Int = 0,
    val listingPrice: Double = 0.0,
    val listingGainAmount: Double = 0.0,
    val listingGainPercent: Double = 0.0,
    val remarks: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
