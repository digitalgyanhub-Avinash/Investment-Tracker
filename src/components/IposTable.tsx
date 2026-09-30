import React, { useState } from 'react';
import { BROKER_OPTIONS } from '../data/initialData';
import { IPOApplication, IPOStatus } from '../types';
import {
  calculateIposAllotmentRate,
  calculateIposBlockedTotal,
  calculateIposTotalGain,
  formatCurrency,
  formatDisplayDate,
  parseDate,
} from '../utils/calculations';
import {
  Building2,
  Calendar,
  CheckCircle2,
  Clock,
  DollarSign,
  Edit2,
  Filter,
  Plus,
  RefreshCw,
  Sparkles,
  Trash2,
  TrendingUp,
  XCircle,
} from 'lucide-react';

interface IposTableProps {
  ipos: IPOApplication[];
  onAddClick: () => void;
  onEditClick: (ipo: IPOApplication) => void;
  onDeleteClick: (ipo: IPOApplication) => void;
  onStatusChange: (ipo: IPOApplication, newStatus: IPOStatus) => void;
}

export const IposTable: React.FC<IposTableProps> = ({
  ipos,
  onAddClick,
  onEditClick,
  onDeleteClick,
  onStatusChange,
}) => {
  // Overall KPI Metrics
  const blockedTotal = calculateIposBlockedTotal(ipos);
  const totalGain = calculateIposTotalGain(ipos);
  const { rate: allotmentRate, allottedCount, decidedCount } = calculateIposAllotmentRate(ipos);
  const appliedCount = ipos.filter((i) => i.status === 'Applied').length;

  const filteredIpos = [...ipos].sort((a, b) => {
    const dateA = a.applyDate ? parseDate(a.applyDate).getTime() : 0;
    const dateB = b.applyDate ? parseDate(b.applyDate).getTime() : 0;
    return dateB - dateA;
  });

  // Color helper for status badge
  const getStatusBadge = (status: IPOStatus) => {
    switch (status) {
      case 'Applied':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-amber-100 text-amber-800 dark:bg-amber-950/80 dark:text-amber-300 border border-amber-300 dark:border-amber-800">
            <Clock className="w-3 h-3 text-amber-600 dark:text-amber-400" />
            Applied (Blocked)
          </span>
        );
      case 'Allotted':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-100 text-emerald-800 dark:bg-emerald-950/80 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-800">
            <CheckCircle2 className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
            Allotted
          </span>
        );
      case 'Not Allotted':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300 border border-slate-300 dark:border-slate-700">
            <XCircle className="w-3 h-3 text-slate-500" />
            Not Allotted
          </span>
        );
      case 'Listed':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-purple-100 text-purple-800 dark:bg-purple-950/80 dark:text-purple-300 border border-purple-300 dark:border-purple-800">
            <TrendingUp className="w-3 h-3 text-purple-600 dark:text-purple-400" />
            Listed / Sold
          </span>
        );
      default:
        return null;
    }
  };

  return (
    <div className="space-y-4 sm:space-y-6">
      {/* KPI Metric Summary Header - Compact */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-2">
        {/* Currently Blocked Funds */}
        <div className="p-2 sm:p-2.5 rounded-xl glass-panel">
          <div className="flex items-center justify-between">
            <span className="text-[9px] sm:text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Currently Blocked
            </span>
            <div className="p-1 rounded-md bg-amber-500/15 text-amber-600 dark:text-amber-400 border border-amber-500/30 backdrop-blur-md">
              <Clock className="w-3.5 h-3.5" />
            </div>
          </div>
          <p className="text-xs sm:text-sm font-extrabold text-slate-900 dark:text-white mt-0.5">
            {formatCurrency(blockedTotal)}
          </p>
          <span className="text-[9px] text-amber-600 dark:text-amber-400 font-medium block mt-0.5">
            {appliedCount} Active of {ipos.length} IPOs
          </span>
        </div>

        {/* Total Applications */}
        <div className="p-2 sm:p-2.5 rounded-xl glass-panel">
          <div className="flex items-center justify-between">
            <span className="text-[9px] sm:text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Applications Tracked
            </span>
            <div className="p-1 rounded-md bg-purple-500/15 text-purple-600 dark:text-purple-400 border border-purple-500/30 backdrop-blur-md">
              <Building2 className="w-3.5 h-3.5" />
            </div>
          </div>
          <p className="text-xs sm:text-sm font-extrabold text-slate-900 dark:text-white mt-0.5">
            {ipos.length} IPOs
          </p>
          <span className="text-[9px] text-purple-600 dark:text-purple-400 font-medium block mt-0.5">
            {appliedCount} Active + {decidedCount} Closed
          </span>
        </div>

        {/* Allotment Rate */}
        <div className="p-2 sm:p-2.5 rounded-xl glass-panel">
          <div className="flex items-center justify-between">
            <span className="text-[9px] sm:text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Allotment Success
            </span>
            <div className="p-1 rounded-md bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30 backdrop-blur-md">
              <Sparkles className="w-3.5 h-3.5" />
            </div>
          </div>
          <p className="text-xs sm:text-sm font-extrabold text-emerald-600 dark:text-emerald-400 mt-0.5">
            {allotmentRate}%
          </p>
          <span className="text-[9px] text-slate-500 dark:text-slate-400 block mt-0.5">
            {allottedCount} won of {decidedCount} decided
          </span>
        </div>

        {/* Realized Listing Gains */}
        <div className="p-2 sm:p-2.5 rounded-xl glass-panel">
          <div className="flex items-center justify-between">
            <span className="text-[9px] sm:text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Listing Gains
            </span>
            <div className="p-1 rounded-md bg-indigo-500/15 text-indigo-600 dark:text-indigo-400 border border-indigo-500/30 backdrop-blur-md">
              <TrendingUp className="w-3.5 h-3.5" />
            </div>
          </div>
          <p
            className={`text-xs sm:text-sm font-extrabold mt-0.5 ${
              totalGain >= 0
                ? 'text-emerald-600 dark:text-emerald-400'
                : 'text-rose-600 dark:text-rose-400'
            }`}
          >
            {totalGain >= 0 ? '+' : ''}
            {formatCurrency(totalGain)}
          </p>
          <span className="text-[9px] text-slate-500 dark:text-slate-400 block mt-0.5">
            Net realized profits
          </span>
        </div>
      </div>

      {/* Main IPO Applications List */}
      {filteredIpos.length === 0 ? (
        <div className="p-6 sm:p-8 text-center rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-2xs space-y-3">
          <div className="w-10 h-10 mx-auto rounded-full bg-purple-100 dark:bg-purple-950/80 text-purple-600 dark:text-purple-400 flex items-center justify-center">
            <Building2 className="w-5 h-5" />
          </div>
          <h3 className="text-xs sm:text-sm font-bold text-slate-900 dark:text-white">
            No IPO Applications Found
          </h3>
          <p className="text-[11px] text-slate-500 dark:text-slate-400 max-w-md mx-auto">
            Keep track of all your Mainboard and SME IPO applications, UPI mandate blocks, allotment status, and listing gains here.
          </p>
          <button
            onClick={onAddClick}
            className="px-3.5 py-1.5 rounded-xl text-xs font-bold text-white bg-purple-600 hover:bg-purple-700 shadow-sm inline-flex items-center gap-1.5"
          >
            <Plus className="w-3.5 h-3.5" />
            Add First IPO Application
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-2.5 sm:gap-3">
          {filteredIpos.map((ipo) => (
            <div
              key={ipo.id}
              className="p-3 sm:p-3.5 rounded-xl glass-panel hover:border-purple-400/50 transition-all flex flex-col md:flex-row md:items-center justify-between gap-3"
            >
              {/* Left Info Column */}
              <div className="space-y-1.5 min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-1.5">
                  <h3 className="text-xs sm:text-sm font-extrabold text-slate-900 dark:text-white truncate">
                    {ipo.companyName}
                  </h3>
                  <span className="px-2 py-0.5 rounded-md text-[10px] font-bold glass-card text-slate-700 dark:text-slate-300">
                    {ipo.category || 'Mainboard'}
                  </span>
                  {getStatusBadge(ipo.status)}
                </div>

                {/* Sub details: Demat Account, Platform */}
                <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500 dark:text-slate-400">
                  <span className="font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-1">
                    <Building2 className="w-3.5 h-3.5 text-purple-500" />
                    {ipo.platform}
                  </span>
                  <span>•</span>
                  <span>
                    Applicant: <strong className="text-slate-800 dark:text-slate-200">{ipo.applicantName || 'Self'}</strong>
                  </span>
                  {ipo.applicationNo && (
                    <>
                      <span>•</span>
                      <span>App #: {ipo.applicationNo}</span>
                    </>
                  )}
                </div>

                {/* Dates */}
                <div className="flex flex-wrap items-center gap-3 text-[11px] text-slate-500 dark:text-slate-400 pt-0.5">
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3 h-3 text-slate-400" />
                    Apply: <strong>{formatDisplayDate(ipo.applyDate)}</strong>
                  </span>
                  {ipo.allotmentDate && (
                    <span>
                      Allotment: <strong>{formatDisplayDate(ipo.allotmentDate)}</strong>
                    </span>
                  )}
                  {ipo.listingDate && (
                    <span>
                      Listing: <strong>{formatDisplayDate(ipo.listingDate)}</strong>
                    </span>
                  )}
                  {ipo.refundDate && (
                    <span>
                      Refund: <strong>{formatDisplayDate(ipo.refundDate)}</strong>
                    </span>
                  )}
                </div>

                {/* Remarks */}
                {ipo.remarks && (
                  <p className="text-xs text-slate-600 dark:text-slate-300 glass-card px-3 py-1.5 rounded-xl italic">
                    "{ipo.remarks}"
                  </p>
                )}
              </div>

              {/* Middle Financial Column */}
              <div className="p-3 sm:p-3.5 rounded-xl glass-card flex flex-row md:flex-col justify-between items-center md:items-end gap-2 shrink-0">
                <div className="text-left md:text-right">
                  <span className="text-[10px] font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider block">
                    Bidding Details
                  </span>
                  <span className="text-xs font-bold text-slate-800 dark:text-slate-200">
                    {ipo.lotsApplied} Lot ({ipo.sharesPerLot * ipo.lotsApplied} shares @ ₹{ipo.cutOffPrice})
                  </span>
                </div>

                <div className="text-right">
                  <span className="text-[10px] font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider block">
                    Total Amount Blocked
                  </span>
                  <span className="text-sm sm:text-base font-black text-purple-700 dark:text-purple-400">
                    {formatCurrency(ipo.totalAmountBlocked)}
                  </span>
                </div>

                {/* Listing Gains if allotted/listed */}
                {typeof ipo.listingGainAmount === 'number' && (
                  <div className="text-right pt-1 border-t border-white/20 dark:border-white/10 w-full flex justify-between md:justify-end gap-2 items-center">
                    <span className="text-[10px] font-semibold text-slate-500 dark:text-slate-400 uppercase">
                      Gain/Loss:
                    </span>
                    <span
                      className={`text-xs sm:text-sm font-black ${
                        ipo.listingGainAmount >= 0
                          ? 'text-emerald-600 dark:text-emerald-400'
                          : 'text-rose-600 dark:text-rose-400'
                      }`}
                    >
                      {ipo.listingGainAmount >= 0 ? '+' : ''}
                      {formatCurrency(ipo.listingGainAmount)}
                      {ipo.listingGainPercent !== undefined && ` (${ipo.listingGainPercent >= 0 ? '+' : ''}${ipo.listingGainPercent}%)`}
                    </span>
                  </div>
                )}
              </div>

              {/* Right Action Buttons */}
              <div className="flex items-center gap-1.5 shrink-0 self-end md:self-center">
                {/* Quick Status Transition Dropdown / Button */}
                {ipo.status === 'Applied' && (
                  <button
                    onClick={() => onStatusChange(ipo, 'Allotted')}
                    className="px-2.5 py-1.5 rounded-lg text-xs font-bold text-emerald-700 dark:text-emerald-300 bg-emerald-50 dark:bg-emerald-950/60 hover:bg-emerald-100 dark:hover:bg-emerald-900/60 border border-emerald-200 dark:border-emerald-800 transition-colors"
                    title="Mark as Allotted"
                  >
                    Mark Allotted
                  </button>
                )}

                <button
                  onClick={() => onEditClick(ipo)}
                  className="p-2 rounded-xl text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
                  title="Edit IPO details"
                >
                  <Edit2 className="w-4 h-4" />
                </button>

                <button
                  onClick={() => onDeleteClick(ipo)}
                  className="p-2 rounded-xl text-rose-500 hover:text-rose-700 hover:bg-rose-50 dark:hover:bg-rose-950/50 transition-colors"
                  title="Delete IPO application"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
