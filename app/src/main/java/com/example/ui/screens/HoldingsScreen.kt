package com.example.ui.screens

import androidx.compose.animation.animateContentSize
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
import com.example.R
import com.example.ui.components.ThemedDeleteDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Holding
import com.example.util.Calculations

@Composable
fun HoldingsScreen(
    holdings: List<Holding>,
    onAddHolding: () -> Unit,
    onEditHolding: (Holding) -> Unit,
    onDeleteHolding: (Holding) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var holdingToDelete by remember { mutableStateOf<Holding?>(null) }

    val sortedHoldings = remember(holdings) {
        Calculations.sortHoldingsRecentFirst(holdings)
    }

    val categories = remember(sortedHoldings) {
        listOf("All") + sortedHoldings.map { it.category }.distinct()
    }

    val filteredHoldings = remember(sortedHoldings, selectedCategory) {
        val list = if (selectedCategory == "All") {
            sortedHoldings
        } else {
            sortedHoldings.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        Calculations.sortHoldingsRecentFirst(list)
    }

    // Grouping holdings by Platform with recent-first ordering for both platforms and entries
    val groupedHoldings = remember(filteredHoldings) {
        val platformOrder = mutableListOf<String>()
        val map = mutableMapOf<String, MutableList<Holding>>()
        filteredHoldings.forEach { holding ->
            val p = holding.platform.ifBlank { "Other" }
            if (!platformOrder.contains(p)) {
                platformOrder.add(p)
            }
            map.getOrPut(p) { mutableListOf() }.add(holding)
        }
        platformOrder.associateWith { p ->
            Calculations.sortHoldingsRecentFirst(map[p] ?: emptyList())
        }
    }

    val totalAmount = remember(filteredHoldings) {
        Calculations.calculateHoldingsTotal(filteredHoldings)
    }

    if (holdingToDelete != null) {
        val h = holdingToDelete!!
        ThemedDeleteDialog(
            title = "Delete Asset Holding",
            subtitle = "Permanently remove this portfolio balance",
            itemBadge = "HOLDING",
            itemName = "${h.platform} — ${h.category}",
            itemSubtitle = if (h.remarks.isNotBlank()) h.remarks else "Asset balance record",
            itemDetailChips = listOf(
                "Total Balance" to Calculations.formatCurrency(h.total),
                "As of Date" to if (h.tillDate.isNotBlank()) h.tillDate else "Recent"
            ),
            themeColor = Color(0xFF2563EB),
            themeGradient = listOf(Color(0xFF3B82F6), Color(0xFF0284C7)),
            themeContainerColor = Color(0xFFEFF6FF),
            themeBorderColor = Color(0xFFBFDBFE),
            iconRes = R.drawable.ic_lucide_layers,
            onDismiss = { holdingToDelete = null },
            onConfirmDelete = {
                holdingToDelete?.let { onDeleteHolding(it) }
                holdingToDelete = null
            },
            testTag = "delete_holding_dialog"
        )
    }

    Box(modifier = modifier.fillMaxSize().testTag("holdings_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            // Header summary
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.52f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "TOTAL HOLDINGS VALUE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Calculations.formatCurrency(totalAmount),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0284C7)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF059669)
                        ) {
                            Text(
                                text = "${filteredHoldings.size} Assets",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (filteredHoldings.isEmpty()) {
                item {
                    Text(
                        text = "No holdings found. Tap '+' to add your first asset.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            } else {
                // Render Grouped Platform Cards (similar to user's screenshot)
                items(groupedHoldings.keys.toList(), key = { it }) { platformName ->
                    val groupItems = groupedHoldings[platformName] ?: emptyList()
                    val groupTotal = groupItems.sumOf { it.total }

                    PlatformGroupCard(
                        platformName = platformName,
                        holdings = groupItems,
                        groupTotal = groupTotal,
                        onEdit = onEditHolding,
                        onDelete = { holdingToDelete = it }
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
private fun PlatformGroupCard(
    platformName: String,
    holdings: List<Holding>,
    groupTotal: Double,
    onEdit: (Holding) -> Unit,
    onDelete: (Holding) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("platform_group_${platformName.lowercase().replace(" ", "_")}"),
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
                            text = "${holdings.size} ${if (holdings.size == 1) "entry" else "entries"}",
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
                            text = Calculations.formatCurrency(groupTotal),
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Group Entries
            holdings.forEachIndexed { index, holding ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Category, Till Date & Amount Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = holding.category,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (holding.tillDate.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "Till: ",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF94A3B8),
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = Calculations.normalizeDateToDdMmYyyy(holding.tillDate),
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF64748B),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Text(
                            text = Calculations.formatCurrency(holding.total),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                    }

                    // Breakdown line (if present)
                    if (holding.breakdownRaw.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        HoldingBreakdownRow(
                            breakdownRaw = holding.breakdownRaw,
                            holdingId = holding.id
                        )
                    }

                    // Remarks line (if present)
                    if (holding.remarks.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = holding.remarks,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons: Edit and Del icon buttons aligned to the right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
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
                                .clickable { onEdit(holding) }
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

                        Spacer(modifier = Modifier.width(5.dp))

                        // Del Icon Button
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .clickable { onDelete(holding) }
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

                // Spacing between multiple holdings in the same platform group
                if (index < holdings.size - 1) {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
private fun HoldingBreakdownRow(
    breakdownRaw: String,
    holdingId: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember(breakdownRaw) { mutableStateOf(false) }
    var reachesSecondLine by remember(breakdownRaw) {
        mutableStateOf(breakdownRaw.contains("\n"))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .then(
                if (reachesSecondLine) {
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isExpanded = !isExpanded }
                } else Modifier
            ),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "Breakdown:  ",
            fontSize = 13.sp,
            color = Color(0xFF475569),
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = breakdownRaw,
            fontSize = 13.sp,
            color = Color(0xFF334155),
            fontFamily = FontFamily.Monospace,
            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            onTextLayout = { textLayoutResult ->
                if (!reachesSecondLine && (textLayoutResult.hasVisualOverflow || textLayoutResult.lineCount > 1)) {
                    reachesSecondLine = true
                }
            }
        )
        if (reachesSecondLine) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF1F5F9))
                    .testTag("breakdown_arrow_$holdingId"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse breakdown" else "Expand breakdown",
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

