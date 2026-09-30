import React from 'react';
import {
  Building2,
  CheckCircle2,
  FileSpreadsheet,
  Layers,
  LayoutDashboard,
  RefreshCw,
  Repeat,
  Search,
  Sheet,
  ShieldAlert,
  Users,
  Wallet,
} from 'lucide-react';
import { Committee, GoogleSheetConfig, Holding, IPOApplication, SIP } from '../types';
import {
  isCommitteeCompleted,
} from '../utils/calculations';

interface NavbarProps {
  activeTab: 'summary' | 'holdings' | 'sips' | 'ipos' | 'committees';
  setActiveTab: (tab: 'summary' | 'holdings' | 'sips' | 'ipos' | 'committees') => void;
  holdings: Holding[];
  sips: SIP[];
  ipos?: IPOApplication[];
  committees?: Committee[];
  sheetConfig: GoogleSheetConfig;
  onOpenSheetModal: () => void;
  onOpenExcelModal: () => void;
  onOpenUniversalSearch: () => void;
  onSyncNow: () => void;
  isSyncing: boolean;
  unreadTabs?: Set<'summary' | 'holdings' | 'sips' | 'ipos' | 'committees'>;
  hasSyncAlert?: boolean;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  setActiveTab,
  holdings,
  sips,
  ipos = [],
  committees = [],
  sheetConfig,
  onOpenSheetModal,
  onOpenExcelModal,
  onOpenUniversalSearch,
  onSyncNow,
  isSyncing,
  unreadTabs,
  hasSyncAlert = false,
}) => {
  const activeIposCount = ipos.filter((i) => i.status === 'Applied' || i.status === 'Allotted').length;
  const activeCommitteesCount = committees.filter((c) => !isCommitteeCompleted(c)).length;

  return (
    <header className="sticky top-0 z-30 glass-header border-b border-white/40 dark:border-white/10 shadow-md shadow-black/5">
      <div className="max-w-7xl mx-auto px-2.5 sm:px-6 lg:px-8">
        {/* Compact Top Header Bar */}
        <div className="py-2 flex items-center justify-between gap-2">
          {/* Logo, Title & Status */}
          <div className="flex items-center gap-2 min-w-0">
            <div className="p-1.5 sm:p-2 rounded-xl bg-gradient-to-tr from-emerald-500/90 to-teal-600/90 text-white shadow-md shadow-emerald-500/20 backdrop-blur-md border border-white/30 shrink-0">
              <Wallet className="w-4 h-4 sm:w-5 sm:h-5" />
            </div>
            <div className="min-w-0">
              <div className="flex items-center gap-1.5">
                <h1 className="text-xs sm:text-base font-extrabold tracking-tight text-slate-900 dark:text-white truncate">
                  Investment Tracker
                </h1>
                {/* Sync Status Badge - Hidden on tiny screens, visible on sm+ */}
                <button
                  onClick={onOpenSheetModal}
                  className={`hidden md:flex px-2 py-0.5 rounded-full text-[10px] font-semibold items-center gap-1 transition-all shrink-0 backdrop-blur-md border cursor-pointer ${
                    sheetConfig.isConfigured
                      ? 'bg-emerald-500/15 text-emerald-800 dark:text-emerald-300 border-emerald-500/30 hover:bg-emerald-500/25'
                      : 'bg-amber-500/15 text-amber-800 dark:text-amber-300 border-amber-500/30 hover:bg-amber-500/25'
                  }`}
                >
                  {sheetConfig.isConfigured ? (
                    <>
                      <CheckCircle2 className="w-2.5 h-2.5 text-emerald-600 dark:text-emerald-400" />
                      <span>Live Sheet</span>
                    </>
                  ) : (
                    <>
                      <ShieldAlert className="w-2.5 h-2.5 text-amber-600 dark:text-amber-400" />
                      <span>Local Demo</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>

          {/* Right Action Bar */}
          <div className="flex items-center gap-1.5 shrink-0">
            {/* Universal Search Button */}
            <button
              onClick={onOpenUniversalSearch}
              title="Universal Search (⌘K)"
              className="p-1.5 sm:px-2.5 sm:py-1.5 rounded-xl bg-emerald-500/10 hover:bg-emerald-500/20 dark:bg-emerald-500/20 dark:hover:bg-emerald-500/30 text-emerald-800 dark:text-emerald-200 border border-emerald-500/30 text-xs font-bold flex items-center gap-1.5 transition-all cursor-pointer shrink-0 active:scale-95"
            >
              <Search className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
              <span className="hidden sm:inline">Search</span>
              <kbd className="hidden md:inline-block px-1.5 py-0.2 text-[9px] font-mono rounded bg-white/40 dark:bg-black/30 text-slate-700 dark:text-slate-300 border border-emerald-400/30">⌘K</kbd>
            </button>

            {/* Sync Button */}
            <button
              onClick={onSyncNow}
              disabled={isSyncing}
              title="Sync with Google Sheet"
              className="p-1.5 sm:px-2.5 sm:py-1.5 rounded-xl glass-button text-slate-700 dark:text-slate-200 text-xs font-semibold flex items-center gap-1 transition-all disabled:opacity-50 cursor-pointer shrink-0"
            >
              <div className="relative inline-flex items-center justify-center shrink-0">
                <RefreshCw className={`w-3.5 h-3.5 ${isSyncing ? 'animate-spin text-emerald-500' : ''}`} />
                {hasSyncAlert && !isSyncing && (
                  <span className="absolute -top-1 -right-1 flex h-2 w-2">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500 ring-1.5 ring-white dark:ring-slate-900" />
                  </span>
                )}
              </div>
              <span className="hidden sm:inline">Sync</span>
            </button>

            {/* Sheet Config Button */}
            <button
              onClick={onOpenSheetModal}
              title="Google Sheet Configuration"
              className="p-1.5 sm:px-2.5 sm:py-1.5 rounded-xl glass-button text-slate-700 dark:text-slate-200 text-xs font-semibold flex items-center gap-1 transition-all cursor-pointer shrink-0"
            >
              <Sheet className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
              <span className="hidden sm:inline">Sheet</span>
            </button>

            {/* Excel Backup Modal Button */}
            <button
              onClick={onOpenExcelModal}
              title="Import/Export Excel"
              className="p-1.5 sm:px-2.5 sm:py-1.5 rounded-xl glass-button text-slate-700 dark:text-slate-200 text-xs font-semibold flex items-center gap-1 transition-all cursor-pointer shrink-0"
            >
              <FileSpreadsheet className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
              <span className="hidden sm:inline">Excel</span>
            </button>
          </div>
        </div>

        {/* Navigation Tabs Bar - Fixed non-scrollable pill bar */}
        <div className="py-1.5 w-full overflow-hidden">
          <div className="grid grid-cols-5 gap-1 p-1 rounded-full bg-white/90 dark:bg-slate-900/90 shadow-sm border border-slate-200/80 dark:border-slate-800/80 w-full items-center backdrop-blur-md">
            <button
              onClick={() => setActiveTab('summary')}
              className={`flex items-center justify-center gap-1 sm:gap-1.5 py-1.5 px-1 sm:px-3 rounded-full text-xs font-bold transition-all duration-200 min-w-0 cursor-pointer active:scale-95 ${
                activeTab === 'summary'
                  ? 'bg-gradient-to-r from-emerald-500 to-teal-600 text-white shadow-sm shadow-emerald-500/30'
                  : 'text-slate-600 dark:text-slate-300 hover:text-emerald-600 dark:hover:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-950/30'
              }`}
            >
              <div className="relative inline-flex items-center justify-center shrink-0">
                <LayoutDashboard className="w-3.5 h-3.5 shrink-0" />
                {unreadTabs?.has('summary') && (
                  <span className="absolute -top-1 -right-1 flex h-2 w-2">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500 ring-1.5 ring-white dark:ring-slate-900" />
                  </span>
                )}
              </div>
              <span className="truncate">
                <span className="inline sm:hidden">Sum...</span>
                <span className="hidden sm:inline">Summary</span>
              </span>
            </button>

            <button
              onClick={() => setActiveTab('holdings')}
              className={`flex items-center justify-center gap-1 sm:gap-1.5 py-1.5 px-1 sm:px-3 rounded-full text-xs font-bold transition-all duration-200 min-w-0 cursor-pointer active:scale-95 ${
                activeTab === 'holdings'
                  ? 'bg-gradient-to-r from-blue-500 to-cyan-600 text-white shadow-sm shadow-blue-500/30'
                  : 'text-slate-600 dark:text-slate-300 hover:text-blue-600 dark:hover:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-950/30'
              }`}
            >
              <div className="relative inline-flex items-center justify-center shrink-0">
                <Layers className="w-3.5 h-3.5 shrink-0" />
                {unreadTabs?.has('holdings') && (
                  <span className="absolute -top-1 -right-1 flex h-2 w-2">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75" />
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-blue-500 ring-1.5 ring-white dark:ring-slate-900" />
                  </span>
                )}
              </div>
              <span className="truncate">
                <span className="inline sm:hidden">Holdi...</span>
                <span className="hidden sm:inline">Holdings</span>
              </span>
            </button>

            <button
              onClick={() => setActiveTab('sips')}
              className={`flex items-center justify-center gap-1 sm:gap-1.5 py-1.5 px-1 sm:px-3 rounded-full text-xs font-bold transition-all duration-200 min-w-0 cursor-pointer active:scale-95 ${
                activeTab === 'sips'
                  ? 'bg-gradient-to-r from-amber-500 to-orange-600 text-white shadow-sm shadow-amber-500/30'
                  : 'text-slate-600 dark:text-slate-300 hover:text-amber-600 dark:hover:text-amber-400 hover:bg-amber-50 dark:hover:bg-amber-950/30'
              }`}
            >
              <div className="relative inline-flex items-center justify-center shrink-0">
                <Repeat className="w-3.5 h-3.5 shrink-0" />
                {unreadTabs?.has('sips') && (
                  <span className="absolute -top-1 -right-1 flex h-2 w-2">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75" />
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-amber-500 ring-1.5 ring-white dark:ring-slate-900" />
                  </span>
                )}
              </div>
              <span className="truncate">SIPs</span>
            </button>

            <button
              onClick={() => setActiveTab('ipos')}
              className={`flex items-center justify-center gap-1 sm:gap-1.5 py-1.5 px-1 sm:px-3 rounded-full text-xs font-bold transition-all duration-200 min-w-0 cursor-pointer active:scale-95 ${
                activeTab === 'ipos'
                  ? 'bg-gradient-to-r from-purple-600 to-fuchsia-600 text-white shadow-sm shadow-purple-500/30'
                  : 'text-slate-600 dark:text-slate-300 hover:text-purple-600 dark:hover:text-purple-400 hover:bg-purple-50 dark:hover:bg-purple-950/30'
              }`}
            >
              <div className="relative inline-flex items-center justify-center shrink-0">
                <Building2 className="w-3.5 h-3.5 shrink-0" />
                {unreadTabs?.has('ipos') && (
                  <span className="absolute -top-1 -right-1 flex h-2 w-2">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-purple-400 opacity-75" />
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-purple-500 ring-1.5 ring-white dark:ring-slate-900" />
                  </span>
                )}
              </div>
              <span className="truncate">IPOs</span>
            </button>

            <button
              onClick={() => setActiveTab('committees')}
              className={`flex items-center justify-center gap-1 sm:gap-1.5 py-1.5 px-1 sm:px-3 rounded-full text-xs font-bold transition-all duration-200 min-w-0 cursor-pointer active:scale-95 ${
                activeTab === 'committees'
                  ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-sm shadow-indigo-500/30'
                  : 'text-slate-600 dark:text-slate-300 hover:text-indigo-600 dark:hover:text-indigo-400 hover:bg-indigo-50 dark:hover:bg-indigo-950/30'
              }`}
            >
              <div className="relative inline-flex items-center justify-center shrink-0">
                <Users className="w-3.5 h-3.5 shrink-0" />
                {unreadTabs?.has('committees') && (
                  <span className="absolute -top-1 -right-1 flex h-2 w-2">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75" />
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-indigo-500 ring-1.5 ring-white dark:ring-slate-900" />
                  </span>
                )}
              </div>
              <span className="truncate">
                <span className="inline sm:hidden">Com...</span>
                <span className="hidden sm:inline">Committees</span>
              </span>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};

