package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.model.Committee
import com.example.data.model.Holding
import com.example.data.model.Ipo
import com.example.data.model.Sip
import com.example.util.Calculations

@Composable
fun AppNavigationDrawer(
    drawerState: DrawerState,
    currentTab: Int,
    holdings: List<Holding>,
    sips: List<Sip>,
    ipos: List<Ipo>,
    committees: List<Committee>,
    isSyncing: Boolean,
    onSelectTab: (Int) -> Unit,
    onCloseDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSheets: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenChangelog: () -> Unit,
    onAddHolding: () -> Unit,
    onAddSip: () -> Unit,
    onAddIpo: () -> Unit,
    onAddCommittee: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        scrimColor = Color(0xFF0F172A).copy(alpha = 0.28f),
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                drawerContainerColor = Color.Transparent,
                drawerTonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier
                    .widthIn(min = 285.dp, max = 335.dp)
                    .fillMaxHeight()
                    .testTag("sidebar_drawer_sheet")
            ) {
                // Translucent frosted glass background matching the ambient iridescent mesh canvas of the tabs
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE6FFFA).copy(alpha = 0.88f), // Soft translucent mint
                                    Color(0xFFF0F9FF).copy(alpha = 0.86f), // Soft translucent sky
                                    Color(0xFFF5F3FF).copy(alpha = 0.88f), // Soft translucent lavender
                                    Color(0xFFFDF2F8).copy(alpha = 0.88f)  // Soft translucent rose
                                )
                            )
                        )
                        .border(
                            BorderStroke(1.2.dp, Color.White.copy(alpha = 0.90f)),
                            RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                        )
                ) {
                    SidebarDrawerContent(
                        currentTab = currentTab,
                        holdings = holdings,
                        sips = sips,
                        ipos = ipos,
                        committees = committees,
                        isSyncing = isSyncing,
                        onSelectTab = onSelectTab,
                        onCloseDrawer = onCloseDrawer,
                        onOpenSearch = onOpenSearch,
                        onOpenSheets = onOpenSheets,
                        onSyncNow = onSyncNow,
                        onOpenBackup = onOpenBackup,
                        onOpenChangelog = onOpenChangelog,
                        onAddHolding = onAddHolding,
                        onAddSip = onAddSip,
                        onAddIpo = onAddIpo,
                        onAddCommittee = onAddCommittee
                    )
                }
            }
        },
        content = content
    )
}

@Composable
private fun SidebarDrawerContent(
    currentTab: Int,
    holdings: List<Holding>,
    sips: List<Sip>,
    ipos: List<Ipo>,
    committees: List<Committee>,
    isSyncing: Boolean,
    onSelectTab: (Int) -> Unit,
    onCloseDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSheets: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenChangelog: () -> Unit,
    onAddHolding: () -> Unit,
    onAddSip: () -> Unit,
    onAddIpo: () -> Unit,
    onAddCommittee: () -> Unit
) {
    val scrollState = rememberScrollState()

    val holdingsTotal = remember(holdings) { Calculations.calculateHoldingsTotal(holdings) }
    val sipsTotal = remember(sips) { Calculations.calculateSipsTotal(sips) }
    val iposBlocked = remember(ipos) { Calculations.calculateIposBlockedTotal(ipos) }
    val committeesTotal = remember(committees) { Calculations.calculateCommitteesTotal(committees) }
    val grandTotal = holdingsTotal + sipsTotal + iposBlocked + committeesTotal

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(scrollState)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        // TOP HEADER CARD with subtle tricolor top border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.70f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.90f)),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Tricolor decorative bar at top
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFFF9933),
                                        Color(0xFFFFFFFF),
                                        Color(0xFF138808)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Brand and Close Row
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
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF10B981), Color(0xFF059669))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_lucide_wallet),
                                        contentDescription = "Wallet",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "Investment Tracker",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A),
                                        letterSpacing = (-0.3).sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Portfolio & Ledger",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            // Close Button
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.80f))
                                    .border(1.dp, Color.White.copy(alpha = 0.90f), CircleShape)
                                    .clickable { onCloseDrawer() }
                                    .testTag("sidebar_close_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Sidebar",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Net Portfolio Snapshot Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.60f))
                                .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL TRACKED PORTFOLIO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    letterSpacing = 0.6.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = Calculations.formatCurrency(grandTotal),
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PortfolioMicroChip(
                                        label = "${holdings.size} Hold",
                                        color = Color(0xFF3B82F6),
                                        modifier = Modifier.weight(1f)
                                    )
                                    PortfolioMicroChip(
                                        label = "${sips.size} SIP",
                                        color = Color(0xFFF59E0B),
                                        modifier = Modifier.weight(1f)
                                    )
                                    PortfolioMicroChip(
                                        label = "${ipos.size} IPO",
                                        color = Color(0xFFEC4899),
                                        modifier = Modifier.weight(1f)
                                    )
                                    PortfolioMicroChip(
                                        label = "${committees.size} Com",
                                        color = Color(0xFF8B5CF6),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 1: NAVIGATION TABS
        SectionHeader(title = "NAVIGATION")

        SidebarNavItem(
            title = "Summary Dashboard",
            subtitle = "Overview & analytics",
            iconRes = R.drawable.ic_lucide_layout_grid,
            badge = "Home",
            isSelected = currentTab == 0,
            activeColor = Color(0xFF10B981),
            onClick = {
                onSelectTab(0)
                onCloseDrawer()
            },
            testTag = "sidebar_nav_summary"
        )

        SidebarNavItem(
            title = "Holdings",
            subtitle = "Stocks & Mutual Funds",
            iconRes = R.drawable.ic_lucide_layers,
            badge = "${holdings.size}",
            isSelected = currentTab == 1,
            activeColor = Color(0xFF3B82F6),
            onClick = {
                onSelectTab(1)
                onCloseDrawer()
            },
            testTag = "sidebar_nav_holdings"
        )

        SidebarNavItem(
            title = "SIP Investments",
            subtitle = "Recurring plans & schedule",
            iconRes = R.drawable.ic_lucide_arrows,
            badge = "${sips.size}",
            isSelected = currentTab == 2,
            activeColor = Color(0xFFF59E0B),
            onClick = {
                onSelectTab(2)
                onCloseDrawer()
            },
            testTag = "sidebar_nav_sips"
        )

        SidebarNavItem(
            title = "IPO Tracker",
            subtitle = "Bids, allotment & listing",
            iconRes = R.drawable.ic_lucide_building,
            badge = "${ipos.size}",
            isSelected = currentTab == 3,
            activeColor = Color(0xFFEC4899),
            onClick = {
                onSelectTab(3)
                onCloseDrawer()
            },
            testTag = "sidebar_nav_ipos"
        )

        SidebarNavItem(
            title = "Committees / Chit Funds",
            subtitle = "Ledger, auctions & bids",
            iconRes = R.drawable.ic_lucide_users,
            badge = "${committees.size}",
            isSelected = currentTab == 4,
            activeColor = Color(0xFF8B5CF6),
            onClick = {
                onSelectTab(4)
                onCloseDrawer()
            },
            testTag = "sidebar_nav_committees"
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.65f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // SECTION 2: QUICK ENTRY ACTIONS
        SectionHeader(title = "QUICK ENTRY")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SidebarQuickAddButton(
                label = "+ Holding",
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f),
                onClick = {
                    onCloseDrawer()
                    onAddHolding()
                },
                testTag = "sidebar_quick_add_holding"
            )
            SidebarQuickAddButton(
                label = "+ SIP",
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f),
                onClick = {
                    onCloseDrawer()
                    onAddSip()
                },
                testTag = "sidebar_quick_add_sip"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SidebarQuickAddButton(
                label = "+ IPO Bid",
                color = Color(0xFFEC4899),
                modifier = Modifier.weight(1f),
                onClick = {
                    onCloseDrawer()
                    onAddIpo()
                },
                testTag = "sidebar_quick_add_ipo"
            )
            SidebarQuickAddButton(
                label = "+ Committee",
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f),
                onClick = {
                    onCloseDrawer()
                    onAddCommittee()
                },
                testTag = "sidebar_quick_add_committee"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.65f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // SECTION 3: CLOUD & DATA TOOLS
        SectionHeader(title = "TOOLS & DATA")

        SidebarToolItem(
            title = "Universal Search",
            subtitle = "Filter across all assets",
            iconRes = R.drawable.ic_lucide_search,
            iconTint = Color(0xFF059669),
            iconBg = Color(0xFFECFDF5),
            onClick = {
                onCloseDrawer()
                onOpenSearch()
            },
            testTag = "sidebar_tool_search"
        )

        SidebarToolItem(
            title = "Sync to Google Sheets",
            subtitle = if (isSyncing) "Syncing now..." else "Sync cloud spreadsheet",
            iconRes = R.drawable.ic_lucide_refresh,
            iconTint = Color(0xFF2563EB),
            iconBg = Color(0xFFEFF6FF),
            isLoading = isSyncing,
            onClick = {
                onCloseDrawer()
                onSyncNow()
            },
            testTag = "sidebar_tool_sync"
        )

        SidebarToolItem(
            title = "Google Sheets Settings",
            subtitle = "Configure web app endpoint",
            iconRes = R.drawable.ic_lucide_table,
            iconTint = Color(0xFF059669),
            iconBg = Color(0xFFECFDF5),
            onClick = {
                onCloseDrawer()
                onOpenSheets()
            },
            testTag = "sidebar_tool_sheets"
        )

        SidebarToolItem(
            title = "Backup & Restore",
            subtitle = "JSON / Excel import & export",
            iconRes = R.drawable.ic_lucide_file_spreadsheet,
            iconTint = Color(0xFF059669),
            iconBg = Color(0xFFECFDF5),
            onClick = {
                onCloseDrawer()
                onOpenBackup()
            },
            testTag = "sidebar_tool_backup"
        )

        SidebarToolItem(
            title = "Version & Modifications",
            subtitle = "Release notes & changelog",
            iconRes = R.drawable.ic_lucide_history,
            iconTint = Color(0xFF64748B),
            iconBg = Color(0xFFF1F5F9),
            onClick = {
                onCloseDrawer()
                onOpenChangelog()
            },
            testTag = "sidebar_tool_changelog"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // FOOTER INFO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline SQLite Active",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                    }

                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF94A3B8),
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
    )
}

@Composable
private fun PortfolioMicroChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
private fun SidebarNavItem(
    title: String,
    subtitle: String,
    iconRes: Int,
    badge: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val bgModifier = if (isSelected) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(activeColor.copy(alpha = 0.15f))
            .border(1.2.dp, activeColor.copy(alpha = 0.40f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .testTag(testTag)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.48f))
            .border(1.dp, Color.White.copy(alpha = 0.70f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .testTag(testTag)
    }

    Row(
        modifier = bgModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) activeColor else Color.White.copy(alpha = 0.80f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color(0xFF475569),
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(11.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isSelected) activeColor else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    if (isSelected) activeColor else Color.White.copy(alpha = 0.80f)
                )
                .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badge,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF334155)
            )
        }
    }
}

@Composable
private fun SidebarQuickAddButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.60f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun SidebarToolItem(
    title: String,
    subtitle: String,
    iconRes: Int,
    iconTint: Color,
    iconBg: Color,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.70f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = iconTint
                )
            } else {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(11.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
