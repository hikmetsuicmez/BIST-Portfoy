'use client';

import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { formatTL, formatTarih, formatTarihKisa } from '@/lib/utils';
import type { OzetGunlukDto } from '@/types';

export function PortfoyGrafik({ data }: { data: OzetGunlukDto[] }) {
  return (
    <ResponsiveContainer width="100%" height={300}>
      <AreaChart data={data} margin={{ top: 4, right: 4, bottom: 0, left: 0 }}>
        <defs>
          <linearGradient id="portfoyGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3} />
            <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" stroke="oklch(0.269 0 0)" />
        <XAxis
          dataKey="tarih"
          tickFormatter={(v) => formatTarihKisa(v)}
          tick={{ fontSize: 11, fill: 'oklch(0.708 0 0)' }}
          tickLine={false}
          axisLine={false}
        />
        <YAxis
          tickFormatter={(v) => `${(v / 1000).toFixed(0)}K`}
          tick={{ fontSize: 11, fill: 'oklch(0.708 0 0)' }}
          tickLine={false}
          axisLine={false}
        />
        <Tooltip
          contentStyle={{
            backgroundColor: 'oklch(0.205 0 0)',
            border: '1px solid oklch(0.269 0 0)',
            borderRadius: '8px',
          }}
          formatter={(val: number) => [formatTL(val), 'Portföy Değeri']}
          labelFormatter={(label) => formatTarih(label)}
        />
        <Area
          type="monotone"
          dataKey="toplamDeger"
          stroke="#3b82f6"
          fill="url(#portfoyGradient)"
          strokeWidth={2}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
}
