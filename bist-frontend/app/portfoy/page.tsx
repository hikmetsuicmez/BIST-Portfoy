'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowUpDown } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';
import { KarZararBadge, YuzdeBadge } from '@/components/shared/KarZararBadge';
import { EmptyState } from '@/components/shared/EmptyState';
import { usePortfoy } from '@/hooks/usePortfoy';
import { formatTL, formatLot, formatTarihKisa, karZararRenk } from '@/lib/utils';
import api from '@/lib/api';
import type { PozisyonDto, TemmettuOzetDto, ApiResponse } from '@/types';

type SortKey = keyof PozisyonDto;

export default function PortfoyPage() {
  const { pozisyonlar, ozet, loading } = usePortfoy();
  const router = useRouter();
  const [sortKey, setSortKey] = useState<SortKey>('karZararYuzde');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc');
  const [temmettuMap, setTemmettuMap] = useState<Record<string, number>>({});

  useEffect(() => {
    api
      .get<ApiResponse<TemmettuOzetDto>>('/temettular/ozet')
      .then((r) => {
        const map: Record<string, number> = {};
        r.data.data.hisseOzet.forEach((h) => {
          map[h.sembol] = (map[h.sembol] ?? 0) + h.toplamNet;
        });
        setTemmettuMap(map);
      })
      .catch(() => {});
  }, []);

  const handleSort = (key: SortKey) => {
    if (sortKey === key) setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    else { setSortKey(key); setSortDir('desc'); }
  };

  const sorted = [...pozisyonlar].sort((a, b) => {
    const av = a[sortKey] ?? 0;
    const bv = b[sortKey] ?? 0;
    const r = av < bv ? -1 : av > bv ? 1 : 0;
    return sortDir === 'asc' ? r : -r;
  });

  const cols: { key: SortKey; label: string }[] = [
    { key: 'sembol', label: 'Sembol' },
    { key: 'sirketAdi', label: 'Şirket' },
    { key: 'toplamLot', label: 'Lot' },
    { key: 'ortalamaMaliyet', label: 'Ort. Maliyet' },
    { key: 'sonKapanisFiyat', label: 'Son Fiyat' },
    { key: 'guncelDeger', label: 'Güncel Değer' },
    { key: 'karZararTl', label: 'K/Z (TL)' },
    { key: 'karZararYuzde', label: 'K/Z (%)' },
    { key: 'ilkAlimTarihi', label: 'İlk Alım' },
  ];

  const temmettuKolonu = 'Temettü Geliri';

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Portföy</h1>
        <p className="text-sm text-muted-foreground">
          {ozet?.pozisyonSayisi ?? 0} pozisyon · Toplam {formatTL(ozet?.toplamDeger)}
        </p>
      </div>

      {/* Özet kartlar */}
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        {[
          { label: 'Toplam Değer', val: ozet?.toplamDeger },
          { label: 'Toplam Maliyet', val: ozet?.toplamMaliyet },
          { label: 'Toplam K/Z', val: ozet?.toplamKarZararTl },
          { label: 'Günlük Değişim', val: ozet?.gunlukDegisimTl },
        ].map(({ label, val }) => (
          <Card key={label}>
            <CardHeader className="pb-1 pt-3 px-4">
              <CardTitle className="text-xs text-muted-foreground">{label}</CardTitle>
            </CardHeader>
            <CardContent className="px-4 pb-3">
              {loading ? (
                <Skeleton className="h-6 w-24" />
              ) : (
                <KarZararBadge value={val} showIcon={false} className="text-base font-bold" />
              )}
            </CardContent>
          </Card>
        ))}
      </div>

      <Card>
        <CardContent className="p-0">
          {loading ? (
            <div className="space-y-2 p-4">
              {[...Array(8)].map((_, i) => <Skeleton key={i} className="h-10 w-full" />)}
            </div>
          ) : sorted.length === 0 ? (
            <EmptyState icon="📊" title="Henüz pozisyon yok" />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border text-left text-xs text-muted-foreground">
                    {cols.map(({ key, label }) => (
                      <th key={key} className="px-4 py-3">
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-auto px-0 py-0 font-medium text-muted-foreground hover:text-foreground"
                          onClick={() => handleSort(key)}
                        >
                          {label}
                          <ArrowUpDown className="ml-1 h-3 w-3" />
                        </Button>
                      </th>
                    ))}
                    <th className="px-4 py-3 text-xs font-medium text-muted-foreground">
                      {temmettuKolonu}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {sorted.map((p) => (
                    <tr
                      key={p.sembol}
                      onClick={() => router.push(`/portfoy/${p.sembol}`)}
                      className="border-b border-border last:border-0 hover:bg-muted/50 cursor-pointer transition-colors"
                    >
                      <td className="px-4 py-3">
                        <Badge variant="outline" className="font-mono font-bold">
                          {p.sembol}
                        </Badge>
                      </td>
                      <td className="px-4 py-3 text-muted-foreground max-w-[180px] truncate">
                        {p.sirketAdi}
                      </td>
                      <td className="px-4 py-3 tabular-nums">{formatLot(p.toplamLot)}</td>
                      <td className="px-4 py-3 tabular-nums">{formatTL(p.ortalamaMaliyet)}</td>
                      <td className="px-4 py-3 tabular-nums">{p.sonKapanisFiyat ? formatTL(p.sonKapanisFiyat) : '-'}</td>
                      <td className="px-4 py-3 tabular-nums font-semibold">{formatTL(p.guncelDeger)}</td>
                      <td className={`px-4 py-3 tabular-nums ${karZararRenk(p.karZararTl)}`}>
                        {formatTL(p.karZararTl)}
                      </td>
                      <td className="px-4 py-3">
                        <YuzdeBadge value={p.karZararYuzde} />
                      </td>
                      <td className="px-4 py-3 text-muted-foreground tabular-nums">
                        {formatTarihKisa(p.ilkAlimTarihi)}
                      </td>
                      <td className="px-4 py-3 tabular-nums font-medium text-green-600">
                        {temmettuMap[p.sembol] != null
                          ? formatTL(temmettuMap[p.sembol])
                          : <span className="text-muted-foreground">—</span>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
