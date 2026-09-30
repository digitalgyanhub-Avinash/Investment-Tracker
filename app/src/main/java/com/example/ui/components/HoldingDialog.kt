package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.InitialData
import com.example.data.model.Holding
import com.example.util.Calculations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingDialog(
    initialHolding: Holding? = null,
    onDismiss: () -> Unit,
    onSave: (Holding) -> Unit
) {
    val isEditing = initialHolding != null

    var platform by remember { mutableStateOf(initialHolding?.platform ?: "Zerodha") }
    var category by remember { mutableStateOf(initialHolding?.category ?: "Stocks") }
    var breakdownRaw by remember { mutableStateOf(initialHolding?.breakdownRaw ?: "") }
    var manualTotal by remember { mutableStateOf(initialHolding?.total?.toString() ?: "") }
    var tillDate by remember {
        mutableStateOf(
            initialHolding?.tillDate?.let { Calculations.normalizeDateToDdMmYyyy(it) }
                ?: SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
        )
    }
    var remarks by remember { mutableStateOf(initialHolding?.remarks ?: "") }

    var platformExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val quickPlatforms = listOf("Zerodha", "Groww", "Indmoney", "Angel One", "Upstox", "Dhan")
    val quickCategories = listOf("Stocks", "Mutual Funds", "Gold", "Crypto", "EPF", "Cash")

    // Compute breakdown sum dynamically
    val parsedBreakdown = remember(breakdownRaw) {
        Calculations.parseBreakdownString(breakdownRaw)
    }
    val computedTotal = remember(parsedBreakdown, manualTotal) {
        if (parsedBreakdown.isNotEmpty()) {
            Calculations.sumBreakdown(parsedBreakdown)
        } else {
            manualTotal.toDoubleOrNull() ?: 0.0
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            val maxDialogHeight = maxHeight * 0.86f

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .heightIn(max = maxDialogHeight)
                    .clip(RoundedCornerShape(28.dp))
                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(28.dp))
                    .testTag("holding_dialog"),
                color = Color.White,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Banner & Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFEFF6FF), Color(0xFFF0F9FF), Color.White)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF3B82F6), Color(0xFF0284C7))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_lucide_layers),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = if (isEditing) "Edit Asset Holding" else "Add New Asset Holding",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = "Record portfolio balances & broker holdings",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    // LIVE CALCULATION HERO PREVIEW
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF1E40AF).copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF3B82F6))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (parsedBreakdown.isNotEmpty()) "EVALUATED FROM FORMULA" else "TOTAL ASSET VALUE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Calculations.formatCurrency(computedTotal),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E3A8A),
                                    letterSpacing = (-0.5).sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "$platform · $category",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // PLATFORM SELECTION WITH QUICK CHIPS
                    Text(
                        text = "Broker / Platform",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickPlatforms.forEach { p ->
                            val isSelected = platform.equals(p, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) Color(0xFF0284C7)
                                        else Color(0xFFF1F5F9)
                                    )
                                    .clickable { platform = p }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = p,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = platformExpanded,
                        onExpandedChange = { platformExpanded = !platformExpanded }
                    ) {
                        OutlinedTextField(
                            value = platform,
                            onValueChange = { platform = it },
                            label = { Text("Selected Platform") },
                            leadingIcon = {
                                Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("holding_platform_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = platformExpanded,
                            onDismissRequest = { platformExpanded = false }
                        ) {
                            InitialData.PLATFORMS.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p, fontWeight = if (platform == p) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        platform = p
                                        platformExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // CATEGORY SELECTION WITH QUICK CHIPS
                    Text(
                        text = "Asset Class / Category",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickCategories.forEach { c ->
                            val isSelected = category.equals(c, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) Color(0xFF0284C7)
                                        else Color(0xFFF1F5F9)
                                    )
                                    .clickable { category = c }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = c,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Selected Category") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Category, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("holding_category_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            InitialData.CATEGORIES.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c, fontWeight = if (category == c) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        category = c
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // BREAKDOWN RAW OR MANUAL TOTAL
                    OutlinedTextField(
                        value = breakdownRaw,
                        onValueChange = { breakdownRaw = it },
                        label = { Text("Breakdown Formula / Equation (Optional)") },
                        placeholder = { Text("e.g. 17.8k, 6300, 9500, -2000") },
                        supportingText = {
                            Text("Supports + / - / comma / 'K' shorthand (e.g. 25k + 5000)", fontSize = 11.sp, color = Color(0xFF64748B))
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.Functions, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("holding_breakdown_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (parsedBreakdown.isEmpty()) {
                        OutlinedTextField(
                            value = manualTotal,
                            onValueChange = { manualTotal = it },
                            label = { Text("Total Amount (₹)") },
                            placeholder = { Text("e.g. 67000") },
                            leadingIcon = {
                                Icon(Icons.Rounded.CurrencyRupee, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("holding_total_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // AS OF DATE
                    OutlinedTextField(
                        value = tillDate,
                        onValueChange = { tillDate = it },
                        label = { Text("As of Date (DD-MM-YYYY)") },
                        placeholder = { Text("DD-MM-YYYY") },
                        leadingIcon = {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("holding_date_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // REMARKS
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks / Notes (Optional)") },
                        placeholder = { Text("e.g. 61,600 invested, 5,700 held in Demat") },
                        leadingIcon = {
                            Icon(Icons.Rounded.EditNote, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("holding_remarks_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        maxLines = 3
                    )
                }

                // BOTTOM ACTION BUTTONS
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF8FAFC),
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF3B82F6), Color(0xFF0284C7))
                                    )
                                )
                                .clickable {
                                    val finalTotal = if (parsedBreakdown.isNotEmpty()) computedTotal
                                    else manualTotal.toDoubleOrNull() ?: 0.0
                                    val normalizedDate = Calculations.normalizeDateToDdMmYyyy(tillDate).ifBlank {
                                        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                                    }

                                    val holding = (initialHolding ?: Holding(
                                        platform = platform,
                                        category = category,
                                        total = finalTotal,
                                        tillDate = normalizedDate
                                    )).copy(
                                        platform = platform.ifBlank { "Other" },
                                        category = category.ifBlank { "Stocks" },
                                        breakdownRaw = breakdownRaw,
                                        total = finalTotal,
                                        tillDate = normalizedDate,
                                        remarks = remarks,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    onSave(holding)
                                    onDismiss()
                                }
                                .testTag("save_holding_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEditing) "Save Changes" else "Add Holding",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
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

