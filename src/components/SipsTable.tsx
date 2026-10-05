import React from 'react';
import { Calendar, Pause, Pencil, Play, Trash2, TrendingUp } from 'lucide-react';
import { SIP } from '../types';
import { calculateSipInvested, formatCurrency, formatDate, toStandardDate } from '../utils/calculations';

interface SipsTableProps {
  sips: SIP[];
  onAdd: () => void;
  onEdit: (sip: SIP) => void;
  onDeleteRequest: (sip: SIP) => void;
  onTogglePause?: (sip: SIP) => void;
}

export const SipsTable: React.FC<SipsTableProps> = ({
  sips,
  onAdd,
  onEdit,
  onDeleteRequest,
  onTogglePause,
}) => {
  const todayFormatted = formatDate(new Date(), 'DD-MM-YYYY');

  const filtered = sips;

  const activeCount = sips.filter((s) => !s.isPaused).length;
  const pausedCount = sips.filter((s) => s.isPaused).length;

  const totalCalculatedAccumulation = filtered.reduce(
    (sum, s) => sum + calculateSipInvested(s),
    0
  );

  const totalMonthlySipCommitment = filtered
    .filter((s) => !s.isPaused)
    .reduce((sum, s) => sum + (s.amount || 0), 0);

  // Group filtered SIPs by Platform
  const platformGroupMap = new Map<string, SIP[]>();
  filtered.forEach((item) => {
    const p = item.platform || 'Other';
    if (!platformGroupMap.has(p)) {
      platformGroupMap.set(p, []);
    }
    platformGroupMap.get(p)!.push(item);
  });

  const groupedPlatforms: { platform: string; items: SIP[]; totalInvested: number }[] = [];
  platformGroupMap.forEach((items, platform) => {
    const totalInvested = items.reduce((sum, s) => sum + calculateSipInvested(s), 0);
    groupedPlatforms.push({ platform, items, totalInvested });
  });

  // Sort groups by totalInvested descending
  groupedPlatforms.sort((a, b) => b.totalInvested - a.totalInvested);

  return (
    <div className="space-y-3">
      {/* Consolidated SIP Header Card - Reformed Design */}
      <div className="p-4 sm:p-5 rounded-2xl glass-panel space-y-3">
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-gradient-to-br from-emerald-400/20 to-teal-500/20 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30 backdrop-blur-md shrink-0">
              <TrendingUp className="w-5 h-5" />
            </div>
            <div className="flex items-center gap-2 flex-wrap">
              <h3 className="text-sm sm:text-base font-bold text-slate-900 dark:text-white">
                Active SIPs ({activeCount})
              </h3>
              {pausedCount > 0 && (
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-500/15 text-amber-700 dark:text-amber-400 border border-amber-500/30">
                  {pausedCount} paused
                </span>
              )}
            </div>
          </div>
          <span className="text-xs font-semibold px-2.5 py-1 rounded-lg bg-slate-100 dark:bg-slate-800/80 text-slate-600 dark:text-slate-400 border border-slate-200 dark:border-slate-700/60 flex items-center gap-1.5 shrink-0 font-mono">
            <Calendar className="w-3.5 h-3.5 text-slate-500" />
            <span>{todayFormatted}</span>
          </span>
        </div>

        <div className="border-t border-slate-200/60 dark:border-slate-700/60 pt-3 flex items-end justify-between gap-4">
          <div>
            <span className="text-[10.5px] uppercase font-bold tracking-wider text-slate-500 dark:text-slate-400 block">
              Total Invested Till Date
            </span>
            <span className="text-xl sm:text-2xl font-black text-slate-900 dark:text-white">
              {formatCurrency(totalCalculatedAccumulation)}
            </span>
          </div>
          <div className="text-right">
            <span className="text-[10px] uppercase font-bold tracking-wider text-slate-400 block">
              Monthly Commitment
            </span>
            <span className="text-xs sm:text-sm font-bold text-emerald-600 dark:text-emerald-400">
              {formatCurrency(totalMonthlySipCommitment)}/mo
            </span>
          </div>
        </div>
      </div>

      {/* Grouped Platform Boxes for SIPs */}
      {groupedPlatforms.length === 0 ? (
        <div className="p-8 text-center glass-panel rounded-2xl text-slate-500">
          No SIP entries found matching search.
        </div>
      ) : (
        groupedPlatforms.map((group) => (
          <div
            key={group.platform}
            className="glass-panel rounded-2xl overflow-hidden"
          >
            {/* Single Platform Header */}
            <div className="px-4 sm:px-5 py-3.5 bg-white/40 dark:bg-slate-800/40 backdrop-blur-md border-b border-white/40 dark:border-white/10 flex items-center justify-between">
              <h3 className="text-lg font-bold text-amber-600 dark:text-amber-400 tracking-tight">
                {group.platform}
              </h3>
              <span className="text-xs font-mono text-slate-600 dark:text-slate-300 font-medium">
                {group.items.length} {group.items.length === 1 ? 'entry' : 'entries'} &bull; Invested: <strong className="text-slate-900 dark:text-white font-extrabold text-sm">{formatCurrency(group.totalInvested)}</strong>
              </span>
            </div>

            {/* Desktop Table View */}
            <div className="hidden md:block overflow-x-auto">
              <table className="w-full text-left border-collapse table-fixed">
                <thead>
                  <tr className="border-b border-white/30 dark:border-white/10 text-[11px] font-semibold uppercase text-slate-500 dark:text-slate-400 tracking-wider bg-white/30 dark:bg-slate-800/30 backdrop-blur-md">
                    <th className="py-2.5 px-4 w-[18%]">Instrument</th>
                    <th className="py-2.5 px-4 text-right w-[15%]">Recurring Amount</th>
                    <th className="py-2.5 px-4 w-[12%]">Frequency</th>
                    <th className="py-2.5 px-4 w-[15%]">Since</th>
                    <th className="py-2.5 px-4 text-right w-[18%]">Invested Till Date</th>
                    <th className="py-2.5 px-4 w-[14%]">Remarks</th>
                    <th className="py-2.5 px-4 text-center w-[8%]">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/20 dark:divide-white/5 text-sm">
                  {group.items.map((item) => {
                    const invested = calculateSipInvested(item);
                    return (
                      <tr key={item.id} className="hover:bg-white/40 dark:hover:bg-slate-800/40 transition-colors">
                        <td className="py-3.5 px-4 font-semibold text-slate-800 dark:text-slate-100 text-sm">
                          <div className="flex items-center gap-2">
                            <span>{item.instrument}</span>
                            {item.isPaused ? (
                              <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded text-[10px] font-bold bg-amber-500/15 text-amber-700 dark:text-amber-400 border border-amber-500/30">
                                <Pause className="w-2.5 h-2.5" /> PAUSED
                              </span>
                            ) : (
                              <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded text-[10px] font-bold bg-emerald-500/15 text-emerald-700 dark:text-emerald-400 border border-emerald-500/30">
                                <Play className="w-2.5 h-2.5" /> ACTIVE
                              </span>
                            )}
                          </div>
                        </td>
                        <td className="py-3.5 px-4 text-right font-bold text-slate-800 dark:text-slate-200">
                          ₹{item.amount.toLocaleString('en-IN')}
                        </td>
                        <td className="py-3.5 px-4">
                          <span className="inline-block px-2.5 py-0.5 text-xs font-semibold rounded-lg bg-emerald-500/15 text-emerald-800 dark:text-emerald-300 border border-emerald-500/30 backdrop-blur-md">
                            {item.frequency}
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-xs font-mono text-slate-600 dark:text-slate-400">
                          {item.since ? toStandardDate(item.since) : '—'}
                        </td>
                        <td className="py-3.5 px-4 text-right font-extrabold text-emerald-600 dark:text-emerald-400 text-base font-mono">
                          {formatCurrency(invested)}
                        </td>
                        <td className="py-3.5 px-4 text-xs text-slate-500 dark:text-slate-400 max-w-xs truncate">
                          {item.remarks || '—'}
                        </td>
                        <td className="py-3.5 px-4 text-center whitespace-nowrap">
                          <div className="flex items-center justify-center gap-1.5">
                            <button
                              onClick={() => onEdit(item)}
                              title="Edit"
                              className="p-1.5 text-blue-600 dark:text-blue-400 hover:bg-blue-500/20 rounded-xl transition-colors cursor-pointer"
                            >
                              <Pencil className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => onDeleteRequest(item)}
                              title="Delete"
                              className="p-1.5 text-red-600 dark:text-red-400 hover:bg-red-500/20 rounded-xl transition-colors cursor-pointer"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            {/* Mobile Stack View */}
            <div className="md:hidden divide-y divide-slate-100 dark:divide-slate-800/60 p-3 space-y-3">
              {group.items.map((item) => {
                const invested = calculateSipInvested(item);
                return (
                  <div key={item.id} className="pt-3 first:pt-0 space-y-2">
                    <div className="flex items-start justify-between">
                      <div>
                        <h4 className="font-bold text-slate-900 dark:text-white text-sm">
                          {item.instrument}
                        </h4>
                        <span className="text-xs text-slate-500">
                          ₹{item.amount.toLocaleString('en-IN')} /{item.frequency}
                        </span>
                      </div>
                      <div className="text-right">
                        <span className="text-xs text-slate-400 uppercase block text-[10px]">Invested</span>
                        <span className="text-base font-bold text-emerald-600 dark:text-emerald-400">
                          {formatCurrency(invested)}
                        </span>
                      </div>
                    </div>

                    <p className="text-xs font-mono text-slate-500 dark:text-slate-400">
                      Since: {item.since ? toStandardDate(item.since) : '—'}
                    </p>

                    {item.remarks && (
                      <p className="text-xs text-slate-500 dark:text-slate-400 italic">
                        "{item.remarks}"
                      </p>
                    )}

                    <div className="flex items-center justify-between gap-2 pt-1">
                      <span className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md text-[11px] font-semibold ${
                        item.isPaused
                          ? 'bg-amber-500/15 text-amber-700 dark:text-amber-400 border border-amber-500/30'
                          : 'bg-emerald-500/15 text-emerald-700 dark:text-emerald-400 border border-emerald-500/30'
                      }`}>
                        <span className={`w-1.5 h-1.5 rounded-full ${item.isPaused ? 'bg-amber-500' : 'bg-emerald-500'}`} />
                        {item.isPaused ? 'Paused' : 'Active'}
                      </span>
                      <div className="flex items-center gap-1.5">
                        <button
                          onClick={() => onEdit(item)}
                          className="px-2.5 py-1 rounded-lg border border-slate-200 dark:border-slate-700 text-blue-600 dark:text-blue-400 text-xs font-semibold flex items-center gap-1 cursor-pointer hover:bg-blue-500/10 transition-colors"
                        >
                          <Pencil className="w-3.5 h-3.5" /> Edit
                        </button>
                        <button
                          onClick={() => onDeleteRequest(item)}
                          className="px-2.5 py-1 rounded-lg border border-red-200 dark:border-red-900/40 text-red-600 dark:text-red-400 text-xs font-semibold flex items-center gap-1 cursor-pointer hover:bg-red-500/10 transition-colors"
                        >
                          <Trash2 className="w-3.5 h-3.5" /> Del
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ))
      )}
    </div>
  );
};
