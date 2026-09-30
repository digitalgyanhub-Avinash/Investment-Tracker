import React from 'react';
import { Pencil, Trash2 } from 'lucide-react';
import { Holding } from '../types';
import { formatCurrency, parseBreakdownString, toStandardDate } from '../utils/calculations';

interface HoldingsTableProps {
  holdings: Holding[];
  onAdd: () => void;
  onEdit: (holding: Holding) => void;
  onDeleteRequest: (holding: Holding) => void;
}

export const HoldingsTable: React.FC<HoldingsTableProps> = ({
  holdings,
  onAdd,
  onEdit,
  onDeleteRequest,
}) => {
  const totalFilteredSum = holdings.reduce((sum, h) => sum + (h.total || 0), 0);

  // Group holdings by Platform
  const platformGroupMap = new Map<string, Holding[]>();
  holdings.forEach((item) => {
    const p = item.platform || 'Other';
    if (!platformGroupMap.has(p)) {
      platformGroupMap.set(p, []);
    }
    platformGroupMap.get(p)!.push(item);
  });

  const groupedPlatforms: { platform: string; items: Holding[]; total: number }[] = [];
  platformGroupMap.forEach((items, platform) => {
    const total = items.reduce((sum, h) => sum + (h.total || 0), 0);
    groupedPlatforms.push({ platform, items, total });
  });

  // Sort groups by total descending
  groupedPlatforms.sort((a, b) => b.total - a.total);

  return (
    <div className="space-y-3">
      {/* Subtotal Bar */}
      <div className="flex items-center justify-between px-2 text-xs text-slate-600 dark:text-slate-400">
        <span>Showing {holdings.length} entries across {groupedPlatforms.length} platforms</span>
        <span>Total Amount: <strong className="text-emerald-600 dark:text-emerald-400 font-bold text-sm">{formatCurrency(totalFilteredSum)}</strong></span>
      </div>

      {/* Grouped Platform Cards Container */}
      {groupedPlatforms.length === 0 ? (
        <div className="p-8 text-center glass-panel rounded-2xl text-slate-500">
          No holdings found matching filters.
        </div>
      ) : (
        groupedPlatforms.map((group) => (
          <div
            key={group.platform}
            className="glass-panel rounded-2xl overflow-hidden"
          >
            {/* Single Platform Header Bar */}
            <div className="px-4 sm:px-5 py-3.5 bg-white/40 dark:bg-slate-800/40 backdrop-blur-md border-b border-white/40 dark:border-white/10 flex items-center justify-between">
              <h3 className="text-lg font-bold text-amber-600 dark:text-amber-400 tracking-tight">
                {group.platform}
              </h3>
              <span className="text-xs font-mono text-slate-600 dark:text-slate-300 font-medium">
                {group.items.length} {group.items.length === 1 ? 'entry' : 'entries'} &bull; <strong className="text-slate-900 dark:text-white font-extrabold text-sm">{formatCurrency(group.total)}</strong>
              </span>
            </div>

            {/* Desktop Table View */}
            <div className="hidden md:block overflow-x-auto">
              <table className="w-full text-left border-collapse table-fixed">
                <thead>
                  <tr className="border-b border-white/30 dark:border-white/10 text-[11px] font-semibold uppercase text-slate-500 dark:text-slate-400 tracking-wider bg-white/30 dark:bg-slate-800/30 backdrop-blur-md">
                    <th className="py-2.5 px-4 w-[18%]">Category</th>
                    <th className="py-2.5 px-4 w-[34%]">Breakdown</th>
                    <th className="py-2.5 px-4 text-right w-[16%]">Total</th>
                    <th className="py-2.5 px-4 w-[14%]">Till Date</th>
                    <th className="py-2.5 px-4 w-[10%]">Remarks</th>
                    <th className="py-2.5 px-4 text-center w-[8%]">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/20 dark:divide-white/5 text-sm">
                  {group.items.map((item) => (
                    <tr key={item.id} className="hover:bg-white/40 dark:hover:bg-slate-800/40 transition-colors">
                      <td className="py-3.5 px-4 font-semibold text-slate-800 dark:text-slate-100 text-sm">
                        {item.category}
                      </td>
                      <td className="py-3.5 px-4 max-w-sm">
                        {(() => {
                          const bdVals =
                            item.breakdown && item.breakdown.length > 0
                              ? item.breakdown
                              : parseBreakdownString(item.breakdownRaw || '');
                          if (bdVals.length === 0) {
                            return <span className="text-slate-400 text-xs">—</span>;
                          }
                          return (
                            <div className="flex flex-wrap gap-1.5 max-w-sm">
                              {bdVals.map((val, idx) => (
                                <span
                                  key={idx}
                                  className={`text-xs px-2 py-0.5 rounded-lg font-mono font-medium backdrop-blur-md border ${
                                    val < 0
                                      ? 'bg-red-500/15 text-red-600 dark:text-red-300 border-red-500/30'
                                      : 'glass-card text-slate-800 dark:text-slate-200'
                                  }`}
                                >
                                  ₹{val.toLocaleString('en-IN')}
                                </span>
                              ))}
                            </div>
                          );
                        })()}
                      </td>
                      <td className="py-3.5 px-4 text-right font-extrabold text-emerald-600 dark:text-emerald-400 text-base font-mono">
                        {formatCurrency(item.total)}
                      </td>
                      <td className="py-3.5 px-4 text-slate-600 dark:text-slate-400 whitespace-nowrap text-xs font-mono">
                        {item.tillDate ? toStandardDate(item.tillDate) : '—'}
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
                  ))}
                </tbody>
              </table>
            </div>

            {/* Mobile Stack View */}
            <div className="md:hidden divide-y divide-slate-100 dark:divide-slate-800/60 p-3 space-y-3">
              {group.items.map((item) => (
                <div key={item.id} className="pt-3 first:pt-0 space-y-2">
                  <div className="flex items-start justify-between">
                    <div>
                      <h4 className="font-bold text-slate-900 dark:text-white text-sm">
                        {item.category}
                      </h4>
                      <p className="text-[11px] text-slate-400 font-mono mt-0.5">
                        Till: {item.tillDate ? toStandardDate(item.tillDate) : 'N/A'}
                      </p>
                    </div>
                    <span className="text-base font-bold text-emerald-600 dark:text-emerald-400">
                      {formatCurrency(item.total)}
                    </span>
                  </div>

                  {item.breakdownRaw ? (
                    <p className="text-xs font-mono text-slate-600 dark:text-slate-400">
                      Breakdown: {item.breakdownRaw}
                    </p>
                  ) : null}

                  {item.remarks && (
                    <p className="text-xs text-slate-500 dark:text-slate-400 italic">
                      "{item.remarks}"
                    </p>
                  )}

                  <div className="flex items-center justify-end gap-2 pt-1">
                    <button
                      onClick={() => onEdit(item)}
                      className="px-2.5 py-1 rounded-lg border border-slate-200 dark:border-slate-700 text-blue-600 dark:text-blue-400 text-xs font-semibold flex items-center gap-1 cursor-pointer"
                    >
                      <Pencil className="w-3.5 h-3.5" /> Edit
                    </button>
                    <button
                      onClick={() => onDeleteRequest(item)}
                      className="px-2.5 py-1 rounded-lg border border-red-200 dark:border-red-900/40 text-red-600 dark:text-red-400 text-xs font-semibold flex items-center gap-1 cursor-pointer"
                    >
                      <Trash2 className="w-3.5 h-3.5" /> Del
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))
      )}
    </div>
  );
};
