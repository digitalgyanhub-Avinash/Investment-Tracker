import React, { useState } from 'react';
import { Check, Copy, ExternalLink, RefreshCw, Sheet, Sparkles, X } from 'lucide-react';
import { GoogleSheetConfig } from '../types';
import { GOOGLE_APPS_SCRIPT_CODE } from '../utils/googleAppsScriptCode';

interface GoogleSheetsModalProps {
  isOpen: boolean;
  config: GoogleSheetConfig;
  onClose: () => void;
  onSaveConfig: (newConfig: GoogleSheetConfig) => void;
  onTestConnection: (scriptUrl: string) => Promise<boolean>;
  onSyncNow: () => Promise<void>;
  isSyncing?: boolean;
}

export const GoogleSheetsModal: React.FC<GoogleSheetsModalProps> = ({
  isOpen,
  config,
  onClose,
  onSaveConfig,
  onTestConnection,
  onSyncNow,
  isSyncing = false,
}) => {
  const [url, setUrl] = useState(config.scriptUrl || '');
  const [copied, setCopied] = useState(false);
  const [testing, setTesting] = useState(false);
  const [testResult, setTestResult] = useState<{ success: boolean; msg: string } | null>(null);

  if (!isOpen) return null;

  const handleCopyCode = () => {
    navigator.clipboard.writeText(GOOGLE_APPS_SCRIPT_CODE);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  const handleTest = async () => {
    if (!url.trim()) {
      setTestResult({ success: false, msg: 'Please paste a Web App URL first.' });
      return;
    }
    setTesting(true);
    setTestResult(null);
    const ok = await onTestConnection(url.trim());
    setTesting(false);
    if (ok) {
      setTestResult({ success: true, msg: 'Connected successfully! Google Sheet data fetched.' });
      onSaveConfig({
        ...config,
        scriptUrl: url.trim(),
        isConfigured: true,
        lastSyncedAt: new Date().toISOString(),
      });
    } else {
      setTestResult({ success: false, msg: 'Could not connect. Please check your Web App URL and deployment access ("Who has access: Anyone").' });
    }
  };

  const handleSave = () => {
    onSaveConfig({
      ...config,
      scriptUrl: url.trim(),
      isConfigured: Boolean(url.trim()),
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div 
        className="relative w-full max-w-2xl bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 p-6 max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-200 dark:border-slate-800 mb-5">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-emerald-100 dark:bg-emerald-900/40 text-emerald-600 dark:text-emerald-400">
              <Sheet className="w-6 h-6" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-slate-900 dark:text-white">
                Google Sheets & Apps Script Sync
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Connect your live Google Sheet for automatic reading & writing
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Script URL Input Section */}
        <div className="space-y-4 mb-6">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 dark:text-slate-300 mb-1.5">
              Google Apps Script Web App URL
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                placeholder="https://script.google.com/macros/s/.../exec"
                className="flex-1 px-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
              <button
                type="button"
                onClick={handleTest}
                disabled={testing}
                className="px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-medium text-sm transition-colors flex items-center gap-1.5 disabled:opacity-50"
              >
                {testing ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Sparkles className="w-4 h-4" />}
                Test & Fetch
              </button>
            </div>
          </div>

          {testResult && (
            <div
              className={`p-3 rounded-xl border text-sm flex items-center gap-2 ${
                testResult.success
                  ? 'bg-emerald-50 border-emerald-200 text-emerald-800 dark:bg-emerald-950/40 dark:border-emerald-800/60 dark:text-emerald-200'
                  : 'bg-red-50 border-red-200 text-red-800 dark:bg-red-950/40 dark:border-red-800/60 dark:text-red-200'
              }`}
            >
              <div className="shrink-0">
                {testResult.success ? <Check className="w-5 h-5 text-emerald-600" /> : <X className="w-5 h-5 text-red-600" />}
              </div>
              <span>{testResult.msg}</span>
            </div>
          )}
        </div>

        {/* Setup Instructions Guide */}
        <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/60 mb-6 space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="font-semibold text-slate-900 dark:text-white text-sm flex items-center gap-2">
              <span>3-Step Quick Setup Guide</span>
            </h3>
            <button
              onClick={handleCopyCode}
              className="px-3 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white font-medium text-xs flex items-center gap-1.5 shadow-sm transition-colors"
            >
              {copied ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
              {copied ? 'Copied Code!' : 'Copy Apps Script Code'}
            </button>
          </div>

          <ol className="text-xs text-slate-600 dark:text-slate-300 space-y-2 list-decimal list-inside leading-relaxed">
            <li>
              Open your <strong>Google Sheet</strong> (or create a new blank Google Sheet).
            </li>
            <li>
              Click <strong>Extensions &rarr; Apps Script</strong> in the top menu of Google Sheets.
            </li>
            <li>
              Replace all default code with the <strong>copied code above</strong>, then click <strong>Deploy &rarr; New Deployment</strong> (select <strong>Web app</strong>, set <i>Execute as: Me</i> and <i>Who has access: Anyone</i>), and paste the Web App URL here!
            </li>
          </ol>
        </div>

        {/* Code Preview Box */}
        <div className="mb-6">
          <div className="flex items-center justify-between mb-1.5 text-xs font-semibold text-slate-600 dark:text-slate-400">
            <span>Google Apps Script Code (Code.gs)</span>
            <span className="text-[11px] text-slate-400">Auto-creates Holdings, SIPs & Committees sheets</span>
          </div>
          <pre className="p-3.5 rounded-xl bg-slate-950 text-slate-200 text-xs font-mono h-36 overflow-auto max-w-full whitespace-pre-wrap break-all sm:whitespace-pre border border-slate-800">
            {GOOGLE_APPS_SCRIPT_CODE}
          </pre>
        </div>

        {/* Footer Actions */}
        <div className="flex items-center justify-between pt-4 border-t border-slate-200 dark:border-slate-800">
          <button
            type="button"
            onClick={onSyncNow}
            disabled={isSyncing || !config.isConfigured}
            className="px-4 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-200 text-sm font-medium transition-colors flex items-center gap-2 disabled:opacity-40"
          >
            <RefreshCw className={`w-4 h-4 ${isSyncing ? 'animate-spin' : ''}`} />
            {isSyncing ? 'Pushing All Data...' : 'Push All Local Data to Sheet'}
          </button>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={onClose}
              className="py-2.5 px-4 rounded-xl border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              Close
            </button>
            <button
              type="button"
              onClick={handleSave}
              className="py-2.5 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-medium shadow-md shadow-emerald-500/20 transition-all"
            >
              Save Configuration
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
