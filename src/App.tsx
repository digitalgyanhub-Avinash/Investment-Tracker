import React, { useEffect, useRef, useState } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import { CommitteeModal } from './components/CommitteeModal';
import { CommitteesTable } from './components/CommitteesTable';
import { DeleteConfirmModal } from './components/DeleteConfirmModal';
import { ExcelImportExportModal } from './components/ExcelImportExportModal';
import { GoogleSheetsModal } from './components/GoogleSheetsModal';
import { HoldingModal } from './components/HoldingModal';
import { HoldingsTable } from './components/HoldingsTable';
import { IpoModal } from './components/IpoModal';
import { IposTable } from './components/IposTable';
import { Navbar } from './components/Navbar';
import { SipModal } from './components/SipModal';
import { SipsTable } from './components/SipsTable';
import { SummaryView } from './components/SummaryView';
import { UniversalSearchModal } from './components/UniversalSearchModal';
import { INITIAL_COMMITTEES, INITIAL_HOLDINGS, INITIAL_IPOS, INITIAL_SIPS } from './data/initialData';
import {
  deleteCommitteeFromSheet,
  deleteHoldingFromSheet,
  deleteIpoFromSheet,
  deleteSipFromSheet,
  fetchSheetData,
  saveCommitteeToSheet,
  saveHoldingToSheet,
  saveIpoToSheet,
  saveSipToSheet,
  syncAllToSheet,
} from './services/sheetsService';
import {
  Committee,
  CommitteeMonth,
  DeleteModalState,
  EditModalState,
  GoogleSheetConfig,
  Holding,
  IPOApplication,
  IPOStatus,
  SIP,
} from './types';
import { GOOGLE_APPS_SCRIPT_CODE } from './utils/googleAppsScriptCode';
import {
  BarChart3,
  Building2,
  Check,
  Copy,
  ExternalLink,
  Layers,
  LayoutDashboard,
  Plus,
  RefreshCw,
  Repeat,
  Search,
  Sheet,
  Sparkles,
  Users,
  Wallet,
} from 'lucide-react';

const SWIPEABLE_TABS: ('summary' | 'holdings' | 'sips' | 'ipos' | 'committees')[] = [
  'summary',
  'holdings',
  'sips',
  'ipos',
  'committees',
];

export default function App() {
  // Navigation - Default to 'summary' as 1st tab
  const [activeTab, setActiveTab] = useState<'summary' | 'holdings' | 'sips' | 'ipos' | 'committees'>('summary');
  const [direction, setDirection] = useState<number>(0);

  // Unread Sync notification dots on Navbar icons
  const [unreadSyncTabs, setUnreadSyncTabs] = useState<Set<'summary' | 'holdings' | 'sips' | 'ipos' | 'committees'>>(
    () => new Set()
  );
  const [hasSyncAlert, setHasSyncAlert] = useState(false);

  const handleTabChange = (newTab: 'summary' | 'holdings' | 'sips' | 'ipos' | 'committees') => {
    const prevIndex = SWIPEABLE_TABS.indexOf(activeTab);
    const nextIndex = SWIPEABLE_TABS.indexOf(newTab);
    setDirection(nextIndex > prevIndex ? 1 : -1);
    setActiveTab(newTab);

    // Clear unread notification for the selected tab
    setUnreadSyncTabs((prev) => {
      if (prev.has(newTab)) {
        const next = new Set(prev);
        next.delete(newTab);
        if (next.size === 0) {
          setHasSyncAlert(false);
        }
        return next;
      }
      return prev;
    });
  };

  // Core Data State (Loaded from localStorage or initial pre-loaded data)
  const [holdings, setHoldings] = useState<Holding[]>(() => {
    const saved = localStorage.getItem('inv_holdings');
    return saved ? JSON.parse(saved) : INITIAL_HOLDINGS;
  });

  const [sips, setSips] = useState<SIP[]>(() => {
    const saved = localStorage.getItem('inv_sips');
    return saved ? JSON.parse(saved) : INITIAL_SIPS;
  });

  const [ipos, setIpos] = useState<IPOApplication[]>(() => {
    const saved = localStorage.getItem('inv_ipos');
    return saved ? JSON.parse(saved) : INITIAL_IPOS;
  });

  const [committees, setCommittees] = useState<Committee[]>(() => {
    const saved = localStorage.getItem('inv_committees_v6');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed) && parsed.length > 0) {
          // Clean out any unbidded future placeholder rows
          return parsed.map((c: Committee) => ({
            ...c,
            months: (c.months || []).filter((m) => m.isPaid || m.winningBid > 0 || (m.winnerName && m.winnerName.trim() !== '' && m.winnerName !== '—')),
          }));
        }
      } catch (e) {
        console.error('Failed to parse saved committees', e);
      }
    }
    return INITIAL_COMMITTEES;
  });

  // Google Sheet Web App Config
  const [sheetConfig, setSheetConfig] = useState<GoogleSheetConfig>(() => {
    const saved = localStorage.getItem('inv_sheet_config');
    return saved
      ? JSON.parse(saved)
      : {
          scriptUrl: '',
          isConfigured: false,
          autoSync: true,
        };
  });

  // UI Modals
  const [isSheetModalOpen, setIsSheetModalOpen] = useState(false);
  const [isExcelModalOpen, setIsExcelModalOpen] = useState(false);
  const [isUniversalSearchOpen, setIsUniversalSearchOpen] = useState(false);
  const [isSyncing, setIsSyncing] = useState(false);
  const [toastMessage, setToastMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  // Pull-To-Refresh State
  const [pullDistance, setPullDistance] = useState(0);
  const [isPullRefreshing, setIsPullRefreshing] = useState(false);
  const isPullingRef = useRef(false);
  const pullStartYRef = useRef(0);
  const pullStartXRef = useRef(0);
  const isMouseDownRef = useRef(false);

  // Touch swipe navigation between tabs & Pull-To-Refresh gesture
  const touchStartX = useRef<number | null>(null);
  const touchStartY = useRef<number | null>(null);

  const handleTouchStart = (e: React.TouchEvent) => {
    // If any modal is open, ignore
    if (
      isUniversalSearchOpen ||
      isSheetModalOpen ||
      isExcelModalOpen ||
      editModal.isOpen ||
      deleteModal.isOpen ||
      isSyncing ||
      isPullRefreshing
    ) {
      touchStartX.current = null;
      touchStartY.current = null;
      isPullingRef.current = false;
      return;
    }

    const touch = e.touches[0];
    touchStartX.current = touch.clientX;
    touchStartY.current = touch.clientY;

    // Trigger pull-to-refresh eligibility only when near the top of the viewport
    const isAtTop = window.scrollY <= 2 && document.documentElement.scrollTop <= 2;
    if (isAtTop) {
      isPullingRef.current = true;
      pullStartYRef.current = touch.clientY;
      pullStartXRef.current = touch.clientX;
    } else {
      isPullingRef.current = false;
    }
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    if (!isPullingRef.current || isSyncing || isPullRefreshing) return;

    const touch = e.touches[0];
    const diffY = touch.clientY - pullStartYRef.current;
    const diffX = touch.clientX - pullStartXRef.current;

    // Must be pulling downward from the top and predominantly vertical
    if (diffY > 0 && diffY > Math.abs(diffX) && window.scrollY <= 2) {
      // Damped elastic calculation
      const damped = Math.min(80, Math.pow(diffY, 0.82) * 1.6);
      setPullDistance(damped);
    } else if (diffY <= 0) {
      setPullDistance(0);
    }
  };

  const handleTouchEnd = async (e: React.TouchEvent) => {
    if (touchStartX.current === null || touchStartY.current === null) {
      setPullDistance(0);
      return;
    }
    if (
      isUniversalSearchOpen ||
      isSheetModalOpen ||
      isExcelModalOpen ||
      editModal.isOpen ||
      deleteModal.isOpen
    ) {
      touchStartX.current = null;
      touchStartY.current = null;
      isPullingRef.current = false;
      setPullDistance(0);
      return;
    }

    const touch = e.changedTouches[0];
    const diffX = touch.clientX - touchStartX.current;
    const diffY = touch.clientY - touchStartY.current;

    touchStartX.current = null;
    touchStartY.current = null;
    const wasPulling = isPullingRef.current;
    isPullingRef.current = false;

    // 1. Pull-To-Refresh Trigger (threshold >= 48px)
    if (wasPulling && pullDistance >= 48 && !isSyncing && !isPullRefreshing) {
      setIsPullRefreshing(true);
      if ('vibrate' in navigator) {
        try {
          navigator.vibrate(20);
        } catch (_) {}
      }
      await handleFetchFromSheet(true);
      setIsPullRefreshing(false);
      setPullDistance(0);
      return;
    }

    setPullDistance(0);

    // 2. Horizontal Tab Swipe Trigger (predominantly horizontal and at least 45px swipe)
    if (Math.abs(diffX) > 45 && Math.abs(diffX) > Math.abs(diffY) * 1.5) {
      const currentIndex = SWIPEABLE_TABS.indexOf(activeTab);
      if (currentIndex === -1) return;

      if (diffX < 0) {
        // Swiped Left -> Move to Next Tab
        if (currentIndex < SWIPEABLE_TABS.length - 1) {
          handleTabChange(SWIPEABLE_TABS[currentIndex + 1]);
        }
      } else {
        // Swiped Right -> Move to Previous Tab
        if (currentIndex > 0) {
          handleTabChange(SWIPEABLE_TABS[currentIndex - 1]);
        }
      }
    }
  };

  // Mouse drag support for desktop users
  const handleMouseDown = (e: React.MouseEvent) => {
    if (
      e.button !== 0 ||
      window.scrollY > 2 ||
      isUniversalSearchOpen ||
      isSheetModalOpen ||
      isExcelModalOpen ||
      editModal.isOpen ||
      deleteModal.isOpen ||
      isSyncing ||
      isPullRefreshing
    ) {
      return;
    }
    isMouseDownRef.current = true;
    pullStartYRef.current = e.clientY;
    pullStartXRef.current = e.clientX;
    isPullingRef.current = true;
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isMouseDownRef.current || !isPullingRef.current || isSyncing || isPullRefreshing) return;
    const diffY = e.clientY - pullStartYRef.current;
    const diffX = e.clientX - pullStartXRef.current;
    if (diffY > 0 && diffY > Math.abs(diffX) && window.scrollY <= 2) {
      const damped = Math.min(80, Math.pow(diffY, 0.82) * 1.6);
      setPullDistance(damped);
    } else if (diffY <= 0) {
      setPullDistance(0);
    }
  };

  const handleMouseUp = async () => {
    if (!isMouseDownRef.current) return;
    isMouseDownRef.current = false;
    isPullingRef.current = false;
    if (pullDistance >= 48 && !isSyncing && !isPullRefreshing) {
      setIsPullRefreshing(true);
      await handleFetchFromSheet(true);
      setIsPullRefreshing(false);
    }
    setPullDistance(0);
  };

  // Edit / Add Modal State
  const [editModal, setEditModal] = useState<EditModalState>({
    isOpen: false,
    type: 'holding',
    mode: 'add',
    item: null,
  });

  // Delete Confirm Modal State (popup on delete click)
  const [deleteModal, setDeleteModal] = useState<DeleteModalState>({
    isOpen: false,
    type: 'holding',
    item: null,
  });

  // Save state to localStorage whenever holdings or sips change
  useEffect(() => {
    localStorage.setItem('inv_holdings', JSON.stringify(holdings));
  }, [holdings]);

  useEffect(() => {
    localStorage.setItem('inv_sips', JSON.stringify(sips));
  }, [sips]);

  useEffect(() => {
    localStorage.setItem('inv_committees_v6', JSON.stringify(committees));
  }, [committees]);

  useEffect(() => {
    localStorage.setItem('inv_ipos', JSON.stringify(ipos));
  }, [ipos]);

  useEffect(() => {
    localStorage.setItem('inv_sheet_config', JSON.stringify(sheetConfig));
  }, [sheetConfig]);

  // Toast Helper
  const showToast = (text: string, type: 'success' | 'error' = 'success') => {
    setToastMessage({ text, type });
    setTimeout(() => setToastMessage(null), 3500);
  };

  // Live Sync with Google Sheet on load if configured
  useEffect(() => {
    if (sheetConfig.isConfigured && sheetConfig.scriptUrl) {
      handleFetchFromSheet(false);
    }
  }, []);

  // Fetch live data from Google Sheet & notify updated tabs
  const handleFetchFromSheet = async (showToastNotice = true) => {
    setIsSyncing(true);
    const updatedCategories: ('summary' | 'holdings' | 'sips' | 'ipos' | 'committees')[] = [];

    if (sheetConfig.scriptUrl) {
      const res = await fetchSheetData(sheetConfig);
      setIsSyncing(false);

      if (res.success) {
        if (res.holdings && res.holdings.length > 0) {
          if (JSON.stringify(res.holdings) !== JSON.stringify(holdings)) {
            setHoldings(res.holdings);
            updatedCategories.push('holdings');
          }
        }
        if (res.sips && res.sips.length > 0) {
          if (JSON.stringify(res.sips) !== JSON.stringify(sips)) {
            setSips(res.sips);
            updatedCategories.push('sips');
          }
        }
        if (res.committees && res.committees.length > 0) {
          if (JSON.stringify(res.committees) !== JSON.stringify(committees)) {
            setCommittees(res.committees);
            updatedCategories.push('committees');
          }
        }
        if (res.ipos && res.ipos.length > 0) {
          if (JSON.stringify(res.ipos) !== JSON.stringify(ipos)) {
            setIpos(res.ipos);
            updatedCategories.push('ipos');
          }
        }

        if (updatedCategories.length > 0 && !updatedCategories.includes('summary')) {
          updatedCategories.push('summary');
        }

        setSheetConfig((prev) => ({ ...prev, lastSyncedAt: new Date().toISOString() }));

        if (updatedCategories.length > 0) {
          setUnreadSyncTabs((prev) => {
            const next = new Set(prev);
            updatedCategories.forEach((cat) => {
              if (cat !== activeTab) {
                next.add(cat);
              }
            });
            return next;
          });
          setHasSyncAlert(true);
          if (showToastNotice) {
            const names = updatedCategories
              .filter((c) => c !== 'summary')
              .map((c) => c.toUpperCase())
              .join(', ');
            showToast(`Synced! Updated: ${names || 'Summary'}`);
          }
        } else if (showToastNotice) {
          showToast('Data is up to date with Google Sheet!');
        }
      } else if (showToastNotice) {
        showToast(res.message || 'Failed to fetch from Google Sheet', 'error');
      }
    } else {
      // Demo mode: simulated sync latency with notification demo
      await new Promise((resolve) => setTimeout(resolve, 750));
      setIsSyncing(false);

      const otherTabs = SWIPEABLE_TABS.filter((t) => t !== activeTab);
      const simulatedUpdates = otherTabs.slice(0, 2);

      setUnreadSyncTabs((prev) => {
        const next = new Set(prev);
        simulatedUpdates.forEach((t) => next.add(t));
        return next;
      });
      setHasSyncAlert(true);

      if (showToastNotice) {
        showToast(
          `Sync complete! Updated ${simulatedUpdates.map((t) => t.toUpperCase()).join(' & ')} with unread alert`
        );
      }
    }
  };

  // Push all local data to Google Sheet
  const handlePushAllToSheet = async () => {
    if (!sheetConfig.scriptUrl) {
      setIsSheetModalOpen(true);
      return;
    }
    setIsSyncing(true);
    const res = await syncAllToSheet(sheetConfig, holdings, sips, committees, ipos);
    setIsSyncing(false);

    if (res.success) {
      if (res.holdings) setHoldings(res.holdings);
      if (res.sips) setSips(res.sips);
      if (res.committees) setCommittees(res.committees);
      if (res.ipos) setIpos(res.ipos);
      showToast('All local holdings, SIPs, Committees & IPOs pushed to Google Sheet!');
    } else {
      showToast(res.message || 'Failed pushing to sheet', 'error');
    }
  };

  // Test Web App URL connection
  const handleTestConnection = async (url: string): Promise<boolean> => {
    const testConfig: GoogleSheetConfig = { ...sheetConfig, scriptUrl: url, isConfigured: true };
    const res = await fetchSheetData(testConfig);
    if (res.success) {
      if (res.holdings && res.holdings.length > 0) setHoldings(res.holdings);
      if (res.sips && res.sips.length > 0) setSips(res.sips);
      if (res.committees && res.committees.length > 0) setCommittees(res.committees);
      if (res.ipos && res.ipos.length > 0) setIpos(res.ipos);
      return true;
    }
    return false;
  };

  // Save / Edit Holding
  const handleSaveHolding = async (savedHolding: Holding) => {
    const isEdit = editModal.mode === 'edit' || holdings.some((h) => h.id === savedHolding.id);

    // Update local state immediately
    setHoldings((prev) => {
      const exists = prev.some((h) => h.id === savedHolding.id);
      if (exists) {
        return prev.map((h) => (h.id === savedHolding.id ? savedHolding : h));
      }
      return [savedHolding, ...prev];
    });

    setEditModal({ isOpen: false, type: 'holding', mode: 'add', item: null });
    showToast(`${isEdit ? 'Updated' : 'Added'} holding entry!`);

    // Sync with Google Sheet if configured
    if (sheetConfig.isConfigured && sheetConfig.scriptUrl) {
      const res = await saveHoldingToSheet(sheetConfig, savedHolding, isEdit);
      if (res.success && res.holdings && res.holdings.length > 0) {
        setHoldings((prev) => {
          return res.holdings!.map((remoteHolding) => {
            if (remoteHolding.id === savedHolding.id) {
              return { ...remoteHolding, ...savedHolding };
            }
            return remoteHolding;
          });
        });
      }
    }
  };

  // Save / Edit SIP
  const handleSaveSip = async (savedSip: SIP) => {
    const isEdit = editModal.mode === 'edit' || sips.some((s) => s.id === savedSip.id);

    // Update local state immediately
    setSips((prev) => {
      const exists = prev.some((s) => s.id === savedSip.id);
      if (exists) {
        return prev.map((s) => (s.id === savedSip.id ? savedSip : s));
      }
      return [savedSip, ...prev];
    });

    setEditModal({ isOpen: false, type: 'sip', mode: 'add', item: null });
    showToast(`${isEdit ? 'Updated' : 'Added'} SIP entry!`);

    // Sync with Google Sheet if configured
    if (sheetConfig.isConfigured && sheetConfig.scriptUrl) {
      const res = await saveSipToSheet(sheetConfig, savedSip, isEdit);
      if (res.success && res.sips && res.sips.length > 0) {
        setSips((prev) => {
          return res.sips!.map((remoteSip) => {
            if (remoteSip.id === savedSip.id) {
              return { ...remoteSip, ...savedSip };
            }
            return remoteSip;
          });
        });
      }
    }
  };

  // Toggle Pause/Resume SIP
  const handleTogglePauseSip = async (sip: SIP) => {
    const updatedSip = { ...sip, isPaused: !sip.isPaused };
    setSips((prev) => prev.map((s) => (s.id === sip.id ? updatedSip : s)));
    showToast(updatedSip.isPaused ? `Paused ${sip.instrument}` : `Resumed ${sip.instrument}`);

    if (sheetConfig.isConfigured && sheetConfig.scriptUrl) {
      await saveSipToSheet(sheetConfig, updatedSip, true);
    }
  };

  // Save / Edit Committee
  const handleSaveCommittee = async (data: Partial<Committee>) => {
    let savedComm: Committee;
    const isEdit = editModal.mode === 'edit' && editModal.item !== null;

    if (!isEdit) {
      savedComm = {
        id: `comm-${Date.now()}`,
        name: data.name || 'New Committee',
        totalAmount: data.totalAmount || 90000,
        totalMembers: data.totalMembers || 12,
        totalMonths: data.totalMonths || 12,
        startDate: data.startDate || new Date().toISOString().substring(0, 10),
        baseMonthlyInstallment: data.baseMonthlyInstallment || 7500,
        currentMonth: data.currentMonth || 1,
        hasTakenPot: data.hasTakenPot || false,
        potTakenMonth: data.potTakenMonth,
        potTakenAmount: data.potTakenAmount,
        remarks: data.remarks || '',
        months: data.months || [],
        updatedAt: new Date().toISOString(),
      };
      setCommittees((prev) => [savedComm, ...prev]);
      showToast('Added new committee successfully!');
    } else {
      savedComm = {
        ...(editModal.item as Committee),
        ...data,
        updatedAt: new Date().toISOString(),
      };
      setCommittees((prev) =>
        prev.map((c) => (c.id === savedComm.id ? savedComm : c))
      );
      showToast('Updated committee successfully!');
    }
    setEditModal({ isOpen: false, type: 'committee', mode: 'add', item: null });

    if (sheetConfig.isConfigured && sheetConfig.scriptUrl) {
      saveCommitteeToSheet(sheetConfig, savedComm, isEdit);
    }
  };

  const handleUpdateCommitteeMonths = (committeeId: string, updatedMonths: CommitteeMonth[]) => {
    let updatedTargetComm: Committee | undefined;
    setCommittees((prev) =>
      prev.map((c) => {
        if (c.id === committeeId) {
          updatedTargetComm = { ...c, months: updatedMonths, updatedAt: new Date().toISOString() };
          return updatedTargetComm;
        }
        return c;
      })
    );
    if (updatedTargetComm && sheetConfig.isConfigured && sheetConfig.scriptUrl) {
      saveCommitteeToSheet(sheetConfig, updatedTargetComm, true);
    }
  };

  // Save / Edit IPO Application
  const handleSaveIpo = async (savedIpo: IPOApplication, isEdit: boolean) => {
    setIpos((prev) => {
      const exists = prev.some((item) => item.id === savedIpo.id);
      if (exists) {
        return prev.map((item) => (item.id === savedIpo.id ? savedIpo : item));
      }
      return [savedIpo, ...prev];
    });
    setEditModal({ isOpen: false, type: 'ipo', mode: 'add', item: null });
    showToast(`${isEdit ? 'Updated' : 'Added'} IPO application!`);

    if (sheetConfig.isConfigured && sheetConfig.scriptUrl) {
      saveIpoToSheet(sheetConfig, savedIpo, isEdit);
    }
  };

  const handleIpoStatusChange = (ipo: IPOApplication, newStatus: IPOStatus) => {
    const updated: IPOApplication = {
      ...ipo,
      status: newStatus,
      updatedAt: new Date().toISOString(),
    };
    setIpos((prev) => prev.map((item) => (item.id === ipo.id ? updated : item)));
    showToast(`Marked ${ipo.companyName} as ${newStatus}`);

    if (sheetConfig.isConfigured && sheetConfig.scriptUrl) {
      saveIpoToSheet(sheetConfig, updated, true);
    }
  };

  // Delete Request Handler (Opens confirmation popup modal)
  const handleDeleteConfirm = async () => {
    if (!deleteModal.item) return;

    const id = deleteModal.item.id;
    const itemType = deleteModal.type;

    if (itemType === 'holding') {
      setHoldings((prev) => prev.filter((h) => h.id !== id));
      showToast('Holding deleted');
      if (sheetConfig.isConfigured && sheetConfig.scriptUrl) {
        deleteHoldingFromSheet(sheetConfig, id);
      }
    } else if (itemType === 'sip') {
      setSips((prev) => prev.filter((s) => s.id !== id));
      showToast('SIP entry deleted');
      if (sheetConfig.isConfigured && sheetConfig.scriptUrl) {
        deleteSipFromSheet(sheetConfig, id);
      }
    } else if (itemType === 'committee') {
      setCommittees((prev) => prev.filter((c) => c.id !== id));
      showToast('Committee entry deleted');
      if (sheetConfig.isConfigured && sheetConfig.scriptUrl) {
        deleteCommitteeFromSheet(sheetConfig, id);
      }
    } else if (itemType === 'ipo') {
      setIpos((prev) => prev.filter((item) => item.id !== id));
      showToast('IPO application deleted');
      if (sheetConfig.isConfigured && sheetConfig.scriptUrl) {
        deleteIpoFromSheet(sheetConfig, id);
      }
    }

    setDeleteModal({ isOpen: false, type: 'holding', item: null });
  };

  return (
    <div className="relative min-h-screen bg-slate-50/80 dark:bg-slate-950/90 text-slate-900 dark:text-slate-100 flex flex-col font-sans selection:bg-emerald-500 selection:text-white pb-16 overflow-x-hidden w-full">
      {/* Ambient Hardware-Accelerated Liquid Canvas */}
      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
        <div className="absolute -top-20 -left-20 w-[30rem] h-[30rem] rounded-full bg-gradient-to-tr from-emerald-400/30 via-teal-300/25 to-cyan-400/20 dark:from-emerald-600/25 dark:via-teal-500/20 dark:to-cyan-600/15 blur-2xl transform-gpu animate-liquid-morph-1" />
        <div className="absolute top-1/4 -right-20 w-[28rem] h-[28rem] rounded-full bg-gradient-to-br from-indigo-500/30 via-purple-400/25 to-pink-500/20 dark:from-indigo-600/25 dark:via-purple-600/20 dark:to-pink-600/15 blur-2xl transform-gpu animate-liquid-morph-2" />
        <div className="absolute bottom-10 left-1/4 w-[26rem] h-[26rem] rounded-full bg-gradient-to-tr from-fuchsia-400/25 via-rose-400/20 to-amber-300/15 dark:from-fuchsia-600/20 dark:via-rose-600/15 dark:to-amber-500/15 blur-2xl transform-gpu animate-liquid-morph-1" />
      </div>

      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed bottom-6 right-6 z-50 animate-in slide-in-from-bottom duration-300">
          <div
            className={`px-4 py-3 rounded-2xl shadow-2xl glass-modal border text-sm font-semibold flex items-center gap-2.5 ${
              toastMessage.type === 'success'
                ? 'bg-slate-900/90 text-white border-emerald-500/40 dark:bg-slate-900/90 dark:border-emerald-500/50'
                : 'bg-red-900/90 text-white border-red-500/40'
            }`}
          >
            <Check className="w-4 h-4 text-emerald-400" />
            {toastMessage.text}
          </div>
        </div>
      )}

      {/* Main Navbar */}
      <Navbar
        activeTab={activeTab}
        setActiveTab={handleTabChange}
        holdings={holdings}
        sips={sips}
        ipos={ipos}
        committees={committees}
        sheetConfig={sheetConfig}
        onOpenSheetModal={() => setIsSheetModalOpen(true)}
        onOpenExcelModal={() => setIsExcelModalOpen(true)}
        onOpenUniversalSearch={() => setIsUniversalSearchOpen(true)}
        onSyncNow={() => handleFetchFromSheet(true)}
        isSyncing={isSyncing}
        unreadTabs={unreadSyncTabs}
        hasSyncAlert={hasSyncAlert}
      />

      {/* Main Content Area with Pull-To-Refresh and Swipe Navigation */}
      <main
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleTouchEnd}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        className="flex-1 max-w-7xl w-full mx-auto px-3 sm:px-6 lg:px-8 py-4 sm:py-6 pb-28 sm:pb-32 overflow-x-hidden select-text"
      >
        {/* Pull to Refresh Visual Indicator */}
        <div
          className="overflow-hidden transition-all duration-200 ease-out flex items-center justify-center pointer-events-none"
          style={{
            height: isSyncing || isPullRefreshing ? 48 : pullDistance > 0 ? pullDistance : 0,
            opacity: pullDistance > 10 || isSyncing || isPullRefreshing ? 1 : 0,
            marginBottom: isSyncing || isPullRefreshing || pullDistance > 0 ? '0.75rem' : '0rem',
          }}
        >
          <div className="flex items-center gap-2 px-4 py-1.5 rounded-full glass-card border border-emerald-500/40 text-xs font-semibold shadow-lg text-slate-800 dark:text-slate-100 backdrop-blur-xl bg-white/90 dark:bg-slate-900/90">
            <RefreshCw
              className={`w-3.5 h-3.5 text-emerald-500 transition-all duration-150 ${
                isSyncing || isPullRefreshing
                  ? 'animate-spin'
                  : pullDistance >= 48
                  ? 'rotate-180 text-emerald-400 scale-110'
                  : ''
              }`}
              style={{
                transform:
                  !isSyncing && !isPullRefreshing ? `rotate(${pullDistance * 4}deg)` : undefined,
              }}
            />
            <span>
              {isSyncing || isPullRefreshing
                ? 'Syncing latest data with Sheet...'
                : pullDistance >= 48
                ? 'Release to refresh'
                : 'Pull down to refresh'}
            </span>
          </div>
        </div>

        <AnimatePresence mode="wait" initial={false} custom={direction}>
          <motion.div
            key={activeTab}
            custom={direction}
            initial={{ opacity: 0, x: direction * 24 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: direction * -24 }}
            transition={{ duration: 0.18, ease: 'easeOut' }}
          >
            {/* Holdings Tab */}
            {activeTab === 'holdings' && (
              <HoldingsTable
                holdings={holdings}
                onAdd={() =>
                  setEditModal({
                    isOpen: true,
                    type: 'holding',
                    mode: 'add',
                    item: null,
                  })
                }
                onEdit={(item) =>
                  setEditModal({
                    isOpen: true,
                    type: 'holding',
                    mode: 'edit',
                    item,
                  })
                }
                onDeleteRequest={(item) =>
                  setDeleteModal({
                    isOpen: true,
                    type: 'holding',
                    item,
                  })
                }
              />
            )}

            {/* SIPs Tab */}
            {activeTab === 'sips' && (
              <SipsTable
                sips={sips}
                onAdd={() =>
                  setEditModal({
                    isOpen: true,
                    type: 'sip',
                    mode: 'add',
                    item: null,
                  })
                }
                onEdit={(item) =>
                  setEditModal({
                    isOpen: true,
                    type: 'sip',
                    mode: 'edit',
                    item,
                  })
                }
                onDeleteRequest={(item) =>
                  setDeleteModal({
                    isOpen: true,
                    type: 'sip',
                    item,
                  })
                }
                onTogglePause={handleTogglePauseSip}
              />
            )}

            {/* IPOs Tab */}
            {activeTab === 'ipos' && (
              <IposTable
                ipos={ipos}
                onAddClick={() =>
                  setEditModal({
                    isOpen: true,
                    type: 'ipo',
                    mode: 'add',
                    item: null,
                  })
                }
                onEditClick={(ipo) =>
                  setEditModal({
                    isOpen: true,
                    type: 'ipo',
                    mode: 'edit',
                    item: ipo,
                  })
                }
                onDeleteClick={(ipo) =>
                  setDeleteModal({
                    isOpen: true,
                    type: 'ipo',
                    item: ipo,
                  })
                }
                onStatusChange={handleIpoStatusChange}
              />
            )}

            {/* Committees Tab */}
            {activeTab === 'committees' && (
              <CommitteesTable
                committees={committees}
                onAddCommittee={() =>
                  setEditModal({
                    isOpen: true,
                    type: 'committee',
                    mode: 'add',
                    item: null,
                  })
                }
                onEditCommittee={(item) =>
                  setEditModal({
                    isOpen: true,
                    type: 'committee',
                    mode: 'edit',
                    item,
                  })
                }
                onDeleteCommittee={(committee) =>
                  setDeleteModal({
                    isOpen: true,
                    type: 'committee',
                    item: committee,
                  })
                }
                onUpdateCommitteeMonths={handleUpdateCommitteeMonths}
              />
            )}

            {/* 1st TAB: Summary (Consolidated Analytics + Projections + Health) */}
            {activeTab === 'summary' && (
              <SummaryView holdings={holdings} sips={sips} ipos={ipos} committees={committees} />
            )}
          </motion.div>
        </AnimatePresence>
      </main>



      {/* Modals */}
      <HoldingModal
        isOpen={editModal.isOpen && editModal.type === 'holding'}
        mode={editModal.mode}
        holding={editModal.item as Holding}
        onClose={() => setEditModal({ isOpen: false, type: 'holding', mode: 'add', item: null })}
        onSave={handleSaveHolding}
      />

      <SipModal
        isOpen={editModal.isOpen && editModal.type === 'sip'}
        mode={editModal.mode}
        sip={editModal.item as SIP}
        onClose={() => setEditModal({ isOpen: false, type: 'sip', mode: 'add', item: null })}
        onSave={handleSaveSip}
      />

      <IpoModal
        isOpen={editModal.isOpen && editModal.type === 'ipo'}
        editingIpo={editModal.item as IPOApplication}
        onClose={() => setEditModal({ isOpen: false, type: 'ipo', mode: 'add', item: null })}
        onSave={handleSaveIpo}
      />

      <CommitteeModal
        isOpen={editModal.isOpen && editModal.type === 'committee'}
        mode={editModal.mode}
        committee={editModal.item as Committee}
        onClose={() => setEditModal({ isOpen: false, type: 'committee', mode: 'add', item: null })}
        onSave={handleSaveCommittee}
      />

      <DeleteConfirmModal
        state={deleteModal}
        onClose={() => setDeleteModal({ isOpen: false, type: 'holding', item: null })}
        onConfirm={handleDeleteConfirm}
      />

      <GoogleSheetsModal
        isOpen={isSheetModalOpen}
        config={sheetConfig}
        onClose={() => setIsSheetModalOpen(false)}
        onSaveConfig={(cfg) => setSheetConfig(cfg)}
        onTestConnection={handleTestConnection}
        onSyncNow={handlePushAllToSheet}
        isSyncing={isSyncing}
      />

      <ExcelImportExportModal
        isOpen={isExcelModalOpen}
        holdings={holdings}
        sips={sips}
        onClose={() => setIsExcelModalOpen(false)}
        onImportData={(newH, newS) => {
          setHoldings(newH);
          setSips(newS);
          showToast('Imported data successfully!');
        }}
      />

      <UniversalSearchModal
        isOpen={isUniversalSearchOpen}
        onClose={() => setIsUniversalSearchOpen(false)}
        holdings={holdings}
        sips={sips}
        ipos={ipos}
        committees={committees}
        onSelectTab={(tab) => setActiveTab(tab)}
      />

      {/* Apple iOS 26 Adaptive Liquid Spatial Quick Dock */}
      <div className="fixed bottom-3 sm:bottom-4 left-1/2 -translate-x-1/2 z-40 max-w-[95vw] px-2 sm:px-3 py-1.5 rounded-2xl sm:rounded-3xl liquid-dock flex items-center gap-1 sm:gap-2 shadow-2xl backdrop-blur-3xl transition-all duration-300 hover:scale-[1.02]">
        <button
          onClick={() => setIsUniversalSearchOpen(true)}
          className="p-2 sm:px-3 sm:py-2 rounded-2xl flex items-center gap-1.5 text-xs font-bold transition-all cursor-pointer bg-white/40 dark:bg-white/10 hover:bg-white/60 dark:hover:bg-white/20 text-slate-700 dark:text-slate-200"
          title="Universal Search (⌘K)"
        >
          <Search className="w-4 h-4 text-emerald-500" />
          <span className="hidden md:inline">Search</span>
        </button>

        <div className="h-5 w-px bg-slate-300 dark:bg-slate-700 my-auto mx-0.5" />
        <button
          onClick={() => handleTabChange('summary')}
          className={`p-2 sm:px-3 sm:py-2 rounded-2xl flex items-center gap-1.5 text-xs font-bold transition-all cursor-pointer ${
            activeTab === 'summary'
              ? 'bg-gradient-to-r from-emerald-500 to-teal-600 text-white shadow-md shadow-emerald-500/30'
              : 'text-slate-600 dark:text-slate-300 hover:bg-emerald-50 dark:hover:bg-emerald-950/30 hover:text-emerald-600 dark:hover:text-emerald-400'
          }`}
          title="Summary"
        >
          <div className="relative inline-flex items-center justify-center">
            <LayoutDashboard className="w-4 h-4" />
            {unreadSyncTabs.has('summary') && (
              <span className="absolute -top-1 -right-1 flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
                <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500 ring-1.5 ring-white dark:ring-slate-900" />
              </span>
            )}
          </div>
          <span className="hidden md:inline">Summary</span>
        </button>

        <button
          onClick={() => handleTabChange('holdings')}
          className={`p-2 sm:px-3 sm:py-2 rounded-2xl flex items-center gap-1.5 text-xs font-bold transition-all cursor-pointer ${
            activeTab === 'holdings'
              ? 'bg-gradient-to-r from-blue-500 to-cyan-600 text-white shadow-md shadow-blue-500/30'
              : 'text-slate-600 dark:text-slate-300 hover:bg-blue-50 dark:hover:bg-blue-950/30 hover:text-blue-600 dark:hover:text-blue-400'
          }`}
          title="Holdings"
        >
          <div className="relative inline-flex items-center justify-center">
            <Layers className="w-4 h-4" />
            {unreadSyncTabs.has('holdings') && (
              <span className="absolute -top-1 -right-1 flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75" />
                <span className="relative inline-flex rounded-full h-2 w-2 bg-blue-500 ring-1.5 ring-white dark:ring-slate-900" />
              </span>
            )}
          </div>
          <span className="hidden md:inline">Holdings</span>
        </button>

        <button
          onClick={() => handleTabChange('sips')}
          className={`p-2 sm:px-3 sm:py-2 rounded-2xl flex items-center gap-1.5 text-xs font-bold transition-all cursor-pointer ${
            activeTab === 'sips'
              ? 'bg-gradient-to-r from-amber-500 to-orange-600 text-white shadow-md shadow-amber-500/30'
              : 'text-slate-600 dark:text-slate-300 hover:bg-amber-50 dark:hover:bg-amber-950/30 hover:text-amber-600 dark:hover:text-amber-400'
          }`}
          title="SIPs"
        >
          <div className="relative inline-flex items-center justify-center">
            <Repeat className="w-4 h-4" />
            {unreadSyncTabs.has('sips') && (
              <span className="absolute -top-1 -right-1 flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75" />
                <span className="relative inline-flex rounded-full h-2 w-2 bg-amber-500 ring-1.5 ring-white dark:ring-slate-900" />
              </span>
            )}
          </div>
          <span className="hidden md:inline">SIPs</span>
        </button>

        <button
          onClick={() => handleTabChange('ipos')}
          className={`p-2 sm:px-3 sm:py-2 rounded-2xl flex items-center gap-1.5 text-xs font-bold transition-all cursor-pointer ${
            activeTab === 'ipos'
              ? 'bg-gradient-to-r from-purple-600 to-fuchsia-600 text-white shadow-md shadow-purple-500/30'
              : 'text-slate-600 dark:text-slate-300 hover:bg-purple-50 dark:hover:bg-purple-950/30 hover:text-purple-600 dark:hover:text-purple-400'
          }`}
          title="IPOs"
        >
          <div className="relative inline-flex items-center justify-center">
            <Building2 className="w-4 h-4" />
            {unreadSyncTabs.has('ipos') && (
              <span className="absolute -top-1 -right-1 flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-purple-400 opacity-75" />
                <span className="relative inline-flex rounded-full h-2 w-2 bg-purple-500 ring-1.5 ring-white dark:ring-slate-900" />
              </span>
            )}
          </div>
          <span className="hidden md:inline">IPOs</span>
        </button>

        <button
          onClick={() => handleTabChange('committees')}
          className={`p-2 sm:px-3 sm:py-2 rounded-2xl flex items-center gap-1.5 text-xs font-bold transition-all cursor-pointer ${
            activeTab === 'committees'
              ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-md shadow-indigo-500/30'
              : 'text-slate-600 dark:text-slate-300 hover:bg-indigo-50 dark:hover:bg-indigo-950/30 hover:text-indigo-600 dark:hover:text-indigo-400'
          }`}
          title="Committees"
        >
          <div className="relative inline-flex items-center justify-center">
            <Users className="w-4 h-4" />
            {unreadSyncTabs.has('committees') && (
              <span className="absolute -top-1 -right-1 flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75" />
                <span className="relative inline-flex rounded-full h-2 w-2 bg-indigo-500 ring-1.5 ring-white dark:ring-slate-900" />
              </span>
            )}
          </div>
          <span className="hidden md:inline">Committees</span>
        </button>

        <div className="h-5 w-px bg-slate-300 dark:bg-slate-700 my-auto mx-0.5" />

        {/* Dynamic Quick Add Button based on active view */}
        <button
          onClick={() => {
            if (activeTab === 'sips') {
              setEditModal({ isOpen: true, type: 'sip', mode: 'add', item: null });
            } else if (activeTab === 'ipos') {
              setEditModal({ isOpen: true, type: 'ipo', mode: 'add', item: null });
            } else if (activeTab === 'committees') {
              setEditModal({ isOpen: true, type: 'committee', mode: 'add', item: null });
            } else {
              setEditModal({ isOpen: true, type: 'holding', mode: 'add', item: null });
            }
          }}
          className="p-2 sm:px-3.5 sm:py-2 rounded-2xl bg-gradient-to-r from-emerald-500 via-teal-500 to-cyan-500 text-white shadow-lg shadow-emerald-500/40 hover:brightness-110 active:scale-95 transition-all text-xs font-black flex items-center gap-1.5 cursor-pointer animate-iridescent"
        >
          <Plus className="w-4 h-4 stroke-[3]" />
          <span className="hidden sm:inline">Add Item</span>
        </button>
      </div>
    </div>
  );
}
