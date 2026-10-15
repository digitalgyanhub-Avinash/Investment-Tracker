package com.example.util

import com.example.data.model.Committee
import com.example.data.model.Holding
import com.example.data.model.Ipo
import com.example.data.model.Sip
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.roundToInt

object Calculations {

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    private fun monthNameToNumber(monthName: String): Int {
        return when (monthName.lowercase(Locale.ROOT).take(3)) {
            "jan" -> 1
            "feb" -> 2
            "mar" -> 3
            "apr" -> 4
            "may" -> 5
            "jun" -> 6
            "jul" -> 7
            "aug" -> 8
            "sep" -> 9
            "oct" -> 10
            "nov" -> 11
            "dec" -> 12
            else -> 1
        }
    }

    /**
     * Normalizes any date string from Google Sheets/API/User input to DD-MM-YYYY format.
     * Accurately eliminates timezone-related date shifts (such as UTC midnight vs IST UTC+05:30).
     */
    fun normalizeDateToDdMmYyyy(raw: String): String {
        var trimmed = raw.trim()
        if (trimmed.isBlank() || trimmed == "—" || trimmed == "-" ||
            trimmed.equals("null", ignoreCase = true) ||
            trimmed.equals("undefined", ignoreCase = true) ||
            trimmed.equals("n/a", ignoreCase = true) ||
            trimmed.equals("none", ignoreCase = true)
        ) {
            return ""
        }

        // Remove surrounding quotes if present (e.g., from raw JSON / CSV: "\"2026-08-19\"")
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            trimmed = trimmed.substring(1, trimmed.length - 1).trim()
        }
        if (trimmed.isBlank()) return ""

        val istTz = TimeZone.getTimeZone("Asia/Kolkata")

        // 1. ISO-8601 strings with time component (e.g. "2026-08-18T18:30:00.000Z", "2026-08-19T00:00:00.000Z", "2026-08-19T05:30:00+05:30")
        // Google Sheets / Apps Script JSON.stringify converts Date objects to ISO UTC (e.g. 19-Aug 00:00 IST -> 2026-08-18T18:30:00.000Z)
        if (trimmed.contains("T", ignoreCase = true)) {
            val isoPatterns = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss"
            )
            for (pattern in isoPatterns) {
                try {
                    val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                        isLenient = false
                    }
                    val parsed = sdf.parse(trimmed)
                    if (parsed != null) {
                        // Format in India Standard Time (Asia/Kolkata) to eliminate timezone date shifts
                        val outSdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).apply {
                            timeZone = istTz
                        }
                        return outSdf.format(parsed)
                    }
                } catch (_: Exception) {}
            }

            // Fallback for ISO with regex if parsing failed
            val isoRegex = Regex("^(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})[T ](\\d{1,2}):(\\d{1,2})")
            val matchIso = isoRegex.find(trimmed)
            if (matchIso != null) {
                val y = matchIso.groupValues[1].toInt()
                val m = matchIso.groupValues[2].toInt()
                val d = matchIso.groupValues[3].toInt()
                val hh = matchIso.groupValues[4].toInt()
                val mm = matchIso.groupValues[5].toInt()
                val isUtc = trimmed.endsWith("Z", ignoreCase = true) || trimmed.contains("+00") || trimmed.contains("-00")

                if (isUtc) {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    cal.set(y, m - 1, d, hh, mm, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val istCal = Calendar.getInstance(istTz)
                    istCal.timeInMillis = cal.timeInMillis
                    return String.format(
                        Locale.ROOT, "%02d-%02d-%04d",
                        istCal.get(Calendar.DAY_OF_MONTH),
                        istCal.get(Calendar.MONTH) + 1,
                        istCal.get(Calendar.YEAR)
                    )
                } else {
                    return String.format(Locale.ROOT, "%02d-%02d-%04d", d, m, y)
                }
            }
        }

        // 2. Pure Year-Month-Day with or without time (e.g. "2026-08-19", "2026/08/19", "2026.08.19", "2026-08-19 00:00:00")
        val yyyyMmDdRegex = Regex("^(\\d{4})[-/. ](\\d{1,2})[-/. ](\\d{1,2})(?:[ T].*)?$")
        val matchYmd = yyyyMmDdRegex.matchEntire(trimmed)
        if (matchYmd != null) {
            val y = matchYmd.groupValues[1].toInt()
            val m = matchYmd.groupValues[2].toInt()
            val d = matchYmd.groupValues[3].toInt()
            if (m in 1..12 && d in 1..31) {
                return String.format(Locale.ROOT, "%02d-%02d-%04d", d, m, y)
            }
        }

        // 3. Day-Month-Year OR Month-Day-Year with or without time (e.g. "19-08-2026", "08/19/2026", "19/08/2026", "19.08.2026", "03-10-26", "03/10/2026 00:00:00")
        val dmyRegex = Regex("^(\\d{1,2})[-/. ](\\d{1,2})[-/. ](\\d{2,4})(?:[ T].*)?$")
        val matchDmy = dmyRegex.matchEntire(trimmed)
        if (matchDmy != null) {
            val part1 = matchDmy.groupValues[1].toInt()
            val part2 = matchDmy.groupValues[2].toInt()
            var year = matchDmy.groupValues[3].toInt()
            if (year < 100) year += 2000

            val (day, month) = when {
                // If part1 > 12, part1 MUST be day (e.g. 19-08-2026 or 19/08/2026)
                part1 > 12 -> Pair(part1, part2)
                // If part2 > 12, part2 MUST be day (e.g. 08/19/2026 -> US format MM/DD/YYYY)
                part2 > 12 -> Pair(part2, part1)
                // Both <= 12 (e.g. 05-08-2026 or 05/08/2026): Indian convention is strictly DD-MM-YYYY
                else -> Pair(part1, part2)
            }

            if (month in 1..12 && day in 1..31) {
                return String.format(Locale.ROOT, "%02d-%02d-%04d", day, month, year)
            }
        }

        // 4. Excel / Google Sheets Serial Number (e.g. "46253" or "46253.0" -> 19-Aug-2026)
        val serialDouble = trimmed.toDoubleOrNull()
        if (serialDouble != null && serialDouble >= 20000.0 && serialDouble <= 80000.0) {
            val epochMillis = kotlin.math.round((serialDouble - 25569.0) * 86400000.0).toLong()
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cal.timeInMillis = epochMillis
            return String.format(
                Locale.ROOT,
                "%02d-%02d-%04d",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR)
            )
        }

        // 5. Epoch timestamp in milliseconds (13 digits) or seconds (10 digits)
        if (trimmed.matches(Regex("^\\d{10,13}$"))) {
            val num = trimmed.toLongOrNull()
            if (num != null) {
                val millis = if (trimmed.length == 10) num * 1000L else num
                val cal = Calendar.getInstance(istTz)
                cal.timeInMillis = millis
                return String.format(
                    Locale.ROOT,
                    "%02d-%02d-%04d",
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR)
                )
            }
        }

        // 6. Formats with textual month names:
        // E.g. "19-Aug-2026", "19 Aug 2026", "19-August-2026", "19 August 2026", "19-Aug-26"
        val dayMonthTextRegex = Regex("^(\\d{1,2})[-/ ]([A-Za-z]+)[-/ ](\\d{2,4})(?:[ T].*)?$")
        val matchDmt = dayMonthTextRegex.matchEntire(trimmed) ?: dayMonthTextRegex.find(trimmed)
        if (matchDmt != null) {
            val day = matchDmt.groupValues[1].toIntOrNull() ?: 1
            val monthStr = matchDmt.groupValues[2]
            var year = matchDmt.groupValues[3].toIntOrNull() ?: 2026
            if (year < 100) year += 2000
            val month = monthNameToNumber(monthStr)
            return String.format(Locale.ROOT, "%02d-%02d-%04d", day, month, year)
        }

        // E.g. "Aug 19, 2026", "August 19, 2026", "Aug 19 2026"
        val monthDayTextRegex = Regex("^([A-Za-z]+)\\s+(\\d{1,2})[-,\\s]+(\\d{2,4})(?:[ T].*)?$")
        val matchMdt = monthDayTextRegex.matchEntire(trimmed) ?: monthDayTextRegex.find(trimmed)
        if (matchMdt != null) {
            val monthStr = matchMdt.groupValues[1]
            val day = matchMdt.groupValues[2].toIntOrNull() ?: 1
            var year = matchMdt.groupValues[3].toIntOrNull() ?: 2026
            if (year < 100) year += 2000
            val month = monthNameToNumber(monthStr)
            return String.format(Locale.ROOT, "%02d-%02d-%04d", day, month, year)
        }

        // 7. JavaScript / RFC Date toString format:
        // E.g. "Wed Aug 19 2026 00:00:00 GMT+0530 (India Standard Time)" or "Wed, 19 Aug 2026 18:30:00 GMT"
        val jsDateRegex1 = Regex("^[A-Za-z]{3},?\\s+([A-Za-z]{3})\\s+(\\d{1,2})\\s+(\\d{4})")
        val matchJs1 = jsDateRegex1.find(trimmed)
        if (matchJs1 != null) {
            val monthStr = matchJs1.groupValues[1]
            val day = matchJs1.groupValues[2].toIntOrNull() ?: 1
            val year = matchJs1.groupValues[3].toIntOrNull() ?: 2026
            val month = monthNameToNumber(monthStr)
            return String.format(Locale.ROOT, "%02d-%02d-%04d", day, month, year)
        }

        val jsDateRegex2 = Regex("^[A-Za-z]{3},?\\s+(\\d{1,2})\\s+([A-Za-z]{3})\\s+(\\d{4})")
        val matchJs2 = jsDateRegex2.find(trimmed)
        if (matchJs2 != null) {
            val day = matchJs2.groupValues[1].toIntOrNull() ?: 1
            val monthStr = matchJs2.groupValues[2]
            val year = matchJs2.groupValues[3].toIntOrNull() ?: 2026
            val month = monthNameToNumber(monthStr)
            if (trimmed.contains("GMT", ignoreCase = true) || trimmed.contains("UTC", ignoreCase = true)) {
                val timeMatch = Regex("(\\d{1,2}):(\\d{1,2})").find(trimmed)
                if (timeMatch != null) {
                    val hh = timeMatch.groupValues[1].toInt()
                    val mm = timeMatch.groupValues[2].toInt()
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    cal.set(year, month - 1, day, hh, mm, 0)
                    val istCal = Calendar.getInstance(istTz)
                    istCal.timeInMillis = cal.timeInMillis
                    return String.format(
                        Locale.ROOT, "%02d-%02d-%04d",
                        istCal.get(Calendar.DAY_OF_MONTH),
                        istCal.get(Calendar.MONTH) + 1,
                        istCal.get(Calendar.YEAR)
                    )
                }
            }
            return String.format(Locale.ROOT, "%02d-%02d-%04d", day, month, year)
        }

        // 8. General fallback with standard SimpleDateFormat patterns
        val fallbackPatterns = listOf(
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "dd-MMM-yyyy",
            "yyyy/MM/dd",
            "dd.MM.yyyy",
            "yyyy.MM.dd",
            "dd-MM-yy",
            "dd/MM/yy",
            "yyyy-MM-dd HH:mm:ss",
            "dd-MM-yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "EEE MMM dd HH:mm:ss z yyyy"
        )
        for (pattern in fallbackPatterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    isLenient = false
                }
                val date = sdf.parse(trimmed)
                if (date != null) {
                    val outSdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).apply {
                        timeZone = istTz
                    }
                    return outSdf.format(date)
                }
            } catch (_: Exception) {}
        }

        return trimmed
    }

    fun parseDate(dateStr: String): Date {
        if (dateStr.isBlank()) return Date()
        val normalized = normalizeDateToDdMmYyyy(dateStr)
        try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                isLenient = false
            }
            val date = sdf.parse(normalized)
            if (date != null) return date
        } catch (_: Exception) {}

        val patterns = listOf(
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "dd-MMM-yyyy",
            "yyyy/MM/dd"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                    isLenient = false
                }
                val date = sdf.parse(dateStr.trim())
                if (date != null) return date
            } catch (_: Exception) {}
        }
        return Date()
    }

    fun formatDate(date: Date, pattern: String = "dd-MM-yyyy"): String {
        val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
        return sdf.format(date)
    }

    fun getCommitteeMonthLabel(startDateStr: String, monthNumber: Int, fallbackLabel: String = ""): String {
        if (fallbackLabel.isNotBlank() && !fallbackLabel.startsWith("Month ", ignoreCase = true)) {
            return fallbackLabel
        }
        try {
            val date = parseDate(startDateStr)
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
            cal.time = date
            cal.add(Calendar.MONTH, max(0, monthNumber - 1))
            val sdf = SimpleDateFormat("MMM yyyy", Locale.ENGLISH).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            }
            return sdf.format(cal.time)
        } catch (_: Exception) {
            return fallbackLabel.ifBlank { "Month $monthNumber" }
        }
    }

    fun parseDateToMillis(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L
        val trimmed = dateStr.trim()
        val normalized = normalizeDateToDdMmYyyy(trimmed)
        try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                isLenient = false
            }
            val date = sdf.parse(normalized)
            if (date != null) return date.time
        } catch (_: Exception) {}

        val patterns = listOf(
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "yyyy/MM/dd",
            "dd-MMM-yyyy",
            "dd.MM.yyyy",
            "yyyy.MM.dd",
            "d-M-yyyy",
            "yyyy-M-d",
            "d/M/yyyy"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                    isLenient = false
                }
                val date = sdf.parse(trimmed)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return 0L
    }

    fun getHoldingTimestamp(holding: Holding): Long {
        val dateMillis = parseDateToMillis(holding.tillDate)
        return if (dateMillis > 0L) dateMillis else holding.updatedAt
    }

    fun sortHoldingsRecentFirst(holdings: List<Holding>): List<Holding> {
        return holdings.sortedWith(
            compareByDescending<Holding> { getHoldingTimestamp(it) }
                .thenByDescending { it.updatedAt }
                .thenByDescending { it.id }
        )
    }

    fun getSipTimestamp(sip: Sip): Long {
        val dateMillis = parseDateToMillis(sip.since)
        return if (dateMillis > 0L) dateMillis else sip.updatedAt
    }

    fun sortSipsRecentFirst(sips: List<Sip>): List<Sip> {
        return sips.sortedWith(
            compareByDescending<Sip> { getSipTimestamp(it) }
                .thenByDescending { it.updatedAt }
                .thenByDescending { it.id }
        )
    }

    fun getIpoTimestamp(ipo: Ipo): Long {
        val dates = listOf(ipo.listingDate, ipo.allotmentDate, ipo.refundDate, ipo.applyDate)
        val maxMillis = dates.map { parseDateToMillis(it) }.maxOrNull() ?: 0L
        return if (maxMillis > 0L) maxMillis else ipo.updatedAt
    }

    fun sortIposRecentFirst(ipos: List<Ipo>): List<Ipo> {
        return ipos.sortedWith(
            compareByDescending<Ipo> { getIpoTimestamp(it) }
                .thenByDescending { it.updatedAt }
                .thenByDescending { it.id }
        )
    }

    fun getCommitteeTimestamp(committee: Committee): Long {
        val startMillis = parseDateToMillis(committee.startDate)
        return if (startMillis > 0L) startMillis else committee.updatedAt
    }

    fun sortCommitteesRecentFirst(committees: List<Committee>): List<Committee> {
        return committees.sortedWith(
            compareByDescending<Committee> { getCommitteeTimestamp(it) }
                .thenByDescending { it.updatedAt }
                .thenByDescending { it.id }
        )
    }

    fun parseBreakdownString(input: String): List<Double> {
        if (input.isBlank()) return emptyList()

        // Replace k/K suffixes e.g. 17.8K -> 17800
        var normalized = input.replace(Regex("([\\d.]+)\\s*[kK]")) { match ->
            val num = match.groupValues[1].toDoubleOrNull() ?: 0.0
            (num * 1000).roundToInt().toString()
        }

        // Replace '+' with ',' and '-' with ',-'
        normalized = normalized.replace("+", ",")
        normalized = normalized.replace(Regex("-(?=\\d)"), ",-")

        return normalized.split(Regex("[\\n,]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toDoubleOrNull() }
    }

    fun sumBreakdown(breakdown: List<Double>): Double {
        return breakdown.sum()
    }

    fun calculateHoldingsTotal(holdings: List<Holding>): Double {
        return holdings.sumOf { it.total }
    }

    fun calculateSipInvested(sip: Sip, targetDate: Date = Date()): Double {
        if (sip.since.isBlank() || sip.amount <= 0.0) return 0.0

        val sinceDate = parseDate(sip.since)
        if (sinceDate.after(targetDate)) return 0.0

        val diffTime = targetDate.time - sinceDate.time
        val diffDays = (diffTime / (1000L * 60 * 60 * 24)).toInt() + 1

        val freq = sip.frequency.lowercase(Locale.ROOT)
        return when (freq) {
            "daily" -> diffDays * sip.amount
            "weekly" -> ((diffDays / 7) + 1) * sip.amount
            "monthly" -> {
                val calSince = Calendar.getInstance().apply { time = sinceDate }
                val calEnd = Calendar.getInstance().apply { time = targetDate }

                val yearDiff = calEnd.get(Calendar.YEAR) - calSince.get(Calendar.YEAR)
                val monthDiff = calEnd.get(Calendar.MONTH) - calSince.get(Calendar.MONTH)
                var totalMonths = yearDiff * 12 + monthDiff

                if (calEnd.get(Calendar.DAY_OF_MONTH) >= calSince.get(Calendar.DAY_OF_MONTH)) {
                    totalMonths += 1
                }
                max(1, totalMonths) * sip.amount
            }
            "yearly" -> {
                val calSince = Calendar.getInstance().apply { time = sinceDate }
                val calEnd = Calendar.getInstance().apply { time = targetDate }
                val yearDiff = calEnd.get(Calendar.YEAR) - calSince.get(Calendar.YEAR) + 1
                max(1, yearDiff) * sip.amount
            }
            else -> sip.amount
        }
    }

    fun calculateSipsTotal(sips: List<Sip>): Double {
        return sips.sumOf { calculateSipInvested(it) }
    }

    fun calculateSipMonthlyCommitment(sips: List<Sip>): Double {
        return sips.filter { !it.isPaused }.sumOf { sip ->
            when (sip.frequency.lowercase(Locale.ROOT)) {
                "monthly" -> sip.amount
                "daily" -> sip.amount * 30.0
                "weekly" -> sip.amount * 4.33
                "yearly" -> sip.amount / 12.0
                else -> sip.amount
            }
        }
    }

    fun calculateIposBlockedTotal(ipos: List<Ipo>): Double {
        return ipos.filter { it.status.equals("Applied", ignoreCase = true) }
            .sumOf { ipo ->
                if (ipo.totalAmountBlocked > 0.0) ipo.totalAmountBlocked
                else (ipo.sharesPerLot * ipo.cutOffPrice * ipo.lotsApplied)
            }
    }

    fun calculateIposTotalGain(ipos: List<Ipo>): Double {
        return ipos.sumOf { ipo ->
            if (ipo.listingGainAmount != 0.0) {
                ipo.listingGainAmount
            } else if (ipo.status.equals("Listed", ignoreCase = true) && ipo.listingPrice > 0.0) {
                val lots = if (ipo.allottedLots > 0) ipo.allottedLots else ipo.lotsApplied
                (ipo.listingPrice - ipo.cutOffPrice) * ipo.sharesPerLot * lots
            } else 0.0
        }
    }

    fun calculateIposAllotmentRate(ipos: List<Ipo>): Triple<Int, Int, Int> {
        if (ipos.isEmpty()) return Triple(0, 0, 0)
        val decided = ipos.filter {
            it.status.equals("Allotted", ignoreCase = true) ||
            it.status.equals("Not Allotted", ignoreCase = true) ||
            it.status.equals("Listed", ignoreCase = true)
        }
        val allotted = ipos.filter {
            it.status.equals("Allotted", ignoreCase = true) ||
            it.status.equals("Listed", ignoreCase = true)
        }
        if (decided.isEmpty()) return Triple(0, allotted.size, 0)
        val rate = ((allotted.size.toDouble() / decided.size) * 100).roundToInt()
        return Triple(rate, allotted.size, decided.size)
    }

    fun isCommitteeCompleted(committee: Committee): Boolean {
        val totalMonths = if (committee.totalMonths > 0) committee.totalMonths else 12
        if (committee.months.isEmpty()) return false
        val paidCount = committee.months.count { it.isPaid }
        return paidCount >= totalMonths
    }

    fun calculateCommitteeInvestedTillDate(committee: Committee): Double {
        if (committee.months.isNotEmpty()) {
            val paidMonths = committee.months.filter { it.isPaid }
            if (paidMonths.isNotEmpty()) {
                return paidMonths.sumOf { it.installmentPerPerson }
            }
        }
        val base = if (committee.baseMonthlyInstallment > 0) committee.baseMonthlyInstallment
        else committee.totalAmount / max(1, committee.totalMembers)
        return committee.currentMonth * base
    }

    fun calculateCommitteeCurrentOutflow(committee: Committee): Double {
        if (isCommitteeCompleted(committee)) return 0.0

        if (committee.months.isNotEmpty()) {
            val latest = committee.months.lastOrNull()
            if (latest != null && latest.installmentPerPerson > 0) {
                return latest.installmentPerPerson
            }
        }

        return if (committee.baseMonthlyInstallment > 0) committee.baseMonthlyInstallment
        else committee.totalAmount / max(1, committee.totalMembers)
    }

    fun calculateCommitteeTotalSaved(committee: Committee): Double {
        val base = if (committee.baseMonthlyInstallment > 0) committee.baseMonthlyInstallment
        else committee.totalAmount / max(1, committee.totalMembers)
        val paidCount = if (committee.months.isNotEmpty()) committee.months.count { it.isPaid } else committee.currentMonth
        val standardInvested = paidCount * base
        val actualInvested = calculateCommitteeInvestedTillDate(committee)
        return max(0.0, standardInvested - actualInvested)
    }

    fun calculateCommitteesTotal(committees: List<Committee>): Double {
        return committees.filter { !isCommitteeCompleted(it) }
            .sumOf { calculateCommitteeInvestedTillDate(it) }
    }

    fun calculateCommitteesMonthlyOutflow(committees: List<Committee>): Double {
        return committees.filter { !isCommitteeCompleted(it) }
            .sumOf { calculateCommitteeCurrentOutflow(it) }
    }

    fun calculateCommitteeProfitLoss(committee: Committee): Double {
        if (!committee.hasTakenPot) return 0.0
        val potAmount = if ((committee.potTakenAmount ?: 0.0) > 0.0) {
            committee.potTakenAmount!!
        } else if (committee.potTakenMonth != null) {
            val m = committee.months.find { it.monthNumber == committee.potTakenMonth }
            if (m != null && m.netPotValue > 0.0) m.netPotValue
            else if (m != null && m.winningBid > 0.0) maxOf(0.0, committee.totalAmount - m.winningBid)
            else 0.0
        } else 0.0

        if (potAmount <= 0.0) return 0.0

        val actualInvested = calculateCommitteeInvestedTillDate(committee)
        if (isCommitteeCompleted(committee)) {
            return potAmount - actualInvested
        }

        val totalMonths = if (committee.totalMonths > 0) committee.totalMonths else 12
        val base = if (committee.baseMonthlyInstallment > 0) committee.baseMonthlyInstallment
        else committee.totalAmount / max(1, committee.totalMembers)

        val paidMonthsCount = if (committee.months.isNotEmpty()) committee.months.count { it.isPaid } else committee.currentMonth
        val remainingMonths = maxOf(0, totalMonths - paidMonthsCount)

        val unpaidLoggedMonths = committee.months.filter { !it.isPaid }
        val unpaidLoggedSum = unpaidLoggedMonths.sumOf { if (it.installmentPerPerson > 0) it.installmentPerPerson else base }
        val unloggedRemainingCount = maxOf(0, remainingMonths - unpaidLoggedMonths.size)
        val estimatedRemaining = unpaidLoggedSum + (unloggedRemainingCount * base)

        val totalExpectedInvestment = actualInvested + estimatedRemaining
        return potAmount - totalExpectedInvestment
    }
}
