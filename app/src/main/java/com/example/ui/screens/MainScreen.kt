package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Committee
import com.example.data.model.Holding
import com.example.data.model.Ipo
import com.example.data.model.Sip
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.ui.components.AmbientCanvasBackground
import com.example.ui.components.AppNavigationDrawer
import com.example.ui.components.CommitteeBidDialog
import com.example.ui.components.CommitteeDialog
import com.example.ui.components.DataBackupDialog
import com.example.ui.components.HoldingDialog
import com.example.ui.components.IpoDialog
import com.example.ui.components.SheetsSyncDialog
import com.example.ui.components.SipDialog
import com.example.ui.components.UniversalSearchDialog
import com.example.ui.components.VersionModificationsBottomSheet
import com.example.ui.viewmodel.InvestmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: InvestmentViewModel) {
    var currentTab by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(pagerState.currentPage) {
        currentTab = pagerState.currentPage
    }

    val selectTab: (Int) -> Unit = { pageIndex ->
        currentTab = pageIndex
        coroutineScope.launch {
            pagerState.animateScrollToPage(pageIndex)
        }
    }

    val holdings by viewModel.holdings.collectAsState()
    val sips by viewModel.sips.collectAsState()
    val ipos by viewModel.ipos.collectAsState()
    val committees by viewModel.committees.collectAsState()
    val sheetConfig by viewModel.sheetConfig.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Modal dialog states
    var showSearchDialog by remember { mutableStateOf(false) }
    var showSheetsDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }

    var editingHolding by remember { mutableStateOf<Holding?>(null) }
    var showHoldingDialog by remember { mutableStateOf(false) }

    var editingSip by remember { mutableStateOf<Sip?>(null) }
    var showSipDialog by remember { mutableStateOf(false) }

    var editingIpo by remember { mutableStateOf<Ipo?>(null) }
    var showIpoDialog by remember { mutableStateOf(false) }

    var editingCommittee by remember { mutableStateOf<Committee?>(null) }
    var showCommitteeDialog by remember { mutableStateOf(false) }

    var bidCommittee by remember { mutableStateOf<Committee?>(null) }
    var bidMonth by remember { mutableIntStateOf(1) }
    var showBidDialog by remember { mutableStateOf(false) }

    var showChangelogBottomSheet by remember { mutableStateOf(false) }

    // Intercept phone back button:
    // 1. If any dialog/bottomsheet is open -> dismiss it
    // 2. If user is on any tab other than Summary (tab 0) -> navigate to Summary tab
    // 3. If user is on Summary tab with no dialogs open -> disable handler to allow system back to exit application
    val isAnyModalOpen = showChangelogBottomSheet ||
            showBidDialog ||
            showCommitteeDialog ||
            showIpoDialog ||
            showSipDialog ||
            showHoldingDialog ||
            showBackupDialog ||
            showSheetsDialog ||
            showSearchDialog

    val isNotOnSummary = pagerState.currentPage != 0 || currentTab != 0
    val isDrawerOpen = drawerState.isOpen

    BackHandler(enabled = isDrawerOpen || isAnyModalOpen || isNotOnSummary) {
        when {
            isDrawerOpen -> coroutineScope.launch { drawerState.close() }
            showChangelogBottomSheet -> showChangelogBottomSheet = false
            showBidDialog -> showBidDialog = false
            showCommitteeDialog -> showCommitteeDialog = false
            showIpoDialog -> showIpoDialog = false
            showSipDialog -> showSipDialog = false
            showHoldingDialog -> showHoldingDialog = false
            showBackupDialog -> showBackupDialog = false
            showSheetsDialog -> showSheetsDialog = false
            showSearchDialog -> showSearchDialog = false
            isNotOnSummary -> selectTab(0)
        }
    }

    AppNavigationDrawer(
        drawerState = drawerState,
        currentTab = currentTab,
        holdings = holdings,
        sips = sips,
        ipos = ipos,
        committees = committees,
        isSyncing = isSyncing,
        onSelectTab = { selectTab(it) },
        onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
        onOpenSearch = { showSearchDialog = true },
        onOpenSheets = { showSheetsDialog = true },
        onSyncNow = { viewModel.syncAllToSheets() },
        onOpenBackup = { showBackupDialog = true },
        onOpenChangelog = { showChangelogBottomSheet = true },
        onAddHolding = {
            editingHolding = null
            showHoldingDialog = true
        },
        onAddSip = {
            editingSip = null
            showSipDialog = true
        },
        onAddIpo = {
            editingIpo = null
            showIpoDialog = true
        },
        onAddCommittee = {
            editingCommittee = null
            showCommitteeDialog = true
        }
    ) {
        // Renders the ambient iridescent pastel mesh gradient background
        AmbientCanvasBackground(modifier = Modifier.fillMaxSize().testTag("main_screen")) {
            Column(modifier = Modifier.fillMaxSize()) {
                // TOP HEADER with Indian Flag trio color background (Saffron, White, Green)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.00f to Color(0xFFFF9933), // Indian Saffron (Kesari)
                                    0.28f to Color(0xFFFF9933),
                                    0.42f to Color(0xFFFFFFFF), // White
                                    0.58f to Color(0xFFFFFFFF),
                                    0.72f to Color(0xFF138808), // India Green
                                    1.00f to Color(0xFF138808)
                                )
                            )
                        )
                        .statusBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        // TOP HEADER BAR matching the website/screenshot
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left Hamburger Menu + Logo + Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                // 0. Hamburger Menu Button to open Left Sidebar
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.85f))
                                        .border(1.dp, Color.White.copy(alpha = 0.90f), CircleShape)
                                        .clickable { coroutineScope.launch { drawerState.open() } }
                                        .testTag("topbar_menu_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Open Sidebar Navigation Menu",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
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
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(7.dp))

                                Text(
                                    text = "Investment Tracker",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = (-0.3).sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Right 4 Action Buttons matching Screenshot 2
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                // 1. Search
                                HeaderCircleButton(
                                    iconRes = R.drawable.ic_lucide_search,
                                    contentDescription = "Search",
                                    iconTint = Color(0xFF059669),
                                    backgroundColor = Color.White.copy(alpha = 0.85f),
                                    borderColor = Color.White.copy(alpha = 0.90f),
                                    onClick = { showSearchDialog = true },
                                    testTag = "topbar_search_button"
                                )

                                // 2. Sync
                                HeaderCircleButton(
                                    iconRes = R.drawable.ic_lucide_refresh,
                                    contentDescription = "Sync",
                                    iconTint = Color(0xFF334155),
                                    backgroundColor = Color.White.copy(alpha = 0.85f),
                                    borderColor = Color.White.copy(alpha = 0.90f),
                                    isLoading = isSyncing,
                                    onClick = { viewModel.syncAllToSheets() },
                                    testTag = "topbar_sync_button"
                                )

                                // 3. Google Sheets
                                HeaderCircleButton(
                                    iconRes = R.drawable.ic_lucide_table,
                                    contentDescription = "Sheet",
                                    iconTint = Color(0xFF059669),
                                    backgroundColor = Color.White.copy(alpha = 0.85f),
                                    borderColor = Color.White.copy(alpha = 0.90f),
                                    onClick = { showSheetsDialog = true },
                                    testTag = "topbar_sheets_button"
                                )

                                // 4. Excel / Backup
                                HeaderCircleButton(
                                    iconRes = R.drawable.ic_lucide_file_spreadsheet,
                                    contentDescription = "Backup",
                                    iconTint = Color(0xFF059669),
                                    backgroundColor = Color.White.copy(alpha = 0.85f),
                                    borderColor = Color.White.copy(alpha = 0.90f),
                                    onClick = { showBackupDialog = true },
                                    testTag = "topbar_backup_button"
                                )
                            }
                        }

                        // HORIZONTAL SCROLLABLE TAB PILLS ROW (Sub-Header) with full visible texts
                        val tabScrollState = rememberScrollState()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(tabScrollState)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TabPill(
                                text = "Summary",
                                iconRes = R.drawable.ic_lucide_layout_grid,
                                isSelected = currentTab == 0,
                                activeBrush = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF0D9488))),
                                onClick = { selectTab(0) },
                                testTag = "tab_summary"
                            )
                            TabPill(
                                text = "Holdings",
                                iconRes = R.drawable.ic_lucide_layers,
                                isSelected = currentTab == 1,
                                activeBrush = Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF0284C7))),
                                onClick = { selectTab(1) },
                                testTag = "tab_holdings"
                            )
                            TabPill(
                                text = "SIPs",
                                iconRes = R.drawable.ic_lucide_arrows,
                                isSelected = currentTab == 2,
                                activeBrush = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C))),
                                onClick = { selectTab(2) },
                                testTag = "tab_sips"
                            )
                            TabPill(
                                text = "IPOs",
                                iconRes = R.drawable.ic_lucide_building,
                                isSelected = currentTab == 3,
                                activeBrush = Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFFE11D48))),
                                onClick = { selectTab(3) },
                                testTag = "tab_ipos"
                            )
                            TabPill(
                                text = "Committees",
                                iconRes = R.drawable.ic_lucide_users,
                                isSelected = currentTab == 4,
                                activeBrush = Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))),
                                onClick = { selectTab(4) },
                                testTag = "tab_committees"
                            )
                        }
                    }
                }

            // MAIN CONTENT VIEW with HorizontalPager for left-to-right and right-to-left swiping
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> SummaryScreen(
                            holdings = holdings,
                            sips = sips,
                            ipos = ipos,
                            committees = committees,
                            onNavigateToTab = { selectTab(it) }
                        )
                        1 -> HoldingsScreen(
                            holdings = holdings,
                            onAddHolding = {
                                editingHolding = null
                                showHoldingDialog = true
                            },
                            onEditHolding = { holding ->
                                editingHolding = holding
                                showHoldingDialog = true
                            },
                            onDeleteHolding = { holding ->
                                viewModel.deleteHolding(holding)
                            }
                        )
                        2 -> SipsScreen(
                            sips = sips,
                            onAddSip = {
                                editingSip = null
                                showSipDialog = true
                            },
                            onEditSip = { sip ->
                                editingSip = sip
                                showSipDialog = true
                            },
                            onDeleteSip = { sip ->
                                viewModel.deleteSip(sip)
                            },
                            onTogglePauseSip = { sip ->
                                viewModel.toggleSipPause(sip)
                            }
                        )
                        3 -> IposScreen(
                            ipos = ipos,
                            onAddIpo = {
                                editingIpo = null
                                showIpoDialog = true
                            },
                            onEditIpo = { ipo ->
                                editingIpo = ipo
                                showIpoDialog = true
                            },
                            onDeleteIpo = { ipo ->
                                viewModel.deleteIpo(ipo)
                            },
                            onMarkNotAllotted = { ipo ->
                                viewModel.saveIpo(ipo.copy(status = "Not Allotted", allottedLots = 0))
                            }
                        )
                        4 -> CommitteesScreen(
                            committees = committees,
                            onAddCommittee = {
                                editingCommittee = null
                                showCommitteeDialog = true
                            },
                            onEditCommittee = { comm ->
                                editingCommittee = comm
                                showCommitteeDialog = true
                            },
                            onDeleteCommittee = { comm ->
                                viewModel.deleteCommittee(comm)
                            },
                            onLogBid = { comm, monthNum ->
                                bidCommittee = comm
                                bidMonth = monthNum
                                showBidDialog = true
                            },
                            onTogglePaid = { comm, monthNum ->
                                viewModel.toggleCommitteeMonthPaid(comm, monthNum)
                            },
                            onDeleteMonth = { comm, monthNum ->
                                viewModel.deleteCommitteeMonth(comm, monthNum)
                            }
                        )
                    }
                }
            }
        }

        // FLOATING DOCKED BOTTOM NAVIGATION PILL
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(36.dp),
                color = Color.White.copy(alpha = 0.85f),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.95f)),
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Search Icon (emerald green outline)
                    IconButton(
                        onClick = { showSearchDialog = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_search),
                            contentDescription = "Search",
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Vertical divider line |
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    // 2. Summary Dashboard
                    BottomNavIcon(
                        iconRes = R.drawable.ic_lucide_layout_grid,
                        isSelected = currentTab == 0,
                        activeBrush = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF0D9488))),
                        onClick = { selectTab(0) },
                        contentDescription = "Summary"
                    )

                    // 3. Holdings
                    BottomNavIcon(
                        iconRes = R.drawable.ic_lucide_layers,
                        isSelected = currentTab == 1,
                        activeBrush = Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF0284C7))),
                        onClick = { selectTab(1) },
                        contentDescription = "Holdings"
                    )

                    // 4. SIPs
                    BottomNavIcon(
                        iconRes = R.drawable.ic_lucide_arrows,
                        isSelected = currentTab == 2,
                        activeBrush = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C))),
                        onClick = { selectTab(2) },
                        contentDescription = "SIPs"
                    )

                    // 5. IPOs
                    BottomNavIcon(
                        iconRes = R.drawable.ic_lucide_building,
                        isSelected = currentTab == 3,
                        activeBrush = Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFE11D48))),
                        onClick = { selectTab(3) },
                        contentDescription = "IPOs"
                    )

                    // 6. Committees
                    BottomNavIcon(
                        iconRes = R.drawable.ic_lucide_users,
                        isSelected = currentTab == 4,
                        activeBrush = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))),
                        onClick = { selectTab(4) },
                        contentDescription = "Committees"
                    )

                    // Vertical divider line |
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    // 7. Green FAB '+' Button to add items
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF10B981), Color(0xFF059669))
                                )
                            )
                            .clickable {
                                when (currentTab) {
                                    0, 1 -> {
                                        editingHolding = null
                                        showHoldingDialog = true
                                    }
                                    2 -> {
                                        editingSip = null
                                        showSipDialog = true
                                    }
                                    3 -> {
                                        editingIpo = null
                                        showIpoDialog = true
                                    }
                                    4 -> {
                                        editingCommittee = null
                                        showCommitteeDialog = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_plus),
                            contentDescription = "Add Item",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 76.dp)
        )

        // DIALOGS
        if (showSearchDialog) {
            UniversalSearchDialog(
                holdings = holdings,
                sips = sips,
                ipos = ipos,
                committees = committees,
                onDismiss = { showSearchDialog = false },
                onSelectItem = { type, _ ->
                    showSearchDialog = false
                    when (type) {
                        "holding" -> selectTab(1)
                        "sip" -> selectTab(2)
                        "ipo" -> selectTab(3)
                        "committee" -> selectTab(4)
                    }
                }
            )
        }

        if (showSheetsDialog) {
            SheetsSyncDialog(
                config = sheetConfig,
                isSyncing = isSyncing,
                onDismiss = { showSheetsDialog = false },
                onSaveConfig = { scriptUrl, autoSync ->
                    viewModel.updateSheetConfig(scriptUrl, autoSync)
                    showSheetsDialog = false
                },
                onSyncNow = {
                    viewModel.syncAllToSheets()
                },
                onPullFromSheet = {
                    viewModel.pullFromSheets()
                }
            )
        }

        if (showBackupDialog) {
            DataBackupDialog(
                holdings = holdings,
                sips = sips,
                ipos = ipos,
                committees = committees,
                onDismiss = { showBackupDialog = false },
                onImportData = { newHoldings, newSips, newIpos, newCommittees, replace ->
                    viewModel.importPortfolioData(newHoldings, newSips, newIpos, newCommittees, replace)
                    showBackupDialog = false
                },
                onShowToast = { msg ->
                    viewModel.showToast(msg)
                },
                onOpenChangelog = {
                    showChangelogBottomSheet = true
                }
            )
        }

        if (showHoldingDialog) {
            HoldingDialog(
                initialHolding = editingHolding,
                onDismiss = { showHoldingDialog = false },
                onSave = { holding ->
                    viewModel.saveHolding(holding)
                    showHoldingDialog = false
                }
            )
        }

        if (showSipDialog) {
            SipDialog(
                initialSip = editingSip,
                onDismiss = { showSipDialog = false },
                onSave = { sip ->
                    viewModel.saveSip(sip)
                    showSipDialog = false
                }
            )
        }

        if (showIpoDialog) {
            IpoDialog(
                initialIpo = editingIpo,
                onDismiss = { showIpoDialog = false },
                onSave = { ipo ->
                    viewModel.saveIpo(ipo)
                    showIpoDialog = false
                }
            )
        }

        if (showCommitteeDialog) {
            CommitteeDialog(
                initialCommittee = editingCommittee,
                onDismiss = { showCommitteeDialog = false },
                onSave = { committee ->
                    viewModel.saveCommittee(committee)
                    showCommitteeDialog = false
                }
            )
        }

        if (showBidDialog && bidCommittee != null) {
            CommitteeBidDialog(
                committee = bidCommittee!!,
                initialMonth = bidMonth,
                onDismiss = { showBidDialog = false },
                onSaveBid = { monthNumber, monthLabel, winningBid, winnerName, isPaid, installmentPerMember ->
                    viewModel.addOrUpdateCommitteeBid(
                        committee = bidCommittee!!,
                        monthNumber = monthNumber,
                        monthLabel = monthLabel,
                        winningBid = winningBid,
                        winnerName = winnerName,
                        isPaid = isPaid,
                        customInstallment = installmentPerMember
                    )
                    showBidDialog = false
                }
            )
        }

        if (showChangelogBottomSheet) {
            VersionModificationsBottomSheet(
                onDismissRequest = { showChangelogBottomSheet = false }
            )
        }
    }
}
}

@Composable
private fun HeaderCircleButton(
    iconRes: Int,
    contentDescription: String,
    iconTint: Color,
    backgroundColor: Color = Color.White.copy(alpha = 0.85f),
    borderColor: Color = Color.White.copy(alpha = 0.90f),
    onClick: () -> Unit,
    isLoading: Boolean = false,
    testTag: String = ""
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
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
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabPill(
    text: String,
    iconRes: Int,
    isSelected: Boolean,
    activeBrush: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    val pillModifier = if (isSelected) {
        modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .clip(RoundedCornerShape(50))
            .background(activeBrush)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag(testTag)
    } else {
        modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.88f))
            .border(1.dp, Color.White.copy(alpha = 0.95f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag(testTag)
    }

    Row(
        modifier = pillModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (isSelected) Color.White else Color(0xFF1E293B),
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isSelected) Color.White else Color(0xFF1E293B),
            letterSpacing = 0.2.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun BottomNavIcon(
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    activeBrush: Brush = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF0D9488)))
) {
    if (isSelected) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(activeBrush)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                tint = Color(0xFF334155),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
