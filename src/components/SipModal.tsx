import React, { useEffect, useState } from 'react';
import { Calculator, Calendar, Plus, Save, X } from 'lucide-react';
import { CATEGORY_OPTIONS, PLATFORM_OPTIONS } from '../data/initialData';
import { SIP, SipFrequency } from '../types';
import { calculateSipInvested, formatCurrency, formatDate, toPickerDate, toStandardDate } from '../utils/calculations';

interface SipModalProps {
  isOpen: boolean;
  mode: 'add' | 'edit';
  sip: SIP | null;
  onClose: () => void;
  onSave: (sip: SIP) => void;
  isSaving?: boolean;
}

export const SipModal: React.FC<SipModalProps> = ({
  isOpen,
  mode,
  sip,
  onClose,
  onSave,
  isSaving = false,
}) => {
  const [platform, setPlatform] = useState('');
  const [instrument, setInstrument] = useState('');
  const [amount, setAmount] = useState<number | ''>('');
  const [frequency, setFrequency] = useState<SipFrequency>('Monthly');
  const [since, setSince] = useState('');
  const [remarks, setRemarks] = useState('');

  useEffect(() => {
    if (isOpen) {
      if (mode === 'edit' && sip) {
        setPlatform(sip.platform || '');
        setInstrument(sip.instrument || '');
        setAmount(sip.amount ?? '');
        setFrequency(sip.frequency || 'Monthly');
        setSince(toPickerDate(sip.since || formatDate(new Date())));
        setRemarks(sip.remarks || '');
      } else {
        setPlatform('Zerodha');
        setInstrument('Stocks');
        setAmount('');
        setFrequency('Monthly');
        setSince(toPickerDate(formatDate(new Date())));
        setRemarks('');
      }
    }
  }, [isOpen, mode, sip]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const updated: SIP = {
      id: mode === 'edit' && sip?.id ? sip.id : `s-${Date.now()}`,
      platform: platform.trim() || 'Zerodha',
      instrument: instrument.trim() || 'Stocks',
      amount: Number(amount) || 0,
      frequency,
      since: toStandardDate(since),
      remarks: remarks.trim(),
      updatedAt: new Date().toISOString(),
    };

    onSave(updated);
  };

  if (!isOpen) return null;

  const tempSipObj: SIP = {
    id: 'temp',
    platform,
    instrument,
    amount: Number(amount) || 0,
    frequency,
    since: toStandardDate(since),
  };
  const liveCalculatedInvested = calculateSipInvested(tempSipObj);

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
            {mode === 'add' ? 'Add Recurring SIP' : 'Edit Recurring SIP'}
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

          {/* Category / Instrument Field */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1.5">
              Category / Instrument Name
            </label>
            <input
              type="text"
              required
              value={instrument}
              onChange={(e) => setInstrument(e.target.value)}
              placeholder="e.g. Stocks, Mutual Fund, ETFs, Gold"
              className="w-full px-3.5 py-2.5 rounded-xl glass-input text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            />
            <div className="flex flex-wrap gap-1.5 mt-2">
              {CATEGORY_OPTIONS.slice(0, 9).map((inst) => (
                <button
                  type="button"
                  key={inst}
                  onClick={() => setInstrument(inst)}
                  className={`text-xs px-2.5 py-1 rounded-lg transition-all cursor-pointer ${
                    instrument === inst
                      ? 'bg-emerald-500 text-white font-semibold shadow-sm'
                      : 'glass-button text-slate-700 dark:text-slate-300'
                  }`}
                >
                  {inst}
                </button>
              ))}
            </div>
          </div>

          {/* Amount & Frequency */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1.5">
                Amount (₹)
              </label>
              <input
                type="number"
                required
                min={1}
                value={amount}
                onChange={(e) => setAmount(e.target.value === '' ? '' : Number(e.target.value))}
                placeholder="e.g. 6500 or 20"
                className="w-full px-3.5 py-2.5 rounded-xl glass-input text-slate-900 dark:text-white font-semibold focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1.5">
                Frequency
              </label>
              <select
                value={frequency}
                onChange={(e) => setFrequency(e.target.value as SipFrequency)}
                className="w-full px-3.5 py-2.5 rounded-xl glass-input text-slate-900 dark:text-white font-medium focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              >
                <option value="Monthly" className="bg-slate-900 text-white">Monthly</option>
                <option value="Daily" className="bg-slate-900 text-white">Daily</option>
                <option value="Weekly" className="bg-slate-900 text-white">Weekly</option>
                <option value="Yearly" className="bg-slate-900 text-white">Yearly</option>
              </select>
            </div>
          </div>

          {/* Since Date */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1.5 flex items-center justify-between">
              <span>Since Date</span>
              <span className="text-emerald-600 dark:text-emerald-400 font-mono text-xs font-normal">
                Format: DD-MMM-YYYY
              </span>
            </label>
            <div className="relative">
              <input
                type="date"
                required
                value={since}
                onChange={(e) => setSince(e.target.value)}
                className="w-full pl-10 pr-3.5 py-2.5 rounded-xl glass-input text-slate-900 dark:text-white font-medium focus:outline-none focus:ring-2 focus:ring-emerald-500/50 cursor-pointer"
              />
              <Calendar className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
            </div>
          </div>

          {/* Auto Calculation Preview Box */}
          <div className="p-3.5 rounded-xl glass-card flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Calculator className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
              <span className="text-xs font-medium text-slate-800 dark:text-slate-200">
                Calculated Invested Till Date:
              </span>
            </div>
            <span className="text-base font-bold text-emerald-600 dark:text-emerald-400">
              {formatCurrency(liveCalculatedInvested)}
            </span>
          </div>

          {/* Remarks */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-400 mb-1.5">
              Remarks
            </label>
            <input
              type="text"
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              placeholder="e.g. Auto-debit from salary account"
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
              {isSaving ? 'Saving...' : 'Save SIP'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
