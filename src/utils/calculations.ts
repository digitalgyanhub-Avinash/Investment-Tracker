import { Committee, Holding, IPOApplication, SIP, SipFrequency } from '../types';

export const MONTH_NAMES = [
  'January',
  'February',
  'March',
  'April',
  'May',
  'June',
  'July',
  'August',
  'September',
  'October',
  'November',
  'December',
];

export const SHORT_MONTH_NAMES = [
  'Jan',
  'Feb',
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
];

/**
 * Normalizes any date value (string, Date, number/Excel serial, ISO) into strict DD-MM-YYYY format.
 */
export function normalizeToDdMmYyyy(raw: any): string {
  if (raw === null || raw === undefined) return '';

  // 1. If it's a JavaScript Date object
  if (raw instanceof Date) {
    if (isNaN(raw.getTime())) return '';
    const d = String(raw.getDate()).padStart(2, '0');
    const m = String(raw.getMonth() + 1).padStart(2, '0');
    const y = raw.getFullYear();
    return `${d}-${m}-${y}`;
  }

  // 2. If it's an Excel / Sheets numeric serial date (e.g. 45500)
  if (typeof raw === 'number') {
    if (raw >= 20000 && raw <= 80000) {
      const date = new Date(Math.round((raw - 25569) * 86400 * 1000));
      if (!isNaN(date.getTime())) {
        const d = String(date.getUTCDate()).padStart(2, '0');
        const m = String(date.getUTCMonth() + 1).padStart(2, '0');
        const y = date.getUTCFullYear();
        return `${d}-${m}-${y}`;
      }
    }
  }

  let str = String(raw).trim();
  if (!str || str === '—' || str === '-' || str === 'N/A' || str.toLowerCase() === 'undefined' || str.toLowerCase() === 'null') {
    return '';
  }

  // Strip wrapping quotes
  str = str.replace(/^["']|["']$/g, '').trim();

  // 3. DD-MM-YYYY (or DD/MM/YYYY or DD.MM.YYYY), with optional time component
  const dmyMatch = str.match(/^(\d{1,2})[-/. ](\d{1,2})[-/. ](\d{4})(?:[ T].*)?$/);
  if (dmyMatch) {
    const part1 = Number(dmyMatch[1]);
    const part2 = Number(dmyMatch[2]);
    const year = dmyMatch[3];
    let day = part1;
    let month = part2;
    // If part1 > 12, part1 is day
    // If part2 > 12, part2 is day (e.g. US MM/DD/YYYY)
    if (part2 > 12) {
      day = part2;
      month = part1;
    }
    const dStr = String(day).padStart(2, '0');
    const mStr = String(month).padStart(2, '0');
    return `${dStr}-${mStr}-${year}`;
  }

  // 4. YYYY-MM-DD (or YYYY/MM/DD or YYYY.MM.DD), with optional time component
  const ymdMatch = str.match(/^(\d{4})[-/. ](\d{1,2})[-/. ](\d{1,2})(?:[ T].*)?$/);
  if (ymdMatch) {
    const year = ymdMatch[1];
    const month = String(Number(ymdMatch[2])).padStart(2, '0');
    const day = String(Number(ymdMatch[3])).padStart(2, '0');
    return `${day}-${month}-${year}`;
  }

  // 5. ISO-8601 string containing T (e.g. "2026-08-18T18:30:00.000Z")
  if (str.includes('T')) {
    try {
      const parsed = new Date(str);
      if (!isNaN(parsed.getTime())) {
        const d = String(parsed.getDate()).padStart(2, '0');
        const m = String(parsed.getMonth() + 1).padStart(2, '0');
        const y = parsed.getFullYear();
        return `${d}-${m}-${y}`;
      }
    } catch (_) {}
  }

  // 6. Textual month format: "19-Aug-2026", "19 August 2026", "19-August-2026"
  const textualMatch = str.match(/^(\d{1,2})[-/ ]([A-Za-z]+)[-/ ](\d{4})/);
  if (textualMatch) {
    const d = String(Number(textualMatch[1])).padStart(2, '0');
    const monStr = textualMatch[2].toLowerCase();
    const y = textualMatch[3];
    const mIdx = MONTH_NAMES.findIndex(
      (m) => m.toLowerCase().startsWith(monStr) || monStr.startsWith(m.toLowerCase().slice(0, 3))
    );
    if (mIdx !== -1) {
      const m = String(mIdx + 1).padStart(2, '0');
      return `${d}-${m}-${y}`;
    }
  }

  // 7. Month name first: "Aug 19, 2026" or "August 19, 2026"
  const monthFirstMatch = str.match(/^([A-Za-z]+)\s+(\d{1,2}),?\s+(\d{4})/);
  if (monthFirstMatch) {
    const monStr = monthFirstMatch[1].toLowerCase();
    const d = String(Number(monthFirstMatch[2])).padStart(2, '0');
    const y = monthFirstMatch[3];
    const mIdx = MONTH_NAMES.findIndex(
      (m) => m.toLowerCase().startsWith(monStr) || monStr.startsWith(m.toLowerCase().slice(0, 3))
    );
    if (mIdx !== -1) {
      const m = String(mIdx + 1).padStart(2, '0');
      return `${d}-${m}-${y}`;
    }
  }

  // 8. General fallback via Date parsing
  try {
    const fallback = new Date(str);
    if (!isNaN(fallback.getTime())) {
      const d = String(fallback.getDate()).padStart(2, '0');
      const m = String(fallback.getMonth() + 1).padStart(2, '0');
      const y = fallback.getFullYear();
      return `${d}-${m}-${y}`;
    }
  } catch (_) {}

  return str;
}

/**
 * Safely parse any date representation into YYYY-MM-DD format for HTML <input type="date">
 */
export function parseToIsoDate(dateVal: any): string {
  return toPickerDate(dateVal);
}

/**
 * Format any date string to DD-MM-YYYY format for UI presentation
 */
export function formatDisplayDate(dateVal: any): string {
  if (!dateVal) return '-';
  const norm = normalizeToDdMmYyyy(dateVal);
  return norm || '-';
}

/**
 * Calculate total capital currently blocked in active IPO applications ('Applied' status)
 */
export function calculateIposBlockedTotal(ipos: IPOApplication[]): number {
  if (!ipos) return 0;
  return ipos
    .filter((ipo) => ipo.status === 'Applied')
    .reduce((sum, ipo) => sum + (ipo.totalAmountBlocked || 0), 0);
}

/**
 * Calculate total listing gains realized from IPOs
 */
export function calculateIposTotalGain(ipos: IPOApplication[]): number {
  if (!ipos) return 0;
  return ipos.reduce((sum, ipo) => sum + (ipo.listingGainAmount || 0), 0);
}

/**
 * Calculate allotment success rate percentage
 */
export function calculateIposAllotmentRate(ipos: IPOApplication[]): {
  rate: number;
  allottedCount: number;
  decidedCount: number;
} {
  if (!ipos || ipos.length === 0) return { rate: 0, allottedCount: 0, decidedCount: 0 };
  const decided = ipos.filter((i) => i.status === 'Allotted' || i.status === 'Not Allotted' || i.status === 'Listed');
  const allotted = ipos.filter((i) => i.status === 'Allotted' || i.status === 'Listed');
  if (decided.length === 0) return { rate: 0, allottedCount: allotted.length, decidedCount: 0 };
  const rate = Math.round((allotted.length / decided.length) * 100);
  return { rate, allottedCount: allotted.length, decidedCount: decided.length };
}

/**
 * Calculate bid auction details for a Committee / Chit Fund
 * E.g., Amount = 90,000, Members = 12, Winning Bid (Discount) = 10,000
 * Net Pot Value = 80,000
 * Installment per person = 80,000 / 12 = 6,666.67
 */
export function calculateCommitteeBidDetails(
  totalAmount: number,
  totalMembers: number,
  winningBid: number
) {
  const members = totalMembers > 0 ? totalMembers : 1;
  const netPotValue = Math.max(0, totalAmount - winningBid);
  const installmentPerPerson = netPotValue / members;
  const baseInstallment = totalAmount / members;
  const discountPerPerson = (winningBid) / members;

  return {
    winningBid,
    netPotValue,
    installmentPerPerson: Math.round(installmentPerPerson),
    baseInstallment: Math.round(baseInstallment),
    discountPerPerson: Math.round(discountPerPerson),
  };
}

/**
 * Check if a committee tenure is completed (all totalMonths bids logged or paid)
 */
export function isCommitteeCompleted(committee: Committee): boolean {
  if (!committee) return false;
  const totalMonths = committee.totalMonths || 12;
  if (!committee.months || committee.months.length === 0) return false;
  const paidCount = committee.months.filter((m) => m.isPaid).length;
  return paidCount >= totalMonths;
}

/**
 * Parse a date string to milliseconds timestamp for accurate sorting
 */
export function parseDateToMs(dateStr: string): number {
  if (!dateStr) return 0;
  const d = parseDate(dateStr);
  return isNaN(d.getTime()) ? 0 : d.getTime();
}

/**
 * Calculate total actual amount invested till date in a Committee
 */
export function calculateCommitteeInvestedTillDate(committee: Committee): number {
  if (!committee) return 0;
  
  if (committee.months && committee.months.length > 0) {
    const paidMonths = committee.months.filter((m) => m.isPaid);
    if (paidMonths.length > 0) {
      return paidMonths.reduce((sum, m) => sum + (m.installmentPerPerson || 0), 0);
    }
  }

  // Fallback if months array is empty: currentMonth * baseMonthlyInstallment
  const base = committee.baseMonthlyInstallment || (committee.totalAmount / (committee.totalMembers || 12));
  return (committee.currentMonth || 1) * base;
}

/**
 * Calculate current monthly outflow for a Committee
 */
export function calculateCommitteeCurrentOutflow(committee: Committee): number {
  if (!committee) return 0;
  if (isCommitteeCompleted(committee)) return 0;

  if (committee.months && committee.months.length > 0) {
    // Priority: use the latest logged month's installmentPerPerson
    const latestMonth = committee.months[committee.months.length - 1];
    if (latestMonth && latestMonth.installmentPerPerson > 0) {
      return latestMonth.installmentPerPerson;
    }
  }

  const base = committee.baseMonthlyInstallment || (committee.totalAmount / (committee.totalMembers || 12));
  return base;
}

/**
 * Get the latest month name/label for a committee (e.g. "Aug 2026", "Sep 2026")
 */
export function getCommitteeLatestMonthLabel(committee: Committee): string {
  if (!committee) return '';

  if (committee.months && committee.months.length > 0) {
    const latestMonth = committee.months[committee.months.length - 1];
    let label = latestMonth.monthLabel?.trim();

    if (label) {
      return label;
    }

    if (committee.startDate) {
      const d = parseDate(committee.startDate);
      if (!isNaN(d.getTime())) {
        d.setMonth(d.getMonth() + (latestMonth.monthNumber - 1));
        return d.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
      }
    }
    return `Month ${latestMonth.monthNumber}`;
  }

  // If no month logs yet, compute based on currentMonth or startDate
  if (committee.startDate) {
    const d = parseDate(committee.startDate);
    if (!isNaN(d.getTime())) {
      d.setMonth(d.getMonth() + ((committee.currentMonth || 1) - 1));
      return d.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
    }
  }

  return `Month ${committee.currentMonth || 1}`;
}

/**
 * Calculate total dividends/discounts saved in a Committee
 */
export function calculateCommitteeTotalSaved(committee: Committee): number {
  if (!committee) return 0;
  const base = committee.baseMonthlyInstallment || (committee.totalAmount / (committee.totalMembers || 12));
  const paidMonthsCount = committee.months ? committee.months.filter((m) => m.isPaid).length : committee.currentMonth;
  const standardInvested = paidMonthsCount * base;
  const actualInvested = calculateCommitteeInvestedTillDate(committee);
  return Math.max(0, standardInvested - actualInvested);
}

/**
 * Sum total invested till date across all active (non-completed) committees
 */
export function calculateCommitteesTotal(committees: Committee[]): number {
  if (!committees) return 0;
  return committees
    .filter((c) => !isCommitteeCompleted(c))
    .reduce((sum, c) => sum + calculateCommitteeInvestedTillDate(c), 0);
}

/**
 * Sum current monthly outflow across all active (non-completed) committees
 */
export function calculateCommitteesMonthlyOutflow(committees: Committee[]): number {
  if (!committees) return 0;
  return committees
    .filter((c) => !isCommitteeCompleted(c))
    .reduce((sum, c) => sum + calculateCommitteeCurrentOutflow(c), 0);
}

/**
 * Calculate Net Profit / Loss for a Committee when the pot is taken
 */
export function calculateCommitteeProfitLoss(committee: Committee): number {
  if (!committee || !committee.hasTakenPot) return 0;
  let potAmount = committee.potTakenAmount || 0;
  if (potAmount <= 0 && committee.potTakenMonth !== undefined && committee.months) {
    const m = committee.months.find((item) => item.monthNumber === committee.potTakenMonth);
    if (m && m.netPotValue && m.netPotValue > 0) {
      potAmount = m.netPotValue;
    } else if (m && m.winningBid && m.winningBid > 0) {
      potAmount = Math.max(0, committee.totalAmount - m.winningBid);
    }
  }

  if (potAmount <= 0) return 0;

  const actualInvested = calculateCommitteeInvestedTillDate(committee);
  if (isCommitteeCompleted(committee)) {
    return potAmount - actualInvested;
  }

  const totalMonths = committee.totalMonths || 12;
  const base = committee.baseMonthlyInstallment || (committee.totalAmount / (committee.totalMembers || 12));
  const paidMonthsCount = committee.months ? committee.months.filter((m) => m.isPaid).length : (committee.currentMonth || 1);
  const remainingMonths = Math.max(0, totalMonths - paidMonthsCount);

  const unpaidLoggedMonths = committee.months ? committee.months.filter((m) => !m.isPaid) : [];
  const unpaidLoggedSum = unpaidLoggedMonths.reduce((sum, m) => sum + (m.installmentPerPerson || base), 0);
  const unloggedRemainingCount = Math.max(0, remainingMonths - unpaidLoggedMonths.length);
  const totalRemainingExpected = unpaidLoggedSum + (unloggedRemainingCount * base);

  const totalExpectedInvestment = actualInvested + totalRemainingExpected;
  return potAmount - totalExpectedInvestment;
}

/**
 * Format numbers as Indian standard currency string (e.g. ₹67,000)
 */
export function formatCurrency(amount: number): string {
  if (isNaN(amount)) return '₹0';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(amount);
}

/**
 * Parses raw date string into a JS Date object safely.
 * Accepts formats: DD-MM-YYYY, YYYY-MM-DD, ISO, Excel serial, etc.
 */
export function parseDate(dateStr: string): Date {
  if (!dateStr) return new Date();

  const norm = normalizeToDdMmYyyy(dateStr);
  const match = norm.match(/^(\d{2})-(\d{2})-(\d{4})$/);
  if (match) {
    const d = Number(match[1]);
    const m = Number(match[2]);
    const y = Number(match[3]);
    return new Date(y, m - 1, d);
  }

  const d = new Date(dateStr);
  return isNaN(d.getTime()) ? new Date() : d;
}

/**
 * Formats a Date object, defaults to DD-MM-YYYY
 */
export function formatDate(date: Date, format: 'DD-MM-YYYY' | 'YYYY-MM-DD' | 'DD-MMM-YYYY' = 'DD-MM-YYYY'): string {
  const day = String(date.getDate()).padStart(2, '0');
  const monthNum = String(date.getMonth() + 1).padStart(2, '0');
  const monthShort = SHORT_MONTH_NAMES[date.getMonth()];
  const year = date.getFullYear();

  if (format === 'YYYY-MM-DD') {
    return `${year}-${monthNum}-${day}`;
  }
  if (format === 'DD-MMM-YYYY') {
    return `${day}-${monthShort}-${year}`;
  }
  return `${day}-${monthNum}-${year}`;
}

/**
 * Converts any date string to YYYY-MM-DD for HTML <input type="date">
 */
export function toPickerDate(dateStr: string): string {
  if (!dateStr) return '';
  const norm = normalizeToDdMmYyyy(dateStr);
  const match = norm.match(/^(\d{2})-(\d{2})-(\d{4})$/);
  if (match) {
    return `${match[3]}-${match[2]}-${match[1]}`;
  }
  const parsed = parseDate(dateStr);
  return formatDate(parsed, 'YYYY-MM-DD');
}

/**
 * Converts any date string to canonical DD-MM-YYYY format
 */
export function toStandardDate(dateStr: string): string {
  if (!dateStr || dateStr === '—' || dateStr === '-') return '—';
  const norm = normalizeToDdMmYyyy(dateStr);
  return norm || '—';
}

/**
 * Calculate total invested till date for a SIP
 * based on 'since' date, today's date, frequency, and recurring amount.
 */
export function calculateSipInvested(sip: SIP, targetDateStr?: string): number {
  if (!sip.since || !sip.amount || sip.amount <= 0) return 0;

  const sinceDate = parseDate(sip.since);
  const endDate = targetDateStr ? parseDate(targetDateStr) : new Date();

  if (sinceDate > endDate) return 0;

  const diffTime = endDate.getTime() - sinceDate.getTime();
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24)) + 1; // inclusive of start day

  const freq = (sip.frequency || 'Monthly').toLowerCase() as Lowercase<SipFrequency>;

  switch (freq) {
    case 'daily': {
      return diffDays * sip.amount;
    }
    case 'weekly': {
      const diffWeeks = Math.floor(diffDays / 7) + 1;
      return diffWeeks * sip.amount;
    }
    case 'monthly': {
      const yearDiff = endDate.getFullYear() - sinceDate.getFullYear();
      const monthDiff = endDate.getMonth() - sinceDate.getMonth();
      let totalMonths = yearDiff * 12 + monthDiff;
      // If end day >= since day, count current month instalment
      if (endDate.getDate() >= sinceDate.getDate()) {
        totalMonths += 1;
      }
      return Math.max(1, totalMonths) * sip.amount;
    }
    case 'yearly': {
      const yearDiff = endDate.getFullYear() - sinceDate.getFullYear() + 1;
      return Math.max(1, yearDiff) * sip.amount;
    }
    default:
      return sip.amount;
  }
}

/**
 * Parse breakdown input string (e.g. "17.8K+6.3K+9K+9.5K-2K+15K" or "17800, 6300, 9000, 9500, -2000, 15000") into number array
 */
export function parseBreakdownString(input: string): number[] {
  if (!input || !input.trim()) return [];

  // Normalize K/k suffix (e.g. 17.8K -> 17800, 5k -> 5000)
  let normalized = input.replace(/([\d.]+)\s*k/gi, (_, num) => {
    return String(Math.round(parseFloat(num) * 1000));
  });

  // Convert '+' to ',' and '-' to ',-' so signed numbers are separated properly
  normalized = normalized.replace(/\+/g, ',');
  normalized = normalized.replace(/-(?=\d)/g, ',-');

  return normalized
    .split(/[\n,]+/)
    .map((s) => s.trim())
    .filter((s) => s !== '' && !isNaN(Number(s)))
    .map((s) => Number(s));
}

/**
 * Sum numbers in a breakdown array
 */
export function sumBreakdown(breakdown: number[]): number {
  if (!breakdown || breakdown.length === 0) return 0;
  return breakdown.reduce((acc, val) => acc + val, 0);
}

/**
 * Calculate total portfolio holdings sum
 */
export function calculateHoldingsTotal(holdings: Holding[]): number {
  return holdings.reduce((sum, h) => sum + (h.total || 0), 0);
}

/**
 * Calculate total SIP invested till date sum across all active SIPs
 */
export function calculateSipsTotal(sips: SIP[]): number {
  return sips.reduce((sum, s) => sum + calculateSipInvested(s), 0);
}

/**
 * Calculate monthly SIP commitment sum
 */
export function calculateSipMonthlyCommitment(sips: SIP[]): number {
  return sips.reduce((sum, s) => {
    const freq = (s.frequency || 'Monthly').toLowerCase();
    if (freq === 'monthly') return sum + s.amount;
    if (freq === 'daily') return sum + s.amount * 30; // approx
    if (freq === 'weekly') return sum + s.amount * 4.33;
    if (freq === 'yearly') return sum + s.amount / 12;
    return sum + s.amount;
  }, 0);
}
