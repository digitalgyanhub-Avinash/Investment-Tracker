export type InvestmentCategory = 
  | 'Stocks'
  | 'ETFs'
  | 'US Stocks & ETFs'
  | 'Bonds'
  | 'Mutual Fund'
  | 'Liquid Fund'
  | 'Gold'
  | 'Silver'
  | string;

export type PlatformName = 
  | 'Zerodha'
  | 'IndMoney'
  | 'Wint Wealth'
  | 'Groww'
  | 'JAR'
  | 'Paytm'
  | 'PhonePe'
  | 'AngelOne'
  | 'Upstox'
  | 'ICICI Direct'
  | 'HDFC Securities'
  | string;

export type SipFrequency = 'Daily' | 'Weekly' | 'Monthly' | 'Yearly';

export type IPOStatus = 'Applied' | 'Allotted' | 'Not Allotted' | 'Listed';
export type IPOCategory = 'Mainboard' | 'SME';

export interface IPOApplication {
  id: string;
  companyName: string;
  category: IPOCategory; // Mainboard or SME
  platform: PlatformName; // e.g. Zerodha, Groww, AngelOne
  applicantName?: string; // e.g. Self, Spouse, Father
  applicationNo?: string;
  
  // Bidding details
  sharesPerLot: number; // e.g. 15 shares
  cutOffPrice: number; // e.g. 500 per share
  lotsApplied: number; // e.g. 1 lot
  totalAmountBlocked: number; // e.g. 15 * 500 * 1 = 7500
  
  // Key Dates
  applyDate: string; // YYYY-MM-DD
  allotmentDate?: string;
  refundDate?: string;
  listingDate?: string;
  
  // Status & Financial Outcome
  status: IPOStatus;
  allottedLots?: number; // 0 if not allotted, >0 if allotted
  listingPrice?: number; // Price on listing/sale
  listingGainAmount?: number; // Net gain/loss in ₹
  listingGainPercent?: number; // Gain/loss %
  
  remarks?: string;
  updatedAt?: string;
}

export interface Holding {
  id: string;
  platform: PlatformName;
  category: InvestmentCategory;
  breakdown: number[]; // e.g. [17800, 6300, 9000, 9500, -2000, 15000]
  breakdownRaw?: string; // string representation if entered manually
  total: number;
  tillDate: string; // YYYY-MM-DD or DD-MM-YYYY
  remarks?: string;
  updatedAt?: string;
}

export interface SIP {
  id: string;
  platform: PlatformName;
  instrument: string; // e.g., 'SIP', 'Daily Gold', 'Daily Silver'
  amount: number;
  frequency: SipFrequency;
  since: string; // YYYY-MM-DD
  currentDate?: string; // auto-filled
  investedTillDate?: number; // auto-calculated
  remarks?: string;
  isPaused?: boolean;
  updatedAt?: string;
}

export interface CommitteeMonth {
  monthNumber: number; // 1, 2, ..., 12
  monthLabel?: string; // e.g. "Jan 2026"
  winningBid: number; // Discount bid e.g. 10000
  netPotValue: number; // Total Pot - winningBid e.g. 90000 - 10000 = 80000
  installmentPerPerson: number; // netPotValue / totalMembers e.g. 80000 / 12 = 6666.67
  isPaid: boolean;
  winnerName?: string;
  remarks?: string;
}

export interface Committee {
  id: string;
  name: string; // e.g. "Committee 1 (₹90,000)"
  totalAmount: number; // e.g. 90000
  totalMembers: number; // e.g. 12
  totalMonths: number; // e.g. 12
  startDate: string; // e.g. "2026-01-01" or "Jan 2026"
  baseMonthlyInstallment: number; // e.g. 7500
  currentMonth: number; // e.g. month 4 out of 12
  hasTakenPot?: boolean;
  potTakenMonth?: number;
  potTakenAmount?: number;
  months: CommitteeMonth[];
  remarks?: string;
  updatedAt?: string;
}

export interface GoogleSheetConfig {
  scriptUrl: string;
  sheetId?: string;
  isConfigured: boolean;
  lastSyncedAt?: string;
  autoSync: boolean;
}

export interface DeleteModalState {
  isOpen: boolean;
  type: 'holding' | 'sip' | 'committee' | 'ipo';
  item: Holding | SIP | Committee | IPOApplication | null;
}

export interface EditModalState {
  isOpen: boolean;
  type: 'holding' | 'sip' | 'committee' | 'ipo';
  mode: 'add' | 'edit';
  item: Holding | SIP | Committee | IPOApplication | null;
}
