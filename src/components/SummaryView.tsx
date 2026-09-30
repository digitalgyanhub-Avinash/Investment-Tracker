import React, { useState, useEffect } from 'react';
import {
  Cell,
  Bar,
  BarChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import {
  Edit2,
  RotateCcw,
  Check,
  Plus,
  Trash2,
  Info,
  TrendingUp,
  Target,
  ShieldCheck,
  Award,
  Zap,
  Calendar,
  DollarSign,
  PieChart,
  ArrowUpRight,
  Sliders,
  CheckCircle2,
  AlertTriangle,
  Repeat,
  Building2,
  Users,
} from 'lucide-react';
import { Committee, Holding, IPOApplication, SIP } from '../types';
import {
  calculateCommitteesMonthlyOutflow,
  calculateCommitteesTotal,
  calculateHoldingsTotal,
  calculateIposBlockedTotal,
  calculateSipMonthlyCommitment,
  calculateSipInvested,
  calculateSipsTotal,
  formatCurrency,
  isCommitteeCompleted,
} from '../utils/calculations';

interface SummaryViewProps {
  holdings: Holding[];
  sips: SIP[];
  ipos?: IPOApplication[];
  committees?: Committee[];
}

const COLORS = [
  '#10B981', // Emerald
  '#3B82F6', // Blue
  '#F59E0B', // Amber
  '#8B5CF6', // Purple
  '#EC4899', // Pink
  '#06B6D4', // Cyan
  '#6366F1', // Indigo
  '#F97316', // Orange
  '#14B8A6', // Teal
  '#EAB308', // Yellow
];

/**
 * Normalizes raw category/instrument names into unified standard buckets:
 * - Stocks
 * - ETFs
 * - US Stocks
 * - Bonds
 * - Mutual Fund (combining Mutual Fund + Liquid Fund)
 * - Gold
 * - Silver
 */
export function normalizeCategoryName(raw: string): string {
  if (!raw) return 'Others';
  const name = raw.trim();
  const lower = name.toLowerCase();

  // Combine Mutual Fund and Liquid Fund together into "Mutual Fund"
  if (
    lower.includes('mutual') ||
    lower.includes('liquid') ||
    lower === 'mf' ||
    lower === 'sip' ||
    lower.includes('mutual fund')
  ) {
    return 'Mutual Fund';
  }

  // US Stocks & ETFs
  if (
    lower.includes('us stock') ||
    lower.includes('us stocks') ||
    lower.includes('us equities')
  ) {
    return 'US Stocks';
  }

  // Stocks
  if (lower === 'stocks' || lower === 'stock' || lower === 'stock sip') {
    return 'Stocks';
  }

  // IPOs
  if (lower.includes('ipo')) {
    return 'IPOs';
  }

  // ETFs
  if (lower === 'etfs' || lower === 'etf') {
    return 'ETFs';
  }

  // Bonds / Fixed Deposits
  if (
    lower.includes('bond') ||
    lower.includes('fixed deposit') ||
    lower === 'fd' ||
    lower.includes('debt')
  ) {
    return 'Bonds';
  }

  // Gold
  if (lower.includes('gold') || lower.includes('sgb')) {
    return 'Gold';
  }

  // Silver
  if (lower.includes('silver')) {
    return 'Silver';
  }

  // Committees / Chit Funds
  if (lower.includes('committee') || lower.includes('chit') || lower.includes('bidding')) {
    return 'Committees';
  }

  return name;
}

// Preset Financial Milestone Targets
const DEFAULT_MILESTONES = [
  { label: '₹1 Lakh', target: 100000 },
  { label: '₹5 Lakhs', target: 500000 },
  { label: '₹10 Lakhs', target: 1000000 },
  { label: '₹25 Lakhs', target: 2500000 },
  { label: '₹50 Lakhs', target: 5000000 },
  { label: '₹1 Crore', target: 10000000 },
  { label: '₹2.5 Crores', target: 25000000 },
  { label: '₹5 Crores', target: 50000000 },
];

export const SummaryView: React.FC<SummaryViewProps> = ({
  holdings,
  sips,
  ipos = [],
  committees = [],
}) => {
  const totalHoldings = calculateHoldingsTotal(holdings);
  const totalSipsCalculated = calculateSipsTotal(sips);
  const totalCommitteesCalculated = calculateCommitteesTotal(committees);
  const totalIposBlocked = calculateIposBlockedTotal(ipos);
  const totalWealthCalculated = totalHoldings + totalSipsCalculated + totalCommitteesCalculated;
  const monthlySipOutflow = calculateSipMonthlyCommitment(sips);
  const monthlyCommitteeOutflow = calculateCommitteesMonthlyOutflow(committees);
  const totalMonthlyOutflow = monthlySipOutflow + monthlyCommitteeOutflow;

  // Projection Return Rate State (Default 12% p.a.)
  const [returnRate, setReturnRate] = useState<number>(12);

  // Custom Target Milestone State
  const [customGoal, setCustomGoal] = useState<number>(() => {
    const saved = localStorage.getItem('inv_custom_wealth_goal');
    return saved ? Number(saved) : 5000000; // Default ₹50 Lakhs
  });
  const [isEditingGoal, setIsEditingGoal] = useState(false);
  const [tempGoalInput, setTempGoalInput] = useState(String(customGoal));

  // Category overrides state (stored in localStorage)
  const [categoryOverrides, setCategoryOverrides] = useState<Record<string, number>>(() => {
    try {
      const saved = localStorage.getItem('inv_category_overrides');
      return saved ? JSON.parse(saved) : {};
    } catch {
      return {};
    }
  });

  // Custom added categories state
  const [customCategories, setCustomCategories] = useState<string[]>(() => {
    try {
      const saved = localStorage.getItem('inv_custom_categories');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  // Toggle edit mode for category allocations
  const [isEditingCategories, setIsEditingCategories] = useState(false);
  const [editingValues, setEditingValues] = useState<Record<string, string>>({});
  const [newCatName, setNewCatName] = useState('');
  const [newCatAmount, setNewCatAmount] = useState('');

  // Save Custom Goal
  useEffect(() => {
    localStorage.setItem('inv_custom_wealth_goal', String(customGoal));
  }, [customGoal]);

  // Persist categoryOverrides
  useEffect(() => {
    localStorage.setItem('inv_category_overrides', JSON.stringify(categoryOverrides));
  }, [categoryOverrides]);

  // Persist customCategories
  useEffect(() => {
    localStorage.setItem('inv_custom_categories', JSON.stringify(customCategories));
  }, [customCategories]);

  // Aggregate by Platform
  const platformMap: Record<string, number> = {};
  holdings.forEach((h) => {
    platformMap[h.platform] = (platformMap[h.platform] || 0) + (h.total || 0);
  });
  sips.forEach((s) => {
    const inv = calculateSipInvested(s);
    platformMap[s.platform] = (platformMap[s.platform] || 0) + inv;
  });
  if (committees && committees.length > 0) {
    const commTotal = calculateCommitteesTotal(committees);
    if (commTotal > 0) {
      platformMap['Committees'] = (platformMap['Committees'] || 0) + commTotal;
    }
  }

  const platformData = Object.entries(platformMap).map(([name, value]) => ({
    name,
    value,
  }));

  // Aggregate by Standard Categories
  const defaultStandardCategories = [
    'Stocks',
    'ETFs',
    'US Stocks',
    'Bonds',
    'Mutual Fund',
    'Gold',
    'Silver',
    'Committees',
  ];

  const lumpsumCategoryMap: Record<string, number> = {};
  holdings.forEach((h) => {
    const norm = normalizeCategoryName(h.category);
    lumpsumCategoryMap[norm] = (lumpsumCategoryMap[norm] || 0) + (h.total || 0);
  });

  const sipCategoryMap: Record<string, number> = {};
  sips.forEach((s) => {
    const inv = calculateSipInvested(s);
    const norm = normalizeCategoryName(s.instrument);
    sipCategoryMap[norm] = (sipCategoryMap[norm] || 0) + inv;
  });

  if (committees && committees.length > 0) {
    const commTotal = calculateCommitteesTotal(committees);
    lumpsumCategoryMap['Committees'] = (lumpsumCategoryMap['Committees'] || 0) + commTotal;
  }

  const allCategoryKeys = Array.from(
    new Set([
      ...defaultStandardCategories,
      ...Object.keys(lumpsumCategoryMap),
      ...Object.keys(sipCategoryMap),
      ...customCategories,
    ])
  );

  const categoryBreakdownList = allCategoryKeys.map((cat) => {
    const lumpsum = lumpsumCategoryMap[cat] || 0;
    const sip = sipCategoryMap[cat] || 0;
    const calculatedTotal = lumpsum + sip;
    const override = categoryOverrides[cat];
    const displayTotal = override !== undefined ? override : calculatedTotal;

    return {
      name: cat,
      lumpsum,
      sip,
      calculatedTotal,
      isOverridden: override !== undefined,
      displayTotal,
    };
  });

  categoryBreakdownList.sort((a, b) => b.displayTotal - a.displayTotal);
  const totalCategoryWealth = categoryBreakdownList.reduce((sum, item) => sum + item.displayTotal, 0);
  const currentTotalWealth = totalCategoryWealth > 0 ? totalCategoryWealth : totalWealthCalculated;

  const categoryChartData = categoryBreakdownList.map((item) => ({
    name: item.name,
    value: item.displayTotal,
    lumpsum: item.lumpsum,
    sip: item.sip,
    calculatedTotal: item.calculatedTotal,
    isOverridden: item.isOverridden,
  }));

  // Category Edit Handlers
  const handleStartEdit = () => {
    const initialInputs: Record<string, string> = {};
    categoryBreakdownList.forEach((item) => {
      initialInputs[item.name] = String(item.displayTotal);
    });
    setEditingValues(initialInputs);
    setIsEditingCategories(true);
  };

  const handleSaveEdit = () => {
    const updated: Record<string, number> = { ...categoryOverrides };
    categoryBreakdownList.forEach((item) => {
      const valStr = editingValues[item.name];
      if (valStr !== undefined && valStr !== '') {
        const num = Number(valStr);
        if (!isNaN(num)) {
          updated[item.name] = num;
        }
      }
    });
    setCategoryOverrides(updated);
    setIsEditingCategories(false);
  };

  const handleResetCategory = (catName: string) => {
    const updated = { ...categoryOverrides };
    delete updated[catName];
    setCategoryOverrides(updated);

    const item = categoryBreakdownList.find((c) => c.name === catName);
    if (item) {
      setEditingValues((prev) => ({
        ...prev,
        [catName]: String(item.calculatedTotal),
      }));
    }
  };

  const handleResetAllCategories = () => {
    setCategoryOverrides({});
    setIsEditingCategories(false);
  };

  const handleAddCustomCategory = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCatName.trim()) return;
    const catName = newCatName.trim();
    if (!customCategories.includes(catName)) {
      setCustomCategories((prev) => [...prev, catName]);
    }
    if (newCatAmount !== '') {
      const amt = Number(newCatAmount);
      if (!isNaN(amt)) {
        setCategoryOverrides((prev) => ({ ...prev, [catName]: amt }));
      }
    }
    setNewCatName('');
    setNewCatAmount('');
  };

  const handleDeleteCustomCategory = (catName: string) => {
    setCustomCategories((prev) => prev.filter((c) => c !== catName));
    const updated = { ...categoryOverrides };
    delete updated[catName];
    setCategoryOverrides(updated);
  };

  // -------------------------------------------------------------
  // NEW FEATURE 1: Wealth Milestone Analysis & Target Tracking
  // -------------------------------------------------------------
  const nextMilestone = DEFAULT_MILESTONES.find((m) => m.target > currentTotalWealth) || {
    label: formatCurrency(customGoal),
    target: customGoal,
  };
  const activeTargetGoal = isEditingGoal ? Number(tempGoalInput) || customGoal : customGoal;
  const milestoneProgressPct = Math.min(100, Math.max(0, (currentTotalWealth / activeTargetGoal) * 100));
  const remainingToGoal = Math.max(0, activeTargetGoal - currentTotalWealth);
  
  // Estimate months to reach goal based on SIP rate + 12% compound growth
  const monthlyInterestRate = returnRate / 100 / 12;
  let estimatedMonthsToGoal = 0;
  if (remainingToGoal > 0) {
    if (totalMonthlyOutflow > 0) {
      // Future Value formula solving for n: FV = P*(1+i)^n + PMT*(((1+i)^n - 1)/i)
      let simulatedWealth = currentTotalWealth;
      while (simulatedWealth < activeTargetGoal && estimatedMonthsToGoal < 600) {
        simulatedWealth = simulatedWealth * (1 + monthlyInterestRate) + totalMonthlyOutflow;
        estimatedMonthsToGoal++;
      }
    } else {
      estimatedMonthsToGoal = Math.round(remainingToGoal / 25000); // placeholder
    }
  }

  // -------------------------------------------------------------
  // NEW FEATURE 2: Portfolio Health & Diversification Rating
  // -------------------------------------------------------------
  let healthScore = 100;
  const healthInsights: { title: string; desc: string; type: 'success' | 'warning' | 'info' }[] = [];

  // Concentration Check across platforms
  const platformEntries = Object.entries(platformMap);
  let maxPlatformPct = 0;
  let dominantPlatform = '';
  platformEntries.forEach(([pName, pVal]) => {
    const pct = (pVal / currentTotalWealth) * 100;
    if (pct > maxPlatformPct) {
      maxPlatformPct = pct;
      dominantPlatform = pName;
    }
  });

  if (maxPlatformPct > 50) {
    healthScore -= 15;
    healthInsights.push({
      title: 'High Single-Platform Concentration',
      desc: `${dominantPlatform} holds ${maxPlatformPct.toFixed(1)}% of total wealth. Consider spreading capital across multiple brokers/platforms.`,
      type: 'warning',
    });
  } else if (maxPlatformPct > 0) {
    healthInsights.push({
      title: 'Healthy Platform Distribution',
      desc: `Capital is distributed across ${platformEntries.length} active platforms. Highest allocation is ${dominantPlatform} (${maxPlatformPct.toFixed(1)}%).`,
      type: 'success',
    });
  }

  // SIP Outflow Velocity Check
  if (totalMonthlyOutflow > 0) {
    healthInsights.push({
      title: 'Strong Wealth Building Velocity',
      desc: `Automatic monthly commitment of ${formatCurrency(totalMonthlyOutflow)}/mo (${formatCurrency(monthlySipOutflow)} SIPs + ${formatCurrency(monthlyCommitteeOutflow)} Committees) builds disciplined net worth compounding.`,
      type: 'success',
    });
  } else {
    healthScore -= 10;
    healthInsights.push({
      title: 'No Active Monthly SIP Outflow',
      desc: 'No monthly SIPs or committee investments detected. Adding recurring monthly investments significantly boosts long-term compound growth.',
      type: 'warning',
    });
  }

  // Asset Class Balance Check
  const mfVal = categoryBreakdownList.find((c) => c.name === 'Mutual Fund')?.displayTotal || 0;
  const stockVal = categoryBreakdownList.find((c) => c.name === 'Stocks')?.displayTotal || 0;
  const goldVal = (categoryBreakdownList.find((c) => c.name === 'Gold')?.displayTotal || 0) + (categoryBreakdownList.find((c) => c.name === 'Silver')?.displayTotal || 0);

  if (goldVal > 0) {
    healthInsights.push({
      title: 'Precious Metals Hedge Active',
      desc: `Gold & Silver allocations total ${formatCurrency(goldVal)} (${((goldVal / currentTotalWealth) * 100).toFixed(1)}%), providing downside inflation protection.`,
      type: 'info',
    });
  }

  // -------------------------------------------------------------
  // NEW FEATURE 3: Multi-Year Compound Wealth Growth Projection Calculator
  // -------------------------------------------------------------
  const calculateCompoundProjection = (years: number) => {
    const months = years * 12;
    const r = returnRate / 100 / 12;
    
    // Future Value of Initial Principal
    const fvPrincipal = currentTotalWealth * Math.pow(1 + r, months);
    
    // Future Value of Monthly SIP Series
    let fvSip = 0;
    if (r > 0 && totalMonthlyOutflow > 0) {
      fvSip = totalMonthlyOutflow * ((Math.pow(1 + r, months) - 1) / r) * (1 + r);
    } else if (totalMonthlyOutflow > 0) {
      fvSip = totalMonthlyOutflow * months;
    }

    const totalProjected = fvPrincipal + fvSip;
    const totalInvested = currentTotalWealth + totalMonthlyOutflow * months;
    const estimatedReturns = Math.max(0, totalProjected - totalInvested);

    return {
      years,
      totalProjected,
      totalInvested,
      estimatedReturns,
    };
  };

  const projection3Y = calculateCompoundProjection(3);
  const projection5Y = calculateCompoundProjection(5);
  const projection10Y = calculateCompoundProjection(10);
  const projection15Y = calculateCompoundProjection(15);

  const appliedIposCount = ipos.filter((i) => i.status === 'Applied').length;
  const totalIposBlockedAmount = calculateIposBlockedTotal(ipos);
  const activeCommitteesCount = committees.filter((c) => !isCommitteeCompleted(c)).length;

  return (
    <div className="space-y-4">
      {/* SECTION 1: Top Core Wealth Key Performance Indicators */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-2">
        <div className="p-2.5 sm:p-3 rounded-xl glass-panel relative overflow-hidden group hover:scale-[1.01] transition-all">
          <div className="flex items-center justify-between mb-0.5">
            <span className="text-[9px] sm:text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Total Net Portfolio
            </span>
            <div className="p-1 rounded-md bg-emerald-500/20 text-emerald-600 dark:text-emerald-400">
              <TrendingUp className="w-3 h-3 sm:w-3.5 sm:h-3.5" />
            </div>
          </div>
          <p className="text-sm sm:text-base font-extrabold text-slate-900 dark:text-white">
            {formatCurrency(currentTotalWealth)}
          </p>
          <p className="text-[9px] font-semibold text-emerald-600 dark:text-emerald-400 mt-0.5 flex items-center gap-1">
            <ArrowUpRight className="w-2.5 h-2.5" /> Lumpsum + SIPs + Committees
          </p>
        </div>

        <div className="p-2.5 sm:p-3 rounded-xl glass-panel relative overflow-hidden group hover:scale-[1.01] transition-all">
          <div className="flex items-center justify-between mb-0.5">
            <span className="text-[9px] sm:text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Holdings Lumpsum
            </span>
            <div className="p-1 rounded-md bg-blue-500/20 text-blue-600 dark:text-blue-400">
              <PieChart className="w-3 h-3 sm:w-3.5 sm:h-3.5" />
            </div>
          </div>
          <p className="text-sm sm:text-base font-extrabold text-slate-900 dark:text-white">
            {formatCurrency(totalHoldings)}
          </p>
          <span className="text-[9px] font-semibold text-slate-500 dark:text-slate-400 mt-0.5 block">
            {holdings.length} Active Records
          </span>
        </div>

        <div className="p-2.5 sm:p-3 rounded-xl glass-panel relative overflow-hidden group hover:scale-[1.01] transition-all">
          <div className="flex items-center justify-between mb-0.5">
            <span className="text-[9px] sm:text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              SIP Invested Value
            </span>
            <div className="p-1 rounded-md bg-indigo-500/20 text-indigo-600 dark:text-indigo-400">
              <Repeat className="w-3 h-3 sm:w-3.5 sm:h-3.5" />
            </div>
          </div>
          <p className="text-sm sm:text-base font-extrabold text-indigo-600 dark:text-indigo-400">
            {formatCurrency(totalSipsCalculated)}
          </p>
          <span className="text-[9px] font-semibold text-indigo-600 dark:text-indigo-300 mt-0.5 block">
            {formatCurrency(monthlySipOutflow)}/mo outflow
          </span>
        </div>

        <div className="p-2.5 sm:p-3 rounded-xl glass-panel relative overflow-hidden group hover:scale-[1.01] transition-all">
          <div className="flex items-center justify-between mb-0.5">
            <span className="text-[9px] sm:text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Committees Net Total
            </span>
            <div className="p-1 rounded-md bg-purple-500/20 text-purple-600 dark:text-purple-400">
              <Users className="w-3 h-3 sm:w-3.5 sm:h-3.5" />
            </div>
          </div>
          <p className="text-sm sm:text-base font-extrabold text-purple-600 dark:text-purple-400">
            {formatCurrency(totalCommitteesCalculated)}
          </p>
          <span className="text-[9px] font-semibold text-purple-600 dark:text-purple-300 mt-0.5 block">
            {activeCommitteesCount} Active Groups
          </span>
        </div>

        {/* IPOs Box */}
        <div className="p-2.5 sm:p-3 rounded-xl glass-panel relative overflow-hidden group hover:scale-[1.01] transition-all">
          <div className="flex items-center justify-between mb-0.5">
            <span className="text-[9px] sm:text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              IPOs Applied
            </span>
            <div className="p-1 rounded-md bg-rose-500/20 text-rose-600 dark:text-rose-400">
              <Building2 className="w-3 h-3 sm:w-3.5 sm:h-3.5" />
            </div>
          </div>
          <p className="text-sm sm:text-base font-extrabold text-rose-600 dark:text-rose-400">
            {formatCurrency(totalIposBlockedAmount)}
          </p>
          <span className="text-[9px] font-semibold text-slate-500 dark:text-slate-400 mt-0.5 block">
            {appliedIposCount} applied of {ipos.length} IPOs
          </span>
        </div>

        <div className="p-2.5 sm:p-3 rounded-xl glass-panel relative overflow-hidden group hover:scale-[1.01] transition-all">
          <div className="flex items-center justify-between mb-0.5">
            <span className="text-[9px] sm:text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              Monthly Outflow
            </span>
            <div className="p-1 rounded-md bg-amber-500/20 text-amber-600 dark:text-amber-400">
              <Zap className="w-3 h-3 sm:w-3.5 sm:h-3.5" />
            </div>
          </div>
          <p className="text-sm sm:text-base font-extrabold text-amber-600 dark:text-amber-400">
            {formatCurrency(totalMonthlyOutflow)}
          </p>
          <span className="text-[9px] font-semibold text-slate-500 dark:text-slate-400 mt-0.5 block">
            Monthly Commitment
          </span>
        </div>
      </div>

      {/* SECTION 2: Platform & Category Distribution Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-3">
        {/* Platform Allocation Boxy Chart & Grid */}
        <div className="p-3 sm:p-3.5 rounded-xl glass-panel flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-xs sm:text-sm font-bold text-slate-900 dark:text-white">
                Allocation by Platform
              </h3>
              <span className="text-[10px] font-mono px-1.5 py-0.5 rounded-md glass-card text-slate-600 dark:text-slate-300">
                Box View ({platformData.length} Platforms)
              </span>
            </div>

            {/* Boxy Bar Chart */}
            <div className="h-40 w-full mb-2">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={platformData} margin={{ top: 10, right: 10, left: 0, bottom: 20 }}>
                  <XAxis dataKey="name" tick={{ fontSize: 10 }} />
                  <YAxis tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}k`} tick={{ fontSize: 10 }} />
                  <Tooltip
                    formatter={(val: any) => formatCurrency(Number(val))}
                    labelStyle={{ fontWeight: 'bold', fontSize: '11px' }}
                  />
                  <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                    {platformData.map((_, index) => (
                      <Cell key={`bar-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Boxy Platform Percentage Progress Bars Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 pt-2 border-t border-white/30 dark:border-white/10">
            {platformData.map((item, index) => {
              const pct = currentTotalWealth > 0 ? (item.value / currentTotalWealth) * 100 : 0;
              const barColor = COLORS[index % COLORS.length];
              return (
                <div
                  key={item.name}
                  className="p-2 rounded-lg glass-card flex flex-col justify-between space-y-1"
                >
                  <div className="flex items-center justify-between text-[11px]">
                    <span className="font-bold text-slate-800 dark:text-slate-200 truncate pr-1">
                      {item.name}
                    </span>
                    <span className="font-mono text-[10px] text-slate-500 font-semibold">
                      {pct.toFixed(1)}%
                    </span>
                  </div>
                  <div className="w-full h-1.5 bg-slate-200/60 dark:bg-slate-700/60 rounded-xs overflow-hidden">
                    <div
                      className="h-full rounded-xs transition-all duration-500"
                      style={{ width: `${Math.min(100, Math.max(3, pct))}%`, backgroundColor: barColor }}
                    />
                  </div>
                  <span className="text-[11px] font-semibold text-slate-700 dark:text-slate-300">
                    {formatCurrency(item.value)}
                  </span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Allocation by Investment Category Portion (Combined Lumpsum + SIPs) */}
        <div className="p-3 sm:p-3.5 rounded-xl glass-panel flex flex-col justify-between">
          <div>
            <div className="flex flex-wrap items-center justify-between gap-2 mb-2">
              <div>
                <h3 className="text-xs sm:text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
                  Allocation by Investment Category
                </h3>
                <p className="text-[9px] sm:text-[10px] text-slate-500 dark:text-slate-400 mt-0.5">
                  Combined Lumpsum + SIPs (Mutual Fund & Liquid Fund merged)
                </p>
              </div>
              <div className="flex items-center gap-1.5">
                {Object.keys(categoryOverrides).length > 0 && (
                  <button
                    onClick={handleResetAllCategories}
                    className="px-2 py-0.5 rounded-md text-[10px] font-semibold glass-button text-slate-600 dark:text-slate-300 flex items-center gap-1 transition-all cursor-pointer"
                    title="Reset all edited category totals to auto-calculated"
                  >
                    <RotateCcw className="w-3 h-3" />
                    Reset
                  </button>
                )}
                {!isEditingCategories ? (
                  <button
                    onClick={handleStartEdit}
                    className="px-2.5 py-1 rounded-lg bg-emerald-600/90 hover:bg-emerald-600 text-white font-medium text-[11px] shadow-sm backdrop-blur-md border border-emerald-400/30 flex items-center gap-1 transition-all cursor-pointer"
                  >
                    <Edit2 className="w-3 h-3" />
                    Edit Totals
                  </button>
                ) : (
                  <button
                    onClick={handleSaveEdit}
                    className="px-2.5 py-1 rounded-lg bg-emerald-600/90 hover:bg-emerald-600 text-white font-medium text-[11px] shadow-sm backdrop-blur-md border border-emerald-400/30 flex items-center gap-1 transition-all cursor-pointer"
                  >
                    <Check className="w-3 h-3" />
                    Done Editing
                  </button>
                )}
              </div>
            </div>

            {/* Category Bar Chart */}
            <div className="h-44 w-full mb-3">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={categoryChartData} margin={{ top: 10, right: 10, left: 0, bottom: 35 }}>
                  <XAxis
                    dataKey="name"
                    angle={-20}
                    textAnchor="end"
                    interval={0}
                    tick={{ fontSize: 10 }}
                  />
                  <YAxis tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}k`} tick={{ fontSize: 10 }} />
                  <Tooltip
                    formatter={(val: any, name: any, item: any) => [
                      formatCurrency(Number(val)),
                      item.payload.isOverridden ? `${item.payload.name} (Custom Total)` : item.payload.name,
                    ]}
                    labelStyle={{ fontWeight: 'bold', fontSize: '11px' }}
                  />
                  <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                    {categoryChartData.map((entry, index) => (
                      <Cell
                        key={`cat-bar-${index}`}
                        fill={entry.isOverridden ? '#8B5CF6' : COLORS[index % COLORS.length]}
                      />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Detailed Category List with Editable Balances & Breakdown */}
          <div className="pt-3 border-t border-slate-100 dark:border-slate-800 space-y-2.5">
            <div className="flex items-center justify-between text-xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider px-1">
              <span>Category</span>
              <span>Lumpsum + SIP = Total</span>
            </div>

            <div className="space-y-2 max-h-72 overflow-y-auto pr-1">
              {categoryBreakdownList.map((item, index) => {
                const pct = currentTotalWealth > 0 ? (item.displayTotal / currentTotalWealth) * 100 : 0;
                const barColor = item.isOverridden ? '#8B5CF6' : COLORS[index % COLORS.length];

                return (
                  <div
                    key={item.name}
                    className={`p-3 rounded-xl border transition-all ${
                      item.isOverridden
                        ? 'bg-purple-50/50 dark:bg-purple-950/20 border-purple-200 dark:border-purple-800/50'
                        : 'bg-slate-50 dark:bg-slate-800/60 border-slate-200/80 dark:border-slate-700/60'
                    }`}
                  >
                    <div className="flex items-center justify-between gap-2 mb-1.5">
                      <div className="flex items-center gap-2 min-w-0">
                        <span
                          className="w-2.5 h-2.5 rounded-full shrink-0"
                          style={{ backgroundColor: barColor }}
                        />
                        <span className="font-bold text-sm text-slate-900 dark:text-white truncate">
                          {item.name}
                        </span>
                        {item.isOverridden && (
                          <span className="text-[10px] font-medium px-1.5 py-0.5 rounded-md bg-purple-100 dark:bg-purple-900/60 text-purple-700 dark:text-purple-300">
                            Edited
                          </span>
                        )}
                        {customCategories.includes(item.name) && (
                          <button
                            type="button"
                            onClick={() => handleDeleteCustomCategory(item.name)}
                            className="text-slate-400 hover:text-red-500 transition-colors"
                            title="Remove custom category"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        )}
                      </div>

                      {/* Right Amount Display or Inline Input */}
                      <div className="flex items-center gap-2 shrink-0">
                        {isEditingCategories ? (
                          <div className="flex items-center gap-1.5">
                            <span className="text-xs text-slate-400">₹</span>
                            <input
                              type="number"
                              value={editingValues[item.name] ?? item.displayTotal}
                              onChange={(e) =>
                                setEditingValues((prev) => ({
                                  ...prev,
                                  [item.name]: e.target.value,
                                }))
                              }
                              className="w-28 px-2 py-1 text-right text-xs font-bold rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                            />
                            {item.isOverridden && (
                              <button
                                type="button"
                                onClick={() => handleResetCategory(item.name)}
                                className="p-1 rounded-md text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
                                title="Reset to auto-calculated total"
                              >
                                <RotateCcw className="w-3.5 h-3.5" />
                              </button>
                            )}
                          </div>
                        ) : (
                          <div className="text-right">
                            <span className="font-extrabold text-sm text-slate-900 dark:text-white block">
                              {formatCurrency(item.displayTotal)}
                            </span>
                            <span className="text-[11px] font-semibold text-slate-500 font-mono">
                              {pct.toFixed(1)}%
                            </span>
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Breakdown details */}
                    <div className="flex items-center justify-between text-[11px] text-slate-500 dark:text-slate-400 mt-1">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span>
                          Lumpsum: <strong className="text-slate-700 dark:text-slate-300">{formatCurrency(item.lumpsum)}</strong>
                        </span>
                        <span>•</span>
                        <span>
                          SIP: <strong className="text-slate-700 dark:text-slate-300">{formatCurrency(item.sip)}</strong>
                        </span>
                      </div>
                      {item.isOverridden && (
                        <span className="text-[10px] text-purple-600 dark:text-purple-400 italic">
                          Calculated: {formatCurrency(item.calculatedTotal)}
                        </span>
                      )}
                    </div>

                    {/* Progress Bar */}
                    <div className="w-full h-1.5 bg-slate-200 dark:bg-slate-700 rounded-xs overflow-hidden mt-2">
                      <div
                        className="h-full rounded-xs transition-all duration-500"
                        style={{
                          width: `${Math.min(100, Math.max(2, pct))}%`,
                          backgroundColor: barColor,
                        }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Add Custom Category form in Edit Mode */}
            {isEditingCategories && (
              <form onSubmit={handleAddCustomCategory} className="pt-2 flex items-center gap-2">
                <input
                  type="text"
                  placeholder="New Category Name (e.g. Crypto)"
                  value={newCatName}
                  onChange={(e) => setNewCatName(e.target.value)}
                  className="flex-1 px-3 py-1.5 text-xs rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
                <input
                  type="number"
                  placeholder="Amount ₹"
                  value={newCatAmount}
                  onChange={(e) => setNewCatAmount(e.target.value)}
                  className="w-24 px-3 py-1.5 text-xs rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
                <button
                  type="submit"
                  className="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-900 dark:bg-slate-700 dark:hover:bg-slate-600 text-white font-medium text-xs flex items-center gap-1 transition-colors"
                >
                  <Plus className="w-3.5 h-3.5" />
                  Add
                </button>
              </form>
            )}

            <div className="flex items-center gap-1.5 text-[11px] text-slate-400 dark:text-slate-500 pt-1">
              <Info className="w-3.5 h-3.5 shrink-0" />
              <span>
                Mutual Fund & Liquid Fund are automatically merged into Mutual Fund. Click "Edit Totals" to customize any category amount.
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* SECTION 3: Interactive Financial Milestones & Wealth Target Progress */}
      <div className="p-3 sm:p-3.5 rounded-xl glass-panel relative overflow-hidden border border-emerald-500/25">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-2.5">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-gradient-to-tr from-emerald-500 to-teal-600 text-white shadow-sm shadow-emerald-500/20">
              <Target className="w-3.5 h-3.5" />
            </div>
            <div>
              <h2 className="text-xs sm:text-sm font-bold text-slate-900 dark:text-white">
                Financial Goal & Milestone Velocity
              </h2>
              <p className="text-[10px] text-slate-500 dark:text-slate-400">
                Track portfolio wealth relative to key financial target milestones
              </p>
            </div>
          </div>

          {/* Goal Selector / Custom Goal Input */}
          <div className="flex items-center gap-2 shrink-0">
            {!isEditingGoal ? (
              <div className="flex items-center gap-1.5">
                <span className="text-[11px] font-semibold text-slate-500">Target Goal:</span>
                <button
                  onClick={() => {
                    setTempGoalInput(String(customGoal));
                    setIsEditingGoal(true);
                  }}
                  className="px-2 py-0.5 rounded-lg glass-button text-slate-900 dark:text-white font-bold text-[11px] flex items-center gap-1 cursor-pointer"
                >
                  <Award className="w-3 h-3 text-amber-500" />
                  {formatCurrency(customGoal)}
                  <Edit2 className="w-2.5 h-2.5 text-slate-400 ml-0.5" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-1">
                <span className="text-[11px] font-semibold text-slate-500">Goal ₹:</span>
                <input
                  type="number"
                  value={tempGoalInput}
                  onChange={(e) => setTempGoalInput(e.target.value)}
                  className="w-24 px-1.5 py-0.5 text-[11px] font-bold rounded-lg glass-input text-slate-900 dark:text-white"
                />
                <button
                  onClick={() => {
                    const num = Number(tempGoalInput);
                    if (num > 0) setCustomGoal(num);
                    setIsEditingGoal(false);
                  }}
                  className="p-1 rounded-md bg-emerald-600 text-white text-[11px] font-bold cursor-pointer"
                >
                  <Check className="w-3 h-3" />
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Big Progress Bar */}
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs sm:text-sm font-extrabold">
            <span className="text-slate-700 dark:text-slate-200">
              Current: <span className="text-emerald-600 dark:text-emerald-400">{formatCurrency(currentTotalWealth)}</span>
            </span>
            <span className="text-slate-500 dark:text-slate-400 font-mono">
              {milestoneProgressPct.toFixed(1)}% Achieved
            </span>
            <span className="text-slate-700 dark:text-slate-200">
              Target: <span className="text-teal-600 dark:text-teal-400">{formatCurrency(activeTargetGoal)}</span>
            </span>
          </div>

          <div className="w-full h-3 bg-slate-200/80 dark:bg-slate-800/80 rounded-full p-0.5 overflow-hidden shadow-inner border border-white/40 dark:border-white/10">
            <div
              className="h-full rounded-full bg-gradient-to-r from-emerald-500 via-teal-500 to-cyan-400 shadow-sm shadow-emerald-500/30 transition-all duration-700 ease-out"
              style={{ width: `${milestoneProgressPct}%` }}
            />
          </div>

          <div className="flex flex-wrap items-center justify-between text-[11px] text-slate-500 dark:text-slate-400 pt-0.5">
            <span>
              Remaining Gap: <strong className="text-slate-800 dark:text-slate-200">{formatCurrency(remainingToGoal)}</strong>
            </span>
            {remainingToGoal > 0 && estimatedMonthsToGoal > 0 && (
              <span className="font-semibold text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
                <Zap className="w-3 h-3 text-amber-500" /> Projected ~{estimatedMonthsToGoal} months at {formatCurrency(totalMonthlyOutflow)}/mo SIP
              </span>
            )}
          </div>
        </div>

        {/* Milestone Badges Strip */}
        <div className="mt-3.5 pt-3 border-t border-slate-200/60 dark:border-slate-800/60 flex items-center gap-1.5 overflow-x-auto pb-0.5 [scrollbar-width:none]">
          {DEFAULT_MILESTONES.map((m) => {
            const isReached = currentTotalWealth >= m.target;
            return (
              <button
                key={m.label}
                onClick={() => setCustomGoal(m.target)}
                className={`px-2.5 py-1 rounded-xl text-[11px] font-bold shrink-0 flex items-center gap-1 transition-all cursor-pointer ${
                  isReached
                    ? 'bg-emerald-500/15 text-emerald-800 dark:text-emerald-300 border border-emerald-500/30'
                    : 'glass-card text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
                }`}
              >
                {isReached ? (
                  <CheckCircle2 className="w-3 h-3 text-emerald-500" />
                ) : (
                  <Target className="w-3 h-3 text-slate-400" />
                )}
                {m.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* SECTION 3: Multi-Year Compound Growth Wealth Projection Engine */}
      <div className="p-3 sm:p-3.5 rounded-xl glass-panel space-y-3 border border-indigo-500/20">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-gradient-to-tr from-indigo-500 to-purple-600 text-white shadow-sm shadow-indigo-500/20">
              <Sliders className="w-3.5 h-3.5" />
            </div>
            <div>
              <h2 className="text-xs sm:text-sm font-bold text-slate-900 dark:text-white">
                Multi-Year Compound Wealth Projection
              </h2>
              <p className="text-[10px] text-slate-500 dark:text-slate-400">
                Simulated future wealth from current principal ({formatCurrency(currentTotalWealth)}) + monthly outflow ({formatCurrency(totalMonthlyOutflow)}/mo)
              </p>
            </div>
          </div>

          {/* Interactive Expected Return Rate Slider */}
          <div className="flex items-center gap-2 p-1.5 rounded-lg glass-card shrink-0">
            <span className="text-[11px] font-bold text-slate-700 dark:text-slate-300">
              Return: <strong className="text-indigo-600 dark:text-indigo-400 font-mono text-[11px]">{returnRate}% p.a.</strong>
            </span>
            <input
              type="range"
              min="6"
              max="20"
              step="1"
              value={returnRate}
              onChange={(e) => setReturnRate(Number(e.target.value))}
              className="w-16 accent-indigo-600 cursor-pointer h-1.5"
            />
          </div>
        </div>

        {/* Projection Timeline Cards */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-2">
          {[projection3Y, projection5Y, projection10Y, projection15Y].map((proj) => (
            <div
              key={proj.years}
              className="p-2.5 rounded-lg glass-card border border-indigo-500/15 flex flex-col justify-between space-y-1 hover:scale-[1.01] transition-all"
            >
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-black uppercase text-indigo-600 dark:text-indigo-400">
                  {proj.years} Years
                </span>
                <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-indigo-500/15 text-indigo-700 dark:text-indigo-300">
                  @{returnRate}% p.a.
                </span>
              </div>

              <div>
                <p className="text-sm sm:text-base font-black text-slate-900 dark:text-white">
                  {formatCurrency(proj.totalProjected)}
                </p>
                <div className="text-[9px] sm:text-[10px] font-semibold space-y-0.5 mt-0.5">
                  <p className="text-slate-500 dark:text-slate-400">
                    Invested: <span className="text-slate-800 dark:text-slate-200">{formatCurrency(proj.totalInvested)}</span>
                  </p>
                  <p className="text-emerald-600 dark:text-emerald-400 font-bold">
                    Returns: +{formatCurrency(proj.estimatedReturns)}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* SECTION 4: Portfolio Health & Smart Financial Insights */}
      <div className="p-3 sm:p-3.5 rounded-xl glass-panel space-y-2.5">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-gradient-to-tr from-emerald-500 to-teal-600 text-white shadow-sm shadow-emerald-500/20">
              <ShieldCheck className="w-3.5 h-3.5" />
            </div>
            <div>
              <h2 className="text-xs sm:text-sm font-bold text-slate-900 dark:text-white">
                Portfolio Health & Diversification Rating
              </h2>
              <p className="text-[10px] text-slate-500 dark:text-slate-400">
                Automated risk analysis across brokers, asset classes, and investment velocity
              </p>
            </div>
          </div>

          <div className="px-2 py-0.5 rounded-lg bg-emerald-500/15 text-emerald-800 dark:text-emerald-300 font-bold text-[11px] flex items-center gap-1 border border-emerald-500/30">
            <CheckCircle2 className="w-3 h-3 text-emerald-500" />
            Score: {healthScore}/100
          </div>
        </div>

        {/* Insights Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2">
          {healthInsights.map((insight, idx) => (
            <div
              key={idx}
              className={`p-2.5 rounded-lg border backdrop-blur-md flex items-start gap-2 ${
                insight.type === 'warning'
                  ? 'bg-amber-500/10 dark:bg-amber-500/15 border-amber-500/30 text-amber-900 dark:text-amber-200'
                  : insight.type === 'success'
                  ? 'bg-emerald-500/10 dark:bg-emerald-500/15 border-emerald-500/30 text-emerald-900 dark:text-emerald-200'
                  : 'bg-blue-500/10 dark:bg-blue-500/15 border-blue-500/30 text-blue-900 dark:text-blue-200'
              }`}
            >
              {insight.type === 'warning' ? (
                <AlertTriangle className="w-3.5 h-3.5 text-amber-500 shrink-0 mt-0.5" />
              ) : (
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 shrink-0 mt-0.5" />
              )}
              <div>
                <h4 className="text-[10px] font-bold mb-0.5">{insight.title}</h4>
                <p className="text-[10px] opacity-85 leading-snug">{insight.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
