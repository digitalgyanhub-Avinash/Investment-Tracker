import React, { useState } from 'react';
import {
  Users,
  Plus,
  Edit2,
  Trash2,
  CheckCircle2,
  Clock,
  Calculator,
  ChevronDown,
  ChevronUp,
  Award,
  Sparkles,
  TrendingUp,
  Info,
  DollarSign,
  Calendar,
} from 'lucide-react';
import { Committee, CommitteeMonth } from '../types';
import {
  formatCurrency,
  calculateCommitteeInvestedTillDate,
  calculateCommitteeCurrentOutflow,
  calculateCommitteeTotalSaved,
  calculateCommitteesTotal,
  calculateCommitteesMonthlyOutflow,
  calculateCommitteeBidDetails,
  getCommitteeLatestMonthLabel,
  isCommitteeCompleted,
  calculateCommitteeProfitLoss,
  toStandardDate,
  parseDateToMs,
} from '../utils/calculations';

interface CommitteesTableProps {
  committees: Committee[];
  onAddCommittee: () => void;
  onEditCommittee: (committee: Committee) => void;
  onDeleteCommittee: (committee: Committee) => void;
  onUpdateCommitteeMonths: (committeeId: string, updatedMonths: CommitteeMonth[]) => void;
}

export const CommitteesTable: React.FC<CommitteesTableProps> = ({
  committees,
  onAddCommittee,
  onEditCommittee,
  onDeleteCommittee,
  onUpdateCommitteeMonths,
}) => {
  // Expanded committee card IDs - default collapsed
  const [expandedIds, setExpandedIds] = useState<Record<string, boolean>>({});

  // Standalone Calculator state
  const [activeCalcCommitteeId, setActiveCalcCommitteeId] = useState<string | null>(null);
  const [calcAmount, setCalcAmount] = useState<number>(90000);
  const [calcMembers, setCalcMembers] = useState<number>(12);
  const [calcBid, setCalcBid] = useState<number>(10000);

  // New month bid inline state for a specific committee
  const [addingMonthForId, setAddingMonthForId] = useState<string | null>(null);
  const [newMonthBid, setNewMonthBid] = useState<string>('10000');
  const [newMonthWinner, setNewMonthWinner] = useState<string>('');
  const [newMonthIsPaid, setNewMonthIsPaid] = useState<boolean>(true);
  const [newMonthLabel, setNewMonthLabel] = useState<string>('');

  // Delete confirmation modal state for a specific month bidding row
  const [deleteMonthModal, setDeleteMonthModal] = useState<{
    isOpen: boolean;
    committee: Committee | null;
    monthNumber: number | null;
    monthLabel: string;
  }>({
    isOpen: false,
    committee: null,
    monthNumber: null,
    monthLabel: '',
  });

  // Editing state for an existing month bid row
  const [editingMonth, setEditingMonth] = useState<{
    committeeId: string;
    monthNumber: number;
    winningBid: string;
    winnerName: string;
    isPaid: boolean;
    monthLabel: string;
  } | null>(null);

  const toggleExpand = (id: string) => {
    setExpandedIds((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const totalInvested = calculateCommitteesTotal(committees);
  const totalMonthlyOutflow = calculateCommitteesMonthlyOutflow(committees);
  const totalSaved = committees.reduce((sum, c) => sum + calculateCommitteeTotalSaved(c), 0);

  const activeCommittees = committees.filter((c) => !isCommitteeCompleted(c));
  const latestMonthLabels = Array.from(
    new Set(activeCommittees.map((c) => getCommitteeLatestMonthLabel(c)).filter(Boolean))
  );
  const overallLatestMonthText = latestMonthLabels.length > 0 ? latestMonthLabels.join(', ') : '';

  const standaloneCalcResult = calculateCommitteeBidDetails(calcAmount, calcMembers, calcBid);

  // Handler to save changes to an edited month bid row
  const handleSaveEditMonthBid = (committee: Committee) => {
    if (!editingMonth) return;
    const bidVal = Number(editingMonth.winningBid) || 0;
    const bidDetails = calculateCommitteeBidDetails(
      committee.totalAmount,
      committee.totalMembers,
      bidVal
    );

    const updatedMonths = (committee.months || []).map((m) => {
      if (m.monthNumber === editingMonth.monthNumber) {
        return {
          ...m,
          monthLabel: editingMonth.monthLabel.trim() || `Month ${m.monthNumber}`,
          winningBid: bidVal,
          netPotValue: bidDetails.netPotValue,
          installmentPerPerson: bidDetails.installmentPerPerson,
          isPaid: editingMonth.isPaid,
          winnerName: editingMonth.winnerName.trim() || 'Winner',
        };
      }
      return m;
    });

    onUpdateCommitteeMonths(committee.id, updatedMonths);
    setEditingMonth(null);
  };

  // Handler to add a new month bid record
  const handleAddMonthBid = (committee: Committee) => {
    const nextMonthNum = (committee.months ? committee.months.length : 0) + 1;
    if (nextMonthNum > committee.totalMonths) return;

    const bidVal = Number(newMonthBid) || 0;
    const bidDetails = calculateCommitteeBidDetails(
      committee.totalAmount,
      committee.totalMembers,
      bidVal
    );

    const label = newMonthLabel.trim() || `Month ${nextMonthNum}`;

    const newMonthObj: CommitteeMonth = {
      monthNumber: nextMonthNum,
      monthLabel: label,
      winningBid: bidVal,
      netPotValue: bidDetails.netPotValue,
      installmentPerPerson: bidDetails.installmentPerPerson,
      isPaid: newMonthIsPaid,
      winnerName: newMonthWinner.trim() || 'Winner',
    };

    const updatedMonths = [...(committee.months || []), newMonthObj];
    onUpdateCommitteeMonths(committee.id, updatedMonths);

    // Reset inline inputs
    setAddingMonthForId(null);
    setNewMonthBid('10000');
    setNewMonthWinner('');
    setNewMonthIsPaid(true);
    setNewMonthLabel('');
  };

  // Handler to delete a month bid log directly without pop-up
  const handleDeleteMonthBid = (committee: Committee, monthNumber: number) => {
    const updatedMonths = (committee.months || []).filter((m) => m.monthNumber !== monthNumber);
    onUpdateCommitteeMonths(committee.id, updatedMonths);
  };

  return (
    <div className="space-y-3">
      {/* Summary Cards */}
      <div className="grid grid-cols-3 gap-2">
        <div className="p-2.5 rounded-xl glass-panel min-w-0">
          <span className="text-[9px] sm:text-[10px] font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider block truncate">
            Committees
          </span>
          <p className="text-xs sm:text-sm font-extrabold text-slate-900 dark:text-white truncate">
            {committees.filter((c) => !isCommitteeCompleted(c)).length} Active
          </p>
          <span className="text-[9px] sm:text-[10px] text-emerald-600 dark:text-emerald-400 font-semibold block truncate">
            {committees.filter((c) => isCommitteeCompleted(c)).length} Done
          </span>
        </div>

        <div className="p-2.5 rounded-xl glass-panel min-w-0">
          <span className="text-[9px] sm:text-[10px] font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider block truncate">
            Total Invested
          </span>
          <p className="text-xs sm:text-sm font-extrabold text-indigo-600 dark:text-indigo-400 truncate mt-0.5">
            {formatCurrency(totalInvested)}
          </p>
        </div>

        <div className="p-2.5 rounded-xl glass-panel min-w-0 flex flex-col justify-between">
          <div>
            <span className="text-[9px] sm:text-[10px] font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-tight block leading-tight">
              Latest Month Paid
            </span>
            <p className="text-xs sm:text-sm font-extrabold text-blue-600 dark:text-blue-400 truncate mt-0.5">
              {formatCurrency(totalMonthlyOutflow)}
            </p>
          </div>
          {overallLatestMonthText && (
            <span className="text-[9px] sm:text-[10px] font-bold text-slate-500 dark:text-slate-400 block truncate mt-0.5">
              {overallLatestMonthText}
            </span>
          )}
        </div>
      </div>

      {/* Committees Cards List Sorted by 1st Paid EMI Date (Latest on Top) */}
      <div className="space-y-2.5">
        {[...committees]
          .sort((a, b) => parseDateToMs(b.startDate) - parseDateToMs(a.startDate))
          .map((committee) => {
          const isExpanded = !!expandedIds[committee.id];
          const isCompleted = isCommitteeCompleted(committee);
          const investedSoFar = calculateCommitteeInvestedTillDate(committee);
          const currentOutflow = calculateCommitteeCurrentOutflow(committee);
          const savedDividend = calculateCommitteeTotalSaved(committee);
          const paidMonthsCount = committee.months ? committee.months.filter((m) => m.isPaid).length : 0;
          const progressPct = committee.totalMonths > 0 ? (paidMonthsCount / committee.totalMonths) * 100 : 0;

          return (
            <div
              key={committee.id}
              className={`p-3.5 sm:p-4 rounded-2xl glass-panel hover:border-indigo-400/50 transition-all duration-200 ${
                isCompleted
                  ? 'border-emerald-300/40 dark:border-emerald-800/40'
                  : ''
              }`}
            >
              {/* Card Header / Summary Bar */}
              <div
                className="flex flex-col md:flex-row md:items-center justify-between gap-3"
              >
                <div className="flex items-start gap-3 min-w-0">
                  <div className={`p-2.5 rounded-xl border shrink-0 ${
                    isCompleted
                      ? 'bg-emerald-50 dark:bg-emerald-950/60 border-emerald-200 dark:border-emerald-800 text-emerald-600 dark:text-emerald-400'
                      : 'bg-indigo-50 dark:bg-indigo-950/60 border-indigo-100 dark:border-indigo-900/50 text-indigo-600 dark:text-indigo-400'
                  }`}>
                    <Users className="w-5 h-5" />
                  </div>

                  <div className="min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <h3 className="text-base font-bold text-slate-900 dark:text-white truncate">
                        {committee.name}
                      </h3>

                      {isCompleted ? (
                        <span className="px-2.5 py-0.5 rounded-full text-xs font-extrabold bg-emerald-600 text-white flex items-center gap-1 shadow-xs">
                          <CheckCircle2 className="w-3.5 h-3.5" />
                          Completed
                        </span>
                      ) : (
                        <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-100 dark:bg-indigo-900/60 text-indigo-700 dark:text-indigo-300">
                          ₹{committee.totalAmount.toLocaleString()} Pot
                        </span>
                      )}

                      {committee.hasTakenPot && (
                        <>
                          <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-100 dark:bg-emerald-900/60 text-emerald-700 dark:text-emerald-300 flex items-center gap-1">
                            <Award className="w-3.5 h-3.5" />
                            Pot Taken ({formatCurrency(committee.potTakenAmount || 0)} / {formatCurrency(committee.totalAmount)})
                          </span>
                          {(() => {
                            const profitLoss = calculateCommitteeProfitLoss(committee);
                            const isProfit = profitLoss >= 0;
                            return (
                              <span
                                className={`px-2.5 py-0.5 rounded-full text-xs font-bold flex items-center gap-1 ${
                                  isProfit
                                    ? 'bg-emerald-100 dark:bg-emerald-900/60 text-emerald-700 dark:text-emerald-300'
                                    : 'bg-red-100 dark:bg-red-900/60 text-red-700 dark:text-red-300'
                                }`}
                              >
                                {isProfit ? `Profit: +${formatCurrency(profitLoss)}` : `Loss: -${formatCurrency(Math.abs(profitLoss))}`}
                              </span>
                            );
                          })()}
                        </>
                      )}
                    </div>

                    <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                      {committee.totalMembers} Members • Base {formatCurrency(committee.baseMonthlyInstallment)}/mo
                    </p>

                    {/* Progress Bar */}
                    <div className="flex items-center gap-2 mt-1.5 max-w-md">
                      <div className="flex-1 h-2 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                        <div
                          className={`h-full rounded-full transition-all duration-500 ${
                            isCompleted ? 'bg-emerald-600' : 'bg-indigo-600'
                          }`}
                          style={{ width: `${Math.min(100, Math.max(5, progressPct))}%` }}
                        />
                      </div>
                      <span className={`text-xs font-bold font-mono ${
                        isCompleted ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-500'
                      }`}>
                        {isCompleted
                          ? `Completed (${paidMonthsCount}/${committee.totalMonths} Mos)`
                          : `${paidMonthsCount}/${committee.totalMonths} Mos (${progressPct.toFixed(0)}%)`}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Right Stat Numbers */}
                <div className="flex items-center justify-between md:justify-end gap-5 pt-2.5 md:pt-0 border-t md:border-t-0 border-slate-100 dark:border-slate-800 shrink-0">
                  <div className="text-left md:text-right">
                    <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block">
                      Invested Till Date
                    </span>
                    <span className="text-base font-extrabold text-indigo-600 dark:text-indigo-400">
                      {formatCurrency(investedSoFar)}
                    </span>
                  </div>

                  <div className="text-left md:text-right">
                    <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block">
                      Latest Month Paid
                    </span>
                    <span className="text-base font-extrabold text-slate-900 dark:text-white block">
                      {formatCurrency(currentOutflow)}
                    </span>
                    {getCommitteeLatestMonthLabel(committee) && (
                      <span className="text-[10px] font-bold text-indigo-600 dark:text-indigo-400 block">
                        {getCommitteeLatestMonthLabel(committee)}
                      </span>
                    )}
                  </div>

                  <div className="flex items-center gap-1.5 shrink-0">
                    <button
                      type="button"
                      onClick={() => onEditCommittee(committee)}
                      className="p-2 rounded-xl text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
                      title="Edit Committee"
                    >
                      <Edit2 className="w-4 h-4" />
                    </button>

                    <button
                      type="button"
                      onClick={() => onDeleteCommittee(committee)}
                      className="p-2 rounded-xl text-slate-400 hover:text-red-600 dark:hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/40 transition-colors cursor-pointer"
                      title="Delete Committee"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>

                    <button
                      type="button"
                      onClick={() => toggleExpand(committee.id)}
                      className="p-2 rounded-xl text-slate-500 hover:text-indigo-600 dark:hover:text-indigo-400 bg-slate-100 hover:bg-indigo-50 dark:bg-slate-800 dark:hover:bg-indigo-950/60 transition-colors flex items-center justify-center shrink-0 cursor-pointer"
                      title={isExpanded ? "Collapse Bidding Details" : "Expand Bidding Details"}
                    >
                      {isExpanded ? <ChevronUp className="w-4 h-4 text-indigo-600 dark:text-indigo-400" /> : <ChevronDown className="w-4 h-4" />}
                    </button>
                  </div>
                </div>
              </div>

              {/* Expanded Month-by-Month Bidding Table & Controls */}
              {isExpanded && (
                <div className="border-t border-slate-100 dark:border-slate-800 bg-slate-50/40 dark:bg-slate-900/50 p-5 space-y-4">
                  <div className="flex items-center justify-between flex-wrap gap-2">
                    <h4 className="text-sm font-bold text-slate-800 dark:text-slate-200 flex items-center gap-1.5">
                      <Calendar className="w-4 h-4 text-indigo-500" />
                      Monthly Bidding Auction Log
                    </h4>

                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        onClick={() => {
                          if (activeCalcCommitteeId === committee.id) {
                            setActiveCalcCommitteeId(null);
                          } else {
                            setActiveCalcCommitteeId(committee.id);
                            setCalcAmount(committee.totalAmount);
                            setCalcMembers(committee.totalMembers);
                          }
                        }}
                        className={`px-2.5 py-1.5 rounded-xl font-semibold text-xs flex items-center gap-1 transition-colors cursor-pointer ${
                          activeCalcCommitteeId === committee.id
                            ? 'bg-indigo-600 text-white shadow-sm'
                            : 'bg-slate-200/70 hover:bg-slate-300 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200'
                        }`}
                        title="Open Bidding Calculator for this committee"
                      >
                        <Calculator className="w-3.5 h-3.5 text-indigo-500" />
                        <span>{activeCalcCommitteeId === committee.id ? 'Hide Calculator' : 'Calculator'}</span>
                      </button>

                      {!isCompleted ? (
                        <button
                          type="button"
                          onClick={() => {
                            if (addingMonthForId === committee.id) {
                              setAddingMonthForId(null);
                            } else {
                              setAddingMonthForId(committee.id);
                              const nextMonthNum = (committee.months?.length || 0) + 1;
                              let autoLabel = `Month ${nextMonthNum}`;
                              if (committee.startDate) {
                                const d = new Date(committee.startDate);
                                if (!isNaN(d.getTime())) {
                                  d.setMonth(d.getMonth() + (nextMonthNum - 1));
                                  autoLabel = d.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
                                }
                              }
                              setNewMonthLabel(autoLabel);
                            }
                          }}
                          className="px-3 py-1.5 rounded-xl bg-indigo-50 dark:bg-indigo-950/60 hover:bg-indigo-100 dark:hover:bg-indigo-900/60 text-indigo-700 dark:text-indigo-300 font-semibold text-xs flex items-center gap-1 transition-colors"
                        >
                          <Plus className="w-3.5 h-3.5" />
                          Log Month Bid
                        </button>
                      ) : (
                        <span className="px-3 py-1 rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300 font-bold text-xs flex items-center gap-1">
                          <CheckCircle2 className="w-3.5 h-3.5" />
                          All {committee.totalMonths} Month Bids Logged
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Inline Committee Bidding Calculator */}
                  {activeCalcCommitteeId === committee.id && (
                    <div className="p-3.5 rounded-2xl bg-slate-900/90 dark:bg-slate-900/95 border border-indigo-500/30 text-white shadow-xl relative overflow-hidden transition-all animate-fade-in my-2">
                      <div className="relative z-10 space-y-2">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <div className="p-1.5 rounded-xl bg-indigo-500/20 border border-indigo-400/30 text-indigo-300 backdrop-blur-md">
                              <Calculator className="w-4 h-4" />
                            </div>
                            <div>
                              <h3 className="text-xs font-bold text-white leading-tight">
                                Bidding Calculator ({committee.name})
                              </h3>
                              <p className="text-[10px] text-indigo-200/80">
                                Calculate Winner Payout & Monthly Installment Per Member
                              </p>
                            </div>
                          </div>

                          <button
                            type="button"
                            onClick={() => setActiveCalcCommitteeId(null)}
                            className="text-[10px] font-semibold px-2 py-0.5 rounded-lg bg-indigo-500/20 hover:bg-indigo-500/30 text-indigo-200 border border-indigo-400/30 transition-colors cursor-pointer flex items-center gap-1 shrink-0 backdrop-blur-md"
                          >
                            <span>Close</span>
                            <ChevronUp className="w-3 h-3" />
                          </button>
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 pt-1">
                          <div>
                            <label className="block text-[10px] font-semibold text-indigo-200 uppercase tracking-wider mb-0.5">
                              Total Pot Amount (₹)
                            </label>
                            <input
                              type="number"
                              step={5000}
                              value={calcAmount}
                              onChange={(e) => setCalcAmount(Number(e.target.value))}
                              className="w-full px-2.5 py-1 text-xs rounded-lg bg-slate-800/80 border border-indigo-700/60 text-white font-bold focus:outline-none focus:ring-1 focus:ring-indigo-400"
                            />
                          </div>

                          <div>
                            <label className="block text-[10px] font-semibold text-indigo-200 uppercase tracking-wider mb-0.5">
                              Total Members
                            </label>
                            <input
                              type="number"
                              min={2}
                              max={100}
                              value={calcMembers}
                              onChange={(e) => setCalcMembers(Number(e.target.value))}
                              className="w-full px-2.5 py-1 text-xs rounded-lg bg-slate-800/80 border border-indigo-700/60 text-white font-bold focus:outline-none focus:ring-1 focus:ring-indigo-400"
                            />
                          </div>

                          <div>
                            <label className="block text-[10px] font-semibold text-indigo-200 uppercase tracking-wider mb-0.5">
                              Winning Bid / Discount (₹)
                            </label>
                            <input
                              type="number"
                              step={500}
                              value={calcBid}
                              onChange={(e) => setCalcBid(Number(e.target.value))}
                              className="w-full px-2.5 py-1 text-xs rounded-lg bg-slate-800/80 border border-indigo-700/60 text-emerald-400 font-bold focus:outline-none focus:ring-1 focus:ring-emerald-400"
                            />
                          </div>
                        </div>

                        {/* Results Bar */}
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-1">
                          <div className="p-1.5 sm:p-2 rounded-lg bg-indigo-950/60 border border-indigo-800/50">
                            <span className="text-[9px] text-indigo-300 font-semibold block uppercase">Standard Base</span>
                            <span className="text-xs font-extrabold text-slate-300">
                              {formatCurrency(standaloneCalcResult.baseInstallment)}/mo
                            </span>
                          </div>

                          <div className="p-1.5 sm:p-2 rounded-lg bg-indigo-950/60 border border-indigo-800/50">
                            <span className="text-[9px] text-indigo-300 font-semibold block uppercase">Winning Discount</span>
                            <span className="text-xs font-extrabold text-amber-400">
                              - {formatCurrency(standaloneCalcResult.winningBid)}
                            </span>
                          </div>

                          <div className="p-1.5 sm:p-2 rounded-lg bg-indigo-950/60 border border-indigo-800/50">
                            <span className="text-[9px] text-indigo-300 font-semibold block uppercase">Winner Takes Pot</span>
                            <span className="text-xs font-extrabold text-white">
                              {formatCurrency(standaloneCalcResult.netPotValue)}
                            </span>
                          </div>

                          <div className="p-1.5 sm:p-2 rounded-lg bg-emerald-950/60 border border-emerald-500/50">
                            <span className="text-[9px] text-emerald-300 font-bold block uppercase">Each Person Pays</span>
                            <span className="text-xs font-extrabold text-emerald-400">
                              {formatCurrency(standaloneCalcResult.installmentPerPerson)}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}

                  {isCompleted && (
                    <div className="p-3.5 rounded-2xl bg-emerald-50 dark:bg-emerald-950/50 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-300 text-xs font-semibold flex items-center justify-between">
                      <span className="flex items-center gap-2">
                        <CheckCircle2 className="w-4 h-4 text-emerald-600 dark:text-emerald-400 shrink-0" />
                        Committee tenure completed — all {committee.totalMonths} monthly bids have been completed!
                      </span>
                      <span className="text-[11px] font-extrabold px-2.5 py-0.5 rounded-full bg-emerald-200 dark:bg-emerald-900 text-emerald-900 dark:text-emerald-200">
                        Finished
                      </span>
                    </div>
                  )}

                  {/* Inline Form to Add a Month's Bidding Result */}
                  {addingMonthForId === committee.id && (
                    <div className="p-4 rounded-2xl bg-white dark:bg-slate-800/80 border border-indigo-200 dark:border-indigo-800 shadow-xs space-y-3 animate-fade-in">
                      <div className="text-xs font-bold text-slate-800 dark:text-slate-200">
                        Record Bidding for Month {(committee.months?.length || 0) + 1}
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-4 gap-3">
                        <div>
                          <label className="text-[10px] font-semibold text-slate-500 uppercase block mb-1">
                            Month Name / Label
                          </label>
                          <input
                            type="text"
                            value={newMonthLabel}
                            onChange={(e) => setNewMonthLabel(e.target.value)}
                            placeholder={`e.g. Month ${(committee.months?.length || 0) + 1}`}
                            className="w-full px-3 py-1.5 text-xs font-semibold rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white"
                          />
                        </div>

                        <div>
                          <label className="text-[10px] font-semibold text-slate-500 uppercase block mb-1">
                            Winning Bid / Discount (₹)
                          </label>
                          <input
                            type="number"
                            step={500}
                            value={newMonthBid}
                            onChange={(e) => setNewMonthBid(e.target.value)}
                            placeholder="10000"
                            className="w-full px-3 py-1.5 text-xs font-bold rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white"
                          />
                        </div>

                        <div>
                          <label className="text-[10px] font-semibold text-slate-500 uppercase block mb-1">
                            Winner Name / Note
                          </label>
                          <input
                            type="text"
                            value={newMonthWinner}
                            onChange={(e) => setNewMonthWinner(e.target.value)}
                            placeholder="e.g. Rahul or Self"
                            className="w-full px-3 py-1.5 text-xs rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white"
                          />
                        </div>

                        <div>
                          <label className="text-[10px] font-semibold text-slate-500 uppercase block mb-1">
                            Payment Status
                          </label>
                          <div className="flex items-center gap-1.5 pt-0.5">
                            <button
                              type="button"
                              onClick={() => setNewMonthIsPaid(true)}
                              className={`flex-1 py-1.5 rounded-lg text-xs font-bold transition-colors cursor-pointer ${
                                newMonthIsPaid
                                  ? 'bg-emerald-600 text-white shadow-xs'
                                  : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
                              }`}
                            >
                              Paid
                            </button>
                            <button
                              type="button"
                              onClick={() => setNewMonthIsPaid(false)}
                              className={`flex-1 py-1.5 rounded-lg text-xs font-bold transition-colors cursor-pointer ${
                                !newMonthIsPaid
                                  ? 'bg-amber-600 text-white shadow-xs'
                                  : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
                              }`}
                            >
                              Pending
                            </button>
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center justify-between gap-3 pt-1 border-t border-slate-100 dark:border-slate-700/50">
                        {/* Preview of calculated result */}
                        <div className="text-[11px] text-slate-600 dark:text-slate-400 flex items-center gap-3">
                          <span>
                            Winner Takes: <strong>{formatCurrency(committee.totalAmount - (Number(newMonthBid) || 0))}</strong>
                          </span>
                          <span>•</span>
                          <span>
                            Each Member Pays: <strong>{formatCurrency((committee.totalAmount - (Number(newMonthBid) || 0)) / committee.totalMembers)}</strong>
                          </span>
                        </div>

                        <div className="flex items-center gap-2">
                          <button
                            type="button"
                            onClick={() => setAddingMonthForId(null)}
                            className="px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-600 dark:text-slate-400 text-xs font-semibold cursor-pointer"
                          >
                            Cancel
                          </button>
                          <button
                            type="button"
                            onClick={() => handleAddMonthBid(committee)}
                            className="px-4 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs shadow-xs cursor-pointer"
                          >
                            Save Month Log
                          </button>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* Desktop Bidding Table */}
                  <div className="overflow-x-auto rounded-2xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900">
                    <table className="w-full text-left border-collapse text-xs">
                      <thead>
                        <tr className="border-b border-slate-100 dark:border-slate-800 text-[11px] font-semibold uppercase text-slate-400 dark:text-slate-500 bg-slate-50/80 dark:bg-slate-800/30">
                          <th className="py-2.5 px-4">Month</th>
                          <th className="py-2.5 px-4 text-right">Winning Bid</th>
                          <th className="py-2.5 px-4 text-right">Winner Takes</th>
                          <th className="py-2.5 px-4 text-right">Installment / Person</th>
                          <th className="py-2.5 px-4">Winner</th>
                          <th className="py-2.5 px-4 text-center">Status</th>
                          <th className="py-2.5 px-4 text-center">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60">
                        {committee.months && committee.months.length > 0 ? (
                          committee.months.map((m) => {
                            const isEditing =
                              editingMonth &&
                              editingMonth.committeeId === committee.id &&
                              editingMonth.monthNumber === m.monthNumber;

                            if (isEditing) {
                              const currentEditBid = Number(editingMonth.winningBid) || 0;
                              const editNetValue = Math.max(0, committee.totalAmount - currentEditBid);
                              const editInstallment = Math.round(editNetValue / committee.totalMembers);

                              return (
                                <tr key={m.monthNumber} className="bg-indigo-50/60 dark:bg-indigo-950/40">
                                  <td className="py-2 px-3">
                                    <input
                                      type="text"
                                      value={editingMonth.monthLabel}
                                      onChange={(e) => setEditingMonth({ ...editingMonth, monthLabel: e.target.value })}
                                      placeholder={`Month ${m.monthNumber}`}
                                      className="w-full px-2 py-1 text-xs font-bold rounded-lg border border-indigo-300 dark:border-indigo-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white"
                                    />
                                  </td>
                                  <td className="py-2 px-3">
                                    <input
                                      type="number"
                                      step={500}
                                      value={editingMonth.winningBid}
                                      onChange={(e) => setEditingMonth({ ...editingMonth, winningBid: e.target.value })}
                                      className="w-full px-2 py-1 text-xs font-bold rounded-lg border border-indigo-300 dark:border-indigo-700 bg-white dark:bg-slate-900 text-amber-600 dark:text-amber-400 text-right"
                                    />
                                  </td>
                                  <td className="py-2 px-3 text-right font-mono text-slate-700 dark:text-slate-300 font-semibold">
                                    {formatCurrency(editNetValue)}
                                  </td>
                                  <td className="py-2 px-3 text-right font-mono text-indigo-600 dark:text-indigo-400 font-extrabold">
                                    {formatCurrency(editInstallment)}
                                  </td>
                                  <td className="py-2 px-3">
                                    <input
                                      type="text"
                                      value={editingMonth.winnerName}
                                      onChange={(e) => setEditingMonth({ ...editingMonth, winnerName: e.target.value })}
                                      placeholder="Winner Name"
                                      className="w-full px-2 py-1 text-xs rounded-lg border border-indigo-300 dark:border-indigo-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white"
                                    />
                                  </td>
                                  <td className="py-2 px-3 text-center">
                                    <div className="flex items-center justify-center gap-1">
                                      <button
                                        type="button"
                                        onClick={() => setEditingMonth({ ...editingMonth, isPaid: true })}
                                        className={`px-2 py-1 rounded-md text-[10px] font-bold transition-colors cursor-pointer ${
                                          editingMonth.isPaid
                                            ? 'bg-emerald-600 text-white shadow-2xs'
                                            : 'bg-slate-200 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
                                        }`}
                                      >
                                        Paid
                                      </button>
                                      <button
                                        type="button"
                                        onClick={() => setEditingMonth({ ...editingMonth, isPaid: false })}
                                        className={`px-2 py-1 rounded-md text-[10px] font-bold transition-colors cursor-pointer ${
                                          !editingMonth.isPaid
                                            ? 'bg-amber-600 text-white shadow-2xs'
                                            : 'bg-slate-200 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
                                        }`}
                                      >
                                        Pending
                                      </button>
                                    </div>
                                  </td>
                                  <td className="py-2 px-3 text-center">
                                    <div className="flex items-center justify-center gap-1.5">
                                      <button
                                        type="button"
                                        onClick={() => handleSaveEditMonthBid(committee)}
                                        className="px-2.5 py-1 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-[11px] shadow-2xs cursor-pointer"
                                      >
                                        Save
                                      </button>
                                      <button
                                        type="button"
                                        onClick={() => setEditingMonth(null)}
                                        className="px-2.5 py-1 rounded-lg border border-slate-300 dark:border-slate-700 text-slate-600 dark:text-slate-400 font-medium text-[11px] cursor-pointer"
                                      >
                                        Cancel
                                      </button>
                                    </div>
                                  </td>
                                </tr>
                              );
                            }

                            return (
                              <tr key={m.monthNumber} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30">
                                <td className="py-2.5 px-4 font-bold text-slate-900 dark:text-white">
                                  {m.monthLabel || `Month ${m.monthNumber}`}
                                </td>
                                <td className="py-2.5 px-4 text-right font-mono font-semibold text-amber-600 dark:text-amber-400">
                                  {formatCurrency(m.winningBid)}
                                </td>
                                <td className="py-2.5 px-4 text-right font-mono text-slate-700 dark:text-slate-300">
                                  {formatCurrency(m.netPotValue)}
                                </td>
                                <td className="py-2.5 px-4 text-right font-mono font-extrabold text-indigo-600 dark:text-indigo-400">
                                  {formatCurrency(m.installmentPerPerson)}
                                </td>
                                <td className="py-2.5 px-4 text-slate-700 dark:text-slate-300 font-medium">
                                  {m.winnerName || '—'}
                                </td>
                                <td className="py-2.5 px-4 text-center">
                                  {/* Non-clickable static status badge */}
                                  <span
                                    className={`px-2.5 py-1 rounded-full text-[11px] font-bold inline-flex items-center justify-center gap-1 mx-auto ${
                                      m.isPaid
                                        ? 'bg-emerald-100 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300'
                                        : 'bg-amber-100 dark:bg-amber-950/60 text-amber-700 dark:text-amber-300'
                                    }`}
                                  >
                                    {m.isPaid ? (
                                      <>
                                        <CheckCircle2 className="w-3 h-3" />
                                        Paid
                                      </>
                                    ) : (
                                      <>
                                        <Clock className="w-3 h-3" />
                                        Pending
                                      </>
                                    )}
                                  </span>
                                </td>
                                <td className="py-2.5 px-4 text-center">
                                  <div className="flex items-center justify-center gap-1.5">
                                    <button
                                      type="button"
                                      onClick={() =>
                                        setEditingMonth({
                                          committeeId: committee.id,
                                          monthNumber: m.monthNumber,
                                          winningBid: String(m.winningBid),
                                          winnerName: m.winnerName || '',
                                          isPaid: m.isPaid,
                                          monthLabel: m.monthLabel || `Month ${m.monthNumber}`,
                                        })
                                      }
                                      className="p-1.5 rounded-lg text-slate-400 hover:text-indigo-600 dark:hover:text-indigo-400 hover:bg-indigo-50 dark:hover:bg-indigo-950/50 transition-colors cursor-pointer"
                                      title="Edit month bid entry"
                                    >
                                      <Edit2 className="w-3.5 h-3.5" />
                                    </button>

                                    <button
                                      type="button"
                                      onClick={() =>
                                        setDeleteMonthModal({
                                          isOpen: true,
                                          committee,
                                          monthNumber: m.monthNumber,
                                          monthLabel: m.monthLabel || `Month ${m.monthNumber}`,
                                        })
                                      }
                                      className="p-1.5 rounded-lg text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-950/50 transition-colors cursor-pointer"
                                      title="Delete month bid log"
                                    >
                                      <Trash2 className="w-3.5 h-3.5" />
                                    </button>
                                  </div>
                                </td>
                              </tr>
                            );
                          })
                        ) : (
                          <tr>
                            <td colSpan={7} className="py-4 px-4 text-center text-slate-400">
                              No monthly bids recorded yet. Click "Log Month Bid" to add bidding auction entries.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>

                  {committee.remarks && (
                    <p className="text-xs text-slate-500 dark:text-slate-400 italic">
                      Remarks: {committee.remarks}
                    </p>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Delete Month Bidding Record Confirmation Pop-up Modal */}
      {deleteMonthModal.isOpen && deleteMonthModal.committee && deleteMonthModal.monthNumber && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 max-w-sm w-full shadow-2xl space-y-4 animate-in fade-in zoom-in duration-200">
            <div className="flex items-center gap-3 text-red-600 dark:text-red-400">
              <div className="p-3 rounded-full bg-red-100 dark:bg-red-950/60 shrink-0">
                <Trash2 className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-base font-bold text-slate-900 dark:text-white">Delete Bidding Record</h3>
                <p className="text-xs text-slate-500 dark:text-slate-400">This action cannot be undone.</p>
              </div>
            </div>
            <p className="text-xs text-slate-600 dark:text-slate-300">
              Are you sure you want to delete the bidding log for <strong className="text-slate-900 dark:text-white">{deleteMonthModal.monthLabel}</strong> in <strong className="text-slate-900 dark:text-white">{deleteMonthModal.committee.name}</strong>?
            </p>
            <div className="flex items-center justify-end gap-2.5 pt-2">
              <button
                type="button"
                onClick={() => setDeleteMonthModal({ isOpen: false, committee: null, monthNumber: null, monthLabel: '' })}
                className="px-4 py-2 rounded-xl border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 text-xs font-semibold hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => {
                  if (deleteMonthModal.committee && deleteMonthModal.monthNumber) {
                    handleDeleteMonthBid(deleteMonthModal.committee, deleteMonthModal.monthNumber);
                  }
                  setDeleteMonthModal({ isOpen: false, committee: null, monthNumber: null, monthLabel: '' });
                }}
                className="px-4 py-2 rounded-xl bg-red-600 hover:bg-red-700 text-white text-xs font-bold shadow-xs transition-colors cursor-pointer"
              >
                Delete Record
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
