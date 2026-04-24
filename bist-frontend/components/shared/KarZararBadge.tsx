import { cn, formatTL, formatYuzde } from '@/lib/utils';

interface KarZararBadgeProps {
  value?: number | null;
  showIcon?: boolean;
  className?: string;
}

export function KarZararBadge({ value, showIcon = true, className }: KarZararBadgeProps) {
  const isKar = (value ?? 0) > 0;
  const isZarar = (value ?? 0) < 0;

  return (
    <span
      className={cn(
        'font-semibold',
        isKar && 'text-green-600',
        isZarar && 'text-red-600',
        !isKar && !isZarar && 'text-muted-foreground',
        className
      )}
    >
      {showIcon && (isKar ? '▲ ' : isZarar ? '▼ ' : '— ')}
      {formatTL(value)}
    </span>
  );
}

interface YuzdeBadgeProps {
  value?: number | null;
  className?: string;
}

export function YuzdeBadge({ value, className }: YuzdeBadgeProps) {
  const isKar = (value ?? 0) > 0;
  const isZarar = (value ?? 0) < 0;

  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold',
        isKar && 'bg-green-600/20 text-green-600',
        isZarar && 'bg-red-600/20 text-red-600',
        !isKar && !isZarar && 'bg-muted text-muted-foreground',
        className
      )}
    >
      {formatYuzde(value)}
    </span>
  );
}
