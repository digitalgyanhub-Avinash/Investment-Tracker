import React from 'react';
import { AlertTriangle, Trash2, X } from 'lucide-react';
import { DeleteModalState } from '../types';
import { formatCurrency } from '../utils/calculations';

interface DeleteConfirmModalProps {
  state: DeleteModalState;
  onClose: () => void;
  onConfirm: () => void;
  isDeleting?: boolean;
}

export const DeleteConfirmModal: React.FC<DeleteConfirmModalProps> = ({
  state,
  onClose,
  onConfirm,
  isDeleting = false,
}) => {
  if (!state.isOpen || !state.item) return null;

  const isHolding = state.type === 'holding';
  const isSip = state.type === 'sip';
  const isCommittee = state.type === 'committee';
  const isIpo = state.type === 'ipo';
  const item = state.item as any;

  const platform = item?.platform || item?.companyName || 'Unspecified';
  const name = isHolding
    ? item?.category
    : isSip
    ? item?.instrument
    : isCommittee
    ? item?.name
    : item?.companyName || 'IPO Application';

  const amount = isHolding
    ? item?.total
    : isSip
    ? item?.amount
    : isCommittee
    ? item?.totalAmount
    : item?.totalAmountBlocked;

  const titleType = isHolding
    ? 'Holding'
    : isSip
    ? 'SIP'
    : isCommittee
    ? 'Committee'
    : 'IPO Application';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-md animate-in fade-in duration-200">
      <div 
        className="relative w-full max-w-md glass-modal p-6 overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1.5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 rounded-xl hover:bg-white/20 dark:hover:bg-white/10 transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Warning Icon */}
        <div className="flex items-center justify-center w-12 h-12 mx-auto mb-4 rounded-2xl bg-red-500/20 text-red-600 dark:text-red-400 backdrop-blur-md border border-red-500/30">
          <AlertTriangle className="w-6 h-6" />
        </div>

        {/* Title */}
        <h3 className="text-xl font-bold text-center text-slate-900 dark:text-white mb-2">
          Delete {titleType} Entry?
        </h3>

        {/* Description */}
        <p className="text-sm text-center text-slate-600 dark:text-slate-400 mb-6">
          Are you sure you want to delete this entry? This action cannot be undone and will update your local records and Google Sheet.
        </p>

        {/* Item Details Box */}
        <div className="p-4 mb-6 rounded-xl glass-card">
          <div className="flex justify-between items-center mb-1 text-xs text-slate-500 dark:text-slate-400">
            <span>Platform: <strong className="text-slate-800 dark:text-slate-200">{platform}</strong></span>
            <span>Type: <strong className="text-slate-800 dark:text-slate-200">{titleType}</strong></span>
          </div>
          <div className="flex justify-between items-center text-slate-900 dark:text-white font-semibold">
            <span className="text-base truncate max-w-[200px]">{name}</span>
            <span className="text-lg font-bold text-emerald-600 dark:text-emerald-400">
              {formatCurrency(amount)}
            </span>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onClose}
            disabled={isDeleting}
            className="flex-1 py-3 px-4 rounded-xl glass-button text-slate-700 dark:text-slate-300 font-medium cursor-pointer disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isDeleting}
            className="flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-red-600/90 hover:bg-red-600 text-white font-medium shadow-md shadow-red-500/20 backdrop-blur-md border border-red-400/30 transition-all cursor-pointer disabled:opacity-50"
          >
            <Trash2 className="w-4 h-4" />
            {isDeleting ? 'Deleting...' : 'Delete Entry'}
          </button>
        </div>
      </div>
    </div>
  );
};
