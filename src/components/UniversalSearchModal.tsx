import React, { useEffect, useRef, useState } from 'react';
import {
  Building2,
  CheckCircle2,
  Clock,
  Layers,
  Repeat,
  Search,
  Sparkles,
  Users,
  X,
} from 'lucide-react';
import { Committee, Holding, IPOApplication, SIP } from '../types';
import { calculateSipInvested, formatCurrency } from '../utils/calculations';

interface UniversalSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
  holdings: Holding[];
  sips: SIP[];
  ipos: IPOApplication[];
  committees: Committee[];
  onNavigateToTab: (
    tab: 'summary' | 'holdings' | 'sips' | 'ipos' | 'committees'
  ) => void;
}

export const UniversalSearchModal: React.FC<UniversalSearchModalProps> = ({
  isOpen,
  onClose,
  holdings,
  sips,
  ipos,
  committees,
  onNavigateToTab,
}) => {
  const [query, setQuery] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (isOpen) {
      setTimeout(() => inputRef.current?.focus(), 50);
    } else {
      setQuery('');
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const trimmed = query.trim().toLowerCase();

  // Search Holdings
  const matchedHoldings = trimmed
    ? holdings.filter(
        (h) =>
          h.platform.toLowerCase().includes(trimmed) ||
          h.category.toLowerCase().includes(trimmed) ||
          (h.remarks && h.remarks.toLowerCase().includes(trimmed))
      )
    : [];

  // Search SIPs
  const matchedSips = trimmed
    ? sips.filter(
        (s) =>
          s.instrument.toLowerCase().includes(trimmed) ||
          s.platform.toLowerCase().includes(trimmed) ||
          (s.remarks && s.remarks.toLowerCase().includes(trimmed))
      )
    : [];

  // Search IPOs
  const matchedIpos = trimmed
    ? ipos.filter(
        (i) =>
          i.companyName.toLowerCase().includes(trimmed) ||
          (i.platform && i.platform.toLowerCase().includes(trimmed)) ||
          (i.applicantName && i.applicantName.toLowerCase().includes(trimmed)) ||
          (i.remarks && i.remarks.toLowerCase().includes(trimmed)) ||
          i.status.toLowerCase().includes(trimmed)
      )
    : [];

  // Search Committees
  const matchedCommittees = trimmed
    ? committees.filter(
        (c) =>
          c.name.toLowerCase().includes(trimmed) ||
          (c.borrowerName && c.borrowerName.toLowerCase().includes(trimmed)) ||
          (c.purpose && c.purpose.toLowerCase().includes(trimmed)) ||
          (c.remarks && c.remarks.toLowerCase().includes(trimmed))
      )
    : [];

  const totalMatches =
    matchedHoldings.length +
    matchedSips.length +
    matchedIpos.length +
    matchedCommittees.length;

  const handleSelect = (
    tab: 'summary' | 'holdings' | 'sips' | 'ipos' | 'committees'
  ) => {
    onNavigateToTab(tab);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center pt-16 sm:pt-24 px-3 sm:px-4 bg-slate-950/60 backdrop-blur-md animate-in fade-in duration-200">
      <div
        className="w-full max-w-2xl bg-white/95 dark:bg-slate-900/95 border border-white/40 dark:border-white/10 rounded-3xl shadow-2xl overflow-hidden flex flex-col max-h-[80vh] transition-all"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Search Header Bar */}
        <div className="p-3.5 sm:p-4 border-b border-slate-200/80 dark:border-slate-800 flex items-center gap-3">
          <Search className="w-5 h-5 text-emerald-500 shrink-0" />
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search across Holdings, SIPs, IPOs & Committees..."
            className="w-full bg-transparent text-sm sm:text-base font-medium text-slate-900 dark:text-white focus:outline-none placeholder:text-slate-400"
          />
          {query ? (
            <button
              onClick={() => setQuery('')}
              className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
            >
              <X className="w-4 h-4" />
            </button>
          ) : (
            <kbd className="hidden sm:inline-block px-2 py-0.5 text-[10px] font-semibold text-slate-400 bg-slate-100 dark:bg-slate-800 rounded-md border border-slate-200 dark:border-slate-700">
              ESC to close
            </kbd>
          )}
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Results Container */}
        <div className="flex-1 overflow-y-auto p-3.5 sm:p-5 space-y-5 [scrollbar-width:thin]">
          {!trimmed && (
            <div className="py-12 text-center text-slate-400 dark:text-slate-500 space-y-2">
              <Sparkles className="w-8 h-8 mx-auto text-emerald-500/60 animate-pulse" />
              <p className="text-sm font-semibold">Universal Investment Search</p>
              <p className="text-xs opacity-75">
                Type any company name, platform, broker, SIP fund, or committee member...
              </p>
            </div>
          )}

          {trimmed && totalMatches === 0 && (
            <div className="py-12 text-center text-slate-400 dark:text-slate-500">
              <p className="text-sm font-semibold text-slate-700 dark:text-slate-300">
                No matching results found for "{query}"
              </p>
              <p className="text-xs mt-1">Try searching for Zerodha, Groww, Tata, Mutual Fund, or SBI...</p>
            </div>
          )}

          {/* Holdings Results */}
          {matchedHoldings.length > 0 && (
            <div>
              <div className="flex items-center justify-between text-xs font-bold uppercase tracking-wider text-emerald-600 dark:text-emerald-400 mb-2 px-1">
                <span className="flex items-center gap-1.5">
                  <Layers className="w-3.5 h-3.5" /> Holdings ({matchedHoldings.length})
                </span>
                <button
                  onClick={() => handleSelect('holdings')}
                  className="text-[11px] hover:underline cursor-pointer"
                >
                  View All Holdings &rarr;
                </button>
              </div>
              <div className="space-y-1.5">
                {matchedHoldings.map((h) => (
                  <div
                    key={h.id}
                    onClick={() => handleSelect('holdings')}
                    className="p-3 rounded-2xl glass-card hover:border-emerald-500/40 transition-all cursor-pointer flex items-center justify-between gap-3 group"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-slate-900 dark:text-white group-hover:text-emerald-500 transition-colors">
                          {h.platform}
                        </span>
                        <span className="px-2 py-0.5 rounded-md bg-emerald-500/10 text-emerald-700 dark:text-emerald-300 text-[10px] font-semibold">
                          {h.category}
                        </span>
                      </div>
                      {h.remarks && (
                        <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5 line-clamp-1">
                          {h.remarks}
                        </p>
                      )}
                    </div>
                    <span className="text-xs font-black text-slate-900 dark:text-white shrink-0">
                      {formatCurrency(h.total)}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* SIP Results */}
          {matchedSips.length > 0 && (
            <div>
              <div className="flex items-center justify-between text-xs font-bold uppercase tracking-wider text-blue-600 dark:text-blue-400 mb-2 px-1">
                <span className="flex items-center gap-1.5">
                  <Repeat className="w-3.5 h-3.5" /> SIPs ({matchedSips.length})
                </span>
                <button
                  onClick={() => handleSelect('sips')}
                  className="text-[11px] hover:underline cursor-pointer"
                >
                  View All SIPs &rarr;
                </button>
              </div>
              <div className="space-y-1.5">
                {matchedSips.map((s) => (
                  <div
                    key={s.id}
                    onClick={() => handleSelect('sips')}
                    className="p-3 rounded-2xl glass-card hover:border-blue-500/40 transition-all cursor-pointer flex items-center justify-between gap-3 group"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-slate-900 dark:text-white group-hover:text-blue-500 transition-colors">
                          {s.instrument}
                        </span>
                        <span className="px-2 py-0.5 rounded-md bg-blue-500/10 text-blue-700 dark:text-blue-300 text-[10px] font-semibold">
                          {s.platform}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">
                        {formatCurrency(s.monthlyAmount)}/mo • Paid: {formatCurrency(calculateSipInvested(s))}
                      </p>
                    </div>
                    <span className="text-xs font-black text-blue-600 dark:text-blue-400 shrink-0">
                      {formatCurrency(calculateSipInvested(s))}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* IPO Results */}
          {matchedIpos.length > 0 && (
            <div>
              <div className="flex items-center justify-between text-xs font-bold uppercase tracking-wider text-purple-600 dark:text-purple-400 mb-2 px-1">
                <span className="flex items-center gap-1.5">
                  <Building2 className="w-3.5 h-3.5" /> IPOs ({matchedIpos.length})
                </span>
                <button
                  onClick={() => handleSelect('ipos')}
                  className="text-[11px] hover:underline cursor-pointer"
                >
                  View All IPOs &rarr;
                </button>
              </div>
              <div className="space-y-1.5">
                {matchedIpos.map((i) => (
                  <div
                    key={i.id}
                    onClick={() => handleSelect('ipos')}
                    className="p-3 rounded-2xl glass-card hover:border-purple-500/40 transition-all cursor-pointer flex items-center justify-between gap-3 group"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-slate-900 dark:text-white group-hover:text-purple-500 transition-colors">
                          {i.companyName}
                        </span>
                        <span className="px-2 py-0.5 rounded-md bg-purple-500/10 text-purple-700 dark:text-purple-300 text-[10px] font-semibold">
                          {i.status}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">
                        {i.applicantName} • {i.platform} • {i.shares} shares @ ₹{i.pricePerShare}
                      </p>
                    </div>
                    <span className="text-xs font-black text-slate-900 dark:text-white shrink-0">
                      {formatCurrency(i.amountBlocked || i.shares * i.pricePerShare)}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Committee Results */}
          {matchedCommittees.length > 0 && (
            <div>
              <div className="flex items-center justify-between text-xs font-bold uppercase tracking-wider text-indigo-600 dark:text-indigo-400 mb-2 px-1">
                <span className="flex items-center gap-1.5">
                  <Users className="w-3.5 h-3.5" /> Committees ({matchedCommittees.length})
                </span>
                <button
                  onClick={() => handleSelect('committees')}
                  className="text-[11px] hover:underline cursor-pointer"
                >
                  View All Committees &rarr;
                </button>
              </div>
              <div className="space-y-1.5">
                {matchedCommittees.map((c) => (
                  <div
                    key={c.id}
                    onClick={() => handleSelect('committees')}
                    className="p-3 rounded-2xl glass-card hover:border-indigo-500/40 transition-all cursor-pointer flex items-center justify-between gap-3 group"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-slate-900 dark:text-white group-hover:text-indigo-500 transition-colors">
                          {c.name}
                        </span>
                        {c.borrowerName && (
                          <span className="px-2 py-0.5 rounded-md bg-indigo-500/10 text-indigo-700 dark:text-indigo-300 text-[10px] font-semibold">
                            {c.borrowerName}
                          </span>
                        )}
                      </div>
                      <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">
                        {formatCurrency(c.monthlyPayout)}/mo • {c.totalMonths} months
                      </p>
                    </div>
                    <span className="text-xs font-black text-indigo-600 dark:text-indigo-400 shrink-0">
                      {formatCurrency(c.monthlyPayout * c.totalMonths)}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
