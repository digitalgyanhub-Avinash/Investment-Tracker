package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.InitialData
import com.example.data.model.Committee
import com.example.data.model.CommitteeMonth
import com.example.data.model.Holding
import com.example.data.model.Ipo
import com.example.data.model.SheetConfig
import com.example.data.model.Sip
import com.example.data.remote.SheetsService
import com.example.data.repository.InvestmentRepository
import com.example.util.Calculations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvestmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InvestmentRepository
    private val sheetsService = SheetsService()
    private val prefs = application.getSharedPreferences("investment_tracker_prefs", Context.MODE_PRIVATE)

    val holdings: StateFlow<List<Holding>>
    val sips: StateFlow<List<Sip>>
    val ipos: StateFlow<List<Ipo>>
    val committees: StateFlow<List<Committee>>

    private val _sheetConfig = MutableStateFlow(
        run {
            val savedUrl = prefs.getString("sheet_script_url", null)
            val effectiveUrl = if (savedUrl.isNullOrBlank()) SheetConfig.DEFAULT_SCRIPT_URL else savedUrl
            SheetConfig(
                scriptUrl = effectiveUrl,
                sheetId = "",
                isConfigured = effectiveUrl.isNotBlank(),
                lastSyncedAt = prefs.getString("sheet_last_sync", "") ?: "",
                autoSync = prefs.getBoolean("sheet_auto_sync", true)
            )
        }
    )
    val sheetConfig: StateFlow<SheetConfig> = _sheetConfig.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = InvestmentRepository(database.investmentDao())

        holdings = repository.allHoldings
            .map { Calculations.sortHoldingsRecentFirst(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        sips = repository.allSips
            .map { Calculations.sortSipsRecentFirst(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        ipos = repository.allIpos
            .map { Calculations.sortIposRecentFirst(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        committees = repository.allCommittees
            .map { Calculations.sortCommitteesRecentFirst(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Ensure all existing dates in the database are normalized to DD-MM-YYYY format
        viewModelScope.launch {
            try {
                val currentHoldings = repository.allHoldings.first()
                currentHoldings.forEach { h ->
                    val norm = Calculations.normalizeDateToDdMmYyyy(h.tillDate)
                    if (norm.isNotBlank() && norm != h.tillDate) {
                        repository.insertHolding(h.copy(tillDate = norm))
                    }
                }
                val currentSips = repository.allSips.first()
                currentSips.forEach { s ->
                    val norm = Calculations.normalizeDateToDdMmYyyy(s.since)
                    if (norm.isNotBlank() && norm != s.since) {
                        repository.insertSip(s.copy(since = norm))
                    }
                }
                val currentIpos = repository.allIpos.first()
                currentIpos.forEach { i ->
                    val normApply = Calculations.normalizeDateToDdMmYyyy(i.applyDate)
                    val normAllot = Calculations.normalizeDateToDdMmYyyy(i.allotmentDate)
                    val normRefund = Calculations.normalizeDateToDdMmYyyy(i.refundDate)
                    val normList = Calculations.normalizeDateToDdMmYyyy(i.listingDate)
                    val repairedBlocked = if (i.totalAmountBlocked <= 0.0 && i.sharesPerLot > 0 && i.cutOffPrice > 0.0 && i.lotsApplied > 0) {
                        i.sharesPerLot * i.cutOffPrice * i.lotsApplied
                    } else i.totalAmountBlocked

                    if (normApply != i.applyDate || normAllot != i.allotmentDate || normRefund != i.refundDate || normList != i.listingDate || repairedBlocked != i.totalAmountBlocked) {
                        repository.insertIpo(
                            i.copy(
                                applyDate = normApply,
                                allotmentDate = normAllot,
                                refundDate = normRefund,
                                listingDate = normList,
                                totalAmountBlocked = repairedBlocked
                            )
                        )
                    }
                }
                val currentCommittees = repository.allCommittees.first()
                currentCommittees.forEach { c ->
                    val norm = Calculations.normalizeDateToDdMmYyyy(c.startDate)
                    if (norm.isNotBlank() && norm != c.startDate) {
                        repository.insertCommittee(c.copy(startDate = norm))
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    // --- Holdings ---

    fun saveHolding(holding: Holding) {
        val cleanHolding = holding.copy(tillDate = Calculations.normalizeDateToDdMmYyyy(holding.tillDate))
        viewModelScope.launch {
            val isEdit = holdings.value.any { it.id == cleanHolding.id }
            repository.insertHolding(cleanHolding)
            showToast("Holding saved: ${cleanHolding.platform} - ${cleanHolding.category}")

            val config = _sheetConfig.value
            if (config.isConfigured && config.autoSync && config.scriptUrl.isNotBlank()) {
                val syncRes = sheetsService.saveHolding(config.scriptUrl, cleanHolding, isEdit)
                if (syncRes.success) {
                    showToast("Synced to Google Sheet")
                } else {
                    showToast("Sheet update warning: ${syncRes.message}")
                }
            }
        }
    }

    fun deleteHolding(holding: Holding) {
        viewModelScope.launch {
            repository.deleteHolding(holding)
            showToast("Holding removed")

            val config = _sheetConfig.value
            if (config.isConfigured && config.autoSync && config.scriptUrl.isNotBlank()) {
                val syncRes = sheetsService.deleteHolding(config.scriptUrl, holding.id)
                if (syncRes.success) {
                    showToast("Deleted from Google Sheet")
                } else {
                    showToast("Sheet deletion warning: ${syncRes.message}")
                }
            }
        }
    }

    // --- SIPs ---

    fun saveSip(sip: Sip) {
        val cleanSip = sip.copy(since = Calculations.normalizeDateToDdMmYyyy(sip.since))
        viewModelScope.launch {
            val isEdit = sips.value.any { it.id == cleanSip.id }
            repository.insertSip(cleanSip)
            showToast("SIP saved: ${cleanSip.instrument}")

            val config = _sheetConfig.value
            if (config.isConfigured && config.autoSync && config.scriptUrl.isNotBlank()) {
                val syncRes = sheetsService.saveSip(config.scriptUrl, cleanSip, isEdit)
                if (syncRes.success) {
                    showToast("Synced to Google Sheet")
                } else {
                    showToast("Sheet update warning: ${syncRes.message}")
                }
            }
        }
    }

    fun deleteSip(sip: Sip) {
        viewModelScope.launch {
            repository.deleteSip(sip)
            showToast("SIP removed")

            val config = _sheetConfig.value
            if (config.isConfigured && config.autoSync && config.scriptUrl.isNotBlank()) {
                val syncRes = sheetsService.deleteSip(config.scriptUrl, sip.id)
                if (syncRes.success) {
                    showToast("Deleted from Google Sheet")
                } else {
                    showToast("Sheet deletion warning: ${syncRes.message}")
                }
            }
        }
    }

    fun toggleSipPause(sip: Sip) {
        viewModelScope.launch {
            val updated = sip.copy(
                isPaused = !sip.isPaused,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertSip(updated)
            showToast(if (updated.isPaused) "SIP paused: ${updated.instrument}" else "SIP resumed: ${updated.instrument}")

            val config = _sheetConfig.value
            if (config.isConfigured && config.autoSync && config.scriptUrl.isNotBlank()) {
                val syncRes = sheetsService.saveSip(config.scriptUrl, updated, isEdit = true)
                if (syncRes.success) {
                    showToast(if (updated.isPaused) "SIP paused in Google Sheet" else "SIP resumed in Google Sheet")
                }
            }
        }
    }

    // --- IPOs ---

    fun saveIpo(ipo: Ipo) {
        val computedBlocked = if (ipo.totalAmountBlocked > 0.0) {
            ipo.totalAmountBlocked
        } else if (ipo.sharesPerLot > 0 && ipo.cutOffPrice > 0.0 && ipo.lotsApplied > 0) {
            ipo.sharesPerLot * ipo.cutOffPrice * ipo.lotsApplied
        } else 0.0

        val cleanIpo = ipo.copy(
            totalAmountBlocked = computedBlocked,
            applyDate = Calculations.normalizeDateToDdMmYyyy(ipo.applyDate),
            allotmentDate = Calculations.normalizeDateToDdMmYyyy(ipo.allotmentDate),
            refundDate = Calculations.normalizeDateToDdMmYyyy(ipo.refundDate),
            listingDate = Calculations.normalizeDateToDdMmYyyy(ipo.listingDate)
        )
        viewModelScope.launch {
            val isEdit = ipos.value.any { it.id == cleanIpo.id }
            repository.insertIpo(cleanIpo)
            showToast("IPO saved: ${cleanIpo.companyName}")

            val config = _sheetConfig.value
            if (config.isConfigured && config.autoSync && config.scriptUrl.isNotBlank()) {
                val syncRes = sheetsService.saveIpo(config.scriptUrl, cleanIpo, isEdit)
                if (syncRes.success) {
                    showToast("Synced to Google Sheet")
                } else {
                    showToast("Sheet update warning: ${syncRes.message}")
                }
            }
        }
    }

    fun deleteIpo(ipo: Ipo) {
        viewModelScope.launch {
            repository.deleteIpo(ipo)
            showToast("IPO removed")

            val config = _sheetConfig.value
            if (config.isConfigured && config.autoSync && config.scriptUrl.isNotBlank()) {
                val syncRes = sheetsService.deleteIpo(config.scriptUrl, ipo.id)
                if (syncRes.success) {
                    showToast("Deleted from Google Sheet")
                } else {
                    showToast("Sheet deletion warning: ${syncRes.message}")
                }
            }
        }
    }

    // --- Committees ---

    fun saveCommittee(committee: Committee) {
        val cleanCommittee = committee.copy(
            startDate = Calculations.normalizeDateToDdMmYyyy(committee.startDate)
        )
        viewModelScope.launch {
            val isEdit = committees.value.any { it.id == cleanCommittee.id }
            repository.insertCommittee(cleanCommittee)
            showToast("Committee saved: ${cleanCommittee.name}")

            val config = _sheetConfig.value
            if (config.isConfigured && config.autoSync && config.scriptUrl.isNotBlank()) {
                val syncRes = sheetsService.saveCommittee(config.scriptUrl, cleanCommittee, isEdit)
                if (syncRes.success) {
                    showToast("Synced to Google Sheet")
                } else {
                    showToast("Sheet update warning: ${syncRes.message}")
                }
            }
        }
    }

    fun deleteCommittee(committee: Committee) {
        viewModelScope.launch {
            repository.deleteCommittee(committee)
            showToast("Committee removed")

            val config = _sheetConfig.value
            if (config.isConfigured && config.autoSync && config.scriptUrl.isNotBlank()) {
                val syncRes = sheetsService.deleteCommittee(config.scriptUrl, committee.id)
                if (syncRes.success) {
                    showToast("Deleted from Google Sheet")
                } else {
                    showToast("Sheet deletion warning: ${syncRes.message}")
                }
            }
        }
    }

    fun toggleCommitteeMonthPaid(committee: Committee, monthNumber: Int) {
        viewModelScope.launch {
            val updatedMonths = committee.months.map { month ->
                if (month.monthNumber == monthNumber) {
                    month.copy(isPaid = !month.isPaid)
                } else {
                    month
                }
            }
            val updated = committee.copy(
                months = updatedMonths,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateCommittee(updated)
            showToast("Month $monthNumber payment status updated")

            val config = _sheetConfig.value
            if (config.isConfigured && config.autoSync && config.scriptUrl.isNotBlank()) {
                sheetsService.saveCommittee(config.scriptUrl, updated, isEdit = true)
            }
        }
    }

    fun addOrUpdateCommitteeBid(
        committee: Committee,
        monthNumber: Int,
        monthLabel: String,
        winningBid: Double,
        winnerName: String,
        isPaid: Boolean,
        customInstallment: Double = 0.0
    ) {
        viewModelScope.launch {
            val netPotValue = maxOf(0.0, committee.totalAmount - winningBid)
            val members = if (committee.totalMembers > 0) committee.totalMembers else 12
            val installmentPerPerson = if (customInstallment > 0.0) {
                customInstallment
            } else if (members > 0) {
                netPotValue / members
            } else 0.0

            val existingIndex = committee.months.indexOfFirst { it.monthNumber == monthNumber }
            val newMonth = CommitteeMonth(
                monthNumber = monthNumber,
                monthLabel = monthLabel.ifBlank { "Month $monthNumber" },
                winningBid = winningBid,
                netPotValue = netPotValue,
                installmentPerPerson = installmentPerPerson,
                isPaid = isPaid,
                winnerName = winnerName
            )

            val newMonths = if (existingIndex >= 0) {
                committee.months.toMutableList().apply { set(existingIndex, newMonth) }
            } else {
                (committee.months + newMonth).sortedBy { it.monthNumber }
            }

            val updatedCurrentMonth = maxOf(committee.currentMonth, monthNumber)
            val updated = committee.copy(
                months = newMonths,
                currentMonth = updatedCurrentMonth,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateCommittee(updated)
            showToast("Auction bid logged for Month $monthNumber")

            val config = _sheetConfig.value
            if (config.isConfigured && config.autoSync && config.scriptUrl.isNotBlank()) {
                sheetsService.saveCommittee(config.scriptUrl, updated, isEdit = true)
            }
        }
    }

    fun deleteCommitteeMonth(committee: Committee, monthNumber: Int) {
        viewModelScope.launch {
            val newMonths = committee.months.filter { it.monthNumber != monthNumber }
            val updated = committee.copy(
                months = newMonths,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateCommittee(updated)
            showToast("Month $monthNumber bid deleted")

            val config = _sheetConfig.value
            if (config.isConfigured && config.autoSync && config.scriptUrl.isNotBlank()) {
                sheetsService.saveCommittee(config.scriptUrl, updated, isEdit = true)
            }
        }
    }

    // --- Google Sheets Sync Configuration & Actions ---

    fun updateSheetConfig(scriptUrl: String, autoSync: Boolean) {
        val trimmed = scriptUrl.trim()
        val isConf = trimmed.isNotBlank()
        _sheetConfig.value = _sheetConfig.value.copy(
            scriptUrl = trimmed,
            isConfigured = isConf,
            autoSync = autoSync
        )
        prefs.edit()
            .putString("sheet_script_url", trimmed)
            .putBoolean("sheet_auto_sync", autoSync)
            .apply()

        showToast(if (isConf) "Google Sheets connected" else "Google Sheets settings saved")
    }

    fun syncAllToSheets() {
        val config = _sheetConfig.value
        if (!config.isConfigured || config.scriptUrl.isBlank()) {
            showToast("Please enter your Google Apps Script URL first")
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            val result = sheetsService.pushAllData(
                scriptUrl = config.scriptUrl,
                holdings = holdings.value,
                sips = sips.value,
                ipos = ipos.value,
                committees = committees.value
            )
            _isSyncing.value = false

            if (result.success) {
                val now = SimpleDateFormat("HH:mm, dd MMM", Locale.ENGLISH).format(Date())
                _sheetConfig.value = config.copy(lastSyncedAt = now)
                prefs.edit().putString("sheet_last_sync", now).apply()
                showToast("All data successfully pushed to Google Sheets!")
            } else {
                showToast(result.message)
            }
        }
    }

    fun pullFromSheets() {
        val config = _sheetConfig.value
        if (!config.isConfigured || config.scriptUrl.isBlank()) {
            showToast("Please enter your Google Apps Script URL first")
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            val result = sheetsService.fetchData(config.scriptUrl)
            _isSyncing.value = false

            if (result.success) {
                if (result.holdings.isNotEmpty()) {
                    repository.clearHoldings()
                    repository.insertHoldings(result.holdings)
                }
                if (result.sips.isNotEmpty()) {
                    repository.clearSips()
                    repository.insertSips(result.sips)
                }
                if (result.ipos.isNotEmpty()) {
                    repository.clearIpos()
                    repository.insertIpos(result.ipos)
                }
                if (result.committees.isNotEmpty()) {
                    repository.clearCommittees()
                    repository.insertCommittees(result.committees)
                }

                val now = SimpleDateFormat("HH:mm, dd MMM", Locale.ENGLISH).format(Date())
                _sheetConfig.value = config.copy(lastSyncedAt = now)
                prefs.edit().putString("sheet_last_sync", now).apply()
                showToast("Downloaded and updated from Google Sheets!")
            } else {
                showToast(result.message)
            }
        }
    }

    // Reset data to demo data
    fun resetToDemoData() {
        viewModelScope.launch {
            repository.clearHoldings()
            repository.clearSips()
            repository.clearIpos()
            repository.clearCommittees()

            repository.insertHoldings(InitialData.HOLDINGS)
            repository.insertSips(InitialData.SIPS)
            repository.insertIpos(InitialData.IPOS)
            repository.insertCommittees(InitialData.COMMITTEES)

            showToast("Portfolio reset to demo data!")
        }
    }

    // Import parsed portfolio data from CSV/Excel
    fun importPortfolioData(
        newHoldings: List<Holding> = emptyList(),
        newSips: List<Sip> = emptyList(),
        newIpos: List<Ipo> = emptyList(),
        newCommittees: List<Committee> = emptyList(),
        replaceExisting: Boolean = false
    ) {
        val cleanHoldings = newHoldings.map { it.copy(tillDate = Calculations.normalizeDateToDdMmYyyy(it.tillDate)) }
        val cleanSips = newSips.map { it.copy(since = Calculations.normalizeDateToDdMmYyyy(it.since)) }
        val cleanIpos = newIpos.map {
            it.copy(
                applyDate = Calculations.normalizeDateToDdMmYyyy(it.applyDate),
                allotmentDate = Calculations.normalizeDateToDdMmYyyy(it.allotmentDate),
                refundDate = Calculations.normalizeDateToDdMmYyyy(it.refundDate),
                listingDate = Calculations.normalizeDateToDdMmYyyy(it.listingDate)
            )
        }
        val cleanCommittees = newCommittees.map { it.copy(startDate = Calculations.normalizeDateToDdMmYyyy(it.startDate)) }

        viewModelScope.launch {
            if (replaceExisting) {
                if (cleanHoldings.isNotEmpty()) repository.clearHoldings()
                if (cleanSips.isNotEmpty()) repository.clearSips()
                if (cleanIpos.isNotEmpty()) repository.clearIpos()
                if (cleanCommittees.isNotEmpty()) repository.clearCommittees()
            }
            if (cleanHoldings.isNotEmpty()) repository.insertHoldings(cleanHoldings)
            if (cleanSips.isNotEmpty()) repository.insertSips(cleanSips)
            if (cleanIpos.isNotEmpty()) repository.insertIpos(cleanIpos)
            if (cleanCommittees.isNotEmpty()) repository.insertCommittees(cleanCommittees)

            val total = cleanHoldings.size + cleanSips.size + cleanIpos.size + cleanCommittees.size
            showToast("Successfully imported $total records!")
        }
    }
}
