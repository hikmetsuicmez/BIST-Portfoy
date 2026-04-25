'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { AlertTriangle, BadgeDollarSign, Clock, TrendingUp, Wallet, BarChart3, Activity } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';
import { KarZararBadge, YuzdeBadge } from '@/components/shared/KarZararBadge';
import { EmptyState } from '@/components/shared/EmptyState';
import { PortfoyGrafik } from '@/components/charts/PortfoyGrafik';
import { usePortfoy } from '@/hooks/usePortfoy';
import { formatTL, formatLot, formatTarihKisa, karZararRenk } from '@/lib/utils';
import api from '@/lib/api';
import type { OzetGunlukDto, GunsonuDurumDto, TemmettuOzetDto, ApiResponse } from '@/types';

export default function DashboardPage() {
  const { ozet, pozisyonlar, loading } = usePortfoy();
  const [gecmis, setGecmis] = useState<OzetGunlukDto[]>([]);
  const [gunsonuDurum, setGunsonuDurum] = useState<GunsonuDurumDto | null>(null);
  const [temmettuOzet, setTemmettuOzet] = useState<TemmettuOzetDto | null>(null);
  // undefined = yükleniyor, null = veri yok, string = zaman damgası
  const [sonGuncelleme, setSonGuncelleme] = useState<string | null | undefined>(undefined);

  useEffect(() => {
    const today = new Date().toISOString().split('T')[0];
    api.get<ApiResponse<OzetGunlukDto[]>>('/gunsonu/gecmis')
      .then((r) => {
        setGecmis(r.data.data);
        setSonGuncelleme(r.data.data[0]?.tamamlanmaZamani ?? null);
      })
      .catch(() => { setSonGuncelleme(null); });
    api.get<ApiResponse<GunsonuDurumDto>>(`/gunsonu/durum/${today}`).then((r) => setGunsonuDurum(r.data.data)).catch(() => {});
    api.get<ApiResponse<TemmettuOzetDto>>('/temettular/ozet').then((r) => setTemmettuOzet(r.data.data)).catch(() => {});
  }, []);

  const trNow = new Date(new Date().toLocaleString('en-US', { timeZone: 'Europe/Istanbul' }));
  const isHaftaSonu = trNow.getDay() === 0 || trNow.getDay() === 6;
  const borsaKapandi = trNow.getHours() > 18 || (trNow.getHours() === 18 && trNow.getMinutes() >= 30);

  const top5 = pozisyonlar.slice(0, 5);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-foreground">Dashboard</h1>
        <p className="text-sm text-muted-foreground">Portföy genel durumu</p>
      </div>

      {!isHaftaSonu && gunsonuDurum && !gunsonuDurum.tamamlandi && (
        borsaKapandi ? (
          <div className="flex items-center gap-3 rounded-lg border border-yellow-600/30 bg-yellow-600/10 px-4 py-3">
            <AlertTriangle className="h-4 w-4 text-yellow-500 shrink-0" />
            <p className="text-sm text-yellow-500">Bugün için günsonu henüz tamamlanmadı.</p>
            <Button asChild size="sm" variant="outline" className="ml-auto border-yellow-600/50 text-yellow-500">
              <Link href="/gunsonu">Günsonu Yap →</Link>
            </Button>
          </div>
        ) : (
          <div className="flex items-center gap-3 rounded-lg border border-blue-600/30 bg-blue-600/10 px-4 py-3">
            <Clock className="h-4 w-4 text-blue-400 shrink-0" />
            <p className="text-sm text-blue-400">Borsa henüz kapanmadı. Günsonu 18:30 sonrası yapılabilir.</p>
          </div>
        )
      )}

      <div className="flex justify-end -mt-2">
        <span className="text-xs text-muted-foreground">
          {sonGuncelleme === undefined
            ? 'Yükleniyor...'
            : sonGuncelleme
              ? `Son güncelleme: ${new Date(sonGuncelleme).toLocaleString('tr-TR', { day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' })}`
              : 'Henüz günsonu yapılmadı'}
        </span>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <SummaryCard title="Toplam Portföy Değeri" icon={<Wallet className="h-4 w-4" />} loading={loading}>
          <p className="text-2xl font-bold">{formatTL(ozet?.toplamDeger)}</p>
        </SummaryCard>
        <SummaryCard title="Toplam Maliyet" icon={<BarChart3 className="h-4 w-4" />} loading={loading}>
          <p className="text-2xl font-bold">{formatTL(ozet?.toplamMaliyet)}</p>
        </SummaryCard>
        <SummaryCard title="Toplam Kar/Zarar" icon={<TrendingUp className="h-4 w-4" />} loading={loading}>
          <div className="flex items-end gap-2">
            <KarZararBadge value={ozet?.toplamKarZararTl} className="text-2xl" />
            <YuzdeBadge value={ozet?.toplamKarZararYuzde} />
          </div>
        </SummaryCard>
        <SummaryCard title="Günlük Değişim" icon={<Activity className="h-4 w-4" />} loading={loading}>
          <div className="flex items-end gap-2">
            <KarZararBadge value={ozet?.gunlukDegisimTl} className="text-2xl" />
            <YuzdeBadge value={ozet?.gunlukDegisimYuzde} />
          </div>
        </SummaryCard>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="text-base">Son Pozisyonlar</CardTitle>
          <Button asChild variant="outline" size="sm"><Link href="/portfoy">Tümünü Gör</Link></Button>
        </CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="space-y-2 p-4">{[...Array(5)].map((_, i) => <Skeleton key={i} className="h-10 w-full" />)}</div>
          ) : top5.length === 0 ? (
            <EmptyState icon="📊" title="Henüz pozisyon yok" description="İlk işleminizi ekleyerek başlayın" />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border text-left text-xs text-muted-foreground">
                    <th className="px-4 py-3">Sembol</th>
                    <th className="px-4 py-3">Lot</th>
                    <th className="px-4 py-3">Ort. Maliyet</th>
                    <th className="px-4 py-3">Son Fiyat</th>
                    <th className="px-4 py-3">Güncel Değer</th>
                    <th className="px-4 py-3">K/Z (TL)</th>
                    <th className="px-4 py-3">K/Z (%)</th>
                  </tr>
                </thead>
                <tbody>
                  {top5.map((p) => (
                    <tr key={p.sembol} className="border-b border-border last:border-0 hover:bg-muted/50 transition-colors">
                      <td className="px-4 py-3">
                        <Link href={`/portfoy/${p.sembol}`} className="font-bold text-primary hover:underline">{p.sembol}</Link>
                      </td>
                      <td className="px-4 py-3 tabular-nums">{formatLot(p.toplamLot)}</td>
                      <td className="px-4 py-3 tabular-nums">{formatTL(p.ortalamaMaliyet)}</td>
                      <td className="px-4 py-3 tabular-nums">{p.sonKapanisFiyat ? formatTL(p.sonKapanisFiyat) : '-'}</td>
                      <td className="px-4 py-3 tabular-nums font-semibold">{formatTL(p.guncelDeger)}</td>
                      <td className={`px-4 py-3 tabular-nums ${karZararRenk(p.karZararTl)}`}>{formatTL(p.karZararTl)}</td>
                      <td className="px-4 py-3"><YuzdeBadge value={p.karZararYuzde} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {temmettuOzet && (
        <Link href="/temmettu" className="block">
          <div className="flex items-center gap-3 rounded-lg border border-green-600/30 bg-green-600/10 px-4 py-3 hover:bg-green-600/15 transition-colors">
            <BadgeDollarSign className="h-4 w-4 text-green-500 shrink-0" />
            <p className="text-sm text-green-500">
              Bu yıl temettü geliri:{' '}
              <span className="font-semibold">{formatTL(temmettuOzet.toplamNetBuYil)}</span>
            </p>
            <span className="ml-auto text-xs text-green-500 font-medium">Tümünü Gör →</span>
          </div>
        </Link>
      )}

      {gecmis.length > 0 && (
        <Card>
          <CardHeader><CardTitle className="text-base">Portföy Performansı (Son 30 Gün)</CardTitle></CardHeader>
          <CardContent><PortfoyGrafik data={gecmis} /></CardContent>
        </Card>
      )}
    </div>
  );
}

function SummaryCard({ title, icon, loading, children }: { title: string; icon: React.ReactNode; loading: boolean; children: React.ReactNode }) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-xs font-medium text-muted-foreground uppercase tracking-wide">{title}</CardTitle>
        <span className="text-muted-foreground">{icon}</span>
      </CardHeader>
      <CardContent>{loading ? <Skeleton className="h-8 w-32" /> : children}</CardContent>
    </Card>
  );
}
