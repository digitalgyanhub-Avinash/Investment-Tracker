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
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.example.data.model.Ipo
import com.example.util.Calculations
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpoDialog(
    initialIpo: Ipo? = null,
    onDismiss: () -> Unit,
    onSave: (Ipo) -> Unit
) {
    val isEditing = initialIpo != null

    var companyName by remember { mutableStateOf(initialIpo?.companyName ?: "") }
    var category by remember { mutableStateOf(initialIpo?.category ?: "Mainboard") }
    var platform by remember { mutableStateOf(initialIpo?.platform ?: "Zerodha") }
    var applicantName by remember { mutableStateOf(initialIpo?.applicantName ?: "Self") }
    var sharesPerLotStr by remember { mutableStateOf(initialIpo?.sharesPerLot?.toString() ?: "100") }
    var cutOffPriceStr by remember { mutableStateOf(initialIpo?.cutOffPrice?.toString() ?: "150") }
    var lotsAppliedStr by remember { mutableStateOf(initialIpo?.lotsApplied?.toString() ?: "1") }

    var status by remember { mutableStateOf(initialIpo?.status ?: "Applied") }
    var allottedLotsStr by remember {
        mutableStateOf(
            if (initialIpo != null && initialIpo.allottedLots > 0) initialIpo.allottedLots.toString()
            else if (initialIpo != null && (initialIpo.status == "Allotted" || initialIpo.status == "Listed")) initialIpo.lotsApplied.toString()
            else "1"
        )
    }
    var listingPriceStr by remember {
        mutableStateOf(
            if (initialIpo != null && initialIpo.listingPrice > 0.0) {
                if (initialIpo.listingPrice % 1.0 == 0.0) initialIpo.listingPrice.toInt().toString() else initialIpo.listingPrice.toString()
            } else ""
        )
    }

    var applyDate by remember {
        mutableStateOf(
            initialIpo?.applyDate?.let { Calculations.normalizeDateToDdMmYyyy(it) }
                ?: SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
        )
    }
    var allotmentDate by remember {
        mutableStateOf(
            initialIpo?.allotmentDate?.let { Calculations.normalizeDateToDdMmYyyy(it) } ?: ""
        )
    }
    var listingDate by remember {
        mutableStateOf(
            initialIpo?.listingDate?.let { Calculations.normalizeDateToDdMmYyyy(it) } ?: ""
        )
    }
    var remarks by remember { mutableStateOf(initialIpo?.remarks ?: "") }

    var platformExpanded by remember { mutableStateOf(false) }
    var activeDatePickerTarget by remember { mutableStateOf<String?>(null) }

    if (activeDatePickerTarget != null) {
        val target = activeDatePickerTarget
        val initialDateMillis = when (target) {
            "apply" -> Calculations.parseDateToMillis(applyDate).takeIf { it > 0L }
            "allotment" -> Calculations.parseDateToMillis(allotmentDate).takeIf { it > 0L }
            "listing" -> Calculations.parseDateToMillis(listingDate).takeIf { it > 0L }
            else -> null
        } ?: System.currentTimeMillis()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { activeDatePickerTarget = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).apply {
                                timeZone = java.util.TimeZone.getTimeZone("UTC")
                            }
                            val formatted = sdf.format(Date(millis))
                            when (target) {
                                "apply" -> applyDate = formatted
                                "allotment" -> allotmentDate = formatted
                                "listing" -> listingDate = formatted
                            }
                        }
                        activeDatePickerTarget = null
                    }
                ) {
                    Text("Select", color = Color(0xFFE11D48), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDatePickerTarget = null }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val categories = listOf("Mainboard", "SME")
    val statuses = listOf("Applied", "Allotted", "Not Allotted", "Listed")
    val quickPlatforms = listOf("Zerodha", "Groww", "Angel One", "Upstox", "ICICI Direct", "HDFC")
    val quickApplicants = listOf("Self", "Spouse", "Father", "Mother", "HUF")

    // Dynamic calculations
    val sharesPerLot = sharesPerLotStr.toIntOrNull() ?: 1
    val cutOffPrice = cutOffPriceStr.toDoubleOrNull() ?: 0.0
    val lotsApplied = lotsAppliedStr.toIntOrNull() ?: 1
    val computedBlocked = sharesPerLot * cutOffPrice * lotsApplied

    val allottedLots = (allottedLotsStr.toIntOrNull() ?: 0).let {
        if (it > 0) it else if (status == "Allotted" || status == "Listed") lotsApplied else 0
    }
    val listingPrice = listingPriceStr.toDoubleOrNull() ?: 0.0
    val computedGainAmount = if (allottedLots > 0 && listingPrice > 0.0) {
        (listingPrice - cutOffPrice) * sharesPerLot * allottedLots
    } else 0.0
    val computedGainPercent = if (allottedLots > 0 && cutOffPrice > 0.0 && listingPrice > 0.0) {
        ((listingPrice - cutOffPrice) / cutOffPrice) * 100.0
    } else 0.0

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
                    .testTag("ipo_dialog"),
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
                                listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3), Color.White)
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
                                            listOf(Color(0xFFEC4899), Color(0xFFE11D48))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_lucide_building),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = if (isEditing) "Edit IPO Application" else "Apply New IPO Lot",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = "Track blocked capital, allotment & listing gains",
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
                    // LIVE CAPITAL BLOCKED & GAIN PREVIEW
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF831843).copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.35f)),
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
                                Text(
                                    text = "CAPITAL BLOCKED (ASBA)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFFDB2777)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = Calculations.formatCurrency(computedBlocked),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF9F1239),
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "$lotsApplied Lot(s) · ${sharesPerLot * lotsApplied} shares @ ₹$cutOffPrice",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }

                            if (computedGainAmount != 0.0) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (computedGainAmount >= 0) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, if (computedGainAmount >= 0) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = if (computedGainAmount >= 0) "+${Calculations.formatCurrency(computedGainAmount)}" else Calculations.formatCurrency(computedGainAmount),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (computedGainAmount >= 0) Color(0xFF047857) else Color(0xFFB91C1C)
                                        )
                                        Text(
                                            text = "${String.format("%.1f", computedGainPercent)}%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (computedGainAmount >= 0) Color(0xFF047857) else Color(0xFFB91C1C)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // COMPANY NAME
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company / IPO Name *") },
                        placeholder = { Text("e.g. Bajaj Housing Finance Ltd") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Apartment, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ipo_company_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE11D48),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // CATEGORY (Mainboard vs SME)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            val isSelected = category.equals(cat, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color(0xFFE11D48)
                                        else Color(0xFFF1F5F9)
                                    )
                                    .clickable { category = cat }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$cat IPO",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // APPLICATION STATUS CHIPS
                    Text(
                        text = "Current Status",
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
                        statuses.forEach { st ->
                            val isSelected = status.equals(st, ignoreCase = true)
                            val chipColor = when (st) {
                                "Allotted" -> Color(0xFF10B981)
                                "Not Allotted" -> Color(0xFF64748B)
                                "Listed" -> Color(0xFF6366F1)
                                else -> Color(0xFFE11D48) // Applied
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) chipColor
                                        else Color(0xFFF1F5F9)
                                    )
                                    .clickable { status = st }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = st,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // APPLICATION FINANCIALS (3 IN A ROW: Shares/Lot, Cutoff Price, Lots)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sharesPerLotStr,
                            onValueChange = { sharesPerLotStr = it },
                            label = { Text("Shares/Lot") },
                            placeholder = { Text("100") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ipo_shares_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE11D48),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = cutOffPriceStr,
                            onValueChange = { cutOffPriceStr = it },
                            label = { Text("Price (₹)") },
                            placeholder = { Text("150") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ipo_price_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE11D48),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = lotsAppliedStr,
                            onValueChange = { lotsAppliedStr = it },
                            label = { Text("Lots") },
                            placeholder = { Text("1") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("ipo_lots_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE11D48),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            singleLine = true
                        )
                    }

                    // IF ALLOTTED OR LISTED: SHOW EXTRA ALLOTMENT INPUTS
                    if (status == "Allotted" || status == "Listed") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = allottedLotsStr,
                                onValueChange = { allottedLotsStr = it },
                                label = { Text("Lots Allotted") },
                                placeholder = { Text(lotsAppliedStr.ifBlank { "1" }) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                ),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = listingPriceStr,
                                onValueChange = { listingPriceStr = it },
                                label = { Text("Selling / Listing (₹)") },
                                placeholder = { Text("210") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1.25f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                ),
                                singleLine = true
                            )
                        }

                        if (listingPrice > 0.0 && allottedLots > 0) {
                            val totalSellingValue = allottedLots * sharesPerLot * listingPrice
                            val totalCost = allottedLots * sharesPerLot * cutOffPrice
                            val gain = totalSellingValue - totalCost
                            val isProfitable = gain >= 0
                            val sign = if (gain > 0) "+" else ""

                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isProfitable) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, if (isProfitable) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "SELLING VALUE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = Calculations.formatCurrency(totalSellingValue),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "REALIZED GAIN",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = "$sign${Calculations.formatCurrency(gain)} (${String.format(Locale.ENGLISH, "%.1f", computedGainPercent)}%)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isProfitable) Color(0xFF059669) else Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // APPLICANT & BROKER
                    Text(
                        text = "Applicant Name",
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
                        quickApplicants.forEach { app ->
                            val isSelected = applicantName.equals(app, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) Color(0xFFE11D48)
                                        else Color(0xFFF1F5F9)
                                    )
                                    .clickable { applicantName = app }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = app,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = applicantName,
                        onValueChange = { applicantName = it },
                        label = { Text("Applicant / Demat Holder") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE11D48),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // BROKER PLATFORM
                    ExposedDropdownMenuBox(
                        expanded = platformExpanded,
                        onExpandedChange = { platformExpanded = !platformExpanded }
                    ) {
                        OutlinedTextField(
                            value = platform,
                            onValueChange = { platform = it },
                            label = { Text("Broker / ASBA Bank") },
                            leadingIcon = {
                                Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE11D48),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = platformExpanded,
                            onDismissRequest = { platformExpanded = false }
                        ) {
                            quickPlatforms.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = {
                                        platform = p
                                        platformExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // KEY IPO DATES SECTION (Applied, Allotment, Listing)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFFE11D48),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Key IPO Dates",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFCE7F3),
                                    modifier = Modifier.clickable {
                                        applyDate = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                                    }
                                ) {
                                    Text(
                                        text = "Today",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFBE185D),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 1. APPLIED DATE
                            OutlinedTextField(
                                value = applyDate,
                                onValueChange = { applyDate = it },
                                label = { Text("Applied Date *") },
                                placeholder = { Text("DD-MM-YYYY") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFFE11D48),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { activeDatePickerTarget = "apply" },
                                        modifier = Modifier.testTag("pick_apply_date_button")
                                    ) {
                                        Icon(
                                            Icons.Rounded.CalendarMonth,
                                            contentDescription = "Pick Applied Date",
                                            tint = Color(0xFFE11D48),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ipo_apply_date_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE11D48),
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. ALLOTMENT DATE
                            OutlinedTextField(
                                value = allotmentDate,
                                onValueChange = { allotmentDate = it },
                                label = { Text("Allotment Date (Optional)") },
                                placeholder = { Text("DD-MM-YYYY") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.EventAvailable,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (allotmentDate.isNotBlank()) {
                                            IconButton(
                                                onClick = { allotmentDate = "" },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Clear,
                                                    contentDescription = "Clear Allotment Date",
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { activeDatePickerTarget = "allotment" },
                                            modifier = Modifier.testTag("pick_allotment_date_button")
                                        ) {
                                            Icon(
                                                Icons.Rounded.CalendarMonth,
                                                contentDescription = "Pick Allotment Date",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ipo_allotment_date_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. LISTING DATE
                            OutlinedTextField(
                                value = listingDate,
                                onValueChange = { listingDate = it },
                                label = { Text("Listing Date (Optional)") },
                                placeholder = { Text("DD-MM-YYYY") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.RocketLaunch,
                                        contentDescription = null,
                                        tint = Color(0xFF6366F1),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (listingDate.isNotBlank()) {
                                            IconButton(
                                                onClick = { listingDate = "" },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Clear,
                                                    contentDescription = "Clear Listing Date",
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { activeDatePickerTarget = "listing" },
                                            modifier = Modifier.testTag("pick_listing_date_button")
                                        ) {
                                            Icon(
                                                Icons.Rounded.CalendarMonth,
                                                contentDescription = "Pick Listing Date",
                                                tint = Color(0xFF6366F1),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ipo_listing_date_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6366F1),
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                ),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // REMARKS
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks (Optional)") },
                        placeholder = { Text("e.g. UPI mandate approved via GPay") },
                        leadingIcon = {
                            Icon(Icons.Rounded.EditNote, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE11D48),
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
                                        listOf(Color(0xFFEC4899), Color(0xFFE11D48))
                                    )
                                )
                                .clickable {
                                    val normalizedApplyDate = Calculations.normalizeDateToDdMmYyyy(applyDate).ifBlank {
                                        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                                    }
                                    val normalizedAllotmentDate = Calculations.normalizeDateToDdMmYyyy(allotmentDate)
                                    val normalizedListingDate = Calculations.normalizeDateToDdMmYyyy(listingDate)

                                    val finalBlockedAmount = if (computedBlocked > 0.0) {
                                        computedBlocked
                                    } else {
                                        (sharesPerLot * cutOffPrice * lotsApplied)
                                    }

                                    val ipo = (initialIpo ?: Ipo(
                                        companyName = companyName,
                                        category = category,
                                        platform = platform,
                                        sharesPerLot = sharesPerLot,
                                        cutOffPrice = cutOffPrice,
                                        lotsApplied = lotsApplied,
                                        totalAmountBlocked = finalBlockedAmount,
                                        applyDate = normalizedApplyDate
                                    )).copy(
                                        companyName = companyName.ifBlank { "Untitled IPO" },
                                        category = category.ifBlank { "Mainboard" },
                                        platform = platform.ifBlank { "Zerodha" },
                                        applicantName = applicantName.ifBlank { "Self" },
                                        sharesPerLot = sharesPerLot,
                                        cutOffPrice = cutOffPrice,
                                        lotsApplied = lotsApplied,
                                        totalAmountBlocked = finalBlockedAmount,
                                        status = status,
                                        allottedLots = allottedLots,
                                        listingPrice = listingPrice,
                                        listingGainAmount = computedGainAmount,
                                        listingGainPercent = computedGainPercent,
                                        applyDate = normalizedApplyDate,
                                        allotmentDate = normalizedAllotmentDate,
                                        listingDate = normalizedListingDate,
                                        remarks = remarks,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    onSave(ipo)
                                    onDismiss()
                                }
                                .testTag("save_ipo_button"),
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
                                    text = if (isEditing) "Save Changes" else "Submit IPO",
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

