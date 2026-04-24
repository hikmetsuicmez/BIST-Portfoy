'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { ChevronLeft } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { KarZararBadge, YuzdeBadge } from '@/components/shared/KarZararBadge';
import { EmptyState } from '@/components/shared/EmptyState';
import api from '@/lib/api';
import { formatTL, formatLot, formatTarihKisa } from '@/lib/utils';
import type { PozisyonDto, IslemDto, KapanisFiyatDto, ApiResponse } from '@/types';

export default function HisseDetayPage() {
  const { sembol } = useParams<{ sembol: string }>();
  const [pozisyon, setPozisyon] = useState<PozisyonDto | null>(null);
  const [islemler, setIslemler] = useState<IslemDto[]>([]);
  const [fiyatlar, setFiyatlar] = useState<KapanisFiyatDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!sembol) return;
    setLoading(true);
    Promise.all([
      api.get<ApiResponse<PozisyonDto>>(`/portfoy/hisse/${sembol}`),
      api.get<ApiResponse<IslemDto[]>>(`/islemler?sembol=${sembol}`),
      api.get<ApiResponse<KapanisFiyatDto[]>>(`/hisseler/${sembol}/fiyatlar`),
    ])
      .then(([pozRes, islRes, fiyRes]) => {
        setPozisyon(pozRes.data.data);
        setIslemler(islRes.data.data);
        setFiyatlar(fiyRes.data.data.slice(0, 60));
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [sembol]);

  if (loading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-48" />
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          {[...Array(7)].map((_, i) => <Skeleton key={i} className="h-20" />)}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Link href="/portfoy" className="text-muted-foreground hover:text-foreground">
          <ChevronLeft className="h-5 w-5" />
        </Link>
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold">{sembol}</h1>
            {pozisyon?.sektor && <Badge variant="secondary">{pozisyon.sektor}</Badge>}
          </div>
          <p className="text-sm text-muted-foreground">{pozisyon?.sirketAdi}</p>
        </div>
      </div>

      {/* Özet kutuları */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {[
          { label: 'Toplam Lot', val: pozisyon ? formatLot(pozisyon.toplamLot) : '-' },
          { label: 'Ort. Maliyet', val: formatTL(pozisyon?.ortalamaMaliyet) },
          { label: 'Toplam Maliyet', val: formatTL(pozisyon?.toplamMaliyet) },
          { label: 'Son Fiyat', val: formatTL(pozisyon?.sonKapanisFiyat) },
          { label: 'Güncel Değer', val: formatTL(pozisyon?.guncelDeger), bold: true },
        ].map(({ label, val, bold }) => (
          <Card key={label}>
            <CardContent className="p-4">
              <p className="text-xs text-muted-foreground">{label}</p>
              <p className={`mt-1 text-sm ${bold ? 'text-lg font-bold' : 'font-semibold'}`}>{val}</p>
            </CardContent>
          </Card>
        ))}
        <Card>
          <CardContent className="p-4">
            <p className="text-xs text-muted-foreground">K/Z (TL)</p>
            <KarZararBadge value={pozisyon?.karZararTl} className="mt-1 text-sm font-bold" />
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <p className="text-xs text-muted-foreground">K/Z (%)</p>
            <div className="mt-1">
              <YuzdeBadge value={pozisyon?.karZararYuzde} />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Son Fiyatlar */}
      {fiyatlar.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">Son Kapanış Fiyatları</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border text-xs text-muted-foreground">
                    <th className="px-4 py-2 text-left">Tarih</th>
                    <th className="px-4 py-2 text-right">Açılış</th>
                    <th className="px-4 py-2 text-right">Kapanış</th>
                    <th className="px-4 py-2 text-right">Yüksek</th>
                    <th className="px-4 py-2 text-right">Düşük</th>
                  </tr>
                </thead>
                <tbody>
                  {fiyatlar.map((f) => (
                    <tr key={f.id} className="border-b border-border last:border-0 hover:bg-muted/50">
                      <td className="px-4 py-2 tabular-nums">{formatTarihKisa(f.tarih)}</td>
                      <td className="px-4 py-2 text-right tabular-nums">{f.acilisFiyat ? formatTL(f.acilisFiyat) : '-'}</td>
                      <td className="px-4 py-2 text-right tabular-nums font-semibold">{formatTL(f.kapanisFiyat)}</td>
                      <td className="px-4 py-2 text-right tabular-nums text-green-600">{f.yuksekFiyat ? formatTL(f.yuksekFiyat) : '-'}</td>
                      <td className="px-4 py-2 text-right tabular-nums text-red-600">{f.dusukFiyat ? formatTL(f.dusukFiyat) : '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      )}

      {/* İşlem Geçmişi */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">İşlem Geçmişi</CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          {islemler.length === 0 ? (
            <EmptyState icon="📋" title="İşlem bulunamadı" />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border text-xs text-muted-foreground">
                    <th className="px-4 py-2 text-left">Tarih</th>
                    <th className="px-4 py-2 text-left">Tür</th>
                    <th className="px-4 py-2 text-right">Lot</th>
                    <th className="px-4 py-2 text-right">Fiyat</th>
                    <th className="px-4 py-2 text-right">Komisyon</th>
                    <th className="px-4 py-2 text-right">Toplam</th>
                    <th className="px-4 py-2 text-left">Notlar</th>
                  </tr>
                </thead>
                <tbody>
                  {islemler.map((i) => (
                    <tr key={i.id} className="border-b border-border last:border-0 hover:bg-muted/50">
                      <td className="px-4 py-2 tabular-nums">{formatTarihKisa(i.tarih)}</td>
                      <td className="px-4 py-2">
                        <Badge
                          variant={i.islemTuru === 'ALIM' ? 'default' : 'destructive'}
                          className="text-xs"
                        >
                          {i.islemTuru}
                        </Badge>
                      </td>
                      <td className="px-4 py-2 text-right tabular-nums">{formatLot(i.lot)}</td>
                      <td className="px-4 py-2 text-right tabular-nums">{formatTL(i.fiyat)}</td>
                      <td className="px-4 py-2 text-right tabular-nums">{formatTL(i.komisyon)}</td>
                      <td className="px-4 py-2 text-right tabular-nums font-semibold">{formatTL(i.toplamTutar)}</td>
                      <td className="px-4 py-2 text-muted-foreground">{i.notlar ?? '-'}</td>
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
