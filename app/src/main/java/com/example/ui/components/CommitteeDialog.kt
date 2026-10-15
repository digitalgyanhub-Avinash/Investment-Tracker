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
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.Committee
import com.example.util.Calculations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommitteeDialog(
    initialCommittee: Committee? = null,
    onDismiss: () -> Unit,
    onSave: (Committee) -> Unit
) {
    val isEditing = initialCommittee != null

    var name by remember { mutableStateOf(initialCommittee?.name ?: "Committee (₹60,000)") }
    var totalAmountStr by remember { mutableStateOf(initialCommittee?.totalAmount?.toString() ?: "60000") }
    var totalMembersStr by remember { mutableStateOf(initialCommittee?.totalMembers?.toString() ?: "12") }
    var totalMonthsStr by remember { mutableStateOf(initialCommittee?.totalMonths?.toString() ?: "12") }
    var startDate by remember {
        mutableStateOf(
            initialCommittee?.startDate?.let { Calculations.normalizeDateToDdMmYyyy(it) }
                ?: SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
        )
    }
    var baseMonthlyStr by remember { mutableStateOf(initialCommittee?.baseMonthlyInstallment?.toString() ?: "5000") }
    var hasTakenPot by remember { mutableStateOf(initialCommittee?.hasTakenPot ?: false) }
    var potTakenMonthStr by remember { mutableStateOf(initialCommittee?.potTakenMonth?.toString() ?: "1") }
    var potTakenAmountStr by remember { mutableStateOf(initialCommittee?.potTakenAmount?.toString() ?: "") }
    var remarks by remember { mutableStateOf(initialCommittee?.remarks ?: "") }

    val presetPots = listOf(50000, 60000, 90000, 100000, 120000, 200000)
    val durationPresets = listOf(10, 12, 15, 20, 24)

    val potAmount = totalAmountStr.toDoubleOrNull() ?: 60000.0
    val members = totalMembersStr.toIntOrNull() ?: 12
    val baseMonthly = if (members > 0) potAmount / members else 0.0

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
                    .testTag("committee_dialog"),
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
                                listOf(Color(0xFFF5F3FF), Color(0xFFFAF5FF), Color.White)
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
                                            listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_lucide_users),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = if (isEditing) "Edit Chit Fund Committee" else "Create Chit Fund Committee",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = "Monthly revolving savings & auction dividends",
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
                    // LIVE CHIT FUND HERO ECONOMICS CARD
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF5B21B6).copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.35f)),
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
                                    text = "TOTAL POT POOL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFF7C3AED)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = Calculations.formatCurrency(potAmount),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4C1D95),
                                    letterSpacing = (-0.5).sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "BASE MONTHLY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFF7C3AED)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${Calculations.formatCurrency(baseMonthly)}/mo",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6D28D9)
                                )
                                Text(
                                    text = "$members members · $totalMonthsStr mos",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // COMMITTEE NAME
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Committee / Organizer Name *") },
                        placeholder = { Text("e.g. Committee Jan 2026 (₹90,000)") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Group, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("committee_name_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // POT PRESETS
                    Text(
                        text = "Quick Pot Preset",
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
                        presetPots.forEach { pot ->
                            val isSelected = totalAmountStr == pot.toString()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) Color(0xFF7C3AED)
                                        else Color(0xFFF1F5F9)
                                    )
                                    .clickable {
                                        totalAmountStr = pot.toString()
                                        val m = totalMembersStr.toIntOrNull() ?: 12
                                        if (m > 0) baseMonthlyStr = (pot / m).toString()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = Calculations.formatCurrency(pot.toDouble()),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // POT AMOUNT INPUT
                    OutlinedTextField(
                        value = totalAmountStr,
                        onValueChange = {
                            totalAmountStr = it
                            val total = it.toDoubleOrNull() ?: 0.0
                            val m = totalMembersStr.toIntOrNull() ?: 12
                            if (m > 0) {
                                baseMonthlyStr = (total / m).toInt().toString()
                            }
                        },
                        label = { Text("Total Pot Value (₹) *") },
                        placeholder = { Text("60000") },
                        leadingIcon = {
                            Icon(Icons.Rounded.CurrencyRupee, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // DURATION PRESETS
                    Text(
                        text = "Committee Duration",
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
                        durationPresets.forEach { dur ->
                            val isSelected = totalMonthsStr == dur.toString()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) Color(0xFF7C3AED)
                                        else Color(0xFFF1F5F9)
                                    )
                                    .clickable {
                                        totalMonthsStr = dur.toString()
                                        totalMembersStr = dur.toString()
                                        val total = totalAmountStr.toDoubleOrNull() ?: 60000.0
                                        baseMonthlyStr = (total / dur).toInt().toString()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$dur Months",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // MEMBERS & MONTHS ROW
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = totalMembersStr,
                            onValueChange = {
                                totalMembersStr = it
                                val m = it.toIntOrNull() ?: 12
                                val total = totalAmountStr.toDoubleOrNull() ?: 60000.0
                                if (m > 0) baseMonthlyStr = (total / m).toInt().toString()
                            },
                            label = { Text("Members") },
                            placeholder = { Text("12") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7C3AED),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = totalMonthsStr,
                            onValueChange = { totalMonthsStr = it },
                            label = { Text("Duration (Months)") },
                            placeholder = { Text("12") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7C3AED),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // BASE MONTHLY INSTALLMENT
                    OutlinedTextField(
                        value = baseMonthlyStr,
                        onValueChange = { baseMonthlyStr = it },
                        label = { Text("Base Monthly Installment (₹)") },
                        placeholder = { Text("5000") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Payments, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // START DATE
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Cycle Start Date (DD-MM-YYYY)") },
                        leadingIcon = {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // POT TAKEN TOGGLE CARD
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (hasTakenPot) Color(0xFFEDE9FE).copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (hasTakenPot) Color(0xFF8B5CF6).copy(alpha = 0.5f) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Have you taken / bid this pot?",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Enable if you won or lifted the lump-sum pot",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Switch(
                                    checked = hasTakenPot,
                                    onCheckedChange = { hasTakenPot = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF7C3AED)
                                    )
                                )
                            }

                            if (hasTakenPot) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = potTakenMonthStr,
                                        onValueChange = { newMonth ->
                                            potTakenMonthStr = newMonth
                                            val mNum = newMonth.toIntOrNull()
                                            val matchedM = initialCommittee?.months?.find { it.monthNumber == mNum }
                                            if (matchedM != null && potTakenAmountStr.isBlank()) {
                                                val autoNet = if (matchedM.netPotValue > 0) matchedM.netPotValue
                                                    else maxOf(0.0, (totalAmountStr.toDoubleOrNull() ?: 60000.0) - matchedM.winningBid)
                                                if (autoNet > 0) {
                                                    potTakenAmountStr = autoNet.toInt().toString()
                                                }
                                            }
                                        },
                                        label = { Text("Taken Month #") },
                                        placeholder = { Text("1") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF7C3AED),
                                            unfocusedBorderColor = Color(0xFFCBD5E1)
                                        ),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = potTakenAmountStr,
                                        onValueChange = { potTakenAmountStr = it },
                                        label = { Text("Payout Received (₹)") },
                                        placeholder = { Text("52000") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1.3f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF7C3AED),
                                            unfocusedBorderColor = Color(0xFFCBD5E1)
                                        ),
                                        singleLine = true
                                    )
                                }

                                val mNum = potTakenMonthStr.toIntOrNull() ?: 1
                                val matchedM = initialCommittee?.months?.find { it.monthNumber == mNum }
                                val previewPot = potTakenAmountStr.toDoubleOrNull() ?: matchedM?.let {
                                    if (it.netPotValue > 0) it.netPotValue
                                    else maxOf(0.0, (totalAmountStr.toDoubleOrNull() ?: 60000.0) - it.winningBid)
                                } ?: 0.0

                                if (previewPot > 0.0) {
                                    val totAmt = totalAmountStr.toDoubleOrNull() ?: 60000.0
                                    val totMems = totalMembersStr.toIntOrNull() ?: 12
                                    val totMos = totalMonthsStr.toIntOrNull() ?: 12
                                    val baseInst = baseMonthlyStr.toDoubleOrNull() ?: (totAmt / if (totMems > 0) totMems else 1)
                                    val tempComm = (initialCommittee ?: Committee(
                                        name = name,
                                        totalAmount = totAmt,
                                        totalMembers = totMems,
                                        totalMonths = totMos,
                                        startDate = startDate,
                                        baseMonthlyInstallment = baseInst
                                    )).copy(
                                        totalAmount = totAmt,
                                        totalMembers = totMems,
                                        totalMonths = totMos,
                                        baseMonthlyInstallment = baseInst,
                                        hasTakenPot = true,
                                        potTakenMonth = mNum,
                                        potTakenAmount = previewPot
                                    )
                                    val calcProfit = Calculations.calculateCommitteeProfitLoss(tempComm)
                                    val isProf = calcProfit >= 0
                                    val absAmount = Calculations.formatCurrency(Math.abs(calcProfit)).replace("₹", "").trim()

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isProf) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                                        border = BorderStroke(1.dp, if (isProf) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_lucide_award),
                                                    contentDescription = null,
                                                    tint = if (isProf) Color(0xFF059669) else Color(0xFFDC2626),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isProf) "Calculated Net Profit" else "Calculated Net Loss",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isProf) Color(0xFF065F46) else Color(0xFF991B1B)
                                                )
                                            }
                                            Text(
                                                text = if (isProf) "+₹$absAmount" else "-₹$absAmount",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isProf) Color(0xFF047857) else Color(0xFFDC2626)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // REMARKS
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks / Notes (Optional)") },
                        placeholder = { Text("e.g. Monthly meeting on 10th") },
                        leadingIcon = {
                            Icon(Icons.Rounded.EditNote, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C3AED),
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
                                        listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))
                                    )
                                )
                                .clickable {
                                    val totAmt = totalAmountStr.toDoubleOrNull() ?: 60000.0
                                    val mem = totalMembersStr.toIntOrNull() ?: 12
                                    val mos = totalMonthsStr.toIntOrNull() ?: 12
                                    val baseInst = baseMonthlyStr.toDoubleOrNull() ?: (totAmt / if (mem > 0) mem else 1)
                                    val normalizedStartDate = Calculations.normalizeDateToDdMmYyyy(startDate).ifBlank {
                                        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                                    }

                                    val committee = (initialCommittee ?: Committee(
                                        name = name,
                                        totalAmount = totAmt,
                                        totalMembers = mem,
                                        totalMonths = mos,
                                        startDate = normalizedStartDate,
                                        baseMonthlyInstallment = baseInst
                                    )).copy(
                                        name = name.ifBlank { "Committee" },
                                        totalAmount = totAmt,
                                        totalMembers = mem,
                                        totalMonths = mos,
                                        startDate = normalizedStartDate,
                                        baseMonthlyInstallment = baseInst,
                                        hasTakenPot = hasTakenPot,
                                        potTakenMonth = potTakenMonthStr.toIntOrNull() ?: 1,
                                        potTakenAmount = potTakenAmountStr.toDoubleOrNull() ?: 0.0,
                                        remarks = remarks,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    onSave(committee)
                                    onDismiss()
                                },
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
                                    text = if (isEditing) "Save Changes" else "Create Committee",
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

@Composable
fun CommitteeBidDialog(
    committee: Committee,
    initialMonth: Int,
    onDismiss: () -> Unit,
    onSaveBid: (monthNumber: Int, monthLabel: String, winningBid: Double, winnerName: String, isPaid: Boolean, installmentPerMember: Double, isSelfWinner: Boolean) -> Unit
) {
    val existing = committee.months.find { it.monthNumber == initialMonth }

    val monthNumStr = initialMonth.toString()
    val autoMonthLabel = existing?.monthLabel?.ifBlank { null }
        ?: Calculations.getCommitteeMonthLabel(committee.startDate, initialMonth, "Month $initialMonth")
    var bidAmountStr by remember {
        mutableStateOf(
            if (existing != null && existing.winningBid > 0.0) {
                if (existing.winningBid % 1.0 == 0.0) existing.winningBid.toInt().toString() else existing.winningBid.toString()
            } else ""
        )
    }
    var installmentPerMemberStr by remember {
        mutableStateOf(
            if (existing != null && existing.installmentPerPerson > 0.0) {
                if (existing.installmentPerPerson % 1.0 == 0.0) existing.installmentPerPerson.toInt().toString() else existing.installmentPerPerson.toString()
            } else ""
        )
    }
    var winnerName by remember { mutableStateOf(existing?.winnerName ?: "") }
    var isSelfWinner by remember {
        mutableStateOf(
            (existing?.winnerName?.equals("Self", ignoreCase = true) == true) ||
            (committee.hasTakenPot && committee.potTakenMonth == initialMonth)
        )
    }
    var isPaid by remember { mutableStateOf(existing?.isPaid ?: true) }

    val bid = bidAmountStr.toDoubleOrNull() ?: 0.0
    val netPot = maxOf(0.0, committee.totalAmount - bid)

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
                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(28.dp)),
                color = Color.White,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFF5F3FF), Color(0xFFFAF5FF), Color.White)
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
                                            listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Gavel,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = if (existing != null) "Edit Auction Bid: Month $initialMonth" else "Log Auction Bid: Month $initialMonth",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = "${committee.name} · Pot: ${Calculations.formatCurrency(committee.totalAmount)}",
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

                // Form body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    // LIVE CALCULATION HERO CARD - Net Pot Payout
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF5B21B6).copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "NET POT PAYOUT",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = Color(0xFF7C3AED)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = Calculations.formatCurrency(netPot),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4C1D95),
                                letterSpacing = (-0.5).sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = bidAmountStr,
                        onValueChange = { bidAmountStr = it },
                        label = { Text("Winning Auction Discount Bid (₹) *") },
                        placeholder = { Text("e.g. 9480") },
                        supportingText = {
                            Text("Total discount foregone by the winning bidder", fontSize = 11.sp, color = Color(0xFF64748B))
                        },
                        leadingIcon = {
                            Icon(Icons.Rounded.Gavel, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = installmentPerMemberStr,
                        onValueChange = { installmentPerMemberStr = it },
                        label = { Text("Installation per Member") },
                        placeholder = { Text("e.g. 5709") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Payments, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = winnerName,
                        onValueChange = { winnerName = it },
                        label = { Text("Winning Bidder / Taken By") },
                        placeholder = { Text("e.g. Self or Member Name") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelfWinner) Color(0xFFEDE9FE).copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isSelfWinner) Color(0xFF8B5CF6).copy(alpha = 0.5f) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Did you take / win this pot?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (isSelfWinner) "You received net payout of ${Calculations.formatCurrency(netPot)}"
                                    else "Enable if you won or lifted the pot in Month $initialMonth",
                                    fontSize = 11.sp,
                                    color = if (isSelfWinner) Color(0xFF7C3AED) else Color(0xFF64748B)
                                )
                            }
                            Switch(
                                checked = isSelfWinner,
                                onCheckedChange = { checked ->
                                    isSelfWinner = checked
                                    if (checked && (winnerName.isBlank() || winnerName == "—")) {
                                        winnerName = "Self"
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF7C3AED)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isPaid) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isPaid) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Mark as Paid by you",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (isPaid) "Month marked as settled" else "Pending monthly installment",
                                    fontSize = 11.sp,
                                    color = if (isPaid) Color(0xFF059669) else Color(0xFF64748B)
                                )
                            }
                            Switch(
                                checked = isPaid,
                                onCheckedChange = { isPaid = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF10B981)
                                )
                            )
                        }
                    }
                }

                // Bottom actions
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
                            Text("Cancel", color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))
                                    )
                                )
                                .clickable {
                                    val mNum = monthNumStr.toIntOrNull() ?: initialMonth
                                    val customInstallment = installmentPerMemberStr.toDoubleOrNull() ?: 0.0
                                    onSaveBid(mNum, autoMonthLabel, bid, winnerName, isPaid, customInstallment, isSelfWinner)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Auction", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

