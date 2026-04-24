'use client';

import { useState, useCallback, useEffect } from 'react';
import Link from 'next/link';
import { ChevronLeft, Search } from 'lucide-react';
import { toast } from 'sonner';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { PortfoyGrafik } from '@/components/charts/PortfoyGrafik';
import { KarZararBadge, YuzdeBadge } from '@/components/shared/KarZararBadge';
import { EmptyState } from '@/components/shared/EmptyState';
import api from '@/lib/api';
import { formatTL, formatTarihKisa, bugunTarih } from '@/lib/utils';
import type { OzetGunlukDto, ApiResponse } from '@/types';

function otuzGunOnce() {
  const d = new Date();
  d.setDate(d.getDate() - 30);
  return d.toISOString().split('T')[0];
}

export default function PerformansPage() {
  const [baslangic, setBaslangic] = useState(otuzGunOnce());
  const [bitis, setBitis] = useState(bugunTarih());
  const [data, setData] = useState<OzetGunlukDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [aramaDone, setAramaDone] = useState(false);

  const fetchData = useCallback(async (bas: string, bit: string) => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ baslangic: bas, bitis: bit });
      const res = await api.get(`/raporlar/performans?${params}`);
      const raw = res.data;
      const list: OzetGunlukDto[] = Array.isArray(raw)
        ? raw
        : ((raw as ApiResponse<OzetGunlukDto[]>)?.data ?? []);
      setData(list);
      setAramaDone(true);
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Rapor yüklenemedi';
      toast.error(msg);
      setAramaDone(true);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(baslangic, bitis); }, []);  // eslint-disable-line react-hooks/exhaustive-deps

  const ilk = data[0];
  const son = data[data.length - 1];
  const donemiGetiri = ilk && son ? son.toplamDeger - ilk.toplamDeger : null;
  const donemiGetiriYuzde =
    ilk && son && ilk.toplamDeger > 0
      ? ((son.toplamDeger / ilk.toplamDeger) - 1) * 100
      : null;

  const enIyiGun = Array.isArray(data) && data.length > 0
    ? data.reduce<OzetGunlukDto | null>(
        (best, g) => (g.gunlukDegisimYuzde ?? -Infinity) > (best?.gunlukDegisimYuzde ?? -Infinity) ? g : best,
        null
      )
    : null;
  const enKotuGun = Array.isArray(data) && data.length > 0
    ? data.reduce<OzetGunlukDto | null>(
        (worst, g) => (g.gunlukDegisimYuzde ?? Infinity) < (worst?.gunlukDegisimYuzde ?? Infinity) ? g : worst,
        null
      )
    : null;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Link href="/raporlar" className="text-muted-foreground hover:text-foreground">
          <ChevronLeft className="h-5 w-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold">Performans Analizi</h1>
          <p className="text-sm text-muted-foreground">Dönem bazlı portföy performansı</p>
        </div>
      </div>

      {/* Filtreler */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-wrap gap-4 items-end">
            <div className="space-y-1">
              <Label>Başlangıç</Label>
              <Input type="date" value={baslangic} onChange={(e) => setBaslangic(e.target.value)} className="w-40" />
            </div>
            <div className="space-y-1">
              <Label>Bitiş</Label>
              <Input type="date" value={bitis} onChange={(e) => setBitis(e.target.value)} className="w-40" />
            </div>
            <Button onClick={() => fetchData(baslangic, bitis)} disabled={loading}>
              <Search className="mr-2 h-4 w-4" />
              Raporu Oluştur
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Özet */}
      {aramaDone && !loading && data.length > 0 && (
        <>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
            <Card>
              <CardContent className="p-4">
                <p className="text-xs text-muted-foreground">Dönem Başı Değer</p>
                <p className="mt-1 text-base font-bold">{formatTL(ilk?.toplamDeger)}</p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-4">
                <p className="text-xs text-muted-foreground">Dönem Sonu Değer</p>
                <p className="mt-1 text-base font-bold">{formatTL(son?.toplamDeger)}</p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-4">
                <p className="text-xs text-muted-foreground">Dönem Getirisi</p>
                <div className="mt-1 flex items-center gap-2">
                  <KarZararBadge value={donemiGetiri} showIcon className="text-base font-bold" />
                  <YuzdeBadge value={donemiGetiriYuzde} />
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-4">
                <p className="text-xs text-muted-foreground">İşlem Günü</p>
                <p className="mt-1 text-base font-bold">{data.length}</p>
              </CardContent>
            </Card>
          </div>

          {(enIyiGun || enKotuGun) && (
            <div className="grid grid-cols-2 gap-4">
              {enIyiGun && (
                <Card>
                  <CardContent className="p-4">
                    <p className="text-xs text-muted-foreground">En İyi Gün</p>
                    <p className="mt-1 text-sm font-semibold">{formatTarihKisa(enIyiGun.tarih)}</p>
                    <YuzdeBadge value={enIyiGun.gunlukDegisimYuzde} />
                  </CardContent>
                </Card>
              )}
              {enKotuGun && (
                <Card>
                  <CardContent className="p-4">
                    <p className="text-xs text-muted-foreground">En Kötü Gün</p>
                    <p className="mt-1 text-sm font-semibold">{formatTarihKisa(enKotuGun.tarih)}</p>
                    <YuzdeBadge value={enKotuGun.gunlukDegisimYuzde} />
                  </CardContent>
                </Card>
              )}
            </div>
          )}

          {/* Grafik */}
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Portföy Değer Grafiği</CardTitle>
            </CardHeader>
            <CardContent>
              <PortfoyGrafik data={data} />
            </CardContent>
          </Card>

          {/* Detay Tablo */}
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Günlük Detaylar</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-border text-left text-xs text-muted-foreground">
                      <th className="px-4 py-3">Tarih</th>
                      <th className="px-4 py-3">Toplam Değer</th>
                      <th className="px-4 py-3">Günlük Değişim</th>
                      <th className="px-4 py-3">Günlük %</th>
                      <th className="px-4 py-3">Toplam K/Z</th>
                    </tr>
                  </thead>
                  <tbody>
                    {[...data].reverse().map((g) => (
                      <tr key={g.tarih} className="border-b border-border last:border-0 hover:bg-muted/50">
                        <td className="px-4 py-3 tabular-nums">{formatTarihKisa(g.tarih)}</td>
                        <td className="px-4 py-3 tabular-nums font-semibold">{formatTL(g.toplamDeger)}</td>
                        <td className="px-4 py-3">
                          <KarZararBadge value={g.gunlukDegisimTl} showIcon />
                        </td>
                        <td className="px-4 py-3">
                          <YuzdeBadge value={g.gunlukDegisimYuzde} />
                        </td>
                        <td className="px-4 py-3">
                          <KarZararBadge value={g.toplamKarZararTl} showIcon={false} />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>
        </>
      )}

      {aramaDone && !loading && data.length === 0 && (
        <EmptyState icon="📈" title="Bu dönem için veri bulunamadı" description="Farklı bir tarih aralığı deneyin" />
      )}

      {loading && (
        <div className="space-y-4">
          <Skeleton className="h-32 w-full" />
          <Skeleton className="h-64 w-full" />
        </div>
      )}
    </div>
  );
}
