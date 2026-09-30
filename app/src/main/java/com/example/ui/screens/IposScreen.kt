package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.components.ThemedDeleteDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Ipo
import com.example.util.Calculations
import java.util.Locale

@Composable
fun IposScreen(
    ipos: List<Ipo>,
    onAddIpo: () -> Unit,
    onEditIpo: (Ipo) -> Unit,
    onDeleteIpo: (Ipo) -> Unit,
    onMarkNotAllotted: (Ipo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf("All") }
    var ipoToDelete by remember { mutableStateOf<Ipo?>(null) }

    val statuses = listOf("All", "Applied", "Allotted/Listed", "Not Allotted")

    val sortedIpos = remember(ipos) {
        Calculations.sortIposRecentFirst(ipos)
    }

    val filteredIpos = remember(sortedIpos, selectedStatus) {
        val list = when (selectedStatus) {
            "All" -> sortedIpos
            "Allotted/Listed" -> sortedIpos.filter {
                it.status.equals("allotted", ignoreCase = true) || it.status.equals("listed", ignoreCase = true)
            }
            else -> sortedIpos.filter { it.status.equals(selectedStatus, ignoreCase = true) }
        }
        Calculations.sortIposRecentFirst(list)
    }

    // KPI Metrics calculation matching screenshot
    val activeIpos = remember(ipos) {
        ipos.filter { it.status.equals("applied", ignoreCase = true) }
    }
    val activeCount = activeIpos.size
    val totalCount = ipos.size
    val closedCount = totalCount - activeCount

    val blockedTotal = remember(activeIpos) {
        activeIpos.sumOf { ipo ->
            if (ipo.totalAmountBlocked > 0.0) ipo.totalAmountBlocked
            else (ipo.sharesPerLot * ipo.cutOffPrice * ipo.lotsApplied)
        }
    }

    val decidedIpos = remember(ipos) {
        ipos.filter {
            it.status.equals("allotted", ignoreCase = true) ||
                    it.status.equals("not allotted", ignoreCase = true) ||
                    it.status.equals("listed", ignoreCase = true)
        }
    }
    val wonIpos = remember(ipos) {
        ipos.filter {
            it.status.equals("allotted", ignoreCase = true) ||
                    it.status.equals("listed", ignoreCase = true)
        }
    }
    val decidedCount = decidedIpos.size
    val wonCount = wonIpos.size
    val allotmentSuccessRate = if (decidedCount > 0) (wonCount * 100) / decidedCount else 0

    val netRealizedGains = remember(ipos) {
        ipos.sumOf { ipo ->
            if (ipo.listingGainAmount != 0.0) {
                ipo.listingGainAmount
            } else if (ipo.status.equals("listed", ignoreCase = true) && ipo.listingPrice > 0.0) {
                val allottedLots = if (ipo.allottedLots > 0) ipo.allottedLots else ipo.lotsApplied
                (ipo.listingPrice - ipo.cutOffPrice) * ipo.sharesPerLot * allottedLots
            } else 0.0
        }
    }

    if (ipoToDelete != null) {
        val i = ipoToDelete!!
        ThemedDeleteDialog(
            title = "Delete IPO Application",
            subtitle = "Permanently remove this application entry",
            itemBadge = "IPO",
            itemName = i.companyName,
            itemSubtitle = "${i.category} • Status: ${i.status}",
            itemDetailChips = listOf(
                "Blocked Amount" to Calculations.formatCurrency(i.totalAmountBlocked),
                "Lots" to "${i.lotsApplied} Lot${if (i.lotsApplied > 1) "s" else ""}",
                "Apply Date" to if (i.applyDate.isNotBlank()) i.applyDate else "N/A"
            ),
            themeColor = Color(0xFFDB2777),
            themeGradient = listOf(Color(0xFFEC4899), Color(0xFFE11D48)),
            themeContainerColor = Color(0xFFFDF2F8),
            themeBorderColor = Color(0xFFFBCFE8),
            iconRes = R.drawable.ic_lucide_building,
            onDismiss = { ipoToDelete = null },
            onConfirmDelete = {
                ipoToDelete?.let { onDeleteIpo(it) }
                ipoToDelete = null
            },
            testTag = "delete_ipo_dialog"
        )
    }

    Box(modifier = modifier.fillMaxSize().testTag("ipos_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            // 2x2 Metric Cards Grid (Matching screenshot exactly)
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Row 1 of 2: Currently Blocked & Applications Tracked
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IpoMetricCard(
                        title = "Currently Blocked",
                        value = Calculations.formatCurrency(blockedTotal),
                        subtitle = "$activeCount Active of $totalCount IPOs",
                        subtitleColor = Color(0xFFD97706),
                        iconRes = R.drawable.ic_lucide_clock,
                        iconTint = Color(0xFFD97706),
                        iconBg = Color(0xFFFEF3C7),
                        modifier = Modifier.weight(1f)
                    )

                    IpoMetricCard(
                        title = "Applications Tracked",
                        value = "$totalCount IPOs",
                        subtitle = "$activeCount Active + $closedCount Closed",
                        subtitleColor = Color(0xFF9333EA),
                        iconRes = R.drawable.ic_lucide_building,
                        iconTint = Color(0xFF9333EA),
                        iconBg = Color(0xFFF3E8FF),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2 of 2: Allotment Success & Listing Gains
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IpoMetricCard(
                        title = "Allotment Success",
                        value = "$allotmentSuccessRate%",
                        valueColor = Color(0xFF059669),
                        subtitle = "$wonCount won of $decidedCount decided",
                        subtitleColor = Color(0xFF64748B),
                        iconRes = R.drawable.ic_lucide_sparkles,
                        iconTint = Color(0xFF0D9488),
                        iconBg = Color(0xFFCCFBF1),
                        modifier = Modifier.weight(1f)
                    )

                    val gainSign = if (netRealizedGains > 0) "+" else ""
                    IpoMetricCard(
                        title = "Listing Gains",
                        value = "$gainSign${Calculations.formatCurrency(netRealizedGains)}",
                        valueColor = if (netRealizedGains >= 0) Color(0xFF059669) else Color(0xFFDC2626),
                        subtitle = "Net realized profits",
                        subtitleColor = Color(0xFF64748B),
                        iconRes = R.drawable.ic_lucide_trending_up,
                        iconTint = Color(0xFF4F46E5),
                        iconBg = Color(0xFFE0E7FF),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Filter Pills (Evenly distributed across screen to avoid horizontal scrolling)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    statuses.forEach { st ->
                        val isSelected = selectedStatus == st
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(if (st == "Allotted/Listed") 1.25f else 1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedStatus = st }
                                .testTag("filter_ipo_$st")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = st,
                                    fontSize = if (st == "Allotted/Listed") 10.5.sp else 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            if (filteredIpos.isEmpty()) {
                item {
                    Text(
                        text = "No IPO applications found. Tap '+' to add an IPO.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            } else {
                items(filteredIpos, key = { it.id }) { ipo ->
                    IpoCard(
                        ipo = ipo,
                        onEdit = { onEditIpo(ipo) },
                        onDelete = { ipoToDelete = ipo },
                        onMarkNotAllotted = { onMarkNotAllotted(ipo) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}

@Composable
private fun IpoMetricCard(
    title: String,
    value: String,
    subtitle: String,
    subtitleColor: Color,
    iconRes: Int,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    valueColor: Color = Color(0xFF0F172A)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.52f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            // Top Row: Title (Single line, small letters) + Icon Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = if (title.length > 18) 10.5.sp else 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 18.5.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun IpoCard(
    ipo: Ipo,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkNotAllotted: () -> Unit
) {
    val isApplied = ipo.status.equals("Applied", ignoreCase = true)
    val isNotAllotted = ipo.status.equals("Not Allotted", ignoreCase = true)
    val isAllotted = ipo.status.equals("Allotted", ignoreCase = true)
    val isListed = ipo.status.equals("Listed", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ipo_card_${ipo.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.52f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Company name on Left, Status Pill on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ipo.companyName.ifBlank { "IPO Application" },
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                )

                // Fixed-size, non-wrapping status pill
                IpoStatusPill(status = ipo.status)
            }

            // Category (Mainboard/SME), Platform and Applicant Row
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mainboard / SME Badge ahead of Platform
                val isSme = ipo.category.equals("SME", ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSme) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                    border = BorderStroke(0.8.dp, if (isSme) Color(0xFFBFDBFE) else Color(0xFFCBD5E1))
                ) {
                    Text(
                        text = ipo.category.ifBlank { "Mainboard" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp,
                        color = if (isSme) Color(0xFF1D4ED8) else Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }

                Text(
                    text = "•",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1)
                )

                // Platform
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_building),
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ipo.platform.ifBlank { "Zerodha" },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Text(
                    text = "•",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1)
                )

                // Applicant
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_users),
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Applicant: ",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = ipo.applicantName.ifBlank { "Self" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            // Milestone Dates Timeline
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.45f),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.65f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step 1: Apply
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "APPLIED",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.4.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Calculations.normalizeDateToDdMmYyyy(ipo.applyDate).ifBlank { "—" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(22.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    // Step 2: Allotment
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ALLOTMENT",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.4.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Calculations.normalizeDateToDdMmYyyy(ipo.allotmentDate).ifBlank { "—" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF1E293B)
                        )
                    }

                    // Step 3: Listing or Refund
                    val step3Label = if (isNotAllotted && ipo.refundDate.isNotBlank()) "REFUND" else "LISTING"
                    val step3Date = if (isNotAllotted && ipo.refundDate.isNotBlank()) {
                        Calculations.normalizeDateToDdMmYyyy(ipo.refundDate)
                    } else {
                        Calculations.normalizeDateToDdMmYyyy(ipo.listingDate)
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(22.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = step3Label,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.4.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = step3Date.ifBlank { "—" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }

            // Financial & Bidding Details Box
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                val appliedLots = ipo.lotsApplied
                val appliedShares = appliedLots * ipo.sharesPerLot
                val cutOff = if (ipo.cutOffPrice % 1.0 == 0.0) ipo.cutOffPrice.toInt().toString() else ipo.cutOffPrice.toString()
                val appliedLotText = if (appliedLots == 1) "1 Lot" else "$appliedLots Lots"
                val biddingDetailsDesc = "$appliedLotText ($appliedShares shares @ ₹$cutOff)"

                val biddingAmount = if (ipo.totalAmountBlocked > 0.0) {
                    ipo.totalAmountBlocked
                } else {
                    appliedShares * ipo.cutOffPrice
                }

                val allottedLots = if (ipo.allottedLots > 0) ipo.allottedLots else appliedLots
                val allottedShares = allottedLots * ipo.sharesPerLot
                val allottedLotText = if (allottedLots == 1) "1 Lot" else "$allottedLots Lots"
                val allottedCost = allottedShares * ipo.cutOffPrice

                val listingPrice = ipo.listingPrice
                val sellingValue = when {
                    listingPrice > 0.0 -> allottedShares * listingPrice
                    ipo.listingGainAmount != 0.0 -> allottedCost + ipo.listingGainAmount
                    else -> allottedCost
                }

                val effectiveSellingPrice = when {
                    listingPrice > 0.0 -> listingPrice
                    allottedShares > 0 && ipo.listingGainAmount != 0.0 -> (allottedCost + ipo.listingGainAmount) / allottedShares
                    else -> ipo.cutOffPrice
                }
                val sellingPriceDisplay = if (effectiveSellingPrice % 1.0 == 0.0) {
                    effectiveSellingPrice.toInt().toString()
                } else if ((effectiveSellingPrice * 10) % 1.0 == 0.0) {
                    String.format(Locale.ENGLISH, "%.1f", effectiveSellingPrice)
                } else {
                    String.format(Locale.ENGLISH, "%.2f", effectiveSellingPrice)
                }
                val sellingDetailsDesc = "$allottedLotText ($allottedShares shares @ ₹$sellingPriceDisplay)"

                val realizedGain = if (ipo.listingGainAmount != 0.0) ipo.listingGainAmount else (sellingValue - allottedCost)
                val realizedGainPercent = if (ipo.listingGainPercent != 0.0) {
                    ipo.listingGainPercent
                } else if (allottedCost > 0.0) {
                    (realizedGain / allottedCost) * 100.0
                } else 0.0

                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    if (isListed) {
                        // 1. Bidding Details Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BIDDING DETAILS",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "INVESTED AMOUNT",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = biddingDetailsDesc,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155),
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Text(
                                text = Calculations.formatCurrency(biddingAmount),
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        // Divider between Bidding & Selling
                        Spacer(modifier = Modifier.height(9.dp))
                        HorizontalDivider(
                            color = Color(0xFFE2E8F0),
                            thickness = 0.8.dp
                        )
                        Spacer(modifier = Modifier.height(9.dp))

                        // 2. Selling / Listing Details Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SELLING / LISTING DETAILS",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = Color(0xFF0D9488)
                            )
                            Text(
                                text = "SELLING VALUE",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = if (realizedGain >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sellingDetailsDesc,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Text(
                                text = Calculations.formatCurrency(sellingValue),
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (realizedGain >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }

                        // Gains / Loss Badge
                        if (realizedGain != 0.0) {
                            Spacer(modifier = Modifier.height(7.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                val sign = if (realizedGain > 0) "+" else ""
                                val isProfitable = realizedGain >= 0
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isProfitable) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                    border = BorderStroke(0.8.dp, if (isProfitable) Color(0xFF86EFAC) else Color(0xFFFCA5A5))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(if (isProfitable) R.drawable.ic_lucide_trending_up else R.drawable.ic_lucide_arrows),
                                            contentDescription = null,
                                            tint = if (isProfitable) Color(0xFF059669) else Color(0xFFDC2626),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Gains: $sign${Calculations.formatCurrency(realizedGain)} (${String.format(Locale.ENGLISH, "%.1f", realizedGainPercent)}%)",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isProfitable) Color(0xFF059669) else Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (isAllotted && allottedLots < appliedLots) {
                        // Partial Allotment: show applied vs allotted
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BIDDING DETAILS",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "BLOCKED AMOUNT",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = biddingDetailsDesc,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155),
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Text(
                                text = Calculations.formatCurrency(biddingAmount),
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ALLOTMENT DETAILS",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = Color(0xFF7C3AED)
                            )
                            Text(
                                text = "ALLOTTED AMOUNT",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = Color(0xFF7C3AED)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$allottedLotText ($allottedShares shares @ ₹$cutOff)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Text(
                                text = Calculations.formatCurrency(allottedCost),
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED)
                            )
                        }
                    } else {
                        // Applied, Not Allotted, or Full Allotment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BIDDING DETAILS",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = when {
                                    isAllotted -> "ALLOTTED AMOUNT"
                                    isNotAllotted -> "REFUNDED AMOUNT"
                                    else -> "TOTAL AMOUNT BLOCKED"
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = biddingDetailsDesc,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )

                            val amountColor = when {
                                isAllotted -> Color(0xFF7C3AED)
                                isNotAllotted -> Color(0xFF64748B)
                                else -> Color(0xFF7C3AED)
                            }

                            Text(
                                text = Calculations.formatCurrency(if (isAllotted) allottedCost else biddingAmount),
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = amountColor
                            )
                        }
                    }
                }
            }

            // Remarks line (positioned below Financial & Bidding Details)
            if (ipo.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.45f),
                    border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "“",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ipo.remarks,
                            fontSize = 12.5.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // Action Buttons / Footer
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Action (Not Allotted) if Applied
                if (isApplied) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onMarkNotAllotted() }
                            .testTag("ipo_not_allotted_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_lucide_x_circle),
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF64748B)
                                        )
                                    ) {
                                        append("mark ")
                                    }
                                    withStyle(
                                        SpanStyle(
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF334155)
                                        )
                                    ) {
                                        append("Not Allotted")
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Edit & Delete Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onEdit)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onDelete)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IpoStatusPill(status: String) {
    val cleanStatus = status.trim()
    when {
        cleanStatus.equals("Applied", ignoreCase = true) -> {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF3C7),
                border = BorderStroke(1.dp, Color(0xFFFCD34D))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_clock),
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Applied",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        cleanStatus.equals("Allotted", ignoreCase = true) -> {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFDCFCE7),
                border = BorderStroke(1.dp, Color(0xFF86EFAC))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_check_circle),
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Allotted",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        cleanStatus.equals("Not Allotted", ignoreCase = true) -> {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_x_circle),
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Not Allotted",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        cleanStatus.equals("Listed", ignoreCase = true) -> {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEDE9FE),
                border = BorderStroke(1.dp, Color(0xFFDDD6FE))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_trending_up),
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Listed",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        else -> {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Text(
                    text = cleanStatus,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
