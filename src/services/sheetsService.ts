import { Committee, GoogleSheetConfig, Holding, IPOApplication, SIP } from '../types';
import { normalizeToDdMmYyyy } from '../utils/calculations';

export interface SyncResponse {
  success: boolean;
  message?: string;
  holdings?: Holding[];
  sips?: SIP[];
  committees?: Committee[];
  ipos?: IPOApplication[];
}

function cleanHoldings(list?: Holding[]): Holding[] {
  if (!list) return [];
  return list.map(h => ({
    ...h,
    tillDate: normalizeToDdMmYyyy(h.tillDate),
  }));
}

function cleanSips(list?: SIP[]): SIP[] {
  if (!list) return [];
  return list.map(s => ({
    ...s,
    since: normalizeToDdMmYyyy(s.since),
  }));
}

function cleanCommittees(list?: Committee[]): Committee[] {
  if (!list) return [];
  return list.map(c => ({
    ...c,
    startDate: normalizeToDdMmYyyy(c.startDate),
  }));
}

function cleanIpos(list?: IPOApplication[]): IPOApplication[] {
  if (!list) return [];
  return list.map(i => ({
    ...i,
    applyDate: normalizeToDdMmYyyy(i.applyDate),
    allotmentDate: i.allotmentDate ? normalizeToDdMmYyyy(i.allotmentDate) : undefined,
    refundDate: i.refundDate ? normalizeToDdMmYyyy(i.refundDate) : undefined,
    listingDate: i.listingDate ? normalizeToDdMmYyyy(i.listingDate) : undefined,
  }));
}

/**
 * Executes a request to the Google Apps Script Web App URL
 */
async function callAppsScript(
  config: GoogleSheetConfig,
  method: 'GET' | 'POST',
  payload?: any
): Promise<any> {
  if (!config.scriptUrl || !config.scriptUrl.trim()) {
    throw new Error('Google Apps Script Web App URL is not configured.');
  }

  const scriptUrl = config.scriptUrl.trim();

  // Try server proxy first to avoid browser CORS redirects
  try {
    const proxyRes = await fetch('/api/sheets/proxy', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        scriptUrl,
        method,
        payload,
      }),
    });

    if (proxyRes.ok) {
      const result = await proxyRes.json();
      if (result.success && result.data) {
        return result.data;
      }
    }
  } catch (proxyErr) {
    console.warn('Proxy request failed, falling back to direct browser fetch:', proxyErr);
  }

  // Fallback to direct client fetch
  const fetchUrl = method === 'GET' && payload 
    ? `${scriptUrl}?${new URLSearchParams(payload).toString()}`
    : scriptUrl;

  const options: RequestInit = {
    method,
    headers: { 'Content-Type': 'application/json' },
  };

  if (method === 'POST' && payload) {
    options.body = JSON.stringify(payload);
  }

  const response = await fetch(fetchUrl, options);
  const json = await response.json();
  return json;
}

/**
 * Fetch current holdings, SIPs, and Committees from Google Sheets
 */
export async function fetchSheetData(config: GoogleSheetConfig): Promise<SyncResponse> {
  try {
    const data = await callAppsScript(config, 'GET');
    if (data && data.status === 'success') {
      return {
        success: true,
        holdings: cleanHoldings(data.holdings),
        sips: cleanSips(data.sips),
        committees: cleanCommittees(data.committees),
        ipos: cleanIpos(data.ipos),
      };
    }
    return { success: false, message: data.message || 'Failed to fetch data from sheet' };
  } catch (err: any) {
    return { success: false, message: err.message || 'Connection to Google Sheet failed' };
  }
}

/**
 * Save or edit a single holding entry in Google Sheet
 */
export async function saveHoldingToSheet(
  config: GoogleSheetConfig,
  holding: Holding,
  isEdit: boolean
): Promise<SyncResponse> {
  if (!config.isConfigured || !config.scriptUrl) {
    return { success: true }; // Local state updated, silent success
  }

  try {
    const action = isEdit ? 'updateHolding' : 'addHolding';
    const cleanHolding = { ...holding, tillDate: normalizeToDdMmYyyy(holding.tillDate) };
    const data = await callAppsScript(config, 'POST', {
      action,
      id: cleanHolding.id,
      data: cleanHolding,
    });

    if (data && data.status === 'success') {
      return {
        success: true,
        holdings: cleanHoldings(data.holdings),
        sips: cleanSips(data.sips),
        committees: cleanCommittees(data.committees),
      };
    }
    return { success: false, message: data.message || 'Failed to save holding to sheet' };
  } catch (err: any) {
    return { success: false, message: err.message || 'Error saving to sheet' };
  }
}

/**
 * Delete a holding entry from Google Sheet
 */
export async function deleteHoldingFromSheet(
  config: GoogleSheetConfig,
  holdingId: string
): Promise<SyncResponse> {
  if (!config.isConfigured || !config.scriptUrl) {
    return { success: true };
  }

  try {
    const data = await callAppsScript(config, 'POST', {
      action: 'deleteHolding',
      id: holdingId,
    });

    if (data && data.status === 'success') {
      return {
        success: true,
        holdings: data.holdings,
        sips: data.sips,
        committees: data.committees,
      };
    }
    return { success: false, message: data.message || 'Failed to delete holding from sheet' };
  } catch (err: any) {
    return { success: false, message: err.message || 'Error deleting from sheet' };
  }
}

/**
 * Save or edit a single SIP entry in Google Sheet
 */
export async function saveSipToSheet(
  config: GoogleSheetConfig,
  sip: SIP,
  isEdit: boolean
): Promise<SyncResponse> {
  if (!config.isConfigured || !config.scriptUrl) {
    return { success: true };
  }

  try {
    const action = isEdit ? 'updateSip' : 'addSip';
    const cleanSip = { ...sip, since: normalizeToDdMmYyyy(sip.since) };
    const data = await callAppsScript(config, 'POST', {
      action,
      id: cleanSip.id,
      data: cleanSip,
    });

    if (data && data.status === 'success') {
      return {
        success: true,
        holdings: cleanHoldings(data.holdings),
        sips: cleanSips(data.sips),
        committees: cleanCommittees(data.committees),
      };
    }
    return { success: false, message: data.message || 'Failed to save SIP to sheet' };
  } catch (err: any) {
    return { success: false, message: err.message || 'Error saving SIP to sheet' };
  }
}

/**
 * Delete a SIP entry from Google Sheet
 */
export async function deleteSipFromSheet(
  config: GoogleSheetConfig,
  sipId: string
): Promise<SyncResponse> {
  if (!config.isConfigured || !config.scriptUrl) {
    return { success: true };
  }

  try {
    const data = await callAppsScript(config, 'POST', {
      action: 'deleteSip',
      id: sipId,
    });

    if (data && data.status === 'success') {
      return {
        success: true,
        holdings: data.holdings,
        sips: data.sips,
        committees: data.committees,
      };
    }
    return { success: false, message: data.message || 'Failed to delete SIP from sheet' };
  } catch (err: any) {
    return { success: false, message: err.message || 'Error deleting SIP from sheet' };
  }
}

/**
 * Save or edit a single Committee entry in Google Sheet
 */
export async function saveCommitteeToSheet(
  config: GoogleSheetConfig,
  committee: Committee,
  isEdit: boolean
): Promise<SyncResponse> {
  if (!config.isConfigured || !config.scriptUrl) {
    return { success: true };
  }

  try {
    const action = isEdit ? 'updateCommittee' : 'addCommittee';
    const cleanCommittee = { ...committee, startDate: normalizeToDdMmYyyy(committee.startDate) };
    const data = await callAppsScript(config, 'POST', {
      action,
      id: cleanCommittee.id,
      data: cleanCommittee,
    });

    if (data && data.status === 'success') {
      return {
        success: true,
        holdings: cleanHoldings(data.holdings),
        sips: cleanSips(data.sips),
        committees: cleanCommittees(data.committees),
      };
    }
    return { success: false, message: data.message || 'Failed to save Committee to sheet' };
  } catch (err: any) {
    return { success: false, message: err.message || 'Error saving Committee to sheet' };
  }
}

/**
 * Delete a Committee entry from Google Sheet
 */
export async function deleteCommitteeFromSheet(
  config: GoogleSheetConfig,
  committeeId: string
): Promise<SyncResponse> {
  if (!config.isConfigured || !config.scriptUrl) {
    return { success: true };
  }

  try {
    const data = await callAppsScript(config, 'POST', {
      action: 'deleteCommittee',
      id: committeeId,
    });

    if (data && data.status === 'success') {
      return {
        success: true,
        holdings: data.holdings,
        sips: data.sips,
        committees: data.committees,
      };
    }
    return { success: false, message: data.message || 'Failed to delete Committee from sheet' };
  } catch (err: any) {
    return { success: false, message: err.message || 'Error deleting Committee from sheet' };
  }
}

/**
 * Save or edit a single IPO entry in Google Sheet
 */
export async function saveIpoToSheet(
  config: GoogleSheetConfig,
  ipo: IPOApplication,
  isEdit: boolean
): Promise<SyncResponse> {
  if (!config.isConfigured || !config.scriptUrl) {
    return { success: true };
  }

  try {
    const action = isEdit ? 'updateIpo' : 'addIpo';
    const cleanIpo = {
      ...ipo,
      applyDate: normalizeToDdMmYyyy(ipo.applyDate),
      allotmentDate: ipo.allotmentDate ? normalizeToDdMmYyyy(ipo.allotmentDate) : undefined,
      refundDate: ipo.refundDate ? normalizeToDdMmYyyy(ipo.refundDate) : undefined,
      listingDate: ipo.listingDate ? normalizeToDdMmYyyy(ipo.listingDate) : undefined,
    };
    const data = await callAppsScript(config, 'POST', {
      action,
      id: cleanIpo.id,
      data: cleanIpo,
    });

    if (data && data.status === 'success') {
      return {
        success: true,
        holdings: cleanHoldings(data.holdings),
        sips: cleanSips(data.sips),
        committees: cleanCommittees(data.committees),
        ipos: cleanIpos(data.ipos),
      };
    }
    return { success: false, message: data.message || 'Failed to save IPO to sheet' };
  } catch (err: any) {
    return { success: false, message: err.message || 'Error saving IPO to sheet' };
  }
}

/**
 * Delete an IPO entry from Google Sheet
 */
export async function deleteIpoFromSheet(
  config: GoogleSheetConfig,
  ipoId: string
): Promise<SyncResponse> {
  if (!config.isConfigured || !config.scriptUrl) {
    return { success: true };
  }

  try {
    const data = await callAppsScript(config, 'POST', {
      action: 'deleteIpo',
      id: ipoId,
    });

    if (data && data.status === 'success') {
      return {
        success: true,
        holdings: cleanHoldings(data.holdings),
        sips: cleanSips(data.sips),
        committees: cleanCommittees(data.committees),
        ipos: cleanIpos(data.ipos),
      };
    }
    return { success: false, message: data.message || 'Failed to delete IPO from sheet' };
  } catch (err: any) {
    return { success: false, message: err.message || 'Error deleting IPO from sheet' };
  }
}

/**
 * Full bidirectional sync (push all local holdings, SIPs, Committees, and IPOs to sheet)
 */
export async function syncAllToSheet(
  config: GoogleSheetConfig,
  holdings: Holding[],
  sips: SIP[],
  committees: Committee[] = [],
  ipos: IPOApplication[] = []
): Promise<SyncResponse> {
  if (!config.scriptUrl) {
    return { success: false, message: 'Google Apps Script URL is missing' };
  }

  try {
    const cleanHoldingsList = cleanHoldings(holdings);
    const cleanSipsList = cleanSips(sips);
    const cleanCommitteesList = cleanCommittees(committees);
    const cleanIposList = cleanIpos(ipos);

    const data = await callAppsScript(config, 'POST', {
      action: 'syncAll',
      holdings: cleanHoldingsList,
      sips: cleanSipsList,
      committees: cleanCommitteesList,
      ipos: cleanIposList,
    });

    if (data && data.status === 'success') {
      return {
        success: true,
        holdings: cleanHoldings(data.holdings || cleanHoldingsList),
        sips: cleanSips(data.sips || cleanSipsList),
        committees: cleanCommittees(data.committees || cleanCommitteesList),
        ipos: cleanIpos(data.ipos || cleanIposList),
      };
    }
    return { success: false, message: data.message || 'Failed full sync to sheet' };
  } catch (err: any) {
    return { success: false, message: err.message || 'Error executing full sync' };
  }
}
