import React, { useState, useEffect } from 'react';
import { X, Users, Calculator, Sparkles, AlertCircle, Calendar } from 'lucide-react';
import { Committee } from '../types';
import { formatCurrency, calculateCommitteeBidDetails, toStandardDate, toPickerDate } from '../utils/calculations';

interface CommitteeModalProps {
  isOpen: boolean;
  mode: 'add' | 'edit';
  item?: Committee | null;
  committee?: Committee | null;
  onClose: () => void;
  onSave: (committee: Partial<Committee>) => void;
}

export const CommitteeModal: React.FC<CommitteeModalProps> = ({
  isOpen,
  mode,
  item,
  committee,
  onClose,
  onSave,
}) => {
  const activeCommittee = committee || item || null;

  const [name, setName] = useState('');
  const [totalAmount, setTotalAmount] = useState<number | ''>('');
  const [totalMembers, setTotalMembers] = useState<number | ''>('');
  const [totalMonths, setTotalMonths] = useState<number | ''>('');
  const [startDate, setStartDate] = useState('');
  const [currentMonth, setCurrentMonth] = useState<number>(1);
  const [hasTakenPot, setHasTakenPot] = useState<boolean>(false);
  const [potTakenMonth, setPotTakenMonth] = useState<number | undefined>(undefined);
  const [potTakenAmount, setPotTakenAmount] = useState<number | undefined>(undefined);
  const [remarks, setRemarks] = useState('');

  // Sample live bid preview calculator inside modal
  const [sampleBid, setSampleBid] = useState<number>(10000);

  useEffect(() => {
    if (isOpen) {
      if (mode === 'edit' && activeCommittee) {
        setName(activeCommittee.name || '');
        setTotalAmount(activeCommittee.totalAmount ?? '');
        setTotalMembers(activeCommittee.totalMembers ?? '');
        setTotalMonths(activeCommittee.totalMonths ?? '');
        setStartDate(activeCommittee.startDate || new Date().toISOString().substring(0, 10));
        setCurrentMonth(activeCommittee.currentMonth || 1);
        setHasTakenPot(activeCommittee.hasTakenPot || false);
        setPotTakenMonth(activeCommittee.potTakenMonth);
        setPotTakenAmount(activeCommittee.potTakenAmount);
        setRemarks(activeCommittee.remarks || '');
      } else {
        // Clean defaults for new committee
        setName('');
        setTotalAmount('');
        setTotalMembers('');
        setTotalMonths('');
        setStartDate(new Date().toISOString().substring(0, 10));
        setCurrentMonth(1);
        setHasTakenPot(false);
        setPotTakenMonth(undefined);
        setPotTakenAmount(undefined);
        setRemarks('');
      }
    }
  }, [isOpen, mode, activeCommittee]);

  if (!isOpen) return null;

  const numAmount = Number(totalAmount) || 0;
  const numMembers = Number(totalMembers) || 0;
  const baseInstallment = numMembers > 0 ? Math.round(numAmount / numMembers) : 0;
  const sampleCalculated = calculateCommitteeBidDetails(numAmount, numMembers, sampleBid);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    onSave({
      id: activeCommittee?.id,
      name: name.trim(),
      totalAmount: Number(totalAmount) || 0,
      totalMembers: Number(totalMembers) || 1,
      totalMonths: Number(totalMonths) || 1,
      baseMonthlyInstallment: baseInstallment,
      startDate: startDate || new Date().toISOString().substring(0, 10),
      currentMonth: Number(currentMonth) || 1,
      hasTakenPot,
      potTakenMonth: hasTakenPot ? Number(potTakenMonth) : undefined,
      potTakenAmount: hasTakenPot ? Number(potTakenAmount) : undefined,
      remarks: remarks.trim(),
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-md animate-fade-in overflow-y-auto">
      <div className="glass-modal w-full max-w-lg my-8 overflow-hidden">
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/20 dark:border-white/10 glass-header">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-indigo-500/20 text-indigo-600 dark:text-indigo-300 backdrop-blur-md">
              <Users className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900 dark:text-white">
                {mode === 'add' ? 'Add New Committee' : 'Edit Committee'}
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Chit Fund / Committee Auction Setup
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-white/20 dark:hover:bg-white/10 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Form */}
        <form onSubmit={handleSubmit} className="p-4 sm:p-6 space-y-3.5 sm:space-y-4 max-h-[85vh] overflow-y-auto">
          <div>
            <label className="block text-[10px] sm:text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1">
              Committee Name
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Committee 1 (₹90,000 / 12 Months)"
              className="w-full h-10 sm:h-11 px-3 sm:px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white text-xs sm:text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none"
            />
          </div>

          <div className="grid grid-cols-2 gap-2.5 sm:gap-3.5 items-end">
            <div className="flex flex-col justify-end">
              <label className="min-h-[30px] sm:min-h-[22px] flex items-end text-[10px] sm:text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1 leading-tight">
                Total Pot Amount (₹)
              </label>
              <input
                type="number"
                required
                min={1000}
                value={totalAmount}
                onChange={(e) => setTotalAmount(e.target.value === '' ? '' : Number(e.target.value))}
                placeholder="e.g. 60000"
                className="w-full h-10 sm:h-11 px-3 sm:px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white text-xs sm:text-sm font-semibold focus:ring-2 focus:ring-indigo-500 focus:outline-none"
              />
            </div>

            <div className="flex flex-col justify-end">
              <label className="min-h-[30px] sm:min-h-[22px] flex items-end text-[10px] sm:text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1 leading-tight">
                Total Members
              </label>
              <input
                type="number"
                required
                min={2}
                max={100}
                value={totalMembers}
                onChange={(e) => setTotalMembers(e.target.value === '' ? '' : Number(e.target.value))}
                placeholder="e.g. 12"
                className="w-full h-10 sm:h-11 px-3 sm:px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white text-xs sm:text-sm font-semibold focus:ring-2 focus:ring-indigo-500 focus:outline-none"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-2.5 sm:gap-3.5 items-end">
            <div className="flex flex-col justify-end">
              <label className="min-h-[30px] sm:min-h-[22px] flex items-end text-[10px] sm:text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1 leading-tight">
                Total Months
              </label>
              <input
                type="number"
                required
                min={1}
                value={totalMonths}
                onChange={(e) => setTotalMonths(e.target.value === '' ? '' : Number(e.target.value))}
                placeholder="e.g. 12"
                className="w-full h-10 sm:h-11 px-3 sm:px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white text-xs sm:text-sm font-semibold focus:ring-2 focus:ring-indigo-500 focus:outline-none"
              />
            </div>

            <div className="flex flex-col justify-end">
              <label className="min-h-[30px] sm:min-h-[22px] flex items-end text-[10px] sm:text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1 leading-tight">
                1st Installment Date
              </label>
              <input
                type="date"
                required
                value={toPickerDate(startDate)}
                onChange={(e) => setStartDate(e.target.value)}
                className="w-full h-10 sm:h-11 px-2.5 sm:px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white text-xs sm:text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none"
              />
            </div>
          </div>

          {startDate && (
            <div className="text-[11px] font-semibold text-indigo-600 dark:text-indigo-400 text-right -mt-2">
              1st Paid: {toStandardDate(startDate)}
            </div>
          )}

          {/* Standard Base Monthly Installment Highlight */}
          <div className="p-3 sm:p-3.5 rounded-xl sm:rounded-2xl bg-indigo-50/70 dark:bg-indigo-950/40 border border-indigo-100 dark:border-indigo-900/50 flex items-center justify-between gap-2">
            <div>
              <span className="text-[11px] sm:text-xs text-indigo-700 dark:text-indigo-300 font-semibold block">
                Standard Base Monthly Installment
              </span>
              <span className="text-[10px] sm:text-[11px] text-slate-500 dark:text-slate-400">
                (₹{numAmount.toLocaleString()} ÷ {numMembers || 0} members)
              </span>
            </div>
            <span className="text-sm sm:text-base md:text-lg font-extrabold text-indigo-600 dark:text-indigo-400 shrink-0">
              {formatCurrency(baseInstallment)}/mo
            </span>
          </div>

          {/* Live Bid Calculation Preview Card */}
          <div className="p-3 sm:p-4 rounded-xl sm:rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/60 space-y-2.5">
            <div className="flex items-center justify-between">
              <span className="text-[11px] sm:text-xs font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1.5">
                <Calculator className="w-3.5 h-3.5 text-emerald-500" />
                Live Bidding Formula Tester
              </span>
              <span className="text-[10px] sm:text-[11px] text-slate-400 font-mono">Formula Preview</span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5 items-end">
              <div className="sm:col-span-1">
                <label className="text-[10px] sm:text-xs font-semibold text-slate-600 dark:text-slate-400 block mb-1">
                  Sample Bid / Discount (₹)
                </label>
                <input
                  type="number"
                  step={500}
                  value={sampleBid}
                  onChange={(e) => setSampleBid(Number(e.target.value))}
                  className="w-full h-10 px-3 py-2 text-xs sm:text-sm font-bold rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-2 sm:col-span-2">
                <div className="p-2 sm:p-2.5 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-center flex flex-col justify-center min-h-[48px]">
                  <span className="text-[9px] sm:text-[10px] text-slate-400 font-medium block uppercase tracking-wider truncate">
                    Winner Takes
                  </span>
                  <span className="text-xs sm:text-sm font-extrabold text-slate-900 dark:text-white">
                    {formatCurrency(sampleCalculated.netPotValue)}
                  </span>
                </div>

                <div className="p-2 sm:p-2.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/50 border border-emerald-200 dark:border-emerald-800 text-center flex flex-col justify-center min-h-[48px]">
                  <span className="text-[9px] sm:text-[10px] text-emerald-600 dark:text-emerald-400 font-medium block uppercase tracking-wider truncate">
                    Each Person Pays
                  </span>
                  <span className="text-xs sm:text-sm font-extrabold text-emerald-600 dark:text-emerald-400">
                    {formatCurrency(sampleCalculated.installmentPerPerson)}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div>
            <label className="block text-[10px] sm:text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1">
              Remarks / Organizer Info
            </label>
            <input
              type="text"
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              placeholder="e.g. Bidding on 10th of every month with Gupta ji"
              className="w-full h-10 sm:h-11 px-3 sm:px-3.5 py-2 rounded-xl glass-input text-slate-900 dark:text-white text-xs sm:text-sm focus:ring-2 focus:ring-indigo-500 focus:outline-none"
            />
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-white/20 dark:border-white/10">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 rounded-xl glass-button text-slate-700 dark:text-slate-300 text-xs font-semibold cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2.5 rounded-xl bg-indigo-600/90 hover:bg-indigo-600 text-white text-xs font-bold shadow-md shadow-indigo-500/20 backdrop-blur-md border border-indigo-400/30 transition-all flex items-center gap-1.5 cursor-pointer"
            >
              <Sparkles className="w-4 h-4" />
              {mode === 'add' ? 'Create Committee' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
