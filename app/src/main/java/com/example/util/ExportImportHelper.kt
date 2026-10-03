package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.Committee
import com.example.data.model.Holding
import com.example.data.model.Ipo
import com.example.data.model.Sip
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ParsedPortfolioData(
    val holdings: List<Holding> = emptyList(),
    val sips: List<Sip> = emptyList(),
    val ipos: List<Ipo> = emptyList(),
    val committees: List<Committee> = emptyList()
)

object ExportImportHelper {

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    // ==========================================
    // 1. CSV EXPORT & IMPORT
    // ==========================================

    fun generateCsv(
        holdings: List<Holding>,
        sips: List<Sip>,
        ipos: List<Ipo>,
        committees: List<Committee>
    ): String {
        val sb = StringBuilder()
        sb.append("# Investment Tracker Portfolio Export\n")
        sb.append("# Exported: ${dateFormatter.format(Date())}\n\n")

        // 1. Holdings
        sb.append("SECTION,HOLDINGS\n")
        sb.append("Platform,Category,Total,TillDate,Breakdown,Remarks\n")
        holdings.forEach { h ->
            val date = Calculations.normalizeDateToDdMmYyyy(h.tillDate)
            sb.append("\"${escapeCsv(h.platform)}\",\"${escapeCsv(h.category)}\",${h.total},\"${escapeCsv(date)}\",\"${escapeCsv(h.breakdownRaw)}\",\"${escapeCsv(h.remarks)}\"\n")
        }
        sb.append("\n")

        // 2. SIPs
        sb.append("SECTION,SIPS\n")
        sb.append("Platform,Instrument,Amount,Frequency,Since,Remarks,Status\n")
        sips.forEach { s ->
            val date = Calculations.normalizeDateToDdMmYyyy(s.since)
            val status = if (s.isPaused) "Paused" else "Active"
            sb.append("\"${escapeCsv(s.platform)}\",\"${escapeCsv(s.instrument)}\",${s.amount},\"${escapeCsv(s.frequency)}\",\"${escapeCsv(date)}\",\"${escapeCsv(s.remarks)}\",\"$status\"\n")
        }
        sb.append("\n")

        // 3. IPOs
        sb.append("SECTION,IPOS\n")
        sb.append("CompanyName,Category,Platform,ApplicantName,SharesPerLot,CutOffPrice,LotsApplied,TotalAmountBlocked,ApplyDate,AllotmentDate,ListingDate,Status,Remarks\n")
        ipos.forEach { i ->
            val apply = Calculations.normalizeDateToDdMmYyyy(i.applyDate)
            val allot = Calculations.normalizeDateToDdMmYyyy(i.allotmentDate)
            val list = Calculations.normalizeDateToDdMmYyyy(i.listingDate)
            sb.append("\"${escapeCsv(i.companyName)}\",\"${escapeCsv(i.category)}\",\"${escapeCsv(i.platform)}\",\"${escapeCsv(i.applicantName)}\",${i.sharesPerLot},${i.cutOffPrice},${i.lotsApplied},${i.totalAmountBlocked},\"${escapeCsv(apply)}\",\"${escapeCsv(allot)}\",\"${escapeCsv(list)}\",\"${escapeCsv(i.status)}\",\"${escapeCsv(i.remarks)}\"\n")
        }
        sb.append("\n")

        // 4. Committees
        sb.append("SECTION,COMMITTEES\n")
        sb.append("Name,TotalAmount,TotalMembers,TotalMonths,StartDate,BaseMonthlyInstallment,CurrentMonth,Remarks\n")
        committees.forEach { c ->
            val start = Calculations.normalizeDateToDdMmYyyy(c.startDate)
            sb.append("\"${escapeCsv(c.name)}\",${c.totalAmount},${c.totalMembers},${c.totalMonths},\"${escapeCsv(start)}\",${c.baseMonthlyInstallment},${c.currentMonth},\"${escapeCsv(c.remarks)}\"\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(str: String): String {
        return str.replace("\"", "\"\"")
    }

    fun parseCsv(csvText: String): ParsedPortfolioData {
        val holdings = mutableListOf<Holding>()
        val sips = mutableListOf<Sip>()
        val ipos = mutableListOf<Ipo>()
        val committees = mutableListOf<Committee>()

        var currentSection = "HOLDINGS"
        var currentHeaderMap = mutableMapOf<String, Int>()
        val lines = csvText.lines()

        fun normalizeKey(s: String) = s.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "")

        fun getCol(tokens: List<String>, vararg keys: String, defaultIdx: Int): String {
            for (k in keys) {
                val nk = normalizeKey(k)
                val idx = currentHeaderMap[nk]
                if (idx != null && idx < tokens.size) {
                    val v = tokens[idx].trim()
                    if (v.isNotBlank()) return v
                }
            }
            return tokens.getOrNull(defaultIdx)?.trim() ?: ""
        }

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isBlank() || line.startsWith("#")) continue

            val upper = line.uppercase()
            if (upper.startsWith("SECTION,HOLDINGS")) {
                currentSection = "HOLDINGS"
                currentHeaderMap.clear()
                continue
            } else if (upper.startsWith("SECTION,SIPS")) {
                currentSection = "SIPS"
                currentHeaderMap.clear()
                continue
            } else if (upper.startsWith("SECTION,IPOS")) {
                currentSection = "IPOS"
                currentHeaderMap.clear()
                continue
            } else if (upper.startsWith("SECTION,COMMITTEES")) {
                currentSection = "COMMITTEES"
                currentHeaderMap.clear()
                continue
            }

            // Auto-detect section if header row is present without SECTION prefix
            if (upper.contains("INSTRUMENT") || upper.contains("FREQUENCY") || (upper.contains("SINCE") && !upper.contains("TOTALMEMBERS"))) {
                currentSection = "SIPS"
                currentHeaderMap.clear()
                val parsedHeader = parseCsvLine(line)
                parsedHeader.forEachIndexed { i, col -> currentHeaderMap[normalizeKey(col)] = i }
                continue
            } else if (upper.contains("COMPANYNAME") || upper.contains("SHARESPERLOT") || upper.contains("CUTOFFPRICE") || upper.contains("APPLYDATE")) {
                currentSection = "IPOS"
                currentHeaderMap.clear()
                val parsedHeader = parseCsvLine(line)
                parsedHeader.forEachIndexed { i, col -> currentHeaderMap[normalizeKey(col)] = i }
                continue
            } else if (upper.contains("TOTALMEMBERS") || upper.contains("TOTALMONTHS") || upper.contains("BASEINSTALLMENT") || upper.contains("HASTAKENPOT")) {
                currentSection = "COMMITTEES"
                currentHeaderMap.clear()
                val parsedHeader = parseCsvLine(line)
                parsedHeader.forEachIndexed { i, col -> currentHeaderMap[normalizeKey(col)] = i }
                continue
            } else if (upper.contains("CATEGORY") && (upper.contains("BREAKDOWN") || upper.contains("TILLDATE") || upper.contains("TILL DATE") || upper.contains("TOTAL"))) {
                currentSection = "HOLDINGS"
                currentHeaderMap.clear()
                val parsedHeader = parseCsvLine(line)
                parsedHeader.forEachIndexed { i, col -> currentHeaderMap[normalizeKey(col)] = i }
                continue
            }

            // Detect any other generic header lines
            if (upper.startsWith("PLATFORM,") || upper.startsWith("COMPANY,") || upper.startsWith("NAME,") || upper.startsWith("ID,")) {
                val parsedHeader = parseCsvLine(line)
                currentHeaderMap.clear()
                parsedHeader.forEachIndexed { i, col -> currentHeaderMap[normalizeKey(col)] = i }
                continue
            }

            val tokens = parseCsvLine(line)
            if (tokens.isEmpty()) continue

            // Determine if row has a leading ID column (e.g. h-1234, s-5678, comm-999, ipo-888 or uuid)
            val firstToken = tokens.getOrNull(0)?.trim() ?: ""
            val hasLeadingId = firstToken.startsWith("h-") || firstToken.startsWith("s-") ||
                    firstToken.startsWith("comm-") || firstToken.startsWith("ipo-") ||
                    (firstToken.length >= 30 && firstToken.contains("-"))
            val idOffset = if (hasLeadingId) 1 else 0

            try {
                when (currentSection) {
                    "HOLDINGS" -> {
                        if (tokens.size >= 2) {
                            val platform = getCol(tokens, "platform", "broker", defaultIdx = 0 + idOffset).ifBlank { "Portfolio" }
                            val category = getCol(tokens, "category", "type", "asset", defaultIdx = 1 + idOffset).ifBlank { "General" }
                            val total = getCol(tokens, "total", "amount", "value", defaultIdx = if (hasLeadingId) 4 else 2).toDoubleOrNull() ?: 0.0
                            val rawDate = getCol(tokens, "tilldate", "till", "date", "asofdate", defaultIdx = if (hasLeadingId) 5 else 3)
                            val tillDate = Calculations.normalizeDateToDdMmYyyy(rawDate).ifBlank {
                                SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                            }
                            val breakdownRaw = getCol(tokens, "breakdown", "items", "details", defaultIdx = if (hasLeadingId) 3 else 4)
                            val remarks = getCol(tokens, "remarks", "remark", "notes", "note", defaultIdx = if (hasLeadingId) 6 else 5)
                            holdings.add(
                                Holding(
                                    id = UUID.randomUUID().toString(),
                                    platform = platform,
                                    category = category,
                                    breakdownRaw = breakdownRaw,
                                    total = total,
                                    tillDate = tillDate,
                                    remarks = remarks
                                )
                            )
                        }
                    }
                    "SIPS" -> {
                        if (tokens.size >= 3) {
                            val platform = getCol(tokens, "platform", "broker", "source", defaultIdx = 0 + idOffset).ifBlank { "Groww" }
                            val instrument = getCol(tokens, "instrument", "fund", "name", "scheme", defaultIdx = 1 + idOffset).ifBlank { "Mutual Fund" }
                            val amount = getCol(tokens, "amount", "monthlyamount", "value", defaultIdx = 2 + idOffset).toDoubleOrNull() ?: 1000.0
                            val frequency = getCol(tokens, "frequency", "freq", "period", defaultIdx = 3 + idOffset).ifBlank { "Monthly" }
                            val rawSince = getCol(tokens, "since", "sincedate", "startdate", "date", defaultIdx = 4 + idOffset)
                            val since = Calculations.normalizeDateToDdMmYyyy(rawSince).ifBlank {
                                SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                            }
                            val remarks = getCol(tokens, "remarks", "remark", "notes", "note", defaultIdx = 5 + idOffset)
                            val rawStatus = getCol(tokens, "status", "state", "ispaused", defaultIdx = 6 + idOffset)
                            val isPaused = rawStatus.equals("Paused", ignoreCase = true) || rawStatus.equals("true", ignoreCase = true)
                            sips.add(
                                Sip(
                                    id = UUID.randomUUID().toString(),
                                    platform = platform,
                                    instrument = instrument,
                                    amount = amount,
                                    frequency = frequency,
                                    since = since,
                                    remarks = remarks,
                                    isPaused = isPaused
                                )
                            )
                        }
                    }
                    "IPOS" -> {
                        if (tokens.size >= 3) {
                            val company = getCol(tokens, "companyname", "company", "name", defaultIdx = 0 + idOffset).ifBlank { "IPO Application" }
                            val category = getCol(tokens, "segment", "category", defaultIdx = 1 + idOffset).ifBlank { "Mainboard" }
                            val platform = getCol(tokens, "platform", "broker", defaultIdx = 2 + idOffset).ifBlank { "Zerodha" }
                            val applicant = getCol(tokens, "applicant", "applicantname", defaultIdx = 3 + idOffset).ifBlank { "Self" }
                            val sharesPerLot = getCol(tokens, "shareslot", "sharesperlot", "shares", defaultIdx = 4 + idOffset).toIntOrNull() ?: 1
                            val cutOffPrice = getCol(tokens, "cutoffprice", "price", defaultIdx = 5 + idOffset).toDoubleOrNull() ?: 0.0
                            val lotsApplied = getCol(tokens, "lotsapplied", "lots", defaultIdx = 6 + idOffset).toIntOrNull() ?: 1
                            val totalAmount = getCol(tokens, "amountblocked", "totalamountblocked", "total", defaultIdx = 7 + idOffset).toDoubleOrNull()
                                ?: (cutOffPrice * sharesPerLot * lotsApplied)
                            val rawApplyDate = getCol(tokens, "applydate", "apply", "date", defaultIdx = 8 + idOffset)
                            val applyDate = Calculations.normalizeDateToDdMmYyyy(rawApplyDate).ifBlank {
                                SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                            }
                            val allotmentDate = Calculations.normalizeDateToDdMmYyyy(getCol(tokens, "allotmentdate", "allotment", defaultIdx = 9 + idOffset))
                            val listingDate = Calculations.normalizeDateToDdMmYyyy(getCol(tokens, "listingdate", "listing", defaultIdx = 10 + idOffset))
                            val status = getCol(tokens, "status", defaultIdx = 11 + idOffset).ifBlank { "Applied" }
                            val remarks = getCol(tokens, "remarks", "remark", "notes", "note", defaultIdx = 12 + idOffset)
                            ipos.add(
                                Ipo(
                                    id = UUID.randomUUID().toString(),
                                    companyName = company,
                                    category = category,
                                    platform = platform,
                                    applicantName = applicant,
                                    sharesPerLot = sharesPerLot,
                                    cutOffPrice = cutOffPrice,
                                    lotsApplied = lotsApplied,
                                    totalAmountBlocked = totalAmount,
                                    applyDate = applyDate,
                                    allotmentDate = allotmentDate,
                                    listingDate = listingDate,
                                    status = status,
                                    remarks = remarks
                                )
                            )
                        }
                    }
                    "COMMITTEES" -> {
                        if (tokens.size >= 2) {
                            val name = getCol(tokens, "name", "committee", defaultIdx = 0 + idOffset).ifBlank { "Committee" }
                            val totalAmount = getCol(tokens, "totalamount", "amount", "total", defaultIdx = 1 + idOffset).toDoubleOrNull() ?: 100000.0
                            val members = getCol(tokens, "totalmembers", "members", defaultIdx = 2 + idOffset).toIntOrNull() ?: 12
                            val months = getCol(tokens, "totalmonths", "months", defaultIdx = 3 + idOffset).toIntOrNull() ?: members
                            val rawStartDate = getCol(tokens, "startdate", "start", "date", defaultIdx = 4 + idOffset)
                            val startDate = Calculations.normalizeDateToDdMmYyyy(rawStartDate).ifBlank {
                                SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
                            }
                            val baseInstallment = getCol(tokens, "baseinstallment", "installment", defaultIdx = 5 + idOffset).toDoubleOrNull() ?: (totalAmount / members)
                            val currentMonth = getCol(tokens, "currentmonth", defaultIdx = 6 + idOffset).toIntOrNull() ?: 1
                            val remarks = getCol(tokens, "remarks", "remark", "notes", "note", defaultIdx = 7 + idOffset)
                            committees.add(
                                Committee(
                                    id = UUID.randomUUID().toString(),
                                    name = name,
                                    totalAmount = totalAmount,
                                    totalMembers = members,
                                    totalMonths = months,
                                    startDate = startDate,
                                    baseMonthlyInstallment = baseInstallment,
                                    currentMonth = currentMonth,
                                    remarks = remarks
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore single corrupt line
            }
        }

        return ParsedPortfolioData(holdings, sips, ipos, committees)
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = java.lang.StringBuilder()
        var inQuotes = false

        for (i in line.indices) {
            val c = line[i]
            if (c == '\"') {
                inQuotes = !inQuotes
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
        }
        result.add(sb.toString().trim())
        return result
    }

    // ==========================================
    // 2. EXCEL XML SPREADSHEET EXPORT (.xls)
    // ==========================================

    fun generateExcelXml(
        holdings: List<Holding>,
        sips: List<Sip>,
        ipos: List<Ipo>,
        committees: List<Committee>
    ): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\"?>\n")
        sb.append("<?mso-application progid=\"Excel.Sheet\"?>\n")
        sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        sb.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n")
        sb.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n")
        sb.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        sb.append(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">\n")

        // Styles
        sb.append("<Styles>\n")
        sb.append("<Style ss:ID=\"Default\" ss:Name=\"Normal\">\n")
        sb.append(" <Alignment ss:Vertical=\"Center\"/>\n")
        sb.append(" <Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Color=\"#0F172A\"/>\n")
        sb.append("</Style>\n")
        sb.append("<Style ss:ID=\"Header\">\n")
        sb.append(" <Font ss:FontName=\"Calibri\" ss:Size=\"11\" ss:Bold=\"1\" ss:Color=\"#FFFFFF\"/>\n")
        sb.append(" <Interior ss:Color=\"#059669\" ss:Pattern=\"Solid\"/>\n")
        sb.append(" <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        sb.append("</Style>\n")
        sb.append("<Style ss:ID=\"Currency\">\n")
        sb.append(" <NumberFormat ss:Format=\"₹#,##0.00\"/>\n")
        sb.append("</Style>\n")
        sb.append("<Style ss:ID=\"Integer\">\n")
        sb.append(" <NumberFormat ss:Format=\"#,##0\"/>\n")
        sb.append("</Style>\n")
        sb.append("</Styles>\n")

        // Worksheet 1: Holdings
        sb.append("<Worksheet ss:Name=\"Holdings\">\n")
        sb.append("<Table>\n")
        sb.append("<Row ss:StyleID=\"Header\">\n")
        listOf("Platform", "Category", "Total Value", "Till Date", "Breakdown", "Remarks").forEach {
            sb.append("<Cell><Data ss:Type=\"String\">$it</Data></Cell>\n")
        }
        sb.append("</Row>\n")
        holdings.forEach { h ->
            sb.append("<Row>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(h.platform)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(h.category)}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">${h.total}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(h.tillDate)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(h.breakdownRaw)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(h.remarks)}</Data></Cell>\n")
            sb.append("</Row>\n")
        }
        sb.append("</Table>\n")
        sb.append("</Worksheet>\n")

        // Worksheet 2: SIPs
        sb.append("<Worksheet ss:Name=\"SIPs\">\n")
        sb.append("<Table>\n")
        sb.append("<Row ss:StyleID=\"Header\">\n")
        listOf("Platform", "Instrument", "Monthly Amount", "Frequency", "Since Date", "Invested Till Date", "Remarks", "Status").forEach {
            sb.append("<Cell><Data ss:Type=\"String\">$it</Data></Cell>\n")
        }
        sb.append("</Row>\n")
        sips.forEach { s ->
            val invested = Calculations.calculateSipInvested(s)
            val status = if (s.isPaused) "Paused" else "Active"
            sb.append("<Row>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(s.platform)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(s.instrument)}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">${s.amount}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(s.frequency)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(s.since)}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">$invested</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(s.remarks)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">$status</Data></Cell>\n")
            sb.append("</Row>\n")
        }
        sb.append("</Table>\n")
        sb.append("</Worksheet>\n")

        // Worksheet 3: IPOs
        sb.append("<Worksheet ss:Name=\"IPOs\">\n")
        sb.append("<Table>\n")
        sb.append("<Row ss:StyleID=\"Header\">\n")
        listOf("Company Name", "Category", "Platform", "Applicant", "Shares/Lot", "Cut-Off Price", "Lots", "Amount Blocked", "Apply Date", "Allotment Date", "Listing Date", "Status", "Remarks").forEach {
            sb.append("<Cell><Data ss:Type=\"String\">$it</Data></Cell>\n")
        }
        sb.append("</Row>\n")
        ipos.forEach { i ->
            sb.append("<Row>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(i.companyName)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(i.category)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(i.platform)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(i.applicantName)}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Integer\"><Data ss:Type=\"Number\">${i.sharesPerLot}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">${i.cutOffPrice}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Integer\"><Data ss:Type=\"Number\">${i.lotsApplied}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">${i.totalAmountBlocked}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(i.applyDate)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(i.allotmentDate)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(i.listingDate)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(i.status)}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(i.remarks)}</Data></Cell>\n")
            sb.append("</Row>\n")
        }
        sb.append("</Table>\n")
        sb.append("</Worksheet>\n")

        // Worksheet 4: Committees
        sb.append("<Worksheet ss:Name=\"Committees\">\n")
        sb.append("<Table>\n")
        sb.append("<Row ss:StyleID=\"Header\">\n")
        listOf("Committee Name", "Total Amount", "Total Members", "Total Months", "Start Date", "Base Installment", "Current Month", "Invested Till Date", "Remarks").forEach {
            sb.append("<Cell><Data ss:Type=\"String\">$it</Data></Cell>\n")
        }
        sb.append("</Row>\n")
        committees.forEach { c ->
            val invested = Calculations.calculateCommitteeInvestedTillDate(c)
            sb.append("<Row>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(c.name)}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">${c.totalAmount}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Integer\"><Data ss:Type=\"Number\">${c.totalMembers}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Integer\"><Data ss:Type=\"Number\">${c.totalMonths}</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(c.startDate)}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">${c.baseMonthlyInstallment}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Integer\"><Data ss:Type=\"Number\">${c.currentMonth}</Data></Cell>\n")
            sb.append("<Cell ss:StyleID=\"Currency\"><Data ss:Type=\"Number\">$invested</Data></Cell>\n")
            sb.append("<Cell><Data ss:Type=\"String\">${escapeXml(c.remarks)}</Data></Cell>\n")
            sb.append("</Row>\n")
        }
        sb.append("</Table>\n")
        sb.append("</Worksheet>\n")

        sb.append("</Workbook>\n")
        return sb.toString()
    }

    private fun escapeXml(str: String): String {
        return str
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    // ==========================================
    // 3. PDF REPORT EXPORT
    // ==========================================

    fun generatePdf(
        context: Context,
        holdings: List<Holding>,
        sips: List<Sip>,
        ipos: List<Ipo>,
        committees: List<Committee>
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Background
        canvas.drawColor(AndroidColor.parseColor("#F8FAFC"))

        // Top Emerald Banner
        paint.color = AndroidColor.parseColor("#059669")
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Header Text
        paint.color = AndroidColor.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Investment Tracker", 24f, 38f, paint)

        paint.textSize = 10f
        paint.isFakeBoldText = false
        val exportTime = dateFormatter.format(Date())
        canvas.drawText("Portfolio Summary & Asset Statement • Generated: $exportTime", 24f, 58f, paint)

        // Total KPIs using Calculations helper
        val holdingsTotal = Calculations.calculateHoldingsTotal(holdings)
        val sipsTotal = Calculations.calculateSipsTotal(sips)
        val committeesTotal = Calculations.calculateCommitteesTotal(committees)
        val ipoBlocked = Calculations.calculateIposBlockedTotal(ipos)
        val grandTotal = holdingsTotal + sipsTotal + committeesTotal + ipoBlocked

        val sipMonthly = Calculations.calculateSipMonthlyCommitment(sips)
        val committeeMonthly = Calculations.calculateCommitteesMonthlyOutflow(committees)
        val monthlyOutflow = sipMonthly + committeeMonthly

        var currentY = 95f

        // 3 Summary Cards
        val cardWidth = 170f
        val cardHeight = 52f
        val cardY = currentY

        // Card 1: Total Portfolio
        drawKpiCard(canvas, paint, 24f, cardY, cardWidth, cardHeight, "NET PORTFOLIO VALUE", Calculations.formatCurrency(grandTotal), "#059669")
        // Card 2: Holdings & SIPs
        drawKpiCard(canvas, paint, 212f, cardY, cardWidth, cardHeight, "HOLDINGS + SIPS", Calculations.formatCurrency(holdingsTotal + sipsTotal), "#334155")
        // Card 3: Monthly Commitments
        drawKpiCard(canvas, paint, 400f, cardY, cardWidth, cardHeight, "MONTHLY OUTFLOW", "${Calculations.formatCurrency(monthlyOutflow)}/mo", "#0284C7")

        currentY += cardHeight + 20f

        // Table 1: Holdings Summary
        paint.color = AndroidColor.parseColor("#0F172A")
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Holdings Portfolio (${holdings.size} items • ${Calculations.formatCurrency(holdingsTotal)})", 24f, currentY, paint)
        currentY += 14f

        // Table Header
        paint.color = AndroidColor.parseColor("#E2E8F0")
        canvas.drawRoundRect(RectF(24f, currentY, 571f, currentY + 20f), 4f, 4f, paint)

        paint.color = AndroidColor.parseColor("#475569")
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("PLATFORM", 32f, currentY + 14f, paint)
        canvas.drawText("CATEGORY", 160f, currentY + 14f, paint)
        canvas.drawText("TOTAL VALUE", 290f, currentY + 14f, paint)
        canvas.drawText("TILL DATE", 430f, currentY + 14f, paint)
        currentY += 24f

        // Table Rows
        paint.isFakeBoldText = false
        paint.textSize = 9f
        holdings.take(8).forEach { h ->
            paint.color = AndroidColor.parseColor("#1E293B")
            canvas.drawText(h.platform.take(18), 32f, currentY + 11f, paint)
            canvas.drawText(h.category.take(18), 160f, currentY + 11f, paint)
            paint.color = AndroidColor.parseColor("#059669")
            canvas.drawText(Calculations.formatCurrency(h.total), 290f, currentY + 11f, paint)
            paint.color = AndroidColor.parseColor("#64748B")
            canvas.drawText(h.tillDate, 430f, currentY + 11f, paint)

            paint.color = AndroidColor.parseColor("#F1F5F9")
            canvas.drawLine(24f, currentY + 16f, 571f, currentY + 16f, paint)
            currentY += 18f
        }

        currentY += 10f

        // Table 2: Active SIPs
        paint.color = AndroidColor.parseColor("#0F172A")
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("SIP Investments (${sips.size} plans • Invested: ${Calculations.formatCurrency(sipsTotal)})", 24f, currentY, paint)
        currentY += 14f

        paint.color = AndroidColor.parseColor("#E2E8F0")
        canvas.drawRoundRect(RectF(24f, currentY, 571f, currentY + 20f), 4f, 4f, paint)

        paint.color = AndroidColor.parseColor("#475569")
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("INSTRUMENT", 32f, currentY + 14f, paint)
        canvas.drawText("PLATFORM", 200f, currentY + 14f, paint)
        canvas.drawText("AMOUNT", 330f, currentY + 14f, paint)
        canvas.drawText("INVESTED TILL DATE", 450f, currentY + 14f, paint)
        currentY += 24f

        paint.isFakeBoldText = false
        paint.textSize = 9f
        sips.take(5).forEach { s ->
            paint.color = AndroidColor.parseColor("#1E293B")
            canvas.drawText(s.instrument.take(24), 32f, currentY + 11f, paint)
            canvas.drawText(s.platform.take(16), 200f, currentY + 11f, paint)
            canvas.drawText("${Calculations.formatCurrency(s.amount)} (${s.frequency})", 330f, currentY + 11f, paint)
            canvas.drawText(Calculations.formatCurrency(Calculations.calculateSipInvested(s)), 450f, currentY + 11f, paint)

            paint.color = AndroidColor.parseColor("#F1F5F9")
            canvas.drawLine(24f, currentY + 16f, 571f, currentY + 16f, paint)
            currentY += 18f
        }

        currentY += 10f

        // Table 3: IPOs & Committees Overview
        paint.color = AndroidColor.parseColor("#0F172A")
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("IPOs (${ipos.size}) & Committees (${committees.size})", 24f, currentY, paint)
        currentY += 14f

        paint.color = AndroidColor.parseColor("#E2E8F0")
        canvas.drawRoundRect(RectF(24f, currentY, 571f, currentY + 20f), 4f, 4f, paint)

        paint.color = AndroidColor.parseColor("#475569")
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText("NAME / ENTITY", 32f, currentY + 14f, paint)
        canvas.drawText("TYPE", 210f, currentY + 14f, paint)
        canvas.drawText("AMOUNT / VALUE", 350f, currentY + 14f, paint)
        canvas.drawText("STATUS", 470f, currentY + 14f, paint)
        currentY += 24f

        paint.isFakeBoldText = false
        paint.textSize = 9f
        ipos.take(3).forEach { ipo ->
            paint.color = AndroidColor.parseColor("#1E293B")
            canvas.drawText(ipo.companyName.take(24), 32f, currentY + 11f, paint)
            canvas.drawText("IPO Application", 210f, currentY + 11f, paint)
            val blockedAmount = if (ipo.totalAmountBlocked > 0.0) ipo.totalAmountBlocked else (ipo.sharesPerLot * ipo.cutOffPrice * ipo.lotsApplied)
            canvas.drawText(Calculations.formatCurrency(blockedAmount), 350f, currentY + 11f, paint)
            canvas.drawText(ipo.status, 470f, currentY + 11f, paint)
            currentY += 18f
        }

        committees.take(3).forEach { c ->
            paint.color = AndroidColor.parseColor("#1E293B")
            canvas.drawText(c.name.take(24), 32f, currentY + 11f, paint)
            canvas.drawText("Committee (BC)", 210f, currentY + 11f, paint)
            canvas.drawText(Calculations.formatCurrency(c.totalAmount), 350f, currentY + 11f, paint)
            canvas.drawText("Month ${c.currentMonth}/${c.totalMonths}", 470f, currentY + 11f, paint)
            currentY += 18f
        }

        // Footer note
        paint.color = AndroidColor.parseColor("#94A3B8")
        paint.textSize = 8f
        paint.isFakeBoldText = false
        canvas.drawText("Generated by Investment Tracker for Android • All financial calculations are evaluated in real-time.", 24f, 815f, paint)

        pdfDoc.finishPage(page)

        val file = File(context.cacheDir, "Investment_Tracker_Report.pdf")
        FileOutputStream(file).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()
        return file
    }

    private fun drawKpiCard(
        canvas: android.graphics.Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        value: String,
        valueColorHex: String
    ) {
        paint.color = AndroidColor.WHITE
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 6f, 6f, paint)

        paint.color = AndroidColor.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = AndroidColor.parseColor("#64748B")
        paint.textSize = 7.5f
        paint.isFakeBoldText = true
        canvas.drawText(title, x + 10f, y + 18f, paint)

        paint.color = AndroidColor.parseColor(valueColorHex)
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText(value, x + 10f, y + 38f, paint)
    }

    // ==========================================
    // 4. SHARING AND INTENT HELPER
    // ==========================================

    fun shareExportedFile(
        context: Context,
        file: File,
        mimeType: String,
        title: String
    ) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareTextFile(
        context: Context,
        fileName: String,
        mimeType: String,
        content: String,
        title: String
    ): File {
        val file = File(context.cacheDir, fileName)
        file.writeText(content, Charsets.UTF_8)
        shareExportedFile(context, file, mimeType, title)
        return file
    }
}
