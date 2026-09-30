package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.model.Committee
import com.example.data.model.Holding
import com.example.data.model.Ipo
import com.example.data.model.Sip
import com.example.util.ExportImportHelper
import com.example.util.ParsedPortfolioData

@Composable
fun DataBackupDialog(
    holdings: List<Holding>,
    sips: List<Sip>,
    ipos: List<Ipo>,
    committees: List<Committee>,
    onDismiss: () -> Unit,
    onImportData: (List<Holding>, List<Sip>, List<Ipo>, List<Committee>, Boolean) -> Unit,
    onShowToast: (String) -> Unit,
    onOpenChangelog: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showPasteImportDialog by remember { mutableStateOf(false) }
    var pastedText by remember { mutableStateOf("") }

    var pendingImportData by remember { mutableStateOf<ParsedPortfolioData?>(null) }
    var replaceExistingOnImport by remember { mutableStateOf(false) }

    // File Picker for CSV / Excel / Text files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val content = stream.bufferedReader().use { it.readText() }
                    val parsed = ExportImportHelper.parseCsv(content)
                    val total = parsed.holdings.size + parsed.sips.size + parsed.ipos.size + parsed.committees.size
                    if (total > 0) {
                        pendingImportData = parsed
                    } else {
                        onShowToast("No valid portfolio records recognized in selected file.")
                    }
                }
            } catch (e: Exception) {
                onShowToast("Error reading file: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    // 1. Paste CSV Import Dialog
    if (showPasteImportDialog) {
        AlertDialog(
            onDismissRequest = { showPasteImportDialog = false },
            title = {
                Text(
                    text = "Paste CSV / Text Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Paste exported CSV records below. Columns like Platform, Category, Amount or Name will be detected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = { pastedText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("SECTION,HOLDINGS\nPlatform,Category,Units,BuyPrice,CurrentPrice...\nor paste any CSV data") },
                        maxLines = 10,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = ExportImportHelper.parseCsv(pastedText)
                        val total = parsed.holdings.size + parsed.sips.size + parsed.ipos.size + parsed.committees.size
                        if (total > 0) {
                            pendingImportData = parsed
                            showPasteImportDialog = false
                        } else {
                            onShowToast("No portfolio records detected in pasted text.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Text("Parse & Review")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPasteImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Pending Import Review Confirmation Dialog
    pendingImportData?.let { data ->
        val total = data.holdings.size + data.sips.size + data.ipos.size + data.committees.size
        AlertDialog(
            onDismissRequest = { pendingImportData = null },
            title = {
                Text(
                    text = "Confirm Data Import",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Detected $total record(s) ready to import:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (data.holdings.isNotEmpty()) {
                                Text("• Holdings: ${data.holdings.size} records", fontSize = 13.sp, color = Color(0xFF1E293B))
                            }
                            if (data.sips.isNotEmpty()) {
                                Text("• SIPs: ${data.sips.size} records", fontSize = 13.sp, color = Color(0xFF1E293B))
                            }
                            if (data.ipos.isNotEmpty()) {
                                Text("• IPOs: ${data.ipos.size} records", fontSize = 13.sp, color = Color(0xFF1E293B))
                            }
                            if (data.committees.isNotEmpty()) {
                                Text("• Committees: ${data.committees.size} records", fontSize = 13.sp, color = Color(0xFF1E293B))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { replaceExistingOnImport = !replaceExistingOnImport }
                    ) {
                        Checkbox(
                            checked = replaceExistingOnImport,
                            onCheckedChange = { replaceExistingOnImport = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Replace existing portfolio records (overwrite)",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onImportData(
                            data.holdings,
                            data.sips,
                            data.ipos,
                            data.committees,
                            replaceExistingOnImport
                        )
                        pendingImportData = null
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Records")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingImportData = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // MAIN DATA MANAGEMENT & BACKUP DIALOG
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("data_backup_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFECFDF5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Storage,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Data Management & Backup",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Export and import to CSV, Excel, and PDF",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // CURRENT ENTRIES CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "CURRENT LOCAL DATABASE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• Holdings: ${holdings.size}", fontSize = 12.sp, color = Color(0xFF334155))
                            Text("• Active SIPs: ${sips.size}", fontSize = 12.sp, color = Color(0xFF334155))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• IPOs: ${ipos.size}", fontSize = 12.sp, color = Color(0xFF334155))
                            Text("• Committees: ${committees.size}", fontSize = 12.sp, color = Color(0xFF334155))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // EXPORT SECTION
                Text(
                    text = "EXPORT PORTFOLIO",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 1. Export to CSV Button
                ExportActionCard(
                    title = "Export to CSV",
                    description = "Comma-separated values with Holdings, SIPs, IPOs & Committees",
                    badge = "CSV",
                    badgeColor = Color(0xFF0284C7),
                    iconRes = R.drawable.ic_lucide_table,
                    onClick = {
                        try {
                            val csv = ExportImportHelper.generateCsv(holdings, sips, ipos, committees)
                            ExportImportHelper.shareTextFile(
                                context = context,
                                fileName = "investment_portfolio.csv",
                                mimeType = "text/csv",
                                content = csv,
                                title = "Share Portfolio CSV"
                            )
                            // Also copy to clipboard for convenience
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("InvestmentTrackerCSV", csv)
                            clipboard.setPrimaryClip(clip)
                            onShowToast("CSV exported and copied to clipboard!")
                        } catch (e: Exception) {
                            onShowToast("Error exporting CSV: ${e.localizedMessage}")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Export to Excel Button
                ExportActionCard(
                    title = "Export to Excel (.xls)",
                    description = "Multi-sheet spreadsheet with styled headers and formatted columns",
                    badge = "EXCEL",
                    badgeColor = Color(0xFF059669),
                    iconRes = R.drawable.ic_lucide_file_spreadsheet,
                    onClick = {
                        try {
                            val xls = ExportImportHelper.generateExcelXml(holdings, sips, ipos, committees)
                            ExportImportHelper.shareTextFile(
                                context = context,
                                fileName = "investment_portfolio.xls",
                                mimeType = "application/vnd.ms-excel",
                                content = xls,
                                title = "Share Excel Portfolio Spreadsheet"
                            )
                            onShowToast("Excel portfolio spreadsheet generated!")
                        } catch (e: Exception) {
                            onShowToast("Error exporting Excel: ${e.localizedMessage}")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Export to PDF Button
                ExportActionCard(
                    title = "Export to PDF Statement",
                    description = "Formatted financial report with summary KPIs and asset breakdown",
                    badge = "PDF",
                    badgeColor = Color(0xFFE11D48),
                    iconRes = R.drawable.ic_lucide_layers,
                    onClick = {
                        try {
                            val pdfFile = ExportImportHelper.generatePdf(context, holdings, sips, ipos, committees)
                            ExportImportHelper.shareExportedFile(
                                context = context,
                                file = pdfFile,
                                mimeType = "application/pdf",
                                title = "Share Investment Portfolio PDF"
                            )
                            onShowToast("Portfolio PDF report ready!")
                        } catch (e: Exception) {
                            onShowToast("Error generating PDF: ${e.localizedMessage}")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // IMPORT SECTION
                Text(
                    text = "IMPORT PORTFOLIO",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pick File Button
                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch("*/*")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick File", fontSize = 13.sp)
                    }

                    // Paste CSV Button
                    OutlinedButton(
                        onClick = {
                            showPasteImportDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paste CSV", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // APP VERSION & CHANGELOG SECTION (exclusively in Data Management & Backup)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenChangelog() }
                        .testTag("app_version_badge")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_lucide_history),
                                    contentDescription = "Changelog History",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME} • Build ${BuildConfig.VERSION_CODE}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Investment & Committee Ledger",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // CHANGELOG Action Pill matching Screenshot 2
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2563EB),
                            modifier = Modifier.testTag("changelog_button")
                        ) {
                            Text(
                                text = "CHANGELOG",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.4.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun ExportActionCard(
    title: String,
    description: String,
    badge: String,
    badgeColor: Color,
    iconRes: Int,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                Icons.Default.FileDownload,
                contentDescription = "Export",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
