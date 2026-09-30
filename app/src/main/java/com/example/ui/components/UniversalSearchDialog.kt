package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Committee
import com.example.data.model.Holding
import com.example.data.model.Ipo
import com.example.data.model.Sip
import com.example.util.Calculations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalSearchDialog(
    holdings: List<Holding>,
    sips: List<Sip>,
    ipos: List<Ipo>,
    committees: List<Committee>,
    onDismiss: () -> Unit,
    onSelectItem: (type: String, id: String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val cleanQuery = query.trim().lowercase()

    val filteredHoldings = remember(cleanQuery, holdings) {
        if (cleanQuery.isBlank()) emptyList()
        else Calculations.sortHoldingsRecentFirst(
            holdings.filter {
                it.platform.lowercase().contains(cleanQuery) ||
                it.category.lowercase().contains(cleanQuery) ||
                it.remarks.lowercase().contains(cleanQuery)
            }
        )
    }

    val filteredSips = remember(cleanQuery, sips) {
        if (cleanQuery.isBlank()) emptyList()
        else Calculations.sortSipsRecentFirst(
            sips.filter {
                it.platform.lowercase().contains(cleanQuery) ||
                it.instrument.lowercase().contains(cleanQuery) ||
                it.remarks.lowercase().contains(cleanQuery)
            }
        )
    }

    val filteredIpos = remember(cleanQuery, ipos) {
        if (cleanQuery.isBlank()) emptyList()
        else Calculations.sortIposRecentFirst(
            ipos.filter {
                it.companyName.lowercase().contains(cleanQuery) ||
                it.platform.lowercase().contains(cleanQuery) ||
                it.applicantName.lowercase().contains(cleanQuery) ||
                it.remarks.lowercase().contains(cleanQuery)
            }
        )
    }

    val filteredCommittees = remember(cleanQuery, committees) {
        if (cleanQuery.isBlank()) emptyList()
        else Calculations.sortCommitteesRecentFirst(
            committees.filter {
                it.name.lowercase().contains(cleanQuery) ||
                it.remarks.lowercase().contains(cleanQuery)
            }
        )
    }

    val totalResults = filteredHoldings.size + filteredSips.size + filteredIpos.size + filteredCommittees.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("universal_search_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Universal Portfolio Search",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("universal_search_input"),
                placeholder = { Text("Search stocks, SIPs, IPOs, committees...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (query.isNotBlank()) {
                Text(
                    text = "Found $totalResults matching assets",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                if (query.isBlank()) {
                    item {
                        Text(
                            text = "Type any keyword above to search across all your investments.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                } else if (totalResults == 0) {
                    item {
                        Text(
                            text = "No investments match '$query'",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                } else {
                    // Holdings Section
                    if (filteredHoldings.isNotEmpty()) {
                        item {
                            SearchResultHeader("HOLDINGS (${filteredHoldings.size})")
                        }
                        items(filteredHoldings) { holding ->
                            SearchResultItem(
                                icon = Icons.Default.PieChart,
                                title = "${holding.platform} · ${holding.category}",
                                subtitle = "Date: ${Calculations.normalizeDateToDdMmYyyy(holding.tillDate)} ${if (holding.remarks.isNotBlank()) "· " + holding.remarks else ""}",
                                amount = Calculations.formatCurrency(holding.total),
                                onClick = {
                                    onSelectItem("holding", holding.id)
                                    onDismiss()
                                }
                            )
                        }
                    }

                    // SIPs Section
                    if (filteredSips.isNotEmpty()) {
                        item {
                            SearchResultHeader("SIPS (${filteredSips.size})")
                        }
                        items(filteredSips) { sip ->
                            val invested = Calculations.calculateSipInvested(sip)
                            SearchResultItem(
                                icon = Icons.Default.Repeat,
                                title = "${sip.instrument} (${sip.frequency}${if (sip.isPaused) " · Paused" else ""})",
                                subtitle = "Platform: ${sip.platform} · Since: ${Calculations.normalizeDateToDdMmYyyy(sip.since)}${if (sip.isPaused) " · [Paused]" else ""}",
                                amount = Calculations.formatCurrency(invested),
                                onClick = {
                                    onSelectItem("sip", sip.id)
                                    onDismiss()
                                }
                            )
                        }
                    }

                    // IPOs Section
                    if (filteredIpos.isNotEmpty()) {
                        item {
                            SearchResultHeader("IPOS (${filteredIpos.size})")
                        }
                        items(filteredIpos) { ipo ->
                            SearchResultItem(
                                icon = Icons.Default.Business,
                                title = "${ipo.companyName} (${ipo.category})",
                                subtitle = "Status: ${ipo.status} · Broker: ${ipo.platform}",
                                amount = Calculations.formatCurrency(
                                    if (ipo.totalAmountBlocked > 0.0) ipo.totalAmountBlocked
                                    else (ipo.sharesPerLot * ipo.cutOffPrice * ipo.lotsApplied)
                                ),
                                onClick = {
                                    onSelectItem("ipo", ipo.id)
                                    onDismiss()
                                }
                            )
                        }
                    }

                    // Committees Section
                    if (filteredCommittees.isNotEmpty()) {
                        item {
                            SearchResultHeader("COMMITTEES (${filteredCommittees.size})")
                        }
                        items(filteredCommittees) { comm ->
                            val invested = Calculations.calculateCommitteeInvestedTillDate(comm)
                            SearchResultItem(
                                icon = Icons.Default.Groups,
                                title = comm.name,
                                subtitle = "Month ${comm.currentMonth}/${comm.totalMonths} · Pot: ${Calculations.formatCurrency(comm.totalAmount)}",
                                amount = Calculations.formatCurrency(invested),
                                onClick = {
                                    onSelectItem("committee", comm.id)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SearchResultHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SearchResultItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    amount: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
