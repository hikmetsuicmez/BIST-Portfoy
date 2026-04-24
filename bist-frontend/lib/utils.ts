import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export const formatTL = (value?: number | null): string => {
  if (value == null) return '-';
  return new Intl.NumberFormat('tr-TR', {
    style: 'currency',
    currency: 'TRY',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
};

export const formatYuzde = (value?: number | null): string => {
  if (value == null) return '-';
  const sign = value > 0 ? '+' : '';
  return `${sign}%${value.toFixed(2).replace('.', ',')}`;
};

export const formatLot = (value: number): string =>
  new Intl.NumberFormat('tr-TR', { minimumFractionDigits: 2, maximumFractionDigits: 4 }).format(value);

export const formatTarih = (tarih: string): string =>
  new Intl.DateTimeFormat('tr-TR', { day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(tarih));

export const formatTarihKisa = (tarih: string): string =>
  new Intl.DateTimeFormat('tr-TR').format(new Date(tarih));

export const karZararRenk = (value?: number | null): string => {
  if (value == null) return 'text-muted-foreground';
  if (value > 0) return 'text-green-600';
  if (value < 0) return 'text-red-600';
  return 'text-muted-foreground';
};

export const bugunTarih = (): string =>
  new Date().toISOString().split('T')[0];
