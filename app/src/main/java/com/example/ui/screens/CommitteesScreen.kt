package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Committee
import com.example.util.Calculations
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun CommitteesScreen(
    committees: List<Committee>,
    onAddCommittee: () -> Unit,
    onEditCommittee: (Committee) -> Unit,
    onDeleteCommittee: (Committee) -> Unit,
    onLogBid: (Committee, Int) -> Unit,
    onTogglePaid: (Committee, Int) -> Unit,
    onDeleteMonth: ((Committee, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var committeeToDelete by remember { mutableStateOf<Committee?>(null) }
    var monthToDelete by remember { mutableStateOf<Triple<Committee, Int, String>?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }

    val activeCommittees = remember(committees) {
        committees.filter { !Calculations.isCommitteeCompleted(it) }
    }
    val doneCommittees = remember(committees) {
        committees.filter { Calculations.isCommitteeCompleted(it) }
    }

    val totalInvested = remember(committees) {
        Calculations.calculateCommitteesTotal(committees)
    }

    val totalLatestPaid = remember(activeCommittees) {
        activeCommittees.sumOf { comm ->
            val sorted = comm.months.sortedBy { it.monthNumber }
            val latestPaid = sorted.filter { it.isPaid }.lastOrNull() ?: sorted.lastOrNull()
            if (latestPaid != null) {
                if (latestPaid.installmentPerPerson > 0) latestPaid.installmentPerPerson
                else {
                    val winnerTakes = if (latestPaid.netPotValue > 0) latestPaid.netPotValue
                        else maxOf(0.0, comm.totalAmount - latestPaid.winningBid)
                    if (comm.totalMembers > 0) winnerTakes / comm.totalMembers
                    else if (comm.baseMonthlyInstallment > 0) comm.baseMonthlyInstallment
                    else comm.totalAmount / max(1, comm.totalMembers)
                }
            } else if (comm.baseMonthlyInstallment > 0) comm.baseMonthlyInstallment
            else comm.totalAmount / max(1, comm.totalMembers)
        }
    }

    val latestActiveMonthLabel = remember(activeCommittees) {
        activeCommittees.mapNotNull { comm ->
            val sorted = comm.months.sortedBy { it.monthNumber }
            val latestPaid = sorted.filter { it.isPaid }.lastOrNull() ?: sorted.lastOrNull()
            if (latestPaid != null) {
                Calculations.getCommitteeMonthLabel(
                    comm.startDate,
                    latestPaid.monthNumber,
                    latestPaid.monthLabel
                )
            } else null
        }.firstOrNull { it.isNotBlank() } ?: "Current month"
    }

    val totalPotDrawn = remember(committees) {
        committees.filter { it.hasTakenPot }.sumOf { it.potTakenAmount ?: 0.0 }
    }
    val potDrawnCount = remember(committees) {
        committees.count { it.hasTakenPot }
    }

    val sortedCommittees = remember(committees) {
        val active = Calculations.sortCommitteesRecentFirst(
            committees.filter { !Calculations.isCommitteeCompleted(it) }
        )
        val completed = Calculations.sortCommitteesRecentFirst(
            committees.filter { Calculations.isCommitteeCompleted(it) }
        )
        active + completed
    }

    val filteredCommittees = remember(sortedCommittees, selectedFilter) {
        when (selectedFilter) {
            "Active" -> sortedCommittees.filter { !Calculations.isCommitteeCompleted(it) }
            "Completed" -> sortedCommittees.filter { Calculations.isCommitteeCompleted(it) }
            "Pot Drawn" -> sortedCommittees.filter { it.hasTakenPot }
            else -> sortedCommittees
        }
    }

    if (committeeToDelete != null) {
        val c = committeeToDelete!!
        ThemedDeleteDialog(
            title = "Delete Committee",
            subtitle = "Permanently remove this committee and all auction records",
            itemBadge = "COMMITTEE",
            itemName = c.name,
            itemSubtitle = "${c.totalMembers} Members • ${if (Calculations.isCommitteeCompleted(c)) "Completed" else "Active"}",
            itemDetailChips = listOf(
                "Total Pot" to Calculations.formatCurrency(c.totalAmount),
                "Base Installment" to "${Calculations.formatCurrency(c.baseMonthlyInstallment)}/mo",
                "Duration" to "${c.totalMonths} Months"
            ),
            themeColor = Color(0xFF7C3AED),
            themeGradient = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)),
            themeContainerColor = Color(0xFFF5F3FF),
            themeBorderColor = Color(0xFFDDD6FE),
            iconRes = R.drawable.ic_lucide_users,
            onDismiss = { committeeToDelete = null },
            onConfirmDelete = {
                committeeToDelete?.let { onDeleteCommittee(it) }
                committeeToDelete = null
            },
            testTag = "delete_committee_dialog"
        )
    }

    if (monthToDelete != null) {
        val (comm, mNum, mLabel) = monthToDelete!!
        ThemedDeleteDialog(
            title = "Delete Auction Bid",
            subtitle = "Reset winning bid and dividend calculations for this month",
            itemBadge = "AUCTION BID",
            itemName = "$mLabel (Month $mNum)",
            itemSubtitle = comm.name,
            itemDetailChips = listOf(
                "Committee" to comm.name,
                "Month" to "Month $mNum of ${comm.totalMonths}"
            ),
            themeColor = Color(0xFF7C3AED),
            themeGradient = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)),
            themeContainerColor = Color(0xFFF5F3FF),
            themeBorderColor = Color(0xFFDDD6FE),
            iconRes = R.drawable.ic_lucide_users,
            onDismiss = { monthToDelete = null },
            onConfirmDelete = {
                onDeleteMonth?.invoke(comm, mNum)
                monthToDelete = null
            },
            testTag = "delete_auction_bid_dialog"
        )
    }

    Box(modifier = modifier.fillMaxSize().testTag("committees_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            // Top Metrics: 2x2 Clean Balanced Grid
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Row 1 of 2: Total Invested & Committees Pool
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CommitteeMetricCard(
                        title = "Total Invested",
                        value = Calculations.formatCurrency(totalInvested),
                        valueColor = Color(0xFF4F46E5),
                        subtitle = "${committees.size} Total Chit Funds",
                        subtitleColor = Color(0xFF64748B),
                        iconRes = R.drawable.ic_lucide_wallet,
                        iconTint = Color(0xFF4F46E5),
                        iconBg = Color(0xFFEEF2FF),
                        modifier = Modifier.weight(1f)
                    )

                    CommitteeMetricCard(
                        title = "Committees Pool",
                        value = "${activeCommittees.size} Active",
                        valueColor = Color(0xFF0F172A),
                        subtitle = "${doneCommittees.size} Completed",
                        subtitleColor = Color(0xFF059669),
                        iconRes = R.drawable.ic_lucide_users,
                        iconTint = Color(0xFF7C3AED),
                        iconBg = Color(0xFFF3E8FF),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2 of 2: Monthly Due & Pot Drawn
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CommitteeMetricCard(
                        title = "Monthly Due",
                        value = Calculations.formatCurrency(totalLatestPaid),
                        valueColor = Color(0xFFD97706),
                        subtitle = if (activeCommittees.isNotEmpty()) latestActiveMonthLabel else "All settled",
                        subtitleColor = Color(0xFFD97706),
                        iconRes = R.drawable.ic_lucide_clock,
                        iconTint = Color(0xFFD97706),
                        iconBg = Color(0xFFFEF3C7),
                        modifier = Modifier.weight(1f)
                    )

                    CommitteeMetricCard(
                        title = "Pot Drawn",
                        value = if (totalPotDrawn > 0) Calculations.formatCurrency(totalPotDrawn) else "₹0",
                        valueColor = Color(0xFF059669),
                        subtitle = "$potDrawnCount of ${committees.size} taken",
                        subtitleColor = Color(0xFF64748B),
                        iconRes = R.drawable.ic_lucide_award,
                        iconTint = Color(0xFF059669),
                        iconBg = Color(0xFFD1FAE5),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Pills (Evenly distributed across the full row without empty space)
                val filterOptions = listOf("All", "Active", "Completed", "Pot Drawn")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filterOptions.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        val weight = when (filter) {
                            "All" -> 0.8f
                            "Active" -> 1.0f
                            "Completed" -> 1.15f
                            "Pot Drawn" -> 1.15f
                            else -> 1.0f
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(weight)
                                .height(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedFilter = filter }
                                .testTag("filter_committee_$filter")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (committees.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.52f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF2FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_lucide_users),
                                    contentDescription = null,
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Committees Tracked",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Track chit funds, monthly installments, auction bids, and pot payouts easily.",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else if (filteredCommittees.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.52f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "No $selectedFilter committees found.",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(20.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredCommittees, key = { it.id }) { committee ->
                    CommitteeItemCard(
                        committee = committee,
                        onEdit = { onEditCommittee(committee) },
                        onDelete = { committeeToDelete = committee },
                        onLogBid = { targetMonth -> onLogBid(committee, targetMonth) },
                        onTogglePaid = { mNum -> onTogglePaid(committee, mNum) },
                        onDeleteMonth = { mNum ->
                            val label = Calculations.getCommitteeMonthLabel(
                                committee.startDate,
                                mNum,
                                committee.months.find { it.monthNumber == mNum }?.monthLabel ?: ""
                            )
                            monthToDelete = Triple(committee, mNum, label)
                        }
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
private fun CommitteeMetricCard(
    title: String,
    value: String,
    valueColor: Color = Color(0xFF0F172A),
    subtitle: String,
    subtitleColor: Color = Color(0xFF64748B),
    iconRes: Int,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.52f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(13.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 17.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CommitteeItemCard(
    committee: Committee,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLogBid: (Int) -> Unit,
    onTogglePaid: (Int) -> Unit,
    onDeleteMonth: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )

    val isCompleted = remember(committee) { Calculations.isCommitteeCompleted(committee) }
    val invested = remember(committee) { Calculations.calculateCommitteeInvestedTillDate(committee) }

    // Sort months chronologically by monthNumber as shown in the Auction Ledger
    val sortedLedgerMonths = remember(committee.months) {
        committee.months.sortedBy { it.monthNumber }
    }
    val paidLedgerMonths = remember(sortedLedgerMonths) {
        sortedLedgerMonths.filter { it.isPaid }
    }
    val hasAnyPaid = paidLedgerMonths.isNotEmpty()
    // Find latest month paid in the auction ledger; fallback to latest month if none marked paid
    val latestPaidMonth = remember(paidLedgerMonths, sortedLedgerMonths) {
        paidLedgerMonths.lastOrNull() ?: sortedLedgerMonths.lastOrNull()
    }
    val latestMonthPaidAmount = remember(committee, isCompleted, latestPaidMonth, hasAnyPaid) {
        if (isCompleted) {
            0.0
        } else if (latestPaidMonth != null) {
            if (latestPaidMonth.installmentPerPerson > 0) {
                latestPaidMonth.installmentPerPerson
            } else {
                val winnerTakes = if (latestPaidMonth.netPotValue > 0) latestPaidMonth.netPotValue
                    else maxOf(0.0, committee.totalAmount - latestPaidMonth.winningBid)
                if (committee.totalMembers > 0) winnerTakes / committee.totalMembers
                else if (committee.baseMonthlyInstallment > 0) committee.baseMonthlyInstallment
                else committee.totalAmount / max(1, committee.totalMembers)
            }
        } else {
            if (committee.baseMonthlyInstallment > 0) committee.baseMonthlyInstallment
            else committee.totalAmount / max(1, committee.totalMembers)
        }
    }
    // Taken directly from the MONTH column of the Auction Ledger by latest month paid
    val latestMonthLabel = remember(committee.startDate, latestPaidMonth, hasAnyPaid) {
        if (latestPaidMonth != null) {
            val label = Calculations.getCommitteeMonthLabel(
                startDateStr = committee.startDate,
                monthNumber = latestPaidMonth.monthNumber,
                fallbackLabel = latestPaidMonth.monthLabel
            )
            if (!hasAnyPaid) "$label (Pending)" else label
        } else ""
    }

    val totalMonths = if (committee.totalMonths > 0) committee.totalMonths else 12
    val paidMonthsCount = remember(committee) {
        if (committee.months.isNotEmpty()) committee.months.count { it.isPaid } else committee.currentMonth
    }
    val progress = (paidMonthsCount.toFloat() / totalMonths).coerceIn(0f, 1f)

    val baseMonthly = remember(committee) {
        if (committee.baseMonthlyInstallment > 0) {
            committee.baseMonthlyInstallment
        } else {
            committee.totalAmount / max(1, totalMonths)
        }
    }

    val (displayName, maintainer) = remember(committee.name) {
        if (committee.name.contains("(Maintained by", ignoreCase = true)) {
            val name = committee.name.substringBefore("(Maintained by").trim()
            val org = committee.name.substringAfter("(Maintained by")
                .substringBefore(")")
                .trim()
            Pair(name, org)
        } else {
            Pair(committee.name, "")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("committee_item_${committee.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.52f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Leading Icon + Title & Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (isCompleted) Color(0xFFD1FAE5) else Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_users),
                        contentDescription = null,
                        tint = if (isCompleted) Color(0xFF059669) else Color(0xFF6366F1),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(11.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName.ifBlank { "Chit Fund Committee" },
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (maintainer.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Organizer: $maintainer",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Status & Pot Badges Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDCFCE7),
                                border = BorderStroke(0.8.dp, Color(0xFF86EFAC))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Completed",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF059669)
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEDE9FE),
                                border = BorderStroke(0.8.dp, Color(0xFFDDD6FE))
                            ) {
                                Text(
                                    text = "Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C3AED),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                )
                            }
                        }

                        // Total Pot Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(0.8.dp, Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = "${Calculations.formatCurrency(committee.totalAmount)} Pot",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                            )
                        }
                    }
                }
            }

            // Pot Taken Highlight Banner (Visible whether active or completed!)
            if (committee.hasTakenPot) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFECFDF5).copy(alpha = 0.60f),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0).copy(alpha = 0.80f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_award),
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pot Drawn: ${Calculations.formatCurrency(committee.potTakenAmount ?: 0.0)}${if (committee.potTakenMonth != null) " (Month ${committee.potTakenMonth})" else ""}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        val potTotal = committee.totalAmount
                        val potAmount = committee.potTakenAmount ?: 0.0
                        if (isCompleted && potAmount > 0) {
                            val profitLoss = potAmount - invested
                            val isProfit = profitLoss >= 0
                            val absAmount = Calculations.formatCurrency(Math.abs(profitLoss)).replace("₹", "").trim()
                            val text = if (isProfit) "Profit: ₹+$absAmount" else "Loss: ₹-$absAmount"
                            val textColor = if (isProfit) Color(0xFF059669) else Color(0xFFDC2626)
                            Text(
                                text = text,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        } else if (potTotal > 0 && potAmount > 0) {
                            val discount = maxOf(0.0, potTotal - potAmount)
                            Text(
                                text = "Bid: ${Calculations.formatCurrency(discount)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }

            // Metadata Strip: Members, Base installment, Start date
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Members count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lucide_users),
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${committee.totalMembers} Members",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                }

                Text("•", fontSize = 11.sp, color = Color(0xFFCBD5E1))

                // Base Installment
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Base: ",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "${Calculations.formatCurrency(baseMonthly)}/mo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }

                if (committee.startDate.isNotBlank()) {
                    Text("•", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_calendar),
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Calculations.normalizeDateToDdMmYyyy(committee.startDate),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // Progress Bar Section
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIMELINE PROGRESS",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isCompleted) "Completed ($totalMonths/$totalMonths Mos)"
                        else "$paidMonthsCount of $totalMonths Mos (${(progress * 100).roundToInt()}%)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color(0xFF059669) else Color(0xFF4F46E5)
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress = { if (isCompleted) 1f else progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isCompleted) Color(0xFF059669) else Color(0xFF6366F1),
                    trackColor = if (isCompleted) Color(0xFFD1FAE5) else Color(0xFFE2E8F0)
                )
            }

            // Bottom Financial & Action Row: Matched to user screenshot with small letter labels
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.50f),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.70f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: Invested till date
                    Column {
                        Text(
                            text = "Invested till\ndate",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Calculations.formatCurrency(invested),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF4338CA)
                        )
                    }

                    // Column 2: Latest month paid
                    Column {
                        Text(
                            text = if (isCompleted) "Status\n " else "Latest month\npaid",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isCompleted) "Settled" else Calculations.formatCurrency(latestMonthPaidAmount),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCompleted) Color(0xFF059669) else Color(0xFF0F172A)
                        )
                        if (isCompleted) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Completed",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        } else if (latestMonthLabel.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = latestMonthLabel,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4338CA)
                            )
                        }
                    }

                    // Column 3: Action buttons (Edit & Delete) stacked vertically, next to Expand/Collapse Chevron
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Action buttons stacked vertically (larger, filling vertical box height)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Edit Icon Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(35.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onEdit() }
                                    .testTag("edit_committee_${committee.id}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Committee",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(16.5.dp)
                                    )
                                }
                            }

                            // Delete Icon Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(35.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onDelete() }
                                    .testTag("delete_committee_${committee.id}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Committee",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.5.dp)
                                    )
                                }
                            }
                        }

                        // Expand / Collapse Chevron Button (76.dp height matching vertical action buttons: 35 + 6 + 35)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isExpanded) Color(0xFF7C3AED) else Color(0xFFEDE9FE),
                            border = BorderStroke(1.dp, if (isExpanded) Color(0xFF6D28D9) else Color(0xFFDDD6FE)),
                            modifier = Modifier
                                .width(36.dp)
                                .height(76.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isExpanded = !isExpanded }
                                .testTag("expand_committee_${committee.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = if (isExpanded) Color.White else Color(0xFF7C3AED),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(arrowRotation)
                                )
                            }
                        }
                    }
                }
            }

            // Expandable Auction Ledger & Month-by-Month Log
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Remarks Section: Hidden in card, visible inside dropdown
                    if (committee.remarks.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.50f),
                            border = BorderStroke(0.8.dp, Color(0xFFE2E8F0)),
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
                                    text = committee.remarks,
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Ledger Header with Log Bid action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AUCTION LEDGER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEEF2FF)
                            ) {
                                Text(
                                    text = "${committee.months.size} Mos",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F46E5),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                )
                            }
                        }

                        val nextMonthNum = (committee.months.maxOfOrNull { it.monthNumber } ?: 0) + 1
                        val canLogMore = nextMonthNum <= totalMonths

                        if (canLogMore) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEEF2FF),
                                border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onLogBid(nextMonthNum) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Gavel,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Log Month $nextMonthNum Bid",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4F46E5)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (committee.months.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.50f),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.70f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No auction bids logged yet",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap 'Log Month 1 Bid' above to record the winning bid and member installments.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    } else {
                        val colMonthWidth = 72.dp
                        val colWinningBidWidth = 82.dp
                        val colWinnerTakesWidth = 86.dp
                        val colInstallmentWidth = 106.dp
                        val colWinnerWidth = 86.dp
                        val colStatusWidth = 72.dp
                        val colActionsWidth = 76.dp
                        val tableHorizontalPadding = 12.dp
                        val totalTableWidth = colMonthWidth + colWinningBidWidth + colWinnerTakesWidth +
                            colInstallmentWidth + colWinnerWidth + colStatusWidth + colActionsWidth + (tableHorizontalPadding * 2)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.55f),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.75f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Column(modifier = Modifier.width(totalTableWidth)) {
                                    // Table Header Row matching screenshot
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = tableHorizontalPadding, vertical = 11.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "MONTH",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF8C98A9),
                                            letterSpacing = 0.3.sp,
                                            modifier = Modifier.width(colMonthWidth)
                                        )
                                        Text(
                                            text = "WINNING BID",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF8C98A9),
                                            letterSpacing = 0.3.sp,
                                            modifier = Modifier.width(colWinningBidWidth)
                                        )
                                        Text(
                                            text = "WINNER TAKES",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF8C98A9),
                                            letterSpacing = 0.3.sp,
                                            modifier = Modifier.width(colWinnerTakesWidth)
                                        )
                                        Text(
                                            text = "INSTALLMENT / PERSON",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF8C98A9),
                                            letterSpacing = 0.2.sp,
                                            modifier = Modifier.width(colInstallmentWidth),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "WINNER",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF8C98A9),
                                            letterSpacing = 0.3.sp,
                                            modifier = Modifier.width(colWinnerWidth)
                                        )
                                        Text(
                                            text = "STATUS",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF8C98A9),
                                            letterSpacing = 0.3.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.width(colStatusWidth)
                                        )
                                        Text(
                                            text = "ACTIONS",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF8C98A9),
                                            letterSpacing = 0.3.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.width(colActionsWidth),
                                            maxLines = 1
                                        )
                                    }

                                    HorizontalDivider(color = Color(0xFFF1F5F9).copy(alpha = 0.85f))

                                    // Data Rows (Sorted chronologically by month number)
                                    committee.months.sortedBy { it.monthNumber }.forEachIndexed { index, month ->
                                        if (index > 0) {
                                            HorizontalDivider(color = Color(0xFFF1F5F9).copy(alpha = 0.85f))
                                        }

                                        val displayMonth = Calculations.getCommitteeMonthLabel(
                                            committee.startDate,
                                            month.monthNumber,
                                            month.monthLabel
                                        )
                                        val winnerTakes = if (month.netPotValue > 0) month.netPotValue
                                            else maxOf(0.0, committee.totalAmount - month.winningBid)
                                        val installment = if (month.installmentPerPerson > 0) month.installmentPerPerson
                                            else if (committee.totalMembers > 0) winnerTakes / committee.totalMembers
                                            else 0.0

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = tableHorizontalPadding, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // 1. MONTH
                                            Text(
                                                text = displayMonth,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A),
                                                modifier = Modifier.width(colMonthWidth),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            // 2. WINNING BID (Warm amber)
                                            Text(
                                                text = Calculations.formatCurrency(month.winningBid),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD97706),
                                                modifier = Modifier.width(colWinningBidWidth),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            // 3. WINNER TAKES (Dark slate)
                                            Text(
                                                text = Calculations.formatCurrency(winnerTakes),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1E293B),
                                                modifier = Modifier.width(colWinnerTakesWidth),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            // 4. INSTALLMENT / PERSON (Vibrant indigo)
                                            Text(
                                                text = Calculations.formatCurrency(installment),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF6366F1),
                                                modifier = Modifier.width(colInstallmentWidth),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            // 5. WINNER
                                            Text(
                                                text = month.winnerName.ifBlank { "—" },
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1E293B),
                                                modifier = Modifier.width(colWinnerWidth),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            // 6. STATUS (Pill badge with checkmark and Paid - read-only display, edited via Edit action)
                                            Box(
                                                modifier = Modifier.width(colStatusWidth),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = if (month.isPaid) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                                    border = BorderStroke(
                                                        0.8.dp,
                                                        if (month.isPaid) Color(0xFF86EFAC).copy(alpha = 0.6f) else Color(0xFFFDE68A)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = if (month.isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                            contentDescription = null,
                                                            tint = if (month.isPaid) Color(0xFF059669) else Color(0xFFD97706),
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = if (month.isPaid) "Paid" else "Pending",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (month.isPaid) Color(0xFF059669) else Color(0xFFD97706)
                                                        )
                                                    }
                                                }
                                            }

                                            // 7. ACTIONS (Pencil edit and Trash delete icons - clearly sized and visible)
                                            Row(
                                                modifier = Modifier.width(colActionsWidth),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(7.dp),
                                                    color = Color(0xFFEFF6FF),
                                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(RoundedCornerShape(7.dp))
                                                        .clickable { onLogBid(month.monthNumber) }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Edit Bid",
                                                            tint = Color(0xFF2563EB),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(6.dp))

                                                Surface(
                                                    shape = RoundedCornerShape(7.dp),
                                                    color = Color(0xFFFEF2F2),
                                                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(RoundedCornerShape(7.dp))
                                                        .clickable { onDeleteMonth(month.monthNumber) }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.DeleteOutline,
                                                            contentDescription = "Delete Bid",
                                                            tint = Color(0xFFDC2626),
                                                            modifier = Modifier.size(14.dp)
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
                }
            }
        }
    }
}
