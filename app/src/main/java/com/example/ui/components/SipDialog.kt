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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.Sip
import com.example.util.Calculations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SipDialog(
    initialSip: Sip? = null,
    onDismiss: () -> Unit,
    onSave: (Sip) -> Unit
) {
    val isEditing = initialSip != null

    var platform by remember { mutableStateOf(initialSip?.platform ?: "Groww") }
    var instrument by remember { mutableStateOf(initialSip?.instrument ?: "Index Fund SIP") }
    var amountStr by remember { mutableStateOf(initialSip?.amount?.toString() ?: "5000") }
    var frequency by remember { mutableStateOf(initialSip?.frequency ?: "Monthly") }
    var since by remember {
        mutableStateOf(
            initialSip?.since?.let { Calculations.normalizeDateToDdMmYyyy(it) }
                ?: SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
        )
    }
    var remarks by remember { mutableStateOf(initialSip?.remarks ?: "") }
    var isPaused by remember { mutableStateOf(initialSip?.isPaused ?: false) }

    var platformExpanded by remember { mutableStateOf(false) }
    val frequencies = listOf("Monthly", "Weekly", "Daily", "Yearly")
    val quickPlatforms = listOf("Groww", "Zerodha", "Coin", "Indmoney", "Angel One", "Upstox")
    val suggestedFunds = listOf("Flexi Cap", "Nifty 50 Index", "Mid Cap", "Small Cap", "Gold SIP", "ELSS Tax Saver")

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val monthlyEquivalent = when (frequency.lowercase()) {
        "daily" -> amount * 30
        "weekly" -> amount * 4.33
        "yearly" -> amount / 12
        else -> amount
    }
    val annualProjected = monthlyEquivalent * 12

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
                    .testTag("sip_dialog"),
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
                                listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color.White)
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
                                            listOf(Color(0xFFF59E0B), Color(0xFFEA580C))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_lucide_arrows),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = if (isEditing) "Edit Systematic Plan" else "Create Systematic Plan (SIP)",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = "Automate regular installments & wealth projection",
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
                    // LIVE CASHFLOW PROJECTION HERO CARD
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF78350F).copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "MONTHLY OUTFLOW COMMITMENT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFFD97706)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${Calculations.formatCurrency(monthlyEquivalent)}/mo",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF9A3412),
                                    letterSpacing = (-0.5).sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "ANNUAL COMMITMENT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFFEA580C)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = Calculations.formatCurrency(annualProjected),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC2410C)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // FREQUENCY SEGMENTED TABS
                    Text(
                        text = "Recurrence Frequency",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        frequencies.forEach { freq ->
                            val isSelected = frequency.equals(freq, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) Brush.linearGradient(
                                            listOf(Color(0xFFF59E0B), Color(0xFFEA580C))
                                        )
                                        else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                    )
                                    .clickable { frequency = freq }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = freq,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // PLATFORM SELECTION
                    Text(
                        text = "Broker / App",
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
                                        if (isSelected) Color(0xFFEA580C)
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
                                Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("sip_platform_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFEA580C),
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

                    // INSTRUMENT / FUND NAME WITH PRESETS
                    Text(
                        text = "Instrument / Fund Preset",
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
                        suggestedFunds.forEach { fund ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .clickable { instrument = "$fund SIP" }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = fund,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = instrument,
                        onValueChange = { instrument = it },
                        label = { Text("Instrument / Fund Name *") },
                        placeholder = { Text("e.g. Parag Parikh Flexi Cap Fund") },
                        leadingIcon = {
                            Icon(Icons.Rounded.ShowChart, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sip_instrument_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEA580C),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // INSTALLMENT AMOUNT
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Installment Amount (₹) *") },
                        placeholder = { Text("5000") },
                        leadingIcon = {
                            Icon(Icons.Rounded.CurrencyRupee, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sip_amount_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEA580C),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // START DATE
                    OutlinedTextField(
                        value = since,
                        onValueChange = { since = it },
                        label = { Text("Start / Since Date (DD-MM-YYYY)") },
                        placeholder = { Text("DD-MM-YYYY") },
                        leadingIcon = {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEA580C),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // SIP STATUS (ACTIVE vs PAUSED)
                    Text(
                        text = "Plan Status (Active / Paused)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Active Option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (!isPaused) Brush.linearGradient(
                                        listOf(Color(0xFF10B981), Color(0xFF059669))
                                    ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .clickable { isPaused = false }
                                .padding(vertical = 8.dp)
                                .testTag("sip_dialog_status_active"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_lucide_play),
                                    contentDescription = null,
                                    tint = if (!isPaused) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Active",
                                    fontSize = 12.sp,
                                    fontWeight = if (!isPaused) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isPaused) Color.White else Color(0xFF475569)
                                )
                            }
                        }

                        // Paused Option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isPaused) Brush.linearGradient(
                                        listOf(Color(0xFFF59E0B), Color(0xFFEA580C))
                                    ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .clickable { isPaused = true }
                                .padding(vertical = 8.dp)
                                .testTag("sip_dialog_status_paused"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_lucide_pause),
                                    contentDescription = null,
                                    tint = if (isPaused) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Paused",
                                    fontSize = 12.sp,
                                    fontWeight = if (isPaused) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPaused) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // REMARKS
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Notes / Strategy (Optional)") },
                        placeholder = { Text("e.g. Deducts on 5th of every month") },
                        leadingIcon = {
                            Icon(Icons.Rounded.EditNote, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEA580C),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        maxLines = 2
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
                                        listOf(Color(0xFFF59E0B), Color(0xFFEA580C))
                                    )
                                )
                                .clickable {
                                    val amt = amountStr.toDoubleOrNull() ?: 5000.0
                                    val normalizedSince = Calculations.normalizeDateToDdMmYyyy(since).ifBlank {
                                        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                                    }

                                    val sip = (initialSip ?: Sip(
                                        platform = platform,
                                        instrument = instrument,
                                        amount = amt,
                                        frequency = frequency,
                                        since = normalizedSince,
                                        isPaused = isPaused
                                    )).copy(
                                        platform = platform.ifBlank { "Groww" },
                                        instrument = instrument.ifBlank { "SIP" },
                                        amount = amt,
                                        frequency = frequency.ifBlank { "Monthly" },
                                        since = normalizedSince,
                                        remarks = remarks,
                                        isPaused = isPaused,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    onSave(sip)
                                    onDismiss()
                                }
                                .testTag("save_sip_button"),
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
                                    text = if (isEditing) "Save Changes" else "Create SIP",
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

