package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R

/**
 * Data models for Version & Modification tracking
 */
data class ModificationItem(
    val id: String,
    val category: String, // "Automation", "UI/UX Glass", "Feature", etc.
    val title: String,
    val section: String,
    val description: String
)

data class VersionRelease(
    val version: String,
    val buildNumber: String,
    val date: String,
    val summary: String,
    val isLatest: Boolean = false,
    val modifications: List<ModificationItem>
)

val SAMPLE_RELEASES = getAppReleases()

fun getAppReleases(
    currentVersion: String = BuildConfig.VERSION_NAME,
    currentBuild: Int = BuildConfig.VERSION_CODE
): List<VersionRelease> {
    return listOf(
        VersionRelease(
            version = "v$currentVersion",
            buildNumber = "$currentBuild",
            date = "Sep 13, 2026",
            summary = "Strict DD-MM-YYYY Date Formatting & Translucent Sidebar Navigation",
            isLatest = true,
            modifications = listOf(
                ModificationItem(
                    id = "mod_date_sync_timezone_dd_mm_yyyy",
                    category = "Data Integrity & Sync",
                    title = "Strict DD-MM-YYYY Date Normalization & Zero Timezone Shift",
                    section = "Google Sheets Sync",
                    description = "Eliminated unexpected date mutations and 1-day shifts when syncing with Google Sheets. All incoming dates (ISO-8601 UTC instants, serial numbers, timestamps, and varied date conventions) are deterministically parsed in India Standard Time and formatted to strictly uniform DD-MM-YYYY across Holdings, SIPs, IPOs, Committees, Dialogs, and CSV Exports."
                ),
                ModificationItem(
                    id = "mod_translucent_left_sidebar",
                    category = "UI/UX Refinement",
                    title = "Translucent Frosted Glass Left Sidebar Aligned with Tabs",
                    section = "Navigation Sidebar",
                    description = "Updated the slide-out Left Navigation Sidebar drawer with an elegant translucent frosted glass aesthetic matching the tabs' ambient liquid mesh canvas. Replaced solid opaque backgrounds with subtle 0.86f-0.88f translucent gradients, glassmorphic item cards (0.48f alpha with crisp borders), and seamless visual continuity."
                ),
                ModificationItem(
                    id = "mod_scrollable_tabs_flag_trio_header",
                    category = "UI/UX Feature",
                    title = "Full Visible Header Names with Smooth Scrollable Pills & Flag Trio Color Header",
                    section = "Top Navigation",
                    description = "Restored full visible section names ('Summary', 'Holdings', 'SIPs', 'IPOs', 'Committees') inside an ergonomically scrollable pill strip with automatic bring-into-view tracking. Maintained the signature Indian Flag trio color background (Saffron, White, Green) with high-contrast pill styling."
                ),
                ModificationItem(
                    id = "mod_ipo_blocked_amount_fix",
                    category = "Bug Fix",
                    title = "Fixed IPO Currently Blocked Amount Calculation",
                    section = "IPOs Tab",
                    description = "Resolved an issue where newly created IPOs displayed ₹0 in the 'Currently Blocked' metric card despite showing the correct amount on the individual IPO card. Added auto-repair on startup for existing records, persistent blocked amount calculation upon saving, and robust fallbacks across all dashboards."
                ),
                ModificationItem(
                    id = "mod_ipo_all_three_dates",
                    category = "Feature Enhancement",
                    title = "All Three IPO Milestone Dates Added to Add/Edit Dialog",
                    section = "IPOs Tab",
                    description = "Added full input and editing options for all 3 key IPO milestone dates: Applied Date, Allotment Date, and Listing Date. Includes interactive Material 3 date pickers, quick-action shortcuts ('Today' and clear buttons), and symmetric milestone timeline visualization."
                ),
                ModificationItem(
                    id = "mod_left_sidebar_navigation",
                    category = "UI/UX Feature",
                    title = "Left Navigation Sidebar Drawer Added",
                    section = "Navigation",
                    description = "Introduced a slide-out Left Navigation Sidebar accessible via the top-left hamburger menu or edge swipe. Features total portfolio live snapshot, one-tap navigation to Summary, Holdings, SIPs, IPOs, Committees, quick-entry shortcuts, and instant access to search and Google Sheets sync."
                ),
                ModificationItem(
                    id = "mod_0_remove_month_cycle_label",
                    category = "UI Simplification",
                    title = "Month Cycle Label Removed from Auction Bid Dialog",
                    section = "Committees Tab",
                    description = "Completely removed the 'Month Cycle Label' input from the Auction Bid dialog. Month cycle naming is now calculated automatically in the background from committee schedule."
                ),
                ModificationItem(
                    id = "mod_0_remove_top_installment_calc",
                    category = "Calculation Refinement",
                    title = "Removed Automatic Installment / Member from Hero Header",
                    section = "Committees Tab",
                    description = "Cleaned up the top auction dialog hero card to focus purely on the Net Pot Payout, removing the automatic installment per member calculation and dividend deduction."
                ),
                ModificationItem(
                    id = "mod_0_add_installment_per_member_row",
                    category = "Data Input",
                    title = "Dedicated 'Installation per Member' Input Row Added",
                    section = "Committees Tab",
                    description = "Added a dedicated row below Winning Auction Discount Bid labeled 'Installation per Member' where organizers/members can enter the exact installment amount to be saved directly into the ledger."
                ),
                ModificationItem(
                    id = "mod_0_latest_month_paid_ledger_sync",
                    category = "Data Synchronization",
                    title = "Latest Month Paid Bound to Auction Ledger",
                    section = "Committees Tab",
                    description = "Synchronized the 'Latest month paid' column label and installment amount on the committee card to strictly take from the MONTH column of the Auction Ledger based on the latest month marked as Paid."
                ),
                ModificationItem(
                    id = "mod_0_card_styling_alignment_ipos",
                    category = "UI & Visual Alignment",
                    title = "Holdings & SIPs Card Styling Aligned with IPOs Screen",
                    section = "Holdings & SIPs Tabs",
                    description = "Aligned card and box styling across Holdings and SIPs screens with IPOs: updated background opacity to 0.52f translucent glassmorphism with 1.dp 0.70f alpha white border, matching blur, contrast, and sub-box surface treatments."
                )
            )
        ),
        VersionRelease(
            version = "v1.0.0.12",
            buildNumber = "1012",
            date = "Sep 13, 2026",
            summary = "Dual Bidding & Selling Details Breakdown with True Listing Value",
            isLatest = false,
            modifications = listOf(
                ModificationItem(
                    id = "mod_0_dual_bidding_selling_details",
                    category = "Financial Accuracy",
                    title = "Dual Bidding & Selling Details on Listed IPOs",
                    section = "IPOs Tab",
                    description = "When an IPO is listed or sold, both Bidding Details (lots, shares, cut-off price, and invested amount) and Selling / Listing Details (allotted lots, shares, selling price per share, and total selling value) are simultaneously displayed in the card."
                ),
                ModificationItem(
                    id = "mod_0_listing_selling_value_calculation",
                    category = "Calculation Fix",
                    title = "Listing Value Corrected to True Selling Price",
                    section = "IPOs Tab",
                    description = "Corrected the listing value display on listed IPOs to reflect the true realized selling amount (allotted shares × selling price per share) instead of duplicating the initial blocked amount."
                ),
                ModificationItem(
                    id = "mod_0_ipo_dialog_live_selling_preview",
                    category = "UI/UX Feature",
                    title = "Live Selling Value & Gain Preview in Dialog",
                    section = "IPO Dialog",
                    description = "Added real-time calculation and preview of Selling Value and Net Realized Gain right inside the IPO add/edit dialog when setting status to Allotted or Listed."
                ),
                ModificationItem(
                    id = "mod_0_build_compilation_fix",
                    category = "Stability",
                    title = "Kotlin Compiler & Material 3 Divider Import Fix",
                    section = "Core Build",
                    description = "Resolved build issues by properly importing HorizontalDivider and fixing lot allotment state resolution in IPO Dialog."
                )
            )
        ),
        VersionRelease(
            version = "v1.0.0.10",
            buildNumber = "1010",
            date = "Sep 13, 2026",
            summary = "IPO Card Layout Refinement & Remarks Box Repositioning",
            isLatest = false,
            modifications = listOf(
                ModificationItem(
                    id = "mod_0_ipo_remarks_down",
                    category = "UI/UX Layout",
                    title = "IPO Card Remarks Box Relocated Down",
                    section = "IPOs Tab",
                    description = "Moved the remarks quote box down directly below the Bidding Details & Total Amount Blocked container. This keeps the application timeline and financial bidding data grouped seamlessly together, with notes positioned right above the action footer."
                )
            )
        ),
        VersionRelease(
            version = "v1.0.0.8",
            buildNumber = "1008",
            date = "Sep 13, 2026",
            summary = "Delete Dialog Button Rendering & Styling Fix",
            isLatest = false,
            modifications = listOf(
                ModificationItem(
                    id = "mod_0_delete_button_box_fix",
                    category = "UI/UX Bug Fix",
                    title = "Delete Button Inner Box Artifact Resolved",
                    section = "All Investment Tabs",
                    description = "Eliminated nested surface elevation overlay artifact on the Delete button in the deletion dialog across all tabs. The button now renders a uniform solid crimson background with rounded corners matching the Cancel button."
                ),
                ModificationItem(
                    id = "mod_0_themed_delete",
                    category = "UI/UX Redesign",
                    title = "Tab-Themed Delete Confirmation Dialogs",
                    section = "All Investment Tabs",
                    description = "Introduced modern dual-layer themed delete popups color-coordinated to each asset class (Blue for Holdings, Amber for SIPs, Pink for IPOs, Purple for Committees) with rich item preview cards and metric chips."
                ),
                ModificationItem(
                    id = "mod_0_committee_actions",
                    category = "UI Alignment",
                    title = "Committee Card Actions Repositioning",
                    section = "Chit Funds / Committees",
                    description = "Moved Edit and Delete action buttons from the top header to the lower right section below Invested Till Date and Latest Due/Paid metrics, matching Holdings and SIP cards."
                )
            )
        ),
        VersionRelease(
            version = "v1.0.0.7",
            buildNumber = "1007",
            date = "Sep 13, 2026",
            summary = "Tab-Themed Delete Confirmations & Model Property Alignments",
            isLatest = false,
            modifications = listOf(
                ModificationItem(
                    id = "mod_0_build_fixes",
                    category = "Build & Types",
                    title = "Compile Alignment & Entity Properties",
                    section = "Architecture",
                    description = "Cleaned conflicting duplicate imports and aligned entity model properties across all investment management screens."
                )
            )
        ),
        VersionRelease(
            version = "v1.0.0.6",
            buildNumber = "1006",
            date = "Sep 12, 2026",
            summary = "Centralized Data Backup & Universal Portfolio Ledger Sync",
            isLatest = false,
            modifications = listOf(
                ModificationItem(
                    id = "mod_0_recent",
                    category = "Data Ordering",
                    title = "Recent-First Chronological Ordering",
                    section = "All Tabs & Search",
                    description = "Data across Holdings, SIPs, IPOs, Committees, and Universal Search is now dynamically sorted recent-first with date parsing and updated timestamp precedence."
                ),
                ModificationItem(
                    id = "mod_1",
                    category = "UI/UX Glass",
                    title = "Centralized Changelog to Data Backup",
                    section = "Data Management & Backup",
                    description = "Consolidated release notes and version history exclusively inside Data Management & Backup, maximizing screen viewport across all tabs."
                ),
                ModificationItem(
                    id = "mod_2",
                    category = "Storage",
                    title = "Universal Portfolio Backup & Restore",
                    section = "Data Management & Backup",
                    description = "Added full JSON and CSV backup exports, interactive file picking, and raw CSV paste import with entity count preview."
                ),
                ModificationItem(
                    id = "mod_3",
                    category = "Automation",
                    title = "Automated Build & CI Version Sync",
                    section = "Build System",
                    description = "Synchronized app telemetry with live BuildConfig version name ($currentVersion) and build number ($currentBuild)."
                ),
                ModificationItem(
                    id = "mod_4",
                    category = "Feature",
                    title = "IPO Quick Action: Mark Not Allotted",
                    section = "IPO Tracker",
                    description = "Updated one-tap quick action on Applied IPOs to 'Mark Not Allotted' with hierarchical styling (smaller 'Mark' prefix) for instant fund unblocking and refund reconciliation."
                ),
                ModificationItem(
                    id = "mod_5",
                    category = "UI/UX Glass",
                    title = "Full-Width Viewport Optimization",
                    section = "Main Screen",
                    description = "Refined edge-to-edge layouts for Summary, Holdings, SIP schedules, IPOs, and Chit Fund Committees."
                ),
                ModificationItem(
                    id = "mod_6",
                    category = "UI/UX Glass",
                    title = "Smart Back Navigation to Summary",
                    section = "Navigation",
                    description = "Phone back button on any tab now routes back to Summary before exiting the app, preventing accidental closure."
                ),
                ModificationItem(
                    id = "mod_7",
                    category = "Automation",
                    title = "Default Google Apps Script Endpoint",
                    section = "Sheets Sync",
                    description = "Pre-configured the default Google Apps Script Web App URL for zero-friction bidirectional cloud sync."
                )
            )
        ),
        VersionRelease(
            version = "v0.9.5",
            buildNumber = "15",
            date = "Sep 10, 2026",
            summary = "Chit Fund Auction & SIP Scheduling Enhancements",
            isLatest = false,
            modifications = listOf(
                ModificationItem(
                    id = "mod_7",
                    category = "Feature",
                    title = "Committee Chit Fund Bid Logging",
                    section = "Committees",
                    description = "Interactive monthly dividend calculation and winning dividend payout auction logger with real-time distribution."
                ),
                ModificationItem(
                    id = "mod_8",
                    category = "Automation",
                    title = "SIP Cash Flow Projection Engine",
                    section = "SIP Engine",
                    description = "Dynamic monthly outflow commitments calculation and scheduled milestone alerts."
                ),
                ModificationItem(
                    id = "mod_9",
                    category = "Feature",
                    title = "Multi-Entity Universal Search",
                    section = "Search",
                    description = "Instant cross-filtering across Holdings, SIP schedules, active IPOs, and Chit Funds."
                ),
                ModificationItem(
                    id = "mod_10",
                    category = "UI/UX Glass",
                    title = "High-Contrast Glass Containers",
                    section = "Visual Theme",
                    description = "Crafted high-contrast surface containers with crisp slate borders for metric cards and portfolio breakdown."
                )
            )
        ),
        VersionRelease(
            version = "v0.9.0",
            buildNumber = "1",
            date = "Aug 28, 2026",
            summary = "Initial Multi-Asset Investment Tracker Release",
            isLatest = false,
            modifications = listOf(
                ModificationItem(
                    id = "mod_11",
                    category = "Feature",
                    title = "Multi-Asset Portfolio Tracking",
                    section = "Core",
                    description = "Foundational release tracking Equity, Mutual Funds, IPOs, SIPs, and Chit Fund Committees."
                ),
                ModificationItem(
                    id = "mod_12",
                    category = "Storage",
                    title = "Room Database Local Schema",
                    section = "Storage",
                    description = "Implemented SQLite Room entities for zero-latency offline performance."
                ),
                ModificationItem(
                    id = "mod_13",
                    category = "UI/UX Glass",
                    title = "Adaptive Material 3 Theme",
                    section = "Theme",
                    description = "Full M3 color scheme with dynamic contrast support across handheld screens."
                )
            )
        )
    )
}

/**
 * Clean Footer Pill displaying version, build, app title, and CHANGELOG button
 */
@Composable
fun VersionFooterPill(
    modifier: Modifier = Modifier,
    versionName: String = BuildConfig.VERSION_NAME,
    buildNumber: String = "${BuildConfig.VERSION_CODE}",
    appName: String = "Investment & Committee Ledger",
    onOpenChangelog: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.88f),
        border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("version_footer_pill")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenChangelog() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_history),
                    contentDescription = "Version History",
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "v$versionName (Build $buildNumber) • $appName",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0F2FE))
                    .clickable { onOpenChangelog() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("changelog_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CHANGELOG",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7),
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}

/**
 * Modal Bottom Sheet presenting Version & Modifications details
 * Matching Screenshot 1
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionModificationsBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    releases: List<VersionRelease> = remember { getAppReleases() }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var selectedRelease by remember(releases) { mutableStateOf(releases.firstOrNull()) }
    var selectedFilter by remember { mutableStateOf("All Changes") }

    val activeRelease = selectedRelease ?: releases.firstOrNull() ?: return

    val allModifications = remember(releases) {
        releases.flatMap { it.modifications }
    }

    val automationCount = remember(releases) { allModifications.count { it.category.equals("Automation", ignoreCase = true) } }
    val uiUxCount = remember(releases) { allModifications.count { it.category.contains("UI/UX", ignoreCase = true) } }
    val featureCount = remember(releases) { allModifications.count { it.category.equals("Feature", ignoreCase = true) } }
    val storageCount = remember(releases) { allModifications.count { it.category.equals("Storage", ignoreCase = true) } }

    val filterCategories = listOf(
        "All Changes (${allModifications.size})",
        "Automation ($automationCount)",
        "UI/UX Glass ($uiUxCount)",
        "Feature ($featureCount)",
        "Storage ($storageCount)"
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 38.dp, height = 4.5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF64748B))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("version_modifications_bottom_sheet")
        ) {
            // Header Row: Icon + Title & Subtitle + Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_history),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Version & Modifications",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Automated release & change tracking",
                            fontSize = 12.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .clickable { onDismissRequest() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Version Selector Chips (Tap any version to inspect its actual data)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                releases.forEach { releaseItem ->
                    val isSelected = releaseItem.version == activeRelease.version
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedRelease = releaseItem }
                            .testTag("version_selector_${releaseItem.version}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (releaseItem.isLatest) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF4ADE80) else Color(0xFF16A34A))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "${releaseItem.version}${if (releaseItem.isLatest) " (Latest)" else ""}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dark Navy Active Hero Card (Displays Selected Version's Actual Data)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_version_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (activeRelease.isLatest) {
                                // ACTIVE VERSION Badge
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF064E3B).copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ACTIVE VERSION",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399),
                                            letterSpacing = 0.3.sp
                                        )
                                    }
                                }

                                // AUTO-TRACKED Badge
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF1E3A8A).copy(alpha = 0.40f),
                                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_lucide_refresh),
                                            contentDescription = null,
                                            tint = Color(0xFF60A5FA),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "AUTO-TRACKED",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF60A5FA),
                                            letterSpacing = 0.3.sp
                                        )
                                    }
                                }
                            } else {
                                // ARCHIVED VERSION Badge
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF334155).copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, Color(0xFF64748B).copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "ARCHIVED RELEASE",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFCBD5E1),
                                        letterSpacing = 0.3.sp,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF312E81).copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "STABLE BUILD",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA5B4FC),
                                        letterSpacing = 0.3.sp,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Build ${activeRelease.buildNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Version Title & Copy Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = activeRelease.version,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = activeRelease.date,
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Circular Copy Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155))
                                .clickable {
                                    val clipText = "${activeRelease.version} (Build ${activeRelease.buildNumber})\n${activeRelease.summary}\nInvestment & Committee Ledger"
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Version Details", clipText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "${activeRelease.version} details copied", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("copy_version_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_lucide_copy),
                                contentDescription = "Copy version details",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = activeRelease.summary,
                        fontSize = 13.5.sp,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterCategories.forEach { categoryLabel ->
                    val cleanCategory = categoryLabel.substringBefore(" (")
                    val isSelected = (cleanCategory == "All Changes" && selectedFilter == "All Changes") ||
                            (cleanCategory == selectedFilter)

                    val chipBg = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0)
                    val chipText = if (isSelected) Color.White else Color(0xFF475569)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(chipBg)
                            .clickable {
                                selectedFilter = cleanCategory
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("filter_chip_$cleanCategory"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = categoryLabel,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = chipText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Changelog List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(releases) { release ->
                    val filteredMods = release.modifications.filter { mod ->
                        selectedFilter == "All Changes" || mod.category.equals(selectedFilter, ignoreCase = true)
                    }

                    if (filteredMods.isNotEmpty()) {
                        ReleaseCard(
                            release = release,
                            filteredModifications = filteredMods,
                            isSelected = release.version == activeRelease.version,
                            onSelect = { selectedRelease = release }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReleaseCard(
    release: VersionRelease,
    filteredModifications: List<ModificationItem>,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("release_card_${release.version}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF8FAFC) else Color.White
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.2.dp,
            if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row of Release Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = release.version,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )

                    if (release.isLatest) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "LATEST",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A),
                                letterSpacing = 0.3.sp
                            )
                        }
                    }

                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEFF6FF))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "VIEWING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB),
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Build ${release.buildNumber} • ${release.date}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = release.summary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Modification Items List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredModifications.forEach { item ->
                    ModificationItemRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun ModificationItemRow(item: ModificationItem) {
    val (tagBg, tagText) = when (item.category.lowercase()) {
        "ui/ux glass" -> Pair(Color(0xFFF3E8FF), Color(0xFF9333EA))
        "automation" -> Pair(Color(0xFFE0F2FE), Color(0xFF0284C7))
        "feature" -> Pair(Color(0xFFECFDF5), Color(0xFF059669))
        "storage" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFEEF2F6)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Tag, Title & Section Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tagBg)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = tagText
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.section.isNotBlank()) {
                    Text(
                        text = item.section,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.description,
                fontSize = 12.5.sp,
                color = Color(0xFF64748B),
                lineHeight = 17.sp
            )
        }
    }
}
