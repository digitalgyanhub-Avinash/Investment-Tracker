package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Sip
import com.example.util.Calculations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SipsScreen(
    sips: List<Sip>,
    onAddSip: () -> Unit,
    onEditSip: (Sip) -> Unit,
    onDeleteSip: (Sip) -> Unit,
    onTogglePauseSip: (Sip) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFreq by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") } // "All", "Active", "Paused"
    var sipToDelete by remember { mutableStateOf<Sip?>(null) }

    val frequencies = listOf("All", "Daily", "Weekly", "Monthly", "Yearly")

    val currentDateFormatted = remember {
        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
    }

    val sortedSips = remember(sips) {
        Calculations.sortSipsRecentFirst(sips)
    }

    val activeSipsCount = remember(sips) { sips.count { !it.isPaused } }
    val pausedSipsCount = remember(sips) { sips.count { it.isPaused } }

    val filteredSips = remember(sortedSips, selectedFreq, selectedStatus) {
        val list = sortedSips.filter { sip ->
            val matchFreq = if (selectedFreq == "All") true else sip.frequency.equals(selectedFreq, ignoreCase = true)
            val matchStatus = when (selectedStatus) {
                "Active" -> !sip.isPaused
                "Paused" -> sip.isPaused
                else -> true
            }
            matchFreq && matchStatus
        }
        Calculations.sortSipsRecentFirst(list)
    }

    val totalInvested = remember(filteredSips) {
        Calculations.calculateSipsTotal(filteredSips)
    }

    // Grouping sips by Platform with recent-first ordering for both platforms and entries
    val groupedSips = remember(filteredSips) {
        val platformOrder = mutableListOf<String>()
        val map = mutableMapOf<String, MutableList<Sip>>()
        filteredSips.forEach { sip ->
            val p = sip.platform.ifBlank { "Other" }
            if (!platformOrder.contains(p)) {
                platformOrder.add(p)
            }
            map.getOrPut(p) { mutableListOf() }.add(sip)
        }
        platformOrder.associateWith { p ->
            Calculations.sortSipsRecentFirst(map[p] ?: emptyList())
        }
    }

    if (sipToDelete != null) {
        val s = sipToDelete!!
        ThemedDeleteDialog(
            title = "Delete SIP Plan",
            subtitle = "Permanently remove this recurring investment",
            itemBadge = "SIP",
            itemName = s.instrument,
            itemSubtitle = if (s.remarks.isNotBlank()) "${s.platform} • ${s.remarks}" else s.platform,
            itemDetailChips = listOf(
                "Installment" to Calculations.formatCurrency(s.amount),
                "Frequency" to s.frequency,
                "Status" to if (s.isPaused) "Paused" else "Active",
                "Since" to if (s.since.isNotBlank()) s.since else "Active"
            ),
            themeColor = Color(0xFFD97706),
            themeGradient = listOf(Color(0xFFF59E0B), Color(0xFFEA580C)),
            themeContainerColor = Color(0xFFFFFBEB),
            themeBorderColor = Color(0xFFFDE68A),
            iconRes = R.drawable.ic_lucide_arrows,
            onDismiss = { sipToDelete = null },
            onConfirmDelete = {
                sipToDelete?.let { onDeleteSip(it) }
                sipToDelete = null
            },
            testTag = "delete_sip_dialog"
        )
    }

    Box(modifier = modifier.fillMaxSize().testTag("sips_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            // Header stats (Matches screenshot with mint green icon, Active SIPs count, date badge, and Total Invested Till Date)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.52f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mint green rounded square icon box
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFD1FAE5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_lucide_trending_up),
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active SIPs ($activeSipsCount)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )

                                if (pausedSipsCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFEF3C7),
                                        border = BorderStroke(0.8.dp, Color(0xFFFDE68A))
                                    ) {
                                        Text(
                                            text = "$pausedSipsCount paused",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.60f),
                                    border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.80f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_lucide_calendar),
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = currentDateFormatted,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Total Invested Till Date: ",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = Calculations.formatCurrency(totalInvested),
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEA580C)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Frequency Filter Pills (Compact, fitted across screen to avoid horizontal scrolling)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    frequencies.forEach { freq ->
                        val isSelected = selectedFreq == freq
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedFreq = freq }
                                .testTag("filter_freq_$freq")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = freq,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status Filter Pills (All / Active / Paused)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "All" to "All (${sips.size})",
                        "Active" to "Active ($activeSipsCount)",
                        "Paused" to "Paused ($pausedSipsCount)"
                    ).forEach { (statusKey, statusLabel) ->
                        val isSelected = selectedStatus == statusKey
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) {
                                if (statusKey == "Paused") Color(0xFFFEF3C7)
                                else MaterialTheme.colorScheme.primaryContainer
                            } else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) {
                                    if (statusKey == "Paused") Color(0xFFF59E0B)
                                    else MaterialTheme.colorScheme.primary
                                } else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedStatus = statusKey }
                                .testTag("filter_status_$statusKey")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = statusLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        if (statusKey == "Paused") Color(0xFFB45309)
                                        else MaterialTheme.colorScheme.onPrimaryContainer
                                    } else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (filteredSips.isEmpty()) {
                item {
                    Text(
                        text = if (selectedStatus == "Paused") "No paused systematic plans found."
                        else if (selectedStatus == "Active") "No active systematic plans found."
                        else "No systematic investment plans found. Tap '+' to create one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            } else {
                // Render Grouped Platform Cards (Matching screenshot)
                items(groupedSips.keys.toList(), key = { it }) { platformName ->
                    val groupItems = groupedSips[platformName] ?: emptyList()
                    val platformInvestedTotal = groupItems.sumOf { Calculations.calculateSipInvested(it) }

                    SipPlatformGroupCard(
                        platformName = platformName,
                        sips = groupItems,
                        platformInvestedTotal = platformInvestedTotal,
                        onEdit = onEditSip,
                        onDelete = { sipToDelete = it },
                        onTogglePause = onTogglePauseSip
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}

@Composable
private fun SipPlatformGroupCard(
    platformName: String,
    sips: List<Sip>,
    platformInvestedTotal: Double,
    onEdit: (Sip) -> Unit,
    onDelete: (Sip) -> Unit,
    onTogglePause: (Sip) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sip_platform_group_${platformName.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.52f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Box: Glassmorphic translucent header matching IPOs screen sub-boxes
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.45f),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.65f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = platformName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${sips.size} ${if (sips.size == 1) "entry" else "entries"}",
                            fontSize = 13.5.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = "  •  ",
                            fontSize = 13.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = Calculations.formatCurrency(platformInvestedTotal),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SIP Entries within this platform
            sips.forEachIndexed { index, sip ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        thickness = 0.8.dp,
                        color = Color(0xFFF8FAFC)
                    )
                }

                val invested = remember(sip) { Calculations.calculateSipInvested(sip) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sip_item_${sip.id}")
                ) {
                    // Line 1: Instrument Name, Till Date & INVESTED Header + Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sip.instrument.ifBlank { "SIP" },
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sip.isPaused) Color(0xFF64748B) else Color(0xFF0F172A),
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (sip.since.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "Since: ",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF94A3B8),
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = Calculations.normalizeDateToDdMmYyyy(sip.since),
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF64748B),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "INVESTED",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 0.4.sp
                            )
                            Text(
                                text = Calculations.formatCurrency(invested),
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    // Line 2: Amount /Frequency (e.g. ₹6,500 /Monthly)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${Calculations.formatCurrency(sip.amount)} /${sip.frequency.replaceFirstChar { it.uppercase() }}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (sip.isPaused) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                        if (sip.isPaused) {
                            Text(
                                text = " • Installment deduction paused",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFFD97706)
                            )
                        }
                    }

                    // Line 4: Remarks (if present)
                    if (sip.remarks.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${sip.remarks}\"",
                            fontSize = 12.5.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Line 5: Read-only Status badge on left, Actions (Edit, Del) on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Read-only Status Chip (Non-clickable)
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = if (sip.isPaused) Color(0xFFFEF3C7) else Color(0xFFECFDF5),
                            border = BorderStroke(0.8.dp, if (sip.isPaused) Color(0xFFFDE68A) else Color(0xFFA7F3D0)),
                            modifier = Modifier.testTag("sip_status_badge_${sip.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = if (sip.isPaused) Color(0xFFD97706) else Color(0xFF059669),
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.5.dp))
                                Text(
                                    text = if (sip.isPaused) "Paused" else "Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (sip.isPaused) Color(0xFFB45309) else Color(0xFF047857)
                                )
                            }
                        }

                        // Action Buttons: Edit and Delete
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Edit Icon Button
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .clickable { onEdit(sip) }
                                    .testTag("sip_edit_${sip.id}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(13.5.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Del Icon Button
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .clickable { onDelete(sip) }
                                    .testTag("sip_delete_${sip.id}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(13.5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
