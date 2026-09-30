import React, { useState } from 'react';
import { Download, FileSpreadsheet, Upload, X } from 'lucide-react';
import * as XLSX from 'xlsx';
import { Holding, SIP } from '../types';
import { calculateSipInvested, formatDate, normalizeToDdMmYyyy } from '../utils/calculations';

interface ExcelImportExportModalProps {
  isOpen: boolean;
  holdings: Holding[];
  sips: SIP[];
  onClose: () => void;
  onImportData: (newHoldings: Holding[], newSips: SIP[]) => void;
}

export const ExcelImportExportModal: React.FC<ExcelImportExportModalProps> = ({
  isOpen,
  holdings,
  sips,
  onClose,
  onImportData,
}) => {
  const [importError, setImportError] = useState<string | null>(null);
  const [importSuccess, setImportSuccess] = useState<string | null>(null);

  if (!isOpen) return null;

  // Handle Excel File Upload
  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setImportError(null);
    setImportSuccess(null);

    const reader = new FileReader();
    reader.onload = (evt) => {
      try {
        const bstr = evt.target?.result;
        const wb = XLSX.read(bstr, { type: 'binary' });

        const newHoldings: Holding[] = [];
        const newSips: SIP[] = [];

        wb.SheetNames.forEach((sheetName) => {
          const ws = wb.Sheets[sheetName];
          const rawRows: any[] = XLSX.utils.sheet_to_json(ws, { header: 1 });

          if (sheetName.toLowerCase().includes('sip')) {
            for (let i = 1; i < rawRows.length; i++) {
              const row = rawRows[i];
              if (row && row[0]) {
                newSips.push({
                  id: `s-imp-${Date.now()}-${i}`,
                  platform: String(row[0] || 'Groww'),
                  instrument: String(row[1] || 'SIP'),
                  amount: Number(row[2]) || 0,
                  frequency: (row[3] as any) || 'Monthly',
                  since: normalizeToDdMmYyyy(row[4]) || formatDate(new Date(), 'DD-MM-YYYY'),
                  remarks: String(row[7] || row[5] || ''),
                });
              }
            }
          } else {
            for (let i = 1; i < rawRows.length; i++) {
              const row = rawRows[i];
              if (row && row[0]) {
                const bdStr = String(row[2] || '');
                const bdArr = bdStr
                  ? bdStr.split(',').map((s) => Number(s.trim())).filter((n) => !isNaN(n))
                  : [];

                newHoldings.push({
                  id: `h-imp-${Date.now()}-${i}`,
                  platform: String(row[0] || 'Zerodha'),
                  category: String(row[1] || 'Stocks'),
                  breakdown: bdArr,
                  breakdownRaw: bdStr,
                  total: Number(row[3]) || 0,
                  tillDate: normalizeToDdMmYyyy(row[4]) || formatDate(new Date(), 'DD-MM-YYYY'),
                  remarks: String(row[5] || ''),
                });
              }
            }
          }
        });

        if (newHoldings.length === 0 && newSips.length === 0) {
          setImportError('No valid data found in Excel file.');
          return;
        }

        onImportData(
          newHoldings.length > 0 ? newHoldings : holdings,
          newSips.length > 0 ? newSips : sips
        );

        setImportSuccess(
          `Successfully imported ${newHoldings.length} holdings and ${newSips.length} SIP entries!`
        );
      } catch (err: any) {
        setImportError(`Failed to parse Excel file: ${err.message}`);
      }
    };
    reader.readAsBinaryString(file);
  };

  // Export current data to Excel
  const handleExport = () => {
    const holdingsExport = holdings.map((h) => ({
      Platform: h.platform,
      Category: h.category,
      Breakdown: h.breakdown ? h.breakdown.join(', ') : h.breakdownRaw || '',
      Total: h.total,
      'Till Date': normalizeToDdMmYyyy(h.tillDate) || formatDate(new Date(), 'DD-MM-YYYY'),
      Remarks: h.remarks || '',
    }));

    const sipsExport = sips.map((s) => ({
      Platform: s.platform,
      Instrument: s.instrument,
      Amount: s.amount,
      Frequency: s.frequency,
      Since: normalizeToDdMmYyyy(s.since) || formatDate(new Date(), 'DD-MM-YYYY'),
      'Current Date': formatDate(new Date(), 'DD-MM-YYYY'),
      'Invested Till Date': calculateSipInvested(s),
      Remarks: s.remarks || '',
    }));

    const wb = XLSX.utils.book_new();
    const wsHoldings = XLSX.utils.json_to_sheet(holdingsExport);
    const wsSips = XLSX.utils.json_to_sheet(sipsExport);

    XLSX.utils.book_append_sheet(wb, wsHoldings, 'Holdings');
    XLSX.utils.book_append_sheet(wb, wsSips, 'SIPs');

    XLSX.writeFile(wb, `Investment_Portfolio_${formatDate(new Date(), 'YYYY-MM-DD')}.xlsx`);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div 
        className="relative w-full max-w-md bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 p-6"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-200 dark:border-slate-800 mb-5">
          <div className="flex items-center gap-2">
            <FileSpreadsheet className="w-5 h-5 text-emerald-500" />
            <h2 className="text-xl font-bold text-slate-900 dark:text-white">
              Import / Export Excel
            </h2>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="space-y-4 mb-6">
          {/* Export Option */}
          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/60">
            <h3 className="text-sm font-semibold text-slate-900 dark:text-white mb-1">
              Export to Excel (.xlsx)
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mb-3">
              Download your full portfolio holdings and calculated SIP totals into a formatted Excel file.
            </p>
            <button
              onClick={handleExport}
              className="w-full py-2.5 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-medium flex items-center justify-center gap-2 shadow-sm transition-colors"
            >
              <Download className="w-4 h-4" />
              Download Excel File
            </button>
          </div>

          {/* Import Option */}
          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/60">
            <h3 className="text-sm font-semibold text-slate-900 dark:text-white mb-1">
              Import Excel File (.xlsx / .csv)
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mb-3">
              Upload your existing investment Excel sheet to quickly replace or update your data.
            </p>
            <label className="w-full py-2.5 px-4 rounded-xl border border-dashed border-slate-300 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300 text-sm font-medium flex items-center justify-center gap-2 cursor-pointer transition-colors">
              <Upload className="w-4 h-4 text-emerald-500" />
              <span>Choose Excel File</span>
              <input
                type="file"
                accept=".xlsx, .xls, .csv"
                onChange={handleFileUpload}
                className="hidden"
              />
            </label>
          </div>

          {importError && (
            <p className="text-xs text-red-600 dark:text-red-400 p-3 rounded-xl bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800">
              {importError}
            </p>
          )}

          {importSuccess && (
            <p className="text-xs text-emerald-600 dark:text-emerald-400 p-3 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800">
              {importSuccess}
            </p>
          )}
        </div>

        <button
          type="button"
          onClick={onClose}
          className="w-full py-2.5 px-4 rounded-xl border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
        >
          Done
        </button>
      </div>
    </div>
  );
};
