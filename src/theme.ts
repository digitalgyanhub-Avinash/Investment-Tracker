/**
 * Comprehensive Theme, Color & Design System Configuration
 * Use this file directly in any project for consistent colors, glassmorphism & typography.
 */

export const THEME_CONFIG = {
  archetype: 'Modern Glassmorphism & Liquid Bento Grid',
  
  // 1. Canvas Backgrounds
  canvas: {
    light: 'bg-gradient-to-br from-slate-50 via-teal-50/20 to-blue-50/20 text-slate-900',
    dark: 'dark:from-slate-950 dark:via-slate-900 dark:to-zinc-950 dark:text-slate-100',
    ambientBlobs: {
      blob1: 'bg-gradient-to-tr from-emerald-400/20 via-teal-300/15 to-transparent blur-3xl',
      blob2: 'bg-gradient-to-bl from-blue-400/20 via-indigo-300/15 to-transparent blur-3xl',
      blob3: 'bg-gradient-to-t from-purple-400/15 via-pink-300/10 to-transparent blur-3xl',
    }
  },

  // 2. Signature Tab Color Schemes
  tabs: {
    summary: {
      name: 'Summary',
      gradient: 'bg-gradient-to-r from-emerald-500 to-teal-600',
      textGradient: 'from-emerald-500 to-teal-600',
      activeText: 'text-white',
      shadow: 'shadow-md shadow-emerald-500/30',
      hoverLight: 'hover:bg-emerald-50 hover:text-emerald-600',
      hoverDark: 'dark:hover:bg-emerald-950/30 dark:hover:text-emerald-400',
      badge: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20',
      accentColor: '#10B981',
    },
    holdings: {
      name: 'Holdings',
      gradient: 'bg-gradient-to-r from-blue-500 to-cyan-600',
      textGradient: 'from-blue-500 to-cyan-600',
      activeText: 'text-white',
      shadow: 'shadow-md shadow-blue-500/30',
      hoverLight: 'hover:bg-blue-50 hover:text-blue-600',
      hoverDark: 'dark:hover:bg-blue-950/30 dark:hover:text-blue-400',
      badge: 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20',
      accentColor: '#3B82F6',
    },
    sips: {
      name: 'SIPs',
      gradient: 'bg-gradient-to-r from-amber-500 to-orange-600',
      textGradient: 'from-amber-500 to-orange-600',
      activeText: 'text-white',
      shadow: 'shadow-md shadow-amber-500/30',
      hoverLight: 'hover:bg-amber-50 hover:text-amber-600',
      hoverDark: 'dark:hover:bg-amber-950/30 dark:hover:text-amber-400',
      badge: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20',
      accentColor: '#F59E0B',
    },
    ipos: {
      name: 'IPOs',
      gradient: 'bg-gradient-to-r from-purple-600 to-fuchsia-600',
      textGradient: 'from-purple-600 to-fuchsia-600',
      activeText: 'text-white',
      shadow: 'shadow-md shadow-purple-500/30',
      hoverLight: 'hover:bg-purple-50 hover:text-purple-600',
      hoverDark: 'dark:hover:bg-purple-950/30 dark:hover:text-purple-400',
      badge: 'bg-purple-500/10 text-purple-600 dark:text-purple-400 border-purple-500/20',
      accentColor: '#9333EA',
    },
    committees: {
      name: 'Committees',
      gradient: 'bg-gradient-to-r from-indigo-600 to-violet-600',
      textGradient: 'from-indigo-600 to-violet-600',
      activeText: 'text-white',
      shadow: 'shadow-md shadow-indigo-500/30',
      hoverLight: 'hover:bg-indigo-50 hover:text-indigo-600',
      hoverDark: 'dark:hover:bg-indigo-950/30 dark:hover:text-indigo-400',
      badge: 'bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 border-indigo-500/20',
      accentColor: '#4F46E5',
    },
  },

  // 3. Functional Financial Status Colors
  status: {
    positive: {
      text: 'text-emerald-600 dark:text-emerald-400',
      bg: 'bg-emerald-500/15',
      border: 'border-emerald-500/30',
      badge: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20',
      hex: '#10B981',
    },
    negative: {
      text: 'text-rose-600 dark:text-rose-400',
      bg: 'bg-rose-500/15',
      border: 'border-rose-500/30',
      badge: 'bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/20',
      hex: '#EF4444',
    },
    pending: {
      text: 'text-amber-600 dark:text-amber-400',
      bg: 'bg-amber-500/15',
      border: 'border-amber-500/30',
      badge: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/20',
      hex: '#F59E0B',
    },
    neutral: {
      text: 'text-slate-500 dark:text-slate-400',
      bg: 'bg-slate-500/10',
      border: 'border-slate-500/20',
      badge: 'bg-slate-500/10 text-slate-600 dark:text-slate-300 border border-slate-500/20',
      hex: '#64748B',
    },
  },

  // 4. Glassmorphism CSS Utility Class Names (defined in src/index.css)
  glassClasses: {
    panel: 'glass-panel',       // Large primary containers / charts
    card: 'glass-card',         // Bento grid metric cards & list tiles
    header: 'glass-header',     // Sticky navigation bars
    modal: 'glass-modal',       // Popups, forms, dialogue windows
    input: 'glass-input',       // Form fields, textboxes, dropdowns
    button: 'glass-button',     // Action buttons with glass tactile feedback
    dock: 'liquid-dock',        // Bottom navigation pill / quick dock
    badge: 'liquid-badge',      // Small status pills
  },

  // 5. Typography Hierarchy
  typography: {
    fontFamilyDisplay: 'Inter, Plus Jakarta Sans, system-ui, sans-serif',
    fontFamilyNumbers: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
    classes: {
      headingPrimary: 'text-2xl sm:text-3xl font-extrabold tracking-tight',
      headingSecondary: 'text-lg sm:text-xl font-bold tracking-tight',
      kpiValue: 'text-2xl sm:text-3xl font-extrabold font-mono tracking-tight',
      currencyChip: 'text-xs px-2.5 py-1 rounded-lg font-mono font-medium glass-card',
      body: 'text-sm text-slate-600 dark:text-slate-300',
      subtext: 'text-xs text-slate-500 dark:text-slate-400',
    }
  },
} as const;

export default THEME_CONFIG;
