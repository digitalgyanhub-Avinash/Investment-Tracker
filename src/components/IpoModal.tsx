import React, { useEffect, useState } from 'react';
import { BROKER_OPTIONS } from '../data/initialData';
import { IPOCategory, IPOStatus, IPOApplication, PlatformName } from '../types';
import { parseToIsoDate } from '../utils/calculations';
import { X, Calendar, DollarSign, Building2, User, HelpCircle, Sparkles } from 'lucide-react';

interface IpoModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (ipo: IPOApplication, isEdit: boolean) => void;
  editingIpo: IPOApplication | null;
}

export const IpoModal: React.FC<IpoModalProps> = ({
  isOpen,
  onClose,
  onSave,
  editingIpo,
}) => {
  const [companyName, setCompanyName] = useState('');
  const [category, setCategory] = useState<IPOCategory>('Mainboard');
  const [platform, setPlatform] = useState<PlatformName>('Zerodha');
  const [applicantName, setApplicantName] = useState('Self');
  const [applicationNo, setApplicationNo] = useState('');

  const [sharesPerLot, setSharesPerLot] = useState<number | ''>(15);
  const [cutOffPrice, setCutOffPrice] = useState<number | ''>(500);
  const [lotsApplied, setLotsApplied] = useState<number | ''>(1);
  const [totalAmountBlocked, setTotalAmountBlocked] = useState<number | ''>(7500);

  const [applyDate, setApplyDate] = useState(new Date().toISOString().split('T')[0]);
  const [allotmentDate, setAllotmentDate] = useState('');
  const [refundDate, setRefundDate] = useState('');
  const [listingDate, setListingDate] = useState('');

  const [status, setStatus] = useState<IPOStatus>('Applied');
  const [allottedLots, setAllottedLots] = useState<number | ''>(1);
  const [listingPrice, setListingPrice] = useState<number | ''>('');
  const [remarks, setRemarks] = useState('');

  // Auto calculate total amount blocked when bidding inputs change
  useEffect(() => {
    if (typeof sharesPerLot === 'number' && typeof cutOffPrice === 'number' && typeof lotsApplied === 'number') {
      const calc = sharesPerLot * cutOffPrice * lotsApplied;
      if (!isNaN(calc) && calc > 0) {
        setTotalAmountBlocked(calc);
      }
    }
  }, [sharesPerLot, cutOffPrice, lotsApplied]);

  // Populate when editing
  useEffect(() => {
    if (editingIpo) {
      setCompanyName(editingIpo.companyName || '');
      setCategory(editingIpo.category || 'Mainboard');
      setPlatform(editingIpo.platform || 'Zerodha');
      setApplicantName(editingIpo.applicantName || 'Self');
      setApplicationNo(editingIpo.applicationNo || '');

      setSharesPerLot(editingIpo.sharesPerLot ?? 15);
      setCutOffPrice(editingIpo.cutOffPrice ?? 500);
      setLotsApplied(editingIpo.lotsApplied ?? 1);
      setTotalAmountBlocked(editingIpo.totalAmountBlocked ?? 7500);

      setApplyDate(parseToIsoDate(editingIpo.applyDate) || parseToIsoDate(new Date()));
      setAllotmentDate(parseToIsoDate(editingIpo.allotmentDate));
      setRefundDate(parseToIsoDate(editingIpo.refundDate));
      setListingDate(parseToIsoDate(editingIpo.listingDate));

      setStatus(editingIpo.status || 'Applied');
      setAllottedLots(editingIpo.allottedLots ?? (editingIpo.lotsApplied || 1));
      setListingPrice(editingIpo.listingPrice ?? '');
      setRemarks(editingIpo.remarks || '');
    } else {
      // Reset defaults
      setCompanyName('');
      setCategory('Mainboard');
      setPlatform('Zerodha');
      setApplicantName('Self');
      setApplicationNo('');
      setSharesPerLot(15);
      setCutOffPrice(500);
      setLotsApplied(1);
      setTotalAmountBlocked(7500);
      setApplyDate(parseToIsoDate(new Date()));
      setAllotmentDate('');
      setRefundDate('');
      setListingDate('');
      setStatus('Applied');
      setAllottedLots(1);
      setListingPrice('');
      setRemarks('');
    }
  }, [editingIpo, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!companyName.trim()) return;

    const shares = typeof sharesPerLot === 'number' ? sharesPerLot : 1;
    const price = typeof cutOffPrice === 'number' ? cutOffPrice : 0;
    const lots = typeof lotsApplied === 'number' ? lotsApplied : 1;
    const blocked = typeof totalAmountBlocked === 'number' ? totalAmountBlocked : shares * price * lots;

    let calcGainAmt: number | undefined = undefined;
    let calcGainPct: number | undefined = undefined;

    const aLots = typeof allottedLots === 'number' ? allottedLots : (status === 'Not Allotted' ? 0 : lots);

    if ((status === 'Listed' || status === 'Allotted') && typeof listingPrice === 'number' && listingPrice > 0) {
      const totalCost = aLots * shares * price;
      const totalRevenue = aLots * shares * listingPrice;
      calcGainAmt = totalRevenue - totalCost;
      calcGainPct = price > 0 ? ((listingPrice - price) / price) * 100 : 0;
    }

    const itemToSave: IPOApplication = {
      id: editingIpo ? editingIpo.id : `ipo-${Date.now()}`,
      companyName: companyName.trim(),
      category,
      platform,
      applicantName: applicantName.trim() || 'Self',
      applicationNo: applicationNo.trim() || undefined,
      sharesPerLot: shares,
      cutOffPrice: price,
      lotsApplied: lots,
      totalAmountBlocked: blocked,
      applyDate: parseToIsoDate(applyDate) || parseToIsoDate(new Date()),
      allotmentDate: parseToIsoDate(allotmentDate) || undefined,
      refundDate: parseToIsoDate(refundDate) || undefined,
      listingDate: parseToIsoDate(listingDate) || undefined,
      status,
      allottedLots: status === 'Not Allotted' ? 0 : aLots,
      listingPrice: typeof listingPrice === 'number' ? listingPrice : undefined,
      listingGainAmount: calcGainAmt,
      listingGainPercent: calcGainPct ? Number(calcGainPct.toFixed(2)) : undefined,
      remarks: remarks.trim() || undefined,
      updatedAt: new Date().toISOString(),
    };

    onSave(itemToSave, !!editingIpo);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-950/60 backdrop-blur-md overflow-y-auto">
      <div className="glass-modal w-full max-w-xl my-8 overflow-hidden transform transition-all">
        {/* Modal Header */}
        <div className="px-5 py-4 sm:px-6 sm:py-5 border-b border-white/20 dark:border-white/10 flex items-center justify-between glass-header">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-purple-500/20 text-purple-600 dark:text-purple-300 backdrop-blur-md">
              <Building2 className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base sm:text-lg font-bold text-slate-900 dark:text-white">
                {editingIpo ? 'Edit IPO Application' : 'Apply / Add IPO Application'}
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Track bids, allotments, UPI mandate blocks, and listing gains
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

        {/* Modal Body / Form */}
        <form onSubmit={handleSubmit} className="p-5 sm:p-6 space-y-4 max-h-[75vh] overflow-y-auto">
          {/* Company Name */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Company Name <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Bajaj Housing Finance, Swiggy, Premier Energies"
              value={companyName}
              onChange={(e) => setCompanyName(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-purple-500/50"
            />
          </div>

          {/* Category & Broker Platform */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                IPO Type / Segment
              </label>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setCategory('Mainboard')}
                  className={`flex-1 py-2 px-3 rounded-xl text-xs font-bold border transition-colors ${
                    category === 'Mainboard'
                      ? 'bg-purple-600 text-white border-purple-600 shadow-xs'
                      : 'bg-slate-50 dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-300 dark:border-slate-700'
                  }`}
                >
                  Mainboard
                </button>
                <button
                  type="button"
                  onClick={() => setCategory('SME')}
                  className={`flex-1 py-2 px-3 rounded-xl text-xs font-bold border transition-colors ${
                    category === 'SME'
                      ? 'bg-purple-600 text-white border-purple-600 shadow-xs'
                      : 'bg-slate-50 dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-300 dark:border-slate-700'
                  }`}
                >
                  SME IPO
                </button>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                Broker Platform / App
              </label>
              <select
                value={platform}
                onChange={(e) => setPlatform(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-purple-500/50"
              >
                {BROKER_OPTIONS.map((b) => (
                  <option key={b} value={b}>
                    {b}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Applicant Name & Application No */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                Demat Account / Applicant
              </label>
              <input
                type="text"
                placeholder="Self, Spouse, Father, Mother..."
                value={applicantName}
                onChange={(e) => setApplicantName(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-purple-500/50"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                Application / UPI Mandate No (Optional)
              </label>
              <input
                type="text"
                placeholder="e.g. 123456789"
                value={applicationNo}
                onChange={(e) => setApplicationNo(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-purple-500/50"
              />
            </div>
          </div>

          {/* Bidding Details Section */}
          <div className="p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/80 space-y-3">
            <h3 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
              <DollarSign className="w-3.5 h-3.5 text-purple-500" />
              Bidding & Amount Details
            </h3>

            <div className="grid grid-cols-3 gap-2.5 items-end">
              <div className="flex flex-col justify-end">
                <label className="min-h-[28px] sm:min-h-[20px] flex items-end text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1 leading-tight">
                  Cut-off Price (₹)
                </label>
                <input
                  type="number"
                  required
                  min="1"
                  step="any"
                  placeholder="500"
                  value={cutOffPrice}
                  onChange={(e) => {
                    const val = e.target.value === '' ? '' : Number(e.target.value);
                    setCutOffPrice(val);
                    if (typeof val === 'number' && typeof sharesPerLot === 'number' && typeof lotsApplied === 'number') {
                      setTotalAmountBlocked(val * sharesPerLot * lotsApplied);
                    }
                  }}
                  className="w-full h-10 px-2.5 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-purple-500/50"
                />
              </div>

              <div className="flex flex-col justify-end">
                <label className="min-h-[28px] sm:min-h-[20px] flex items-end text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1 leading-tight">
                  Shares / Lot
                </label>
                <input
                  type="number"
                  required
                  min="1"
                  placeholder="15"
                  value={sharesPerLot}
                  onChange={(e) => {
                    const val = e.target.value === '' ? '' : Number(e.target.value);
                    setSharesPerLot(val);
                    if (typeof val === 'number' && typeof cutOffPrice === 'number' && typeof lotsApplied === 'number') {
                      setTotalAmountBlocked(val * cutOffPrice * lotsApplied);
                    }
                  }}
                  className="w-full h-10 px-2.5 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-purple-500/50"
                />
              </div>

              <div className="flex flex-col justify-end">
                <label className="min-h-[28px] sm:min-h-[20px] flex items-end text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1 leading-tight">
                  Lots Applied
                </label>
                <input
                  type="number"
                  required
                  min="1"
                  placeholder="1"
                  value={lotsApplied}
                  onChange={(e) => {
                    const val = e.target.value === '' ? '' : Number(e.target.value);
                    setLotsApplied(val);
                    if (typeof val === 'number' && typeof cutOffPrice === 'number' && typeof sharesPerLot === 'number') {
                      setTotalAmountBlocked(val * cutOffPrice * sharesPerLot);
                    }
                  }}
                  className="w-full h-10 px-2.5 py-2 rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-purple-500/50"
                />
              </div>
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">
                Total Amount Blocked (₹)
              </label>
              <input
                type="number"
                required
                min="0"
                step="any"
                placeholder="7500"
                value={totalAmountBlocked}
                onChange={(e) => setTotalAmountBlocked(e.target.value === '' ? '' : Number(e.target.value))}
                className="w-full px-3 py-2 rounded-lg border border-purple-300 dark:border-purple-800 bg-white dark:bg-slate-900 text-purple-700 dark:text-purple-300 font-bold text-sm focus:outline-none focus:ring-2 focus:ring-purple-500/50"
              />
              <span className="text-[10px] text-slate-500 dark:text-slate-400 mt-1 block">
                Calculated: {lotsApplied || 1} lot(s) × {sharesPerLot || 0} shares × ₹{cutOffPrice || 0} = ₹{((Number(lotsApplied) || 1) * (Number(sharesPerLot) || 0) * (Number(cutOffPrice) || 0)).toLocaleString('en-IN')}
              </span>
            </div>
          </div>

          {/* Key Dates */}
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
            <div>
              <label className="block text-[11px] font-semibold text-slate-700 dark:text-slate-300 mb-1">
                Apply Date
              </label>
              <input
                type="date"
                required
                value={applyDate}
                onChange={(e) => setApplyDate(e.target.value)}
                className="w-full px-2.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-xs focus:outline-none focus:ring-2 focus:ring-purple-500/50"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-slate-700 dark:text-slate-300 mb-1">
                Allotment Date
              </label>
              <input
                type="date"
                value={allotmentDate}
                onChange={(e) => setAllotmentDate(e.target.value)}
                className="w-full px-2.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-xs focus:outline-none focus:ring-2 focus:ring-purple-500/50"
              />
            </div>

            <div className="col-span-2 sm:col-span-1">
              <label className="block text-[11px] font-semibold text-slate-700 dark:text-slate-300 mb-1">
                Listing Date
              </label>
              <input
                type="date"
                value={listingDate}
                onChange={(e) => setListingDate(e.target.value)}
                className="w-full px-2.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-xs focus:outline-none focus:ring-2 focus:ring-purple-500/50"
              />
            </div>
          </div>

          {/* Application Status Selection */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
              Application Status
            </label>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
              <button
                type="button"
                onClick={() => setStatus('Applied')}
                className={`py-2 px-2.5 rounded-xl text-xs font-bold border transition-colors flex items-center justify-center gap-1 ${
                  status === 'Applied'
                    ? 'bg-amber-500 text-white border-amber-500 shadow-xs'
                    : 'bg-slate-50 dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-300 dark:border-slate-700'
                }`}
              >
                🟡 Applied
              </button>

              <button
                type="button"
                onClick={() => setStatus('Allotted')}
                className={`py-2 px-2.5 rounded-xl text-xs font-bold border transition-colors flex items-center justify-center gap-1 ${
                  status === 'Allotted'
                    ? 'bg-emerald-600 text-white border-emerald-600 shadow-xs'
                    : 'bg-slate-50 dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-300 dark:border-slate-700'
                }`}
              >
                🟢 Allotted
              </button>

              <button
                type="button"
                onClick={() => setStatus('Not Allotted')}
                className={`py-2 px-2.5 rounded-xl text-xs font-bold border transition-colors flex items-center justify-center gap-1 ${
                  status === 'Not Allotted'
                    ? 'bg-slate-600 text-white border-slate-600 shadow-xs'
                    : 'bg-slate-50 dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-300 dark:border-slate-700'
                }`}
              >
                ⚪ Not Allotted
              </button>

              <button
                type="button"
                onClick={() => setStatus('Listed')}
                className={`py-2 px-2.5 rounded-xl text-xs font-bold border transition-colors flex items-center justify-center gap-1 ${
                  status === 'Listed'
                    ? 'bg-purple-600 text-white border-purple-600 shadow-xs'
                    : 'bg-slate-50 dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-300 dark:border-slate-700'
                }`}
              >
                🟣 Listed / Sold
              </button>
            </div>
          </div>

          {/* Conditional Fields for Allotted or Listed */}
          {(status === 'Allotted' || status === 'Listed') && (
            <div className="p-3.5 rounded-2xl bg-emerald-50/80 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800/60 space-y-3">
              <h4 className="text-xs font-bold text-emerald-800 dark:text-emerald-300 flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5" />
                Allotment & Listing Gain Outcome
              </h4>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Allotted Lots Count
                  </label>
                  <input
                    type="number"
                    min="1"
                    placeholder="1"
                    value={allottedLots}
                    onChange={(e) => setAllottedLots(e.target.value === '' ? '' : Number(e.target.value))}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white text-xs focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                  />
                </div>

                <div>
                  <label className="block text-[11px] font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Listing / Sale Price per Share (₹)
                  </label>
                  <input
                    type="number"
                    step="any"
                    placeholder="e.g. 750"
                    value={listingPrice}
                    onChange={(e) => setListingPrice(e.target.value === '' ? '' : Number(e.target.value))}
                    className="w-full px-3 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white text-xs focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                  />
                </div>
              </div>
            </div>
          )}

          {/* Remarks */}
          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Remarks / Notes
            </label>
            <input
              type="text"
              placeholder="e.g. Payment done thru PAYTM UPI, Mandate and Block request has been accepted."
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-purple-500/50"
            />
          </div>

          {/* Actions */}
          <div className="pt-3 flex items-center justify-end gap-2.5 border-t border-white/20 dark:border-white/10">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 rounded-xl text-xs font-semibold glass-button text-slate-700 dark:text-slate-300 cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-purple-600/90 hover:bg-purple-600 shadow-md shadow-purple-600/20 backdrop-blur-md border border-purple-400/30 transition-all cursor-pointer"
            >
              {editingIpo ? 'Update IPO Application' : 'Save IPO Application'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
