package com.example.data.remote

import com.example.data.model.Committee
import com.example.data.model.CommitteeMonth
import com.example.data.model.Holding
import com.example.data.model.Ipo
import com.example.data.model.Sip
import com.example.util.Calculations
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit

data class SyncResult(
    val success: Boolean,
    val message: String,
    val holdings: List<Holding> = emptyList(),
    val sips: List<Sip> = emptyList(),
    val ipos: List<Ipo> = emptyList(),
    val committees: List<Committee> = emptyList()
)

class SheetsService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private suspend fun postAction(scriptUrl: String, payload: JsonObject): SyncResult = withContext(Dispatchers.IO) {
        if (scriptUrl.isBlank()) {
            return@withContext SyncResult(false, "Google Apps Script URL is empty")
        }

        try {
            val requestBody = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(scriptUrl.trim())
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext SyncResult(false, "HTTP ${response.code}: ${response.message}")
            }

            val json = try {
                gson.fromJson(body, JsonObject::class.java)
            } catch (e: Exception) {
                null
            }

            val status = json?.get("status")?.asString ?: "success"
            if (status.equals("error", ignoreCase = true)) {
                val msg = json?.get("message")?.asString ?: "Error processing request in sheet"
                SyncResult(false, msg)
            } else {
                val msg = json?.get("message")?.asString ?: "Google Sheet updated"
                SyncResult(true, msg)
            }
        } catch (e: Exception) {
            SyncResult(false, "Sheet sync error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    // --- Single-record operations (Insert / Update / Delete) ---

    suspend fun saveHolding(scriptUrl: String, holding: Holding, isEdit: Boolean): SyncResult {
        val cleanHolding = holding.copy(tillDate = Calculations.normalizeDateToDdMmYyyy(holding.tillDate))
        val payload = JsonObject().apply {
            addProperty("action", if (isEdit) "updateHolding" else "addHolding")
            if (isEdit) addProperty("id", cleanHolding.id)
            add("data", gson.toJsonTree(cleanHolding))
        }
        return postAction(scriptUrl, payload)
    }

    suspend fun deleteHolding(scriptUrl: String, id: String): SyncResult {
        val payload = JsonObject().apply {
            addProperty("action", "deleteHolding")
            addProperty("id", id)
        }
        return postAction(scriptUrl, payload)
    }

    suspend fun saveSip(scriptUrl: String, sip: Sip, isEdit: Boolean): SyncResult {
        val cleanSip = sip.copy(since = Calculations.normalizeDateToDdMmYyyy(sip.since))
        val payload = JsonObject().apply {
            addProperty("action", if (isEdit) "updateSip" else "addSip")
            if (isEdit) addProperty("id", cleanSip.id)
            add("data", gson.toJsonTree(cleanSip))
        }
        return postAction(scriptUrl, payload)
    }

    suspend fun deleteSip(scriptUrl: String, id: String): SyncResult {
        val payload = JsonObject().apply {
            addProperty("action", "deleteSip")
            addProperty("id", id)
        }
        return postAction(scriptUrl, payload)
    }

    suspend fun saveIpo(scriptUrl: String, ipo: Ipo, isEdit: Boolean): SyncResult {
        val cleanIpo = ipo.copy(
            applyDate = Calculations.normalizeDateToDdMmYyyy(ipo.applyDate),
            allotmentDate = Calculations.normalizeDateToDdMmYyyy(ipo.allotmentDate),
            refundDate = Calculations.normalizeDateToDdMmYyyy(ipo.refundDate),
            listingDate = Calculations.normalizeDateToDdMmYyyy(ipo.listingDate)
        )
        val payload = JsonObject().apply {
            addProperty("action", if (isEdit) "updateIpo" else "addIpo")
            if (isEdit) addProperty("id", cleanIpo.id)
            add("data", gson.toJsonTree(cleanIpo))
        }
        return postAction(scriptUrl, payload)
    }

    suspend fun deleteIpo(scriptUrl: String, id: String): SyncResult {
        val payload = JsonObject().apply {
            addProperty("action", "deleteIpo")
            addProperty("id", id)
        }
        return postAction(scriptUrl, payload)
    }

    suspend fun saveCommittee(scriptUrl: String, committee: Committee, isEdit: Boolean): SyncResult {
        val cleanCommittee = committee.copy(startDate = Calculations.normalizeDateToDdMmYyyy(committee.startDate))
        val payload = JsonObject().apply {
            addProperty("action", if (isEdit) "updateCommittee" else "addCommittee")
            if (isEdit) addProperty("id", cleanCommittee.id)
            add("data", gson.toJsonTree(cleanCommittee))
        }
        return postAction(scriptUrl, payload)
    }

    suspend fun deleteCommittee(scriptUrl: String, id: String): SyncResult {
        val payload = JsonObject().apply {
            addProperty("action", "deleteCommittee")
            addProperty("id", id)
        }
        return postAction(scriptUrl, payload)
    }

    // --- Bulk Operations (Push All / Pull All) ---

    suspend fun pushAllData(
        scriptUrl: String,
        holdings: List<Holding>,
        sips: List<Sip>,
        ipos: List<Ipo>,
        committees: List<Committee>
    ): SyncResult = withContext(Dispatchers.IO) {
        if (scriptUrl.isBlank()) {
            return@withContext SyncResult(false, "Google Apps Script URL is empty")
        }

        try {
            val cleanHoldings = holdings.map { it.copy(tillDate = Calculations.normalizeDateToDdMmYyyy(it.tillDate)) }
            val cleanSips = sips.map { it.copy(since = Calculations.normalizeDateToDdMmYyyy(it.since)) }
            val cleanIpos = ipos.map {
                it.copy(
                    applyDate = Calculations.normalizeDateToDdMmYyyy(it.applyDate),
                    allotmentDate = Calculations.normalizeDateToDdMmYyyy(it.allotmentDate),
                    refundDate = Calculations.normalizeDateToDdMmYyyy(it.refundDate),
                    listingDate = Calculations.normalizeDateToDdMmYyyy(it.listingDate)
                )
            }
            val cleanCommittees = committees.map { it.copy(startDate = Calculations.normalizeDateToDdMmYyyy(it.startDate)) }

            val payload = JsonObject().apply {
                addProperty("action", "syncAll")
                add("holdings", gson.toJsonTree(cleanHoldings))
                add("sips", gson.toJsonTree(cleanSips))
                add("ipos", gson.toJsonTree(cleanIpos))
                add("committees", gson.toJsonTree(cleanCommittees))
            }

            val requestBody = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(scriptUrl.trim())
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext SyncResult(false, "HTTP ${response.code}: ${response.message}")
            }

            SyncResult(true, "All records pushed to Google Sheets successfully!")
        } catch (e: Exception) {
            SyncResult(false, "Push failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun getJsonStringField(obj: JsonObject, vararg keys: String): String {
        for (key in keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull) {
                val elem = obj.get(key)
                val str = if (elem.isJsonPrimitive) elem.asString else elem.toString().trim('"')
                if (str.isNotBlank()) return str
            }
        }
        return ""
    }

    suspend fun fetchData(scriptUrl: String): SyncResult = withContext(Dispatchers.IO) {
        if (scriptUrl.isBlank()) {
            return@withContext SyncResult(false, "Google Apps Script URL is empty")
        }

        try {
            val request = Request.Builder()
                .url(scriptUrl.trim())
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext SyncResult(false, "HTTP ${response.code}: ${response.message}")
            }

            val json = gson.fromJson(body, JsonObject::class.java)
            val status = json?.get("status")?.asString ?: ""

            if (!status.equals("success", ignoreCase = true)) {
                val msg = json?.get("message")?.asString ?: "Failed to fetch data from sheet"
                return@withContext SyncResult(false, msg)
            }

            // Parse Holdings
            val parsedHoldings = mutableListOf<Holding>()
            json.getAsJsonArray("holdings")?.forEach { elem ->
                if (elem.isJsonObject) {
                    val obj = elem.asJsonObject
                    val id = obj.get("id")?.asString?.ifBlank { null } ?: UUID.randomUUID().toString()
                    val platform = obj.get("platform")?.asString ?: ""
                    val category = obj.get("category")?.asString ?: ""
                    val breakdownRaw = obj.get("breakdownRaw")?.asString
                        ?: obj.get("breakdown")?.let { b ->
                            if (b.isJsonArray) b.asJsonArray.joinToString(", ") { it.asString } else ""
                        } ?: ""
                    val total = obj.get("total")?.asDouble ?: 0.0
                    val rawTillDate = getJsonStringField(obj, "tillDate", "till_date", "TillDate", "date", "Date", "asOfDate")
                    val tillDate = Calculations.normalizeDateToDdMmYyyy(rawTillDate)
                    val remarks = obj.get("remarks")?.asString ?: ""
                    parsedHoldings.add(Holding(id = id, platform = platform, category = category, breakdownRaw = breakdownRaw, total = total, tillDate = tillDate, remarks = remarks))
                }
            }

            // Parse SIPs
            val parsedSips = mutableListOf<Sip>()
            json.getAsJsonArray("sips")?.forEach { elem ->
                if (elem.isJsonObject) {
                    val obj = elem.asJsonObject
                    val id = obj.get("id")?.asString?.ifBlank { null } ?: UUID.randomUUID().toString()
                    val platform = obj.get("platform")?.asString ?: ""
                    val instrument = obj.get("instrument")?.asString ?: ""
                    val amount = obj.get("amount")?.asDouble ?: 0.0
                    val frequency = obj.get("frequency")?.asString ?: "Monthly"
                    val rawSince = getJsonStringField(obj, "since", "sinceDate", "since_date", "Since", "startDate", "start_date")
                    val since = Calculations.normalizeDateToDdMmYyyy(rawSince)
                    val remarks = obj.get("remarks")?.asString ?: ""
                    val rawStatus = getJsonStringField(obj, "status", "Status", "isPaused", "state")
                    val isPaused = rawStatus.equals("paused", ignoreCase = true) || obj.get("isPaused")?.asBoolean == true
                    parsedSips.add(Sip(id = id, platform = platform, instrument = instrument, amount = amount, frequency = frequency, since = since, remarks = remarks, isPaused = isPaused))
                }
            }

            // Parse IPOs
            val parsedIpos = mutableListOf<Ipo>()
            json.getAsJsonArray("ipos")?.forEach { elem ->
                if (elem.isJsonObject) {
                    val obj = elem.asJsonObject
                    val id = obj.get("id")?.asString?.ifBlank { null } ?: UUID.randomUUID().toString()
                    val companyName = obj.get("companyName")?.asString ?: ""
                    val category = obj.get("category")?.asString ?: "Mainboard"
                    val platform = obj.get("platform")?.asString ?: "Zerodha"
                    val applicantName = obj.get("applicantName")?.asString ?: "Self"
                    val sharesPerLot = obj.get("sharesPerLot")?.asInt ?: 1
                    val cutOffPrice = obj.get("cutOffPrice")?.asDouble ?: 0.0
                    val lotsApplied = obj.get("lotsApplied")?.asInt ?: 1
                    val totalAmountBlocked = obj.get("totalAmountBlocked")?.asDouble ?: (sharesPerLot * cutOffPrice * lotsApplied)
                    val applyDate = Calculations.normalizeDateToDdMmYyyy(getJsonStringField(obj, "applyDate", "apply_date", "ApplyDate", "date", "Date"))
                    val allotmentDate = Calculations.normalizeDateToDdMmYyyy(getJsonStringField(obj, "allotmentDate", "allotment_date", "AllotmentDate"))
                    val refundDate = Calculations.normalizeDateToDdMmYyyy(getJsonStringField(obj, "refundDate", "refund_date", "RefundDate"))
                    val listingDate = Calculations.normalizeDateToDdMmYyyy(getJsonStringField(obj, "listingDate", "listing_date", "ListingDate"))
                    val itemStatus = obj.get("status")?.asString ?: "Applied"
                    val allottedLots = obj.get("allottedLots")?.asInt ?: 0
                    val listingPrice = obj.get("listingPrice")?.asDouble ?: 0.0
                    val listingGainAmount = obj.get("listingGainAmount")?.asDouble ?: 0.0
                    val listingGainPercent = obj.get("listingGainPercent")?.asDouble ?: 0.0
                    val remarks = obj.get("remarks")?.asString ?: ""
                    parsedIpos.add(
                        Ipo(
                            id = id,
                            companyName = companyName,
                            category = category,
                            platform = platform,
                            applicantName = applicantName,
                            sharesPerLot = sharesPerLot,
                            cutOffPrice = cutOffPrice,
                            lotsApplied = lotsApplied,
                            totalAmountBlocked = totalAmountBlocked,
                            applyDate = applyDate,
                            allotmentDate = allotmentDate,
                            refundDate = refundDate,
                            listingDate = listingDate,
                            status = itemStatus,
                            allottedLots = allottedLots,
                            listingPrice = listingPrice,
                            listingGainAmount = listingGainAmount,
                            listingGainPercent = listingGainPercent,
                            remarks = remarks
                        )
                    )
                }
            }

            // Parse Committees
            val parsedCommittees = mutableListOf<Committee>()
            json.getAsJsonArray("committees")?.forEach { elem ->
                if (elem.isJsonObject) {
                    val obj = elem.asJsonObject
                    val id = obj.get("id")?.asString?.ifBlank { null } ?: UUID.randomUUID().toString()
                    val name = obj.get("name")?.asString ?: ""
                    val totalAmount = obj.get("totalAmount")?.asDouble ?: 0.0
                    val totalMembers = obj.get("totalMembers")?.asInt ?: 12
                    val totalMonths = obj.get("totalMonths")?.asInt ?: 12
                    val startDate = Calculations.normalizeDateToDdMmYyyy(getJsonStringField(obj, "startDate", "start_date", "StartDate", "date", "Date"))
                    val baseInstallment = obj.get("baseMonthlyInstallment")?.asDouble ?: if (totalMonths > 0) totalAmount / totalMonths else 0.0
                    val currentMonth = obj.get("currentMonth")?.asInt ?: 1
                    val hasTakenPot = obj.get("hasTakenPot")?.asBoolean ?: false
                    val potTakenMonth = obj.get("potTakenMonth")?.let { if (it.isJsonNull) null else it.asInt }
                    val potTakenAmount = obj.get("potTakenAmount")?.let { if (it.isJsonNull) null else it.asDouble }
                    val remarks = obj.get("remarks")?.asString ?: ""

                    val monthsList = mutableListOf<CommitteeMonth>()
                    obj.getAsJsonArray("months")?.forEach { mElem ->
                        if (mElem.isJsonObject) {
                            val mObj = mElem.asJsonObject
                            monthsList.add(
                                CommitteeMonth(
                                    monthNumber = mObj.get("monthNumber")?.asInt ?: 1,
                                    monthLabel = mObj.get("monthLabel")?.asString ?: "",
                                    winningBid = mObj.get("winningBid")?.asDouble ?: 0.0,
                                    netPotValue = mObj.get("netPotValue")?.asDouble ?: 0.0,
                                    installmentPerPerson = mObj.get("installmentPerPerson")?.asDouble ?: 0.0,
                                    isPaid = mObj.get("isPaid")?.asBoolean ?: false,
                                    winnerName = mObj.get("winnerName")?.asString ?: ""
                                )
                            )
                        }
                    }

                    parsedCommittees.add(
                        Committee(
                            id = id,
                            name = name,
                            totalAmount = totalAmount,
                            totalMembers = totalMembers,
                            totalMonths = totalMonths,
                            startDate = startDate,
                            baseMonthlyInstallment = baseInstallment,
                            currentMonth = currentMonth,
                            hasTakenPot = hasTakenPot,
                            potTakenMonth = potTakenMonth,
                            potTakenAmount = potTakenAmount,
                            remarks = remarks,
                            months = monthsList
                        )
                    )
                }
            }

            SyncResult(
                success = true,
                message = "Fetched data from Google Sheet successfully",
                holdings = parsedHoldings,
                sips = parsedSips,
                ipos = parsedIpos,
                committees = parsedCommittees
            )
        } catch (e: Exception) {
            SyncResult(false, "Failed to load from sheet: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
