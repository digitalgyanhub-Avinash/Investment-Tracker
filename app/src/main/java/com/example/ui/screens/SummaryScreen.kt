package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.model.Committee
import com.example.data.model.Holding
import com.example.data.model.Ipo
import com.example.data.model.Sip
import com.example.ui.components.StatCard
import com.example.util.Calculations
import kotlin.math.max

private val CHART_COLORS = listOf(
    Color(0xFF10B981), // Emerald
    Color(0xFF3B82F6), // Blue
    Color(0xFFF59E0B), // Amber
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEC4899), // Pink
    Color(0xFF06B6D4), // Cyan
    Color(0xFF6366F1), // Indigo
    Color(0xFFF97316), // Orange
    Color(0xFF14B8A6), // Teal
    Color(0xFFEAB308)  // Yellow
)

@Composable
fun SummaryScreen(
    holdings: List<Holding>,
    sips: List<Sip>,
    ipos: List<Ipo>,
    committees: List<Committee>,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val holdingsTotal = remember(holdings) { Calculations.calculateHoldingsTotal(holdings) }
    val sipsTotal = remember(sips) { Calculations.calculateSipsTotal(sips) }
    val sipMonthly = remember(sips) { Calculations.calculateSipMonthlyCommitment(sips) }
    val ipoBlocked = remember(ipos) { Calculations.calculateIposBlockedTotal(ipos) }
    val committeesTotal = remember(committees) { Calculations.calculateCommitteesTotal(committees) }
    val committeeOutflow = remember(committees) { Calculations.calculateCommitteesMonthlyOutflow(committees) }

    val grandTotal = holdingsTotal + sipsTotal + committeesTotal + ipoBlocked
    val totalMonthlyOutflow = sipMonthly + committeeOutflow

    // Grouping by Platform across Holdings, SIPs, and active Committees
    val platformDistribution = remember(holdings, sips, committees) {
        val map = mutableMapOf<String, Double>()
        holdings.forEach { h ->
            map[h.platform] = (map[h.platform] ?: 0.0) + h.total
        }
        sips.forEach { s ->
            map[s.platform] = (map[s.platform] ?: 0.0) + Calculations.calculateSipInvested(s)
        }
        if (committees.isNotEmpty()) {
            val commTotal = Calculations.calculateCommitteesTotal(committees)
            if (commTotal > 0) {
                map["Committees"] = (map["Committees"] ?: 0.0) + commTotal
            }
        }
        map.toList().sortedByDescending { it.second }
    }

    // Grouping holdings by Category
    val categoryTotals = remember(holdings, sips, committees) {
        val map = mutableMapOf<String, Double>()
        holdings.forEach { h ->
            map[h.category] = (map[h.category] ?: 0.0) + h.total
        }
        sips.forEach { s ->
            val cat = when {
                s.instrument.contains("mutual", ignoreCase = true) || s.instrument.contains("fund", ignoreCase = true) -> "Mutual Fund"
                s.instrument.contains("gold", ignoreCase = true) -> "Gold"
                else -> "SIPs"
            }
            map[cat] = (map[cat] ?: 0.0) + Calculations.calculateSipInvested(s)
        }
        if (committees.isNotEmpty()) {
            map["Committees"] = (map["Committees"] ?: 0.0) + Calculations.calculateCommitteesTotal(committees)
        }
        map.toList().sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("summary_screen")
    ) {
        // Top Spacing
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Row 1: TOTAL NET WORTH & HOLDINGS LUMPSUM
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Net Worth",
                    value = Calculations.formatCurrency(grandTotal),
                    subtitle = "↗ Lumpsum + SIPs + Committees",
                    iconRes = R.drawable.ic_lucide_trending_up,
                    iconTint = Color(0xFF059669),
                    iconBackground = Color(0xFFD1FAE5),
                    subtitleColor = Color(0xFF059669),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(0) }
                )
                StatCard(
                    title = "Holdings Lumpsum",
                    value = Calculations.formatCurrency(holdingsTotal),
                    subtitle = "${holdings.size} Active Records",
                    iconRes = R.drawable.ic_lucide_pie_chart,
                    iconTint = Color(0xFF2563EB),
                    iconBackground = Color(0xFFDBEAFE),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(1) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Row 2: SIP INVESTED VALUE & COMMITTEES NET TOTAL
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "SIP Invested Value",
                    value = Calculations.formatCurrency(sipsTotal),
                    subtitle = "${Calculations.formatCurrency(sipMonthly)}/mo outflow",
                    iconRes = R.drawable.ic_lucide_arrows,
                    iconTint = Color(0xFF4F46E5),
                    iconBackground = Color(0xFFE0E7FF),
                    valueColor = Color(0xFF4F46E5),
                    subtitleColor = Color(0xFF4F46E5),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(2) }
                )
                StatCard(
                    title = "Committees Net Total",
                    value = Calculations.formatCurrency(committeesTotal),
                    subtitle = "${committees.count { !Calculations.isCommitteeCompleted(it) }} Active Groups",
                    iconRes = R.drawable.ic_lucide_users,
                    iconTint = Color(0xFF9333EA),
                    iconBackground = Color(0xFFF3E8FF),
                    valueColor = Color(0xFF9333EA),
                    subtitleColor = Color(0xFF9333EA),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(4) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Row 3: IPOS APPLIED & MONTHLY OUTFLOW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "IPOs Applied",
                    value = Calculations.formatCurrency(ipoBlocked),
                    subtitle = "${ipos.count { it.status.equals("Applied", ignoreCase = true) }} applied of ${ipos.size} IPOs",
                    iconRes = R.drawable.ic_lucide_building,
                    iconTint = Color(0xFFE11D48),
                    iconBackground = Color(0xFFFFE4E6),
                    valueColor = Color(0xFFE11D48),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(3) }
                )
                StatCard(
                    title = "Monthly Outflow",
                    value = Calculations.formatCurrency(totalMonthlyOutflow),
                    subtitle = "Monthly Commitment",
                    iconRes = R.drawable.ic_lucide_zap,
                    iconTint = Color(0xFFD97706),
                    iconBackground = Color(0xFFFEF3C7),
                    valueColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(2) }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // SECTION 2: Allocation by Platform Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("allocation_by_platform_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.52f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header: Title + Pill Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Allocation by Platform",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.60f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Box View (${platformDistribution.size} Platforms)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Canvas Bar Chart
                    if (platformDistribution.isNotEmpty()) {
                        val maxVal = max(1.0, platformDistribution.maxOf { it.second })
                        val roundedMax = ((maxVal / 20000.0).toInt() + 1) * 20000.0

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(185.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val leftPadding = 48.dp.toPx()
                                val bottomPadding = 48.dp.toPx()
                                val chartWidth = w - leftPadding
                                val chartHeight = h - bottomPadding

                                // Y-axis steps: 4 intervals
                                val steps = 4
                                val stepVal = roundedMax / steps
                                val textPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#64748B")
                                    textSize = 10.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.RIGHT
                                    isAntiAlias = true
                                }
                                val labelPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#334155")
                                    textSize = 10.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.RIGHT
                                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                    isAntiAlias = true
                                }

                                for (i in 0..steps) {
                                    val y = chartHeight - (i * chartHeight / steps)
                                    val labelNum = (i * stepVal / 1000.0).toInt()
                                    val yLabel = "₹${labelNum}k"

                                    // Draw grid line
                                    drawLine(
                                        color = Color(0xFFE2E8F0),
                                        start = Offset(leftPadding, y),
                                        end = Offset(w, y),
                                        strokeWidth = 1f
                                    )
                                    // Draw Y-axis label text
                                    drawContext.canvas.nativeCanvas.drawText(
                                        yLabel,
                                        leftPadding - 8f,
                                        y + 4.dp.toPx(),
                                        textPaint
                                    )
                                }

                                // Draw vertical bars
                                val barCount = platformDistribution.size
                                val totalSlotWidth = chartWidth / barCount
                                val barWidth = (totalSlotWidth * 0.55f).coerceAtMost(36.dp.toPx())

                                platformDistribution.forEachIndexed { index, pair ->
                                    val barHeight = ((pair.second / roundedMax) * chartHeight).toFloat().coerceAtLeast(4f)
                                    val slotCenterX = leftPadding + (index * totalSlotWidth) + (totalSlotWidth / 2f)
                                    val barLeft = slotCenterX - (barWidth / 2f)
                                    val barTop = chartHeight - barHeight
                                    val barColor = CHART_COLORS[index % CHART_COLORS.size]

                                    // Bar with rounded top corners
                                    drawRoundRect(
                                        color = barColor,
                                        topLeft = Offset(barLeft, barTop),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )

                                    // Platform name label below bar tilted from bottom to top
                                    val displayLabel = if (pair.first.length > 12) pair.first.take(11) + "…" else pair.first
                                    val labelAnchorX = slotCenterX + (barWidth * 0.25f)
                                    val labelAnchorY = chartHeight + 10.dp.toPx()

                                    drawContext.canvas.nativeCanvas.save()
                                    drawContext.canvas.nativeCanvas.rotate(-45f, labelAnchorX, labelAnchorY)
                                    drawContext.canvas.nativeCanvas.drawText(
                                        displayLabel,
                                        labelAnchorX,
                                        labelAnchorY,
                                        labelPaint
                                    )
                                    drawContext.canvas.nativeCanvas.restore()
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2-Column Grid of Platform Progress Items
                    val chunkedPlatforms = platformDistribution.chunked(2)
                    chunkedPlatforms.forEach { rowPairs ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowPairs.forEachIndexed { _, (platformName, amount) ->
                                val pct = if (grandTotal > 0) (amount / grandTotal) * 100 else 0.0
                                val colorIndex = platformDistribution.indexOfFirst { it.first == platformName }
                                val barColor = CHART_COLORS[if (colorIndex >= 0) colorIndex % CHART_COLORS.size else 0]

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = platformName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = String.format("%.1f%%", pct),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(50)),
                                            color = barColor,
                                            trackColor = Color(0xFFE2E8F0)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = Calculations.formatCurrency(amount),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF334155)
                                        )
                                    }
                                }
                            }
                            if (rowPairs.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // SECTION 3: Category Allocation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_allocation_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.52f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Asset Class Allocation",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.60f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${categoryTotals.size} Categories",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    categoryTotals.forEachIndexed { index, (category, amount) ->
                        val pct = if (grandTotal > 0) (amount / grandTotal) * 100 else 0.0
                        val barColor = CHART_COLORS[index % CHART_COLORS.size]

                        Column(modifier = Modifier.padding(vertical = 5.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(barColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = category,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = Calculations.formatCurrency(amount),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = String.format("%.1f%%", pct),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = barColor,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}
