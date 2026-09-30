import React, { useEffect, useState } from 'react';
import { Calculator, Calendar, Plus, Save, X } from 'lucide-react';
import { CATEGORY_OPTIONS, PLATFORM_OPTIONS } from '../data/initialData';
import { Holding } from '../types';
import { formatDate, parseBreakdownString, sumBreakdown, toPickerDate, toStandardDate } from '../utils/calculations';

interface HoldingModalProps {
  isOpen: boolean;
  mode: 'add' | 'edit';
  holding: Holding | null;
  onClose: () => void;
  onSave: (holding: Holding) => void;
  isSaving?: boolean;
}

export const HoldingModal: React.FC<HoldingModalProps> = ({
  isOpen,
  mode,
  holding,
  onClose,
  onSave,
  isSaving = false,
}) => {
  const [platform, setPlatform] = useState('');
  const [category, setCategory] = useState('');
  const [breakdownInput, setBreakdownInput] = useState('');
  const [total, setTotal] = useState<number | ''>('');
  const [isManualTotal, setIsManualTotal] = useState(false);
  const [tillDate, setTillDate] = useState('');
  const [remarks, setRemarks] = useState('');

  useEffect(() => {
    if (isOpen) {
      if (mode === 'edit' && holding) {
        setPlatform(holding.platform || '');
        setCategory(holding.category || '');
        const bdRaw = holding.breakdownRaw || (holding.breakdown ? holding.breakdown.join(', ') : '');
        setBreakdownInput(bdRaw);
        setTotal(holding.total ?? '');
        setIsManualTotal(false);
        setTillDate(toPickerDate(holding.tillDate || formatDate(new Date())));
        setRemarks(holding.remarks || '');
      } else {
        setPlatform('Zerodha');
        setCategory('Stocks');
        setBreakdownInput('');
        setTotal('');
        setIsManualTotal(false);
        setTillDate(toPickerDate(formatDate(new Date())));
        setRemarks('');
      }
    }
  }, [isOpen, mode, holding]);

  // Recalculate total if breakdown input changes and total hasn't been manually overridden
  const handleBreakdownChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const val = e.target.value;
    setBreakdownInput(val);
    const parsed = parseBreakdownString(val);
    if (parsed.length > 0 && !isManualTotal) {
      setTotal(sumBreakdown(parsed));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const parsedBreakdown = parseBreakdownString(breakdownInput);
    const finalTotal = typeof total === 'number' ? total : sumBreakdown(parsedBreakdown);

    const updated: Holding = {
      id: holding?.id || `h-${Date.now()}`,
      platform: platform.trim() || 'Other',
      category: category.trim() || 'General',
      breakdown: parsedBreakdown,
      breakdownRaw: breakdownInput,
      total: Number(finalTotal) || 0,
      tillDate: toStandardDate(tillDate),
      remarks: remarks.trim(),
      updatedAt: new Date().toISOString(),
    };

    onSave(updated);
  };

  if (!isOpen) return null;

  const currentBreakdownSum = sumBreakdown(parseBreakdownString(breakdownInput));

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-md animate-in fade-in duration-200">
      <div 
        className="relative w-full max-w-lg glass-modal p-6 max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-white/20 dark:border-white/10 mb-5">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            {mode === 'add' ? <Plus className="w-5 h-5 text-emerald-500" /> : <Save className="w-5 h-5 text-emerald-500" />}
            {mode === 'add' ? 'Add Investment Entry' : 'Edit Investment Entry'}
          </h2>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 rounded-xl hover:bg-white/20 dark:hover:bg-white/10 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Platform Field */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1.5">
              Platform / Broker
            </label>
            <input
              type="text"
              required
              value={platform}
              onChange={(e) => setPlatform(e.target.value)}
              placeholder="e.g. Zerodha, IndMoney, Groww"
              className="w-full px-3.5 py-2.5 rounded-xl glass-input text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            />
            <div className="flex flex-wrap gap-1.5 mt-2">
              {PLATFORM_OPTIONS.slice(0, 7).map((p) => (
                <button
                  type="button"
                  key={p}
                  onClick={() => setPlatform(p)}
                  className={`text-xs px-2.5 py-1 rounded-lg transition-all cursor-pointer ${
                    platform === p
                      ? 'bg-emerald-500 text-white font-semibold shadow-sm'
                      : 'glass-button text-slate-700 dark:text-slate-300'
                  }`}
                >
                  {p}
                </button>
              ))}
            </div>
          </div>

          {/* Category Field */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1.5">
              Investment Category
            </label>
            <input
              type="text"
              required
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              placeholder="e.g. Stocks, IPOs, Gold, ETFs, Mutual Fund"
              className="w-full px-3.5 py-2.5 rounded-xl glass-input text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            />
            <div className="flex flex-wrap gap-1.5 mt-2">
              {CATEGORY_OPTIONS.slice(0, 9).map((c) => (
                <button
                  type="button"
                  key={c}
                  onClick={() => setCategory(c)}
                  className={`text-xs px-2.5 py-1 rounded-lg transition-all cursor-pointer ${
                    category === c
                      ? 'bg-emerald-500 text-white font-semibold shadow-sm'
                      : 'glass-button text-slate-700 dark:text-slate-300'
                  }`}
                >
                  {c}
                </button>
              ))}
            </div>
          </div>

          {/* Breakdown Items */}
          <div>
            <div className="flex justify-between items-center mb-1.5">
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400">
                Breakdown Amounts (Comma Separated)
              </label>
              {currentBreakdownSum > 0 && (
                <span className="text-xs text-emerald-600 dark:text-emerald-400 font-medium flex items-center gap-1">
                  <Calculator className="w-3.5 h-3.5" /> Sum: ₹{currentBreakdownSum.toLocaleString('en-IN')}
                </span>
              )}
            </div>
            <input
              type="text"
              value={breakdownInput}
              onChange={handleBreakdownChange}
              placeholder="e.g. 17800, 6300, 9000, 9500, -2000, 15000"
              className="w-full px-3.5 py-2.5 rounded-xl glass-input text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            />
            <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-1">
              Enter individual transaction/stock amounts separated by commas or new lines.
            </p>
          </div>

          {/* Total Amount */}
          <div>
            <div className="flex justify-between items-center mb-1.5">
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400">
                Total Amount (₹)
              </label>
              {currentBreakdownSum > 0 && total !== currentBreakdownSum && (
                <button
                  type="button"
                  onClick={() => {
                    setTotal(currentBreakdownSum);
                    setIsManualTotal(false);
                  }}
                  className="text-xs text-emerald-600 dark:text-emerald-400 hover:underline"
                >
                  Use Breakdown Sum (₹{currentBreakdownSum.toLocaleString('en-IN')})
                </button>
              )}
            </div>
            <input
              type="number"
              required
              value={total}
              onChange={(e) => {
                setTotal(e.target.value === '' ? '' : Number(e.target.value));
                setIsManualTotal(true);
              }}
              placeholder="e.g. 67000"
              className="w-full px-3.5 py-2.5 rounded-xl glass-input text-slate-900 dark:text-white font-semibold text-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            />
          </div>

          {/* Till Date */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1.5 flex items-center justify-between">
              <span>Till Date</span>
              <span className="text-emerald-600 dark:text-emerald-400 font-mono text-xs font-normal">
                Format: DD-MMM-YYYY
              </span>
            </label>
            <div className="relative">
              <input
                type="date"
                required
                value={tillDate}
                onChange={(e) => setTillDate(e.target.value)}
                className="w-full pl-10 pr-3.5 py-2.5 rounded-xl glass-input text-slate-900 dark:text-white font-medium focus:outline-none focus:ring-2 focus:ring-emerald-500/50 cursor-pointer"
              />
              <Calendar className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
            </div>
          </div>

          {/* Remarks */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1.5">
              Remarks / Notes
            </label>
            <textarea
              rows={2}
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              placeholder="e.g. 61600 invested, 5700 held in Demat"
              className="w-full px-3.5 py-2.5 rounded-xl glass-input text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            />
          </div>

          {/* Buttons */}
          <div className="flex items-center gap-3 pt-3 border-t border-white/20 dark:border-white/10">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-3 px-4 rounded-xl glass-button text-slate-700 dark:text-slate-300 font-medium cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSaving}
              className="flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-emerald-600/90 hover:bg-emerald-600 text-white font-medium shadow-md shadow-emerald-500/20 backdrop-blur-md border border-emerald-400/30 transition-all cursor-pointer disabled:opacity-50"
            >
              <Save className="w-4 h-4" />
              {isSaving ? 'Saving...' : 'Save Entry'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
